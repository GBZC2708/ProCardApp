package pe.com.zzynan.procardapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.flatMapLatest
import pe.com.zzynan.procardapp.core.di.ServiceLocator
import pe.com.zzynan.procardapp.data.repository.TrainingRepository
import pe.com.zzynan.procardapp.domain.model.RoutineDay
import pe.com.zzynan.procardapp.domain.model.RoutineExercise
import pe.com.zzynan.procardapp.domain.model.TrainingDayState
import pe.com.zzynan.procardapp.domain.model.TrainingDayStatus
import pe.com.zzynan.procardapp.domain.model.WorkoutSession
import pe.com.zzynan.procardapp.domain.model.WorkoutSessionStatus
import pe.com.zzynan.procardapp.domain.usecase.AddWorkoutSetUseCase
import pe.com.zzynan.procardapp.domain.usecase.CalculateBestStatsUseCase
import pe.com.zzynan.procardapp.domain.usecase.CloseWorkoutSessionUseCase
import pe.com.zzynan.procardapp.domain.usecase.CreateOrResumeWorkoutSessionUseCase
import pe.com.zzynan.procardapp.domain.usecase.RemoveWorkoutSetUseCase
import pe.com.zzynan.procardapp.domain.usecase.SaveWorkoutSetUseCase
import pe.com.zzynan.procardapp.ui.model.ExerciseEditorUiModel
import pe.com.zzynan.procardapp.ui.model.RoutineDayUiModel
import pe.com.zzynan.procardapp.ui.model.RoutineExerciseUiModel
import pe.com.zzynan.procardapp.ui.model.SessionExerciseUiModel
import pe.com.zzynan.procardapp.ui.model.TrainingDayDialogState
import pe.com.zzynan.procardapp.ui.model.TrainingDayStatusUi
import pe.com.zzynan.procardapp.ui.model.TrainingDayUiModel
import pe.com.zzynan.procardapp.ui.model.TrainingMuscleGroups
import pe.com.zzynan.procardapp.ui.model.TrainingSessionUiModel
import pe.com.zzynan.procardapp.ui.model.TrainingTab
import pe.com.zzynan.procardapp.ui.model.TrainingUiState
import pe.com.zzynan.procardapp.ui.model.WorkoutExerciseUiModel
import pe.com.zzynan.procardapp.ui.model.WorkoutSetUiModel

