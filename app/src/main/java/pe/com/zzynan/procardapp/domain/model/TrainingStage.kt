package pe.com.zzynan.procardapp.domain.model

/**
 * Modelo de dominio para representar la etapa de entrenamiento sin acoplarse a Room.
 */
enum class TrainingStage(val value: Int) {
    DEFINICION(0),
    MANTENIMIENTO(1),
    DEFICIT(2);

    companion object {
        /**
         * Conversión segura desde el entero persistido, evitando branching pesado en la UI.
         */
        fun fromValue(value: Int): TrainingStage = when (value) {
            DEFINICION.value -> DEFINICION
            MANTENIMIENTO.value -> MANTENIMIENTO
            else -> DEFICIT
        }
    }
}
