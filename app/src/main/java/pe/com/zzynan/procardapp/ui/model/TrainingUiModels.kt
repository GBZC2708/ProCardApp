package pe.com.zzynan.procardapp.ui.model

enum class TrainingTab { Catalog, Train }

data class WorkoutExerciseUiModel(
    val id: Int,
    val name: String,
    val muscleGroup: String,
    val isActive: Boolean
)

data class ExerciseEditorUiModel(
    val isVisible: Boolean = false,
    val title: String = "",
    val name: String = "",
    val selectedGroup: String = "",
    val isEditing: Boolean = false,
    val confirmLabel: String = "Guardar"
)

data class RoutineExerciseUiModel(
    val id: Int,
    val name: String,
    val muscleGroup: String,
    val defaultSets: Int
)

data class RoutineDayUiModel(
    val id: Int,
    val dayOfWeek: Int,
    val label: String,
    val exercises: List<RoutineExerciseUiModel>
)

enum class TrainingDayStatusUi { NOT_STARTED, IN_PROGRESS, COMPLETED }

data class TrainingDayUiModel(
    val dayId: Int,
    val dayOfWeek: Int,
    val label: String,
    val statusLabel: String,
    val status: TrainingDayStatusUi,
    val sessionId: Int? = null
)

data class TrainingDayDialogState(
    val day: RoutineDayUiModel,
    val status: TrainingDayStatusUi,
    val sessionId: Int?,
    val statusMessage: String
)

data class WorkoutSetUiModel(
    val id: Int,
    val setIndex: Int,
    val label: String,
    val bestLabel: String,
    val weightText: String,
    val repsText: String,
    val isCompleted: Boolean,
    val isEditable: Boolean
)

data class SessionExerciseUiModel(
    val exerciseId: Int,
    val name: String,
    val muscleGroup: String,
    val sets: List<WorkoutSetUiModel>
)

data class TrainingSessionUiModel(
    val isVisible: Boolean = false,
    val isReadOnly: Boolean = false,
    val isPreview: Boolean = false,
    val dayLabel: String = "",
    val timerText: String = "00:00:00",
    val statusText: String = "",
    val exercises: List<SessionExerciseUiModel> = emptyList(),
    val showFinishButton: Boolean = false
)

data class TrainingUiState(
    val selectedTab: TrainingTab = TrainingTab.Catalog,
    val exercises: List<WorkoutExerciseUiModel> = emptyList(),
    val muscleGroups: List<String> = emptyList(),
    val exerciseEditor: ExerciseEditorUiModel = ExerciseEditorUiModel(),
    val routineDays: List<RoutineDayUiModel> = emptyList(),
    val isRoutineDialogVisible: Boolean = false,
    val trainingDays: List<TrainingDayUiModel> = emptyList(),
    val trainingDayDialog: TrainingDayDialogState? = null,
    val sessionUi: TrainingSessionUiModel = TrainingSessionUiModel(),
    val statusMessage: String = ""
)

val TrainingMuscleGroups = listOf(
    "Pecho",
    "Espalda",
    "Hombros",
    "Bíceps",
    "Tríceps",
    "Piernas – Cuádriceps",
    "Piernas – Isquios/Glúteos",
    "Pantorrillas",
    "Abdomen",
    "Trapecios",
    "Antebrazos",
    "Full Body",
    "Cardio"
)