class TrainingViewModel(
    private val repository: TrainingRepository,
    private val createOrResumeWorkoutSessionUseCase: CreateOrResumeWorkoutSessionUseCase,
    private val closeWorkoutSessionUseCase: CloseWorkoutSessionUseCase,
    private val saveWorkoutSetUseCase: SaveWorkoutSetUseCase,
    private val addWorkoutSetUseCase: AddWorkoutSetUseCase,
    private val removeWorkoutSetUseCase: RemoveWorkoutSetUseCase
) : ViewModel() {

    private val selectedTabFlow = MutableStateFlow(TrainingTab.Catalog)
    private val exerciseEditorFlow = MutableStateFlow(ExerciseEditorUiModel())
    private val editingExerciseId = MutableStateFlow<Int?>(null)
    private val isRoutineDialogVisibleFlow = MutableStateFlow(false)
    private val trainingDayDialogFlow = MutableStateFlow<TrainingDayDialogState?>(null)
    private val isSessionVisibleFlow = MutableStateFlow(false)
    private val isViewerModeFlow = MutableStateFlow(false)
    private val previewRoutineDayFlow = MutableStateFlow<RoutineDay?>(null)
    private val activeSessionIdFlow = MutableStateFlow<Int?>(null)
    private val statusMessageFlow = MutableStateFlow("")
    private val finishDialogFlow = MutableStateFlow(false)

    private val weekStart: LocalDate = LocalDate.now().with(DayOfWeek.MONDAY)

    private val exercisesDomainFlow = repository.observeExercises().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    private val routineDomainFlow = repository.observeRoutine().stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    private val trainingDaysDomainFlow = repository.observeTrainingDays(weekStart).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    private val sessionFlow = activeSessionIdFlow.flatMapLatest { id ->
        if (id == null) {
            flowOf<WorkoutSession?>(null)
        } else {
            repository.observeSession(id)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val sessionSetsFlow = activeSessionIdFlow.flatMapLatest { id ->
        if (id == null) {
            flowOf(emptyList())
        } else {
            repository.observeSessionSets(id)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val sessionRoutineFlow = combine(sessionFlow, routineDomainFlow) { session, days ->
        session?.let { sessionValue -> days.firstOrNull { it.id == sessionValue.routineDayId } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val sessionExerciseIdsFlow = sessionRoutineFlow.map { day ->
        day?.exercises?.map { it.exercise.id } ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val statsFlow = sessionExerciseIdsFlow.flatMapLatest { ids ->
        if (ids.isEmpty()) flowOf(emptyList()) else repository.observeStats(ids)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val tickerFlow = flow {
        while (currentCoroutineContext().isActive) {
            emit(System.currentTimeMillis())
            delay(1_000)
        }
    }

    private val timerTextFlow = combine(sessionFlow, tickerFlow) { session, now ->
        session?.let {
            val elapsed = if (it.status == WorkoutSessionStatus.IN_PROGRESS) {
                now - it.startedAt
            } else {
                it.durationMillis ?: 0L
            }
            formatDuration(elapsed)
        } ?: "00:00:00"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "00:00:00")

    private val sessionUiFlow: StateFlow<TrainingSessionUiModel> = combine(
        isSessionVisibleFlow,
        isViewerModeFlow,
        previewRoutineDayFlow,
        sessionFlow,
        sessionRoutineFlow,
        sessionSetsFlow,
        statsFlow,
        timerTextFlow,
        statusMessageFlow
    ) { visible, viewer, previewDay, session, routineDay, sets, stats, timerText, statusMessage ->
        if (!visible) {
            TrainingSessionUiModel()
        } else if (previewDay != null) {
            TrainingSessionUiModel(
                isVisible = true,
                isReadOnly = true,
                isPreview = true,
                dayLabel = dayTitle(previewDay),
                timerText = "00:00:00",
                statusText = "Solo visualización",
                exercises = previewDay.exercises.map { exercise ->
                    SessionExerciseUiModel(
                        exerciseId = exercise.exercise.id,
                        name = exercise.exercise.name,
                        muscleGroup = exercise.exercise.muscleGroup,
                        sets = buildPreviewSets(exercise)
                    )
                },
                showFinishButton = false
            )
        } else if (session != null && routineDay != null) {
            val statsMap = stats.associateBy { it.exerciseId to it.setIndex }
            val setsByExercise = sets.groupBy { it.exerciseId }
            val exercises = routineDay.exercises.map { routineExercise ->
                val exerciseSets = setsByExercise[routineExercise.exercise.id] ?: emptyList()
                        SessionExerciseUiModel(
                            exerciseId = routineExercise.exercise.id,
                            name = routineExercise.exercise.name,
                            muscleGroup = routineExercise.exercise.muscleGroup,
                            sets = exerciseSets.map { set ->
                                WorkoutSetUiModel(
                                    id = set.id,
                                    setIndex = set.setIndex,
                                    label = set.setIndex.toString(),
                                    bestLabel = bestLabel(statsMap[set.exerciseId to set.setIndex]),
                                    weightText = formatWeightInput(set.weight),
                                    repsText = set.reps?.toString() ?: "",
                                    isCompleted = set.isCompleted,
                                    isEditable = !viewer && !set.isCompleted
                        )
                    }
                )
            }
            TrainingSessionUiModel(
                isVisible = true,
                isReadOnly = viewer,
                isPreview = false,
                dayLabel = session.dayLabelSnapshot,
                timerText = if (viewer) "00:00:00" else timerText,
                statusText = statusMessage.ifEmpty {
                    when (session.status) {
                        WorkoutSessionStatus.IN_PROGRESS -> "Sesión en progreso"
                        WorkoutSessionStatus.COMPLETED -> "Entrenamiento completado"
                    }
                },
                exercises = exercises,
                showFinishButton = !viewer && session.status == WorkoutSessionStatus.IN_PROGRESS
            )
        } else {
            TrainingSessionUiModel()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TrainingSessionUiModel())

    val finishDialogVisible: StateFlow<Boolean> = finishDialogFlow

    val uiState: StateFlow<TrainingUiState> = combine(
        selectedTabFlow,
        exercisesDomainFlow,
        routineDomainFlow,
        trainingDaysDomainFlow,
        exerciseEditorFlow,
        isRoutineDialogVisibleFlow,
        trainingDayDialogFlow,
        sessionUiFlow,
        statusMessageFlow
    ) { tab, exercises, routine, trainingDays, editor, routineDialog, dayDialog, sessionUi, statusMessage ->
        TrainingUiState(
            selectedTab = tab,
            exercises = exercises.map { it.toUiModel() },
            muscleGroups = TrainingMuscleGroups,
            exerciseEditor = editor,
            routineDays = routine.sortedBy { it.dayOfWeek }.map { it.toUiModel() },
            isRoutineDialogVisible = routineDialog,
            trainingDays = trainingDays.sortedBy { it.routineDay.dayOfWeek }.map { it.toUiModel() },
            trainingDayDialog = dayDialog,
            sessionUi = sessionUi,
            statusMessage = statusMessage
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, TrainingUiState())

    init {
        viewModelScope.launch {
            repository.ensureRoutineDays()
        }
    }

    fun onSelectTab(tab: TrainingTab) {
        selectedTabFlow.value = tab
    }

    fun onOpenAddExercise() {
        editingExerciseId.value = null
        exerciseEditorFlow.value = ExerciseEditorUiModel(
            isVisible = true,
            title = "Nuevo ejercicio",
            name = "",
            selectedGroup = TrainingMuscleGroups.first(),
            isEditing = false,
            confirmLabel = "Agregar"
        )
    }

    fun onEditExercise(exercise: WorkoutExerciseUiModel) {
        editingExerciseId.value = exercise.id
        exerciseEditorFlow.value = ExerciseEditorUiModel(
            isVisible = true,
            title = "Editar ejercicio",
            name = exercise.name,
            selectedGroup = exercise.muscleGroup,
            isEditing = true,
            confirmLabel = "Guardar"
        )
    }

    fun onExerciseEditorNameChange(value: String) {
        exerciseEditorFlow.value = exerciseEditorFlow.value.copy(name = value)
    }

    fun onExerciseEditorGroupChange(value: String) {
        exerciseEditorFlow.value = exerciseEditorFlow.value.copy(selectedGroup = value)
    }

    fun onDismissExerciseEditor() {
        exerciseEditorFlow.value = ExerciseEditorUiModel()
        editingExerciseId.value = null
    }

    fun onConfirmExerciseEditor() {
        val editor = exerciseEditorFlow.value
        if (editor.name.isBlank()) return
        viewModelScope.launch {
            val group = editor.selectedGroup.ifEmpty { TrainingMuscleGroups.first() }
            val editingId = editingExerciseId.value
            if (editingId == null) {
                repository.addExercise(editor.name.trim(), group)
            } else {
                repository.renameExercise(editingId, editor.name.trim())
                repository.changeMuscleGroup(editingId, group)
            }
            statusMessageFlow.value = "Cambios guardados"
            onDismissExerciseEditor()
        }
    }

    fun onToggleExerciseActive(id: Int) {
        viewModelScope.launch {
            repository.toggleExerciseActive(id)
            statusMessageFlow.value = "Cambios guardados"
        }
    }

    fun onOpenRoutineDialog() {
        isRoutineDialogVisibleFlow.value = true
    }

    fun onDismissRoutineDialog() {
        isRoutineDialogVisibleFlow.value = false
    }

    fun onRoutineLabelChange(dayId: Int, label: String) {
        viewModelScope.launch {
            repository.updateRoutineLabel(dayId, label)
            statusMessageFlow.value = "Cambios guardados"
        }
    }

    fun onAddRoutineExercise(dayId: Int, exerciseId: Int) {
        viewModelScope.launch {
            repository.addExerciseToRoutine(dayId, exerciseId)
            statusMessageFlow.value = "Cambios guardados"
        }
    }

    fun onRemoveRoutineExercise(entryId: Int) {
        viewModelScope.launch {
            repository.removeRoutineExercise(entryId)
            statusMessageFlow.value = "Cambios guardados"
        }
    }

    fun onTrainingDaySelected(dayUi: TrainingDayUiModel) {
        val routineDay = routineDomainFlow.value.firstOrNull { it.id == dayUi.dayId } ?: return
        trainingDayDialogFlow.value = TrainingDayDialogState(
            day = routineDay.toUiModel(),
            status = dayUi.status,
            sessionId = dayUi.sessionId,
            statusMessage = dayUi.statusLabel
        )
    }

    fun onDismissTrainingDayDialog() {
        trainingDayDialogFlow.value = null
    }

    fun onSoloVerSelected(dayId: Int, sessionId: Int?) {
        viewModelScope.launch {
            trainingDayDialogFlow.value = null
            if (sessionId != null) {
                activeSessionIdFlow.value = sessionId
                isViewerModeFlow.value = true
                isSessionVisibleFlow.value = true
                previewRoutineDayFlow.value = null
            } else {
                val routine = routineDomainFlow.value.firstOrNull { it.id == dayId }
                previewRoutineDayFlow.value = routine
                isSessionVisibleFlow.value = true
                isViewerModeFlow.value = true
            }
        }
    }

    fun onStartTraining(dayId: Int, forceNew: Boolean = false) {
        viewModelScope.launch {
            val routineDay = routineDomainFlow.value.firstOrNull { it.id == dayId } ?: return@launch
            val session = createOrResumeWorkoutSessionUseCase(
                routineDay = routineDay,
                date = dateForDay(routineDay.dayOfWeek),
                startedAt = System.currentTimeMillis(),
                forceNew = forceNew
            )
            activeSessionIdFlow.value = session.id
            isViewerModeFlow.value = false
            isSessionVisibleFlow.value = true
            previewRoutineDayFlow.value = null
            statusMessageFlow.value = if (forceNew) "Sesión en progreso" else "Reanudando sesión anterior"
            trainingDayDialogFlow.value = null
        }
    }

    fun onCloseSessionScreen() {
        isSessionVisibleFlow.value = false
        isViewerModeFlow.value = false
        previewRoutineDayFlow.value = null
    }

    fun onSetWeightChange(setId: Int, value: String) {
        viewModelScope.launch {
            val sanitized = value.replace(',', '.').trim()
            if (sanitized.isEmpty()) {
                saveWorkoutSetUseCase.updateWeight(setId, null)
            } else if (isValidDecimal(sanitized)) {
                saveWorkoutSetUseCase.updateWeight(setId, sanitized.toFloat())
            }
            statusMessageFlow.value = "Cambios guardados"
        }
    }

    fun onSetRepsChange(setId: Int, value: String) {
        viewModelScope.launch {
            if (value.isBlank()) {
                saveWorkoutSetUseCase.updateReps(setId, null)
            } else {
                value.toIntOrNull()?.let { saveWorkoutSetUseCase.updateReps(setId, it) }
            }
            statusMessageFlow.value = "Cambios guardados"
        }
    }

    fun onToggleSetCompleted(setId: Int, completed: Boolean) {
        viewModelScope.launch {
            saveWorkoutSetUseCase.toggleCompletion(setId, completed)
            statusMessageFlow.value = "Cambios guardados"
        }
    }

    fun onAddSet(sessionExerciseId: Int) {
        val sessionId = activeSessionIdFlow.value ?: return
        viewModelScope.launch {
            addWorkoutSetUseCase(sessionId, sessionExerciseId)
            statusMessageFlow.value = "Cambios guardados"
        }
    }

    fun onRemoveSet(sessionExerciseId: Int) {
        val sessionId = activeSessionIdFlow.value ?: return
        viewModelScope.launch {
            if (removeWorkoutSetUseCase(sessionId, sessionExerciseId)) {
                statusMessageFlow.value = "Cambios guardados"
            }
        }
    }

    fun onShowFinishDialog() {
        finishDialogFlow.value = true
    }

    fun onDismissFinishDialog() {
        finishDialogFlow.value = false
    }

    fun onConfirmFinishSession() {
        val session = sessionFlow.value ?: return
        viewModelScope.launch {
            closeWorkoutSessionUseCase(session, System.currentTimeMillis())
            finishDialogFlow.value = false
            statusMessageFlow.value = "Entrenamiento completado"
            isSessionVisibleFlow.value = false
        }
    }

    private fun dateForDay(dayOfWeek: Int): LocalDate {
        return weekStart.plusDays(dayOfWeek.toLong())
    }

    private fun RoutineDay.toUiModel(): RoutineDayUiModel = RoutineDayUiModel(
        id = id,
        dayOfWeek = dayOfWeek,
        label = label,
        exercises = exercises.map { it.toUiModel() }
    )

    private fun RoutineExercise.toUiModel(): RoutineExerciseUiModel = RoutineExerciseUiModel(
        id = id,
        name = exercise.name,
        muscleGroup = exercise.muscleGroup,
        defaultSets = defaultSets
    )

    private fun pe.com.zzynan.procardapp.domain.model.WorkoutExercise.toUiModel() = WorkoutExerciseUiModel(
        id = id,
        name = name,
        muscleGroup = muscleGroup,
        isActive = isActive
    )

    private fun pe.com.zzynan.procardapp.domain.model.TrainingDayStatus.toUiModel(): TrainingDayUiModel {
        val statusUi = when (status) {
            TrainingDayState.NOT_STARTED -> TrainingDayStatusUi.NOT_STARTED
            TrainingDayState.IN_PROGRESS -> TrainingDayStatusUi.IN_PROGRESS
            TrainingDayState.COMPLETED -> TrainingDayStatusUi.COMPLETED
        }
        val label = when (statusUi) {
            TrainingDayStatusUi.NOT_STARTED -> "Sin iniciar"
            TrainingDayStatusUi.IN_PROGRESS -> "En progreso"
            TrainingDayStatusUi.COMPLETED -> "Completado"
        }
        return TrainingDayUiModel(
            dayId = routineDay.id,
            dayOfWeek = routineDay.dayOfWeek,
            label = routineDay.label,
            statusLabel = label,
            status = statusUi,
            sessionId = activeSession?.id
        )
    }

    private fun bestLabel(stats: pe.com.zzynan.procardapp.domain.model.ExerciseSetStats?): String {
        if (stats == null) return "Best: -- kg x --"
        val weight = stats.maxWeight?.let { "${formatWeightInput(it)} kg" } ?: "--"
        val reps = stats.maxReps?.toString() ?: "--"
        return "Best: $weight x $reps"
    }

    private fun formatWeightInput(value: Float?): String {
        if (value == null) return ""
        return if (value % 1f == 0f) value.toInt().toString() else String.format(Locale.getDefault(), "%.2f", value)
    }

    private fun buildPreviewSets(exercise: RoutineExercise): List<WorkoutSetUiModel> {
        val count = exercise.defaultSets.coerceAtLeast(1)
        return (1..count).map { index ->
            WorkoutSetUiModel(
                id = index,
                setIndex = index,
                label = index.toString(),
                bestLabel = "Best: -- kg x --",
                weightText = "",
                repsText = "",
                isCompleted = false,
                isEditable = false
            )
        }
    }

    private fun dayTitle(day: RoutineDay): String {
        val dayName = when (day.dayOfWeek) {
            0 -> "Lunes"
            1 -> "Martes"
            2 -> "Miércoles"
            3 -> "Jueves"
            4 -> "Viernes"
            5 -> "Sábado"
            else -> "Domingo"
        }
        return "$dayName – ${day.label}"
    }

    private fun formatDuration(millis: Long): String {
        val totalSeconds = (millis / 1000).coerceAtLeast(0)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun isValidDecimal(value: String): Boolean {
        val parts = value.split('.')
        return parts.size <= 2 && (parts.getOrNull(1)?.length ?: 0) <= 2 && value.toFloatOrNull() != null
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val repository = ServiceLocator.provideTrainingRepository(context)
                val calculateBestStatsUseCase = CalculateBestStatsUseCase(repository)
                @Suppress("UNCHECKED_CAST")
                return TrainingViewModel(
                    repository = repository,
                    createOrResumeWorkoutSessionUseCase = CreateOrResumeWorkoutSessionUseCase(repository),
                    closeWorkoutSessionUseCase = CloseWorkoutSessionUseCase(repository, calculateBestStatsUseCase),
                    saveWorkoutSetUseCase = SaveWorkoutSetUseCase(repository),
                    addWorkoutSetUseCase = AddWorkoutSetUseCase(repository),
                    removeWorkoutSetUseCase = RemoveWorkoutSetUseCase(repository)
                ) as T
            }
        }
    }
}
