package pe.com.zzynan.procardapp.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.log10
import pe.com.zzynan.procardapp.core.di.ServiceLocator
import pe.com.zzynan.procardapp.domain.model.DailyMetrics
import pe.com.zzynan.procardapp.domain.model.Sex
import pe.com.zzynan.procardapp.domain.model.TrainingStage
import pe.com.zzynan.procardapp.domain.model.UserProfile
import pe.com.zzynan.procardapp.domain.usecase.ObserveUserProfileUseCase
import pe.com.zzynan.procardapp.domain.usecase.ObserveWeeklyMetricsUseCase
import pe.com.zzynan.procardapp.domain.usecase.SaveUserProfileUseCase
import pe.com.zzynan.procardapp.ui.model.ActivityUi
import pe.com.zzynan.procardapp.ui.model.BodyCompositionUi
import pe.com.zzynan.procardapp.ui.model.BodyDashboardAlert
import pe.com.zzynan.procardapp.ui.model.BodyDashboardUiState
import pe.com.zzynan.procardapp.ui.model.BodyGoalsUi
import pe.com.zzynan.procardapp.ui.model.BodyMeasurementsUi
import pe.com.zzynan.procardapp.ui.model.KpiSummaryUi
import pe.com.zzynan.procardapp.ui.model.MacrosUi
import pe.com.zzynan.procardapp.ui.model.MetabolismUi
import pe.com.zzynan.procardapp.ui.model.PhysicalDataUi
import pe.com.zzynan.procardapp.ui.model.TimeSeriesPoint

