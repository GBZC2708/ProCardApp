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

    private val profileFlow = observeUserProfileUseCase()
    private val metricsFlow = profileFlow.flatMapLatest { profile ->
        observeWeeklyMetricsUseCase(
            username = profile.displayName,
            endDateEpoch = LocalDate.now().toEpochDay(),
            days = 14
        )
    }

    private val combined = combine(profileFlow, metricsFlow) { profile, metrics ->
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

    fun onPharmaUsageChange(enabled: Boolean) = persistProfile { it.copy(usesPharmacology = enabled) }

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
            val currentProfile = profileFlow.stateIn(viewModelScope, SharingStarted.Eagerly, UserProfile())
                .value
            val updated = block(currentProfile)
            saveUserProfileUseCase(updated)
        }
    }

    private suspend fun updateState(profile: UserProfile, metrics: List<DailyMetrics>) {
        val lastMetric = metrics.maxByOrNull { it.dateEpoch }
        val lastDate = lastMetric?.let { LocalDate.ofEpochDay(it.dateEpoch) }
        val weightPoints = metrics.sortedBy { it.dateEpoch }.map {
            TimeSeriesPoint(LocalDate.ofEpochDay(it.dateEpoch), it.weightFasted.takeIf { w -> w > 0f })
        }
        val weight = lastMetric?.weightFasted?.takeIf { it > 0f }
        val heightM = profile.heightCm?.div(100f)
        val bmi = if (weight != null && heightM != null && heightM > 0) weight / (heightM * heightM) else null

        val waist = profile.waistCm
        val neck = profile.neckCm
        val hip = profile.hipCm
        val bodyFat = computeBodyFat(profile.sex, profile.heightCm, neck, waist, hip)
        val leanMass = bodyFat?.let { bf -> weight?.let { it - (it * bf / 100f) } }
        val ffmi = if (leanMass != null && heightM != null && heightM > 0) leanMass / (heightM * heightM) else null

        val bmrMifflin = weight?.let { w ->
            val s = if (profile.sex == Sex.Male) 5 else -161
            profile.age?.let { age -> profile.heightCm?.let { h -> 10 * w + 6.25 * h - 5 * age + s } }
        }
        val bmrKatch = leanMass?.let { 370 + 21.6f * it }

        val steps = lastMetric?.dailySteps ?: 0
        val cardio = lastMetric?.cardioMinutes ?: 0
        val gym = (lastMetric?.trainingDone == true)
        val stepsKcal = steps * 0.04f
        val cardioKcal = cardio * 7f
        val gymKcal = if (gym) 250f else 0f
        val activityKcal = stepsKcal + cardioKcal + gymKcal
        val tdeeTheoretical = (bmrMifflin ?: 0f) + activityKcal

        val macros = computeMacros(profile, tdeeTheoretical, leanMass ?: weight)

        val alerts = mutableListOf<BodyDashboardAlert>()
        if (bodyFat != null && bodyFat < 6f) {
            alerts += BodyDashboardAlert("⚠️ Grasa muy baja. Mantén esta condición solo por periodos cortos.", true)
        }

        _uiState.value = _uiState.value.copy(
            phaseLabel = (lastMetric?.stage ?: TrainingStage.DEFINICION).name,
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
                calories = String.format("%d kcal", tdeeTheoretical.toInt()),
                weightVelocity = computeVelocity(metrics)
            ),
            composition = BodyCompositionUi(
                weight = weight?.let { String.format("%.1f kg", it) } ?: "—",
                height = profile.heightCm?.let { String.format("%.0f cm", it) } ?: "—",
                age = profile.age?.toString() ?: "—",
                bmi = bmi?.let { String.format("%.1f", it) } ?: "—",
                ffmi = ffmi?.let { String.format("%.1f", it) } ?: "—",
                bodyFat = bodyFat?.let { String.format("%.1f %%", it) } ?: "—",
                fatMass = bodyFat?.let { bf -> weight?.let { w -> String.format("%.1f kg", w * bf / 100f) } } ?: "—",
                leanMass = leanMass?.let { String.format("%.1f kg", it) } ?: "—",
                whr = if (waist != null && hip != null && hip > 0) String.format("%.2f", waist / hip) else "—",
                whtr = if (waist != null && profile.heightCm != null && profile.heightCm > 0) String.format("%.2f", waist / profile.heightCm) else "—"
            ),
            metabolism = MetabolismUi(
                bmrKatch = bmrKatch?.let { String.format("%.0f kcal", it) } ?: "—",
                bmrMifflin = bmrMifflin?.let { String.format("%.0f kcal", it) } ?: "—",
                tdeeReal = String.format("%.0f kcal", tdeeTheoretical),
                tdeeTheoretical = String.format("%.0f kcal", tdeeTheoretical),
                factor = if (bmrMifflin != null && bmrMifflin > 0) String.format("%.2f", tdeeTheoretical / bmrMifflin) else "—",
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

    private fun computeBodyFat(sex: Sex?, heightCm: Float?, neckCm: Float?, waistCm: Float?, hipCm: Float?): Float? {
        val heightIn = heightCm?.takeIf { it > 0 }?.div(2.54f) ?: return null
        val neckIn = neckCm?.takeIf { it > 0 }?.div(2.54f) ?: return null
        val waistIn = waistCm?.takeIf { it > 0 }?.div(2.54f) ?: return null
        return when (sex) {
            Sex.Male -> if (waistIn > neckIn) {
                (495 / (1.0324 - 0.19077 * log10(waistIn - neckIn) + 0.15456 * log10(heightIn)) - 450).toFloat()
            } else null
            Sex.Female -> {
                val hipIn = hipCm?.takeIf { it > 0 }?.div(2.54f) ?: return null
                if (waistIn + hipIn > neckIn) {
                    (495 / (1.29579 - 0.35004 * log10(waistIn + hipIn - neckIn) + 0.22100 * log10(heightIn)) - 450).toFloat()
                } else null
            }
            else -> null
        }
    }

    private fun computeMacros(profile: UserProfile, tdee: Float, leanMass: Float?): MacrosUi {
        val ffm = leanMass ?: return MacrosUi(calories = String.format("%d kcal", tdee.toInt()))
        val proteinPerKg = when (profile.sex) {
            Sex.Female -> if (profile.usesPharmacology) 2.1f else 2.45f
            else -> if (profile.usesPharmacology) 2.2f else 2.45f
        }
        val fatPerKg = when (profile.sex) {
            Sex.Female -> if (profile.usesPharmacology) 0.7f else 0.9f
            else -> if (profile.usesPharmacology) 0.6f else 0.9f
        }
        val protein = proteinPerKg * ffm
        val fat = fatPerKg * ffm
        val calFromProtein = protein * 4
        val calFromFat = fat * 9
        val carbCalories = (tdee - calFromProtein - calFromFat).coerceAtLeast(0f)
        val carbs = carbCalories / 4
        return MacrosUi(
            calories = String.format("%d kcal", tdee.toInt()),
            delta = "0 kcal",
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
        val days = ChronoUnit.DAYS.between(LocalDate.ofEpochDay(first.dateEpoch), LocalDate.ofEpochDay(last.dateEpoch)).coerceAtLeast(1)
        val delta = last.weightFasted - first.weightFasted
        val weekly = delta / (days / 7f)
        return String.format("%.2f kg/sem", weekly)
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val appContext = context.applicationContext
                val dailyMetricsRepository = ServiceLocator.provideDailyMetricsRepository(appContext)
                val userProfileRepository = ServiceLocator.provideUserProfileRepository(appContext)
                val observeProfileUseCase = ObserveUserProfileUseCase(userProfileRepository)
                val saveProfileUseCase = SaveUserProfileUseCase(userProfileRepository)
                val observeWeeklyMetricsUseCase = ObserveWeeklyMetricsUseCase(dailyMetricsRepository)
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
