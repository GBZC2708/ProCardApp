package pe.com.zzynan.procardapp.domain.model

enum class SupplementTimeSlot {
    FASTED,
    BEFORE_BREAKFAST,
    WITH_BREAKFAST,
    AFTER_BREAKFAST,
    MID_MORNING,
    PRE_WORKOUT,
    INTRA_WORKOUT,
    POST_WORKOUT,
    BEFORE_LUNCH,
    WITH_LUNCH,
    AFTER_LUNCH,
    AFTERNOON_SNACK,
    BEFORE_DINNER,
    WITH_DINNER,
    AFTER_DINNER,
    BEFORE_SLEEP
}

fun SupplementTimeSlot.label(): String = when (this) {
    SupplementTimeSlot.FASTED -> "Ayunas"
    SupplementTimeSlot.BEFORE_BREAKFAST -> "Antes del desayuno"
    SupplementTimeSlot.WITH_BREAKFAST -> "Con el desayuno"
    SupplementTimeSlot.AFTER_BREAKFAST -> "Después del desayuno"
    SupplementTimeSlot.MID_MORNING -> "Media mañana"
    SupplementTimeSlot.PRE_WORKOUT -> "Pre-entreno"
    SupplementTimeSlot.INTRA_WORKOUT -> "Intra-entreno"
    SupplementTimeSlot.POST_WORKOUT -> "Post-entreno"
    SupplementTimeSlot.BEFORE_LUNCH -> "Antes del almuerzo"
    SupplementTimeSlot.WITH_LUNCH -> "Con el almuerzo"
    SupplementTimeSlot.AFTER_LUNCH -> "Después del almuerzo"
    SupplementTimeSlot.AFTERNOON_SNACK -> "Merienda tarde"
    SupplementTimeSlot.BEFORE_DINNER -> "Antes de la cena"
    SupplementTimeSlot.WITH_DINNER -> "Con la cena"
    SupplementTimeSlot.AFTER_DINNER -> "Después de la cena"
    SupplementTimeSlot.BEFORE_SLEEP -> "Antes de dormir"
}

fun String.toSupplementTimeSlot(): SupplementTimeSlot = runCatching {
    SupplementTimeSlot.valueOf(this)
}.getOrDefault(SupplementTimeSlot.FASTED)