class BodyDashboardViewModel(
    private val observeUserProfileUseCase: ObserveUserProfileUseCase,
    private val saveUserProfileUseCase: SaveUserProfileUseCase,
    private val observeWeeklyMetricsUseCase: ObserveWeeklyMetricsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BodyDashboardUiState())
    val uiState: StateFlow<BodyDashboardUiState> = _uiState.asStateFlow()

    // Perfil actual estable, inicializado con UserProfile() y luego con lo de BD
    private val profileState: StateFlow<UserProfile> =
        observeUserProfileUseCase()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = UserProfile()
            )

    private val metricsFlow = profileState.flatMapLatest { profile ->
        observeWeeklyMetricsUseCase(
            username = profile.displayName,
            endDateEpoch = LocalDate.now().toEpochDay(),
            days = 14
        )
    }

    private val combined = combine(profileState, metricsFlow) { profile, metrics ->
        profile to metrics
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            combined.collect { pair ->
                pair ?: return@collect
                val (profile, metrics) = pair
                updateState(profile, metrics)
            }
        }
    }

    fun onQuickReviewModeChange(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isQuickReviewMode = enabled)
    }

    fun onSexChange(sex: Sex) = persistProfile { it.copy(sex = sex) }

    fun onPharmaUsageChange(enabled: Boolean) =
        persistProfile { it.copy(usesPharmacology = enabled) }

    fun onAgeChange(age: Int?) = persistProfile { it.copy(age = age) }

    fun onHeightChange(heightCm: Float?) = persistProfile { it.copy(heightCm = heightCm) }

    fun onMeasurementChange(field: String, value: Float?) {
        persistProfile { profile ->
            when (field) {
                "neck" -> profile.copy(neckCm = value)
                "waist" -> profile.copy(waistCm = value)
                "hip" -> profile.copy(hipCm = value)
                "chest" -> profile.copy(chestCm = value)
                "wrist" -> profile.copy(wristCm = value)
                "thigh" -> profile.copy(thighCm = value)
                "calf" -> profile.copy(calfCm = value)
                "relaxedBiceps" -> profile.copy(relaxedBicepsCm = value)
                "flexedBiceps" -> profile.copy(flexedBicepsCm = value)
                "forearm" -> profile.copy(forearmCm = value)
                "foot" -> profile.copy(footCm = value)
                else -> profile
            }
        }
    }

    private fun persistProfile(block: (UserProfile) -> UserProfile) {
        viewModelScope.launch {
            val currentProfile = profileState.value
            val updated = block(currentProfile)
            saveUserProfileUseCase(
                updated.copy(
                    displayName = updated.displayName.ifBlank { UserProfile.DEFAULT_DISPLAY_NAME }
                )
            )
        }
    }

    private suspend fun updateState(profile: UserProfile, metrics: List<DailyMetrics>) {
        val lastMetric = metrics.maxByOrNull { it.dateEpoch }
        val lastDate = lastMetric?.let { LocalDate.ofEpochDay(it.dateEpoch) }
        val weightPoints = metrics.sortedBy { it.dateEpoch }.map {
            TimeSeriesPoint(
                LocalDate.ofEpochDay(it.dateEpoch),
                it.weightFasted.takeIf { w -> w > 0f }
            )
        }

        val weight = lastMetric?.weightFasted?.takeIf { it > 0f }
        val heightM = profile.heightCm?.div(100f)
        val bmi =
            if (weight != null && heightM != null && heightM > 0) weight / (heightM * heightM) else null

        val waist = profile.waistCm
        val neck = profile.neckCm
        val hip = profile.hipCm
        val bodyFat = computeBodyFat(profile.sex, profile.heightCm, neck, waist, hip)
        val leanMass = bodyFat?.let { bf -> weight?.let { w -> w * (1f - bf / 100f) } }
        val ffmi =
            if (leanMass != null && heightM != null && heightM > 0) leanMass / (heightM * heightM) else null

        val bmrMifflin = weight?.let { w ->
            val s = if (profile.sex == Sex.Male) 5f else -161f
            profile.age?.let { age ->
                profile.heightCm?.let { h ->
                    10f * w + 6.25f * h - 5f * age + s
                }
            }
        }
        val bmrKatch = leanMass?.let { 370f + 21.6f * it }

        val steps = lastMetric?.dailySteps ?: 0
        val cardio = lastMetric?.cardioMinutes ?: 0
        val gym = (lastMetric?.trainingDone == true)
        val stepsKcal = steps * 0.04f
        val cardioKcal = cardio * 7f
        val gymKcal = if (gym) 250f else 0f
        val activityKcal = stepsKcal + cardioKcal + gymKcal
        val tdeeTheoretical = (bmrMifflin ?: 0f) + activityKcal

        val stage = lastMetric?.stage ?: TrainingStage.DEFINICION
        val recommendedCalories = computeRecommendedCalories(stage, tdeeTheoretical, bodyFat)

        val macros = computeMacros(
            profile = profile,
            stage = stage,
            bodyFat = bodyFat,
            calories = recommendedCalories,
            // 👇 AHORA solo usamos la masa magra real; si no hay, devolvemos "—"
            leanMass = leanMass
        )

        val alerts = mutableListOf<BodyDashboardAlert>()
        if (bodyFat != null && bodyFat < 6f) {
            alerts += BodyDashboardAlert(
                "⚠️ Grasa muy baja. Mantén esta condición solo por periodos cortos.",
                true
            )
        }

        _uiState.value = _uiState.value.copy(
            phaseLabel = stage.toReadableLabel(),
            lastDate = lastDate,
            physicalData = PhysicalDataUi(
                sex = profile.sex,
                usesPharma = profile.usesPharmacology,
                age = profile.age,
                height = profile.heightCm
            ),
            measurements = BodyMeasurementsUi(
                neck = neck,
                waist = waist,
                hip = hip,
                chest = profile.chestCm,
                wrist = profile.wristCm,
                thigh = profile.thighCm,
                calf = profile.calfCm,
                relaxedBiceps = profile.relaxedBicepsCm,
                flexedBiceps = profile.flexedBicepsCm,
                forearm = profile.forearmCm,
                foot = profile.footCm
            ),
            kpiSummary = KpiSummaryUi(
                bodyFat = bodyFat?.let { String.format("%.1f %%", it) } ?: "—",
                fatFreeMass = leanMass?.let { String.format("%.1f kg", it) } ?: "—",
                calories = String.format("%d kcal", recommendedCalories.toInt()),
                weightVelocity = computeVelocity(metrics)
            ),
            composition = BodyCompositionUi(
                weight = weight?.let { String.format("%.1f kg", it) } ?: "—",
                height = profile.heightCm?.let { String.format("%.0f cm", it) } ?: "—",
                age = profile.age?.toString() ?: "—",
                bmi = bmi?.let { String.format("%.1f", it) } ?: "—",
                ffmi = ffmi?.let { String.format("%.1f", it) } ?: "—",
                bodyFat = bodyFat?.let { String.format("%.1f %%", it) } ?: "—",
                fatMass = bodyFat?.let { bf ->
                    weight?.let { w -> String.format("%.1f kg", w * bf / 100f) }
                } ?: "—",
                leanMass = leanMass?.let { String.format("%.1f kg", it) } ?: "—",
                whr = if (waist != null && hip != null && hip > 0)
                    String.format("%.2f", waist / hip) else "—",
                whtr = if (waist != null && profile.heightCm != null && profile.heightCm > 0)
                    String.format("%.2f", waist / profile.heightCm) else "—"
            ),
            metabolism = MetabolismUi(
                bmrKatch = bmrKatch?.let { String.format("%.0f kcal", it) } ?: "—",
                bmrMifflin = bmrMifflin?.let { String.format("%.0f kcal", it) } ?: "—",
                // Por ahora usamos el mismo valor para real/teórico hasta conectar nutrición
                tdeeReal = String.format("%.0f kcal", tdeeTheoretical),
                tdeeTheoretical = String.format("%.0f kcal", tdeeTheoretical),
                factor = if (bmrMifflin != null && bmrMifflin > 0f)
                    String.format("%.2f", tdeeTheoretical / bmrMifflin)
                else "—",
                activityDate = lastDate
            ),
            activity = ActivityUi(
                steps = steps,
                stepsKcal = stepsKcal,
                cardioMinutes = cardio,
                cardioKcal = cardioKcal,
                gymKcal = gymKcal,
                trained = gym
            ),
            macros = macros,
            goals = computeGoals(leanMass),
            alerts = alerts,
            weightSparkline = weightPoints
        )
    }

    private fun computeBodyFat(
        sex: Sex?,
        heightCm: Float?,
        neckCm: Float?,
        waistCm: Float?,
        hipCm: Float?
    ): Float? {
        val heightIn = heightCm?.takeIf { it > 0f }?.div(2.54f) ?: return null
        val neckIn = neckCm?.takeIf { it > 0f }?.div(2.54f) ?: return null
        val waistIn = waistCm?.takeIf { it > 0f }?.div(2.54f) ?: return null

        val bf = when (sex) {
            Sex.Male -> {
                // HOMBRE – US Navy (pulgadas)
                if (waistIn > neckIn) {
                    (86.010f * log10(waistIn - neckIn) -
                            70.041f * log10(heightIn) +
                            36.76f)
                } else null
            }

            Sex.Female -> {
                // MUJER – US Navy (pulgadas)
                val hipIn = hipCm?.takeIf { it > 0f }?.div(2.54f) ?: return null
                if (waistIn + hipIn > neckIn) {
                    (163.205f * log10(waistIn + hipIn - neckIn) -
                            97.684f * log10(heightIn) -
                            78.387f)
                } else null
            }

            else -> null
        }

        // Clamp para evitar valores absurdos por malas medidas
        return bf?.coerceIn(3f, 60f)
    }

    private fun computeMacros(
        profile: UserProfile,
        stage: TrainingStage,
        bodyFat: Float?, // reservado por si luego afinamos por %BF
        calories: Float,
        leanMass: Float?
    ): MacrosUi {
        val ffm = leanMass ?: return MacrosUi(
            calories = String.format("%d kcal", calories.toInt())
        )

        // Ratios g/kg FFM (punto medio de rangos)
        val (proteinPerKg, fatPerKg) = when (profile.sex) {
            Sex.Female -> {
                if (profile.usesPharmacology) {
                    when (stage) {
                        TrainingStage.DEFINICION -> 2.25f to 0.6f
                        TrainingStage.MANTENIMIENTO -> 2.15f to 0.7f
                        TrainingStage.VOLUMEN -> 2.05f to 0.9f
                    }
                } else {
                    when (stage) {
                        TrainingStage.DEFINICION -> 2.45f to 0.9f
                        TrainingStage.MANTENIMIENTO -> 2.35f to 1.0f
                        TrainingStage.VOLUMEN -> 2.10f to 1.1f
                    }
                }
            }

            else -> { // Hombre
                if (profile.usesPharmacology) {
                    // HOMBRE + FARMA (definición 2.0–2.3 / 0.4–0.6)
                    when (stage) {
                        TrainingStage.DEFINICION -> 2.15f to 0.5f
                        TrainingStage.MANTENIMIENTO -> 2.15f to 0.7f
                        TrainingStage.VOLUMEN -> 1.95f to 0.7f
                    }
                } else {
                    // HOMBRE NATURAL
                    when (stage) {
                        TrainingStage.DEFINICION -> 2.60f to 0.8f
                        TrainingStage.MANTENIMIENTO -> 2.45f to 0.9f
                        TrainingStage.VOLUMEN -> 2.35f to 1.0f
                    }
                }
            }
        }

        val protein = proteinPerKg * ffm
        val fat = fatPerKg * ffm

        val calFromProtein = protein * 4f
        val calFromFat = fat * 9f
        val carbCalories = (calories - calFromProtein - calFromFat).coerceAtLeast(0f)
        val carbs = carbCalories / 4f

        return MacrosUi(
            calories = String.format("%d kcal", calories.toInt()),
            delta = "—",
            protein = String.format("%.0f g", protein),
            fat = String.format("%.0f g", fat),
            carbs = String.format("%.0f g", carbs)
        )
    }

    private fun computeGoals(leanMass: Float?): BodyGoalsUi {
        leanMass ?: return BodyGoalsUi()
        val targets = listOf(12, 10, 8, 6, 5)
        val list = targets.map { bf ->
            val weightTarget = leanMass / (1 - bf / 100f)
            "${bf}% → ${String.format("%.1f kg", weightTarget)}"
        }
        return BodyGoalsUi(
            targets = list,
            summary = "Masa magra actual usada como base"
        )
    }

    private fun computeVelocity(metrics: List<DailyMetrics>): String {
        if (metrics.size < 2) return "—"
        val sorted = metrics.sortedBy { it.dateEpoch }
        val first = sorted.first()
        val last = sorted.last()
        val days = ChronoUnit.DAYS.between(
            LocalDate.ofEpochDay(first.dateEpoch),
            LocalDate.ofEpochDay(last.dateEpoch)
        ).coerceAtLeast(1)
        val delta = last.weightFasted - first.weightFasted
        val weekly = delta / (days / 7f)
        return String.format("%.2f kg/sem", weekly)
    }

    private fun computeRecommendedCalories(
        stage: TrainingStage,
        tdee: Float,
        bodyFat: Float?
    ): Float {
        val bf = bodyFat ?: 15f

        return when (stage) {
            TrainingStage.DEFINICION -> {
                // Déficit según % grasa actual
                val deficit = when {
                    bf >= 25f -> 600f
                    bf >= 18f -> 500f
                    bf >= 12f -> 400f
                    bf >= 8f -> 300f
                    else -> 200f
                }
                // Límite de seguridad: no más del 30% de TDEE
                (tdee - deficit).coerceAtLeast(tdee * 0.7f)
            }

            TrainingStage.MANTENIMIENTO -> tdee
            TrainingStage.VOLUMEN -> tdee + 250f
        }
    }

    private fun TrainingStage.toReadableLabel(): String = when (this) {
        TrainingStage.DEFINICION -> "DEFINICIÓN"
        TrainingStage.MANTENIMIENTO -> "MANTENIMIENTO"
        TrainingStage.VOLUMEN -> "VOLUMEN"
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val appContext = context.applicationContext
                    val dailyMetricsRepository =
                        ServiceLocator.provideDailyMetricsRepository(appContext)
                    val userProfileRepository =
                        ServiceLocator.provideUserProfileRepository(appContext)
                    val observeProfileUseCase =
                        ObserveUserProfileUseCase(userProfileRepository)
                    val saveProfileUseCase =
                        SaveUserProfileUseCase(userProfileRepository)
                    val observeWeeklyMetricsUseCase =
                        ObserveWeeklyMetricsUseCase(dailyMetricsRepository)
                    @Suppress("UNCHECKED_CAST")
                    return BodyDashboardViewModel(
                        observeProfileUseCase,
                        saveProfileUseCase,
                        observeWeeklyMetricsUseCase
                    ) as T
                }
            }
    }
}
