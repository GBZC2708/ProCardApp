package pe.com.zzynan.procardapp.core.extensions

import java.time.LocalDate

/**
 * Convierte un LocalDate al número de días desde epoch (1970-01-01) optimizando almacenamiento.
 * Ejemplo: val hoyEpoch = LocalDate.now().toEpochDayLong()
 */
fun LocalDate.toEpochDayLong(): Long = this.toEpochDay()

/**
 * Convierte un epochDay persistido a un LocalDate para manipular fechas en la capa de dominio.
 * Ejemplo: val fecha = storedEpoch.toLocalDate()
 */
fun Long.toLocalDate(): LocalDate = LocalDate.ofEpochDay(this)
