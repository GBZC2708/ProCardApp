package pe.com.zzynan.procardapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import pe.com.zzynan.procardapp.ui.ProcardApp

// File: app/src/main/java/pe/com/zzynan/procardapp/MainActivity.kt
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pide el permiso de actividad física solo si es necesario (Android 10+).
        requestActivityRecognitionPermissionIfNeeded()

        // Activa el modo edge-to-edge para aprovechar todo el espacio disponible en pantalla.
        enableEdgeToEdge()
        setContent {
            // Inicia el árbol de composables principal de la aplicación.
            ProcardApp()
        }
    }

    /**
     * Pide el permiso de reconocimiento de actividad (pasos) solo en Android 10+
     * y únicamente si aún no ha sido concedido.
     */
    private fun requestActivityRecognitionPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val permission = Manifest.permission.ACTIVITY_RECOGNITION

            val alreadyGranted = ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED

            if (!alreadyGranted) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permission),
                    REQUEST_ACTIVITY_RECOGNITION
                )
            }
        }
    }

    companion object {
        private const val REQUEST_ACTIVITY_RECOGNITION = 1002
    }
}
