package pe.com.zzynan.procardapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import pe.com.zzynan.procardapp.ui.ProcardApp

// File: app/src/main/java/pe/com/zzynan/procardapp/MainActivity.kt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Activa el modo edge-to-edge para aprovechar todo el espacio disponible en pantalla.
        enableEdgeToEdge()
        setContent {
            // Inicia el árbol de composables principal de la aplicación.
            ProcardApp()
        }
    }
}
