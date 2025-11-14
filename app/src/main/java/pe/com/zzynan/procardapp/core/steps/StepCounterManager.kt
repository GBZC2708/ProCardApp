package pe.com.zzynan.procardapp.core.steps

import android.content.Context
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.StateFlow

/**
 * Gestor accesible desde ViewModels para disparar acciones del servicio sin exponer Intents en la UI.
 */
class StepCounterManager(context: Context) {

    private val appContext = context.applicationContext

    fun observeState(): StateFlow<StepCounterState> = StepCounterStateHolder.state

    /**
     * Asegura que el servicio esté activo sin iniciar el conteo, evitando arranques repetidos.
     */
    fun ensureServiceRunning() {
        ContextCompat.startForegroundService(
            appContext,
            StepCounterService.intent(appContext, StepCounterService.ACTION_INITIALIZE)
        )
    }

    /**
     * Alterna entre play y pausa delegando la lógica al servicio para mantener un solo punto de verdad.
     */
    fun toggle() {
        ContextCompat.startForegroundService(
            appContext,
            StepCounterService.intent(appContext, StepCounterService.ACTION_TOGGLE)
        )
    }
}
