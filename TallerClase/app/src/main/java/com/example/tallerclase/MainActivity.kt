package com.example.tallerclase

// Importaciones necesarias para trabajar con permisos, ubicación y vistas.
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.tallerclase.databinding.ActivityMainBinding
import com.google.android.gms.location.*

// Clase principal de la actividad, extiende AppCompatActivity.
class MainActivity : AppCompatActivity() {

    // Declaración de variables para el enlace de vistas y cliente de ubicación.
    private lateinit var binding: ActivityMainBinding
    private val CODIGO_PERMISO_UBICACION = 100
    private var isPermisos = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    // Método onCreate: se ejecuta cuando la actividad se crea.
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Solicitud inicial de permisos de ubicación.
        solicitarPermisosUbicacion()
    }

    // Método para solicitar permisos de ubicación.
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun solicitarPermisosUbicacion() {
        when {
            // Si los permisos están concedidos, iniciar localización.
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED -> {
                isPermisos = true
                iniciarLocalizacion()
            }

            // Si se deben mostrar explicaciones adicionales.
            ActivityCompat.shouldShowRequestPermissionRationale(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                mostrarDialogoExplicativo()
            }

            // Solicitar permisos si no están concedidos.
            else -> {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    CODIGO_PERMISO_UBICACION
                )
            }
        }
    }

    // Mostrar un diálogo para explicar la necesidad de permisos.
    private fun mostrarDialogoExplicativo() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Permisos necesarios")
            .setMessage("Esta aplicación requiere acceso a tu ubicación para mostrar coordenadas.")
            .setPositiveButton("Sí") { _, _ ->
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ), CODIGO_PERMISO_UBICACION
                )
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                mostrarMensajePermisosDenegados()
            }
            .create()
            .show()
    }

    // Mostrar un mensaje de error si se deniegan los permisos.
    private fun mostrarMensajePermisosDenegados() {
        Toast.makeText(
            this, "La aplicación necesita permisos de ubicación para funcionar", Toast.LENGTH_LONG
        ).show()
    }

    // Iniciar la configuración y actualización de la ubicación.
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun iniciarLocalizacion() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        try {
            // Configuración de solicitud de ubicación con alta precisión.
            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 30000
            ).apply {
                setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                setWaitForAccurateLocation(true)
            }.build()

            // Callback para recibir actualizaciones de ubicación.
            locationCallback = object : LocationCallback() {
                @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    locationResult.lastLocation?.let { location -> actualizarUbicacion(location) }
                }
            }

            // Validación de permisos nuevamente antes de iniciar las actualizaciones.
            if (ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) return

            // Obtener última ubicación conocida.
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let { actualizarUbicacion(it) }
                }
                .addOnFailureListener { e ->
                    Log.e("Location", "Error obteniendo ubicación", e)
                    Toast.makeText(this, "Error al obtener la ubicación", Toast.LENGTH_SHORT).show()
                }

            // Solicitar actualizaciones de ubicación.
            fusedLocationClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            )

        } catch (e: SecurityException) {
            Log.e("Location", "Error de permisos: ${e.message}")
            Toast.makeText(this, "Error: Permisos no disponibles", Toast.LENGTH_LONG).show()
        }
    }

    // Actualizar la ubicación en la interfaz de usuario.
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun actualizarUbicacion(location: android.location.Location) {
        binding.apply {
            tvLatitud.text = String.format("%.6f", location.latitude)
            tvLongitud.text = String.format("%.6f", location.longitude)
        }
        Log.d("Location", "Lat: ${location.latitude}, Lon: ${location.longitude}")
    }

    // Método para manejar la respuesta de permisos del usuario.
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            CODIGO_PERMISO_UBICACION -> {
                if (grantResults.isNotEmpty() &&
                    grantResults.all { it == PackageManager.PERMISSION_GRANTED }
                ) {
                    isPermisos = true
                    iniciarLocalizacion()
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this, Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    ) {
                        mostrarDialogoExplicativo()
                    } else {
                        mostrarDialogoConfiguracion()
                    }
                }
            }
        }
    }

    // Mostrar diálogo para configurar permisos manualmente.
    private fun mostrarDialogoConfiguracion() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Permisos requeridos")
            .setMessage("Es necesario habilitar permisos en configuración para que funcione correctamente.")
            .setPositiveButton("Ir a Configuración") { _, _ -> abrirConfiguracionAplicacion() }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
                mostrarMensajePermisosDenegados()
            }
            .create()
            .show()
    }

    // Abrir la configuración de la aplicación.
    private fun abrirConfiguracionAplicacion() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.fromParts("package", packageName, null)
            startActivity(this)
        }
    }

    // Eliminar actualizaciones de ubicación al destruir la actividad.
    override fun onDestroy() {
        super.onDestroy()
        if (::locationCallback.isInitialized && ::fusedLocationClient.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    // Abrir una nueva actividad (posiblemente un mapa).
    fun onMaps(view: View?) {
        val intent = Intent(this, MainActivity1::class.java)
        startActivity(intent)
    }
}
