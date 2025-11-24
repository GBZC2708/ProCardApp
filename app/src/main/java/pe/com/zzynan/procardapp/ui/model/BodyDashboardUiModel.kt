package pe.com.zzynan.procardapp.ui.model

import java.time.LocalDate
import pe.com.zzynan.procardapp.domain.model.Sex

/** Modelos compactos para la pantalla Body Dashboard. */
data class BodyDashboardUiState(
    val isQuickReviewMode: Boolean = false,
    val phaseLabel: String = "DEFINICIÓN",
    val lastDate: LocalDate? = null,
    val physicalData: PhysicalDataUi = PhysicalDataUi(),
    val measurements: BodyMeasurementsUi = BodyMeasurementsUi(),
    val kpiSummary: KpiSummaryUi = KpiSummaryUi(),
    val composition: BodyCompositionUi = BodyCompositionUi(),
    val metabolism: MetabolismUi = MetabolismUi(),
    val activity: ActivityUi = ActivityUi(),
    val macros: MacrosUi = MacrosUi(),
    val goals: BodyGoalsUi = BodyGoalsUi(),
    val alerts: List<BodyDashboardAlert> = emptyList(),
    val weightSparkline: List<TimeSeriesPoint> = emptyList()
)

data class PhysicalDataUi(
    val sex: Sex? = null,
    val usesPharma: Boolean = false,
    val age: Int? = null,
    val height: Float? = null
)

data class BodyMeasurementsUi(
    val neck: Float? = null,
    val waist: Float? = null,
    val hip: Float? = null,
    val chest: Float? = null,
    val wrist: Float? = null,
    val thigh: Float? = null,
    val calf: Float? = null,
    val relaxedBiceps: Float? = null,
    val flexedBiceps: Float? = null,
    val forearm: Float? = null,
    val foot: Float? = null
)

data class KpiSummaryUi(
    val bodyFat: String = "—",
    val fatFreeMass: String = "—",
    val calories: String = "—",
    val weightVelocity: String = "—"
)

data class BodyCompositionUi(
    val weight: String = "—",
    val height: String = "—",
    val age: String = "—",
    val bmi: String = "—",
    val ffmi: String = "—",
    val bodyFat: String = "—",
    val fatMass: String = "—",
    val leanMass: String = "—",
    val whr: String = "—",
    val whtr: String = "—"
)

data class MetabolismUi(
    val bmrKatch: String = "—",
    val bmrMifflin: String = "—",
    val tdeeReal: String = "—",
    val tdeeTheoretical: String = "—",
    val factor: String = "—",
    val activityDate: LocalDate? = null
)

data class ActivityUi(
    val steps: Int = 0,
    val stepsKcal: Float = 0f,
    val cardioMinutes: Int = 0,
    val cardioKcal: Float = 0f,
    val gymKcal: Float = 0f,
    val trained: Boolean = false
)

data class MacrosUi(
    val calories: String = "—",
    val delta: String = "—",
    val protein: String = "—",
    val fat: String = "—",
    val carbs: String = "—"
)

data class BodyGoalsUi(
    val targets: List<String> = emptyList(),
    val summary: String = ""
)

data class BodyDashboardAlert(
    val message: String,
    val isWarning: Boolean
)

data class TimeSeriesPoint(
    val date: LocalDate,
    val value: Float?
)
