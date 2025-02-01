package co.edu.unal.reto9;

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.colorspace.WhitePoint
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import org.osmdroid.config.Configuration
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import com.google.android.gms.location.LocationServices
import org.osmdroid.views.overlay.Polygon
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var mas: Button
    private lateinit var texto: TextView
    private lateinit var menos: Button
    private var radio: Int = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        setContentView(R.layout.activity_main)

        map = findViewById(R.id.map)
        mas = findViewById(R.id.mas)
        menos = findViewById(R.id.menos)
        texto = findViewById(R.id.texto)
        texto.setVisibility(View.VISIBLE)
        map.setMultiTouchControls(true)

        // Establece un punto inicial en el mapa
        val startPoint = GeoPoint(0.0, 0.0) // Cambiar luego a la ubicación actual
        map.controller.setZoom(20.0)
        map.controller.setCenter(startPoint)

        // Habilitar los controles de multi-touch (gestos como zoom)
        map.setMultiTouchControls(true)

        // Habilitar el seguimiento de la ubicación (opcional, solo si quieres que siga al usuario)
        map.setTilesScaledToDpi(true)

        // Verificar permisos de ubicación
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            obtenerUbicacionActual()
        }


        mas.setOnClickListener{
            radio=radio+1000
            texto.text = (radio/1000).toString() + " km"
            map.overlays.clear()
            obtenerUbicacionActual()
        }

        menos.setOnClickListener{
            if (radio>1000){
                radio=radio-1000
                texto.text = (radio/1000).toString() + " km"
                map.overlays.clear()
                obtenerUbicacionActual()
            }
        }
    }

    private fun agregarMarcador(lat: Double, lon: Double, titulo: String, tipo: String) {
        val marker = Marker(map)
        marker.position = GeoPoint(lat, lon)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = titulo

        marker.setOnMarkerClickListener { _, _ ->
            marker.showInfoWindow() // Muestra el nombre como ventana emergente
            true
        }

        // Asignar el ícono según el tipo
        val icono = when (tipo) {
            "hospital" -> R.drawable.hospital // Ícono para hospitales
            "restaurant" -> R.drawable.restaurant // Ícono para restaurantes
            "school" -> R.drawable.education // Ícono para escuelas
            else -> org.osmdroid.library.R.drawable.marker_default // Ícono predeterminado
        }
        marker.icon = resources.getDrawable(icono, null)

        map.overlays.add(marker)
    }



    private fun obtenerUbicacionActual() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLat = location.latitude
                val currentLon = location.longitude

                // Centrar el mapa en la ubicación actual
                val currentLocation = GeoPoint(currentLat, currentLon)
                map.controller.setCenter(currentLocation)  // Centrar el mapa
                map.controller.setZoom(15.0)  // Ajustar el zoom

                // Obtener el radio de preferencias y dibujar el círculo
                agregarRadio(currentLat, currentLon, radio)  // Dibujar el círculo con el radio

                // Agregar un marcador en la ubicación actual
                agregarMarcador(currentLat, currentLon, "Estás aquí","Estás aquí")

                // Cargar los puntos de interés dentro del radio
                cargarPuntosDeInteres(currentLat, currentLon)
            }
        }
    }


    private fun cargarPuntosDeInteres(lat: Double, lon: Double) {
        val url = "https://overpass-api.de/api/interpreter?data=[out:json];node(around:${radio},${lat},${lon});out;"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                response.body?.string()?.let { responseData ->
                    val json = JSONObject(responseData)
                    val elements = json.getJSONArray("elements")

                    runOnUiThread {
                        for (i in 0 until elements.length()) {
                            val element = elements.getJSONObject(i)
                            val lat = element.getDouble("lat")
                            val lon = element.getDouble("lon")

                            // Obtener el objeto "tags"
                            val tags = element.optJSONObject("tags")

                            // Filtrar los puntos de interés que sean del tipo deseado
                            val amenity = tags?.optString("amenity")  // Tipo del lugar (hospital, restaurante, etc.)
                            if (amenity in listOf("hospital", "restaurant", "school")) { // Cambia los filtros según lo necesario
                                val name = tags?.optString("name", "Punto de interés") ?: "Punto de interés"
                                if (amenity != null) {
                                    agregarMarcador(lat, lon, name, amenity)
                                }
                            }
                        }
                    }
                }
            }
        })
    }


    // Función para agregar un círculo (radio) en el mapa
    private fun agregarRadio(lat: Double, lon: Double, radio: Int) {
        val numPoints = 360  // Número de puntos para el círculo (más puntos = círculo más suave)
        val points = mutableListOf<GeoPoint>()

        // Convertimos el radio a grados de latitud/longitud (aproximadamente)
        val earthRadius = 6371000.0  // Radio de la Tierra en metros
        val latRad = Math.toRadians(lat)
        val lonRad = Math.toRadians(lon)

        for (i in 0 until numPoints) {
            val angle = i * (2 * PI / numPoints)  // Ángulo para cada punto del círculo
            val dLat = (radio / earthRadius) * (180.0 / PI) * cos(angle)  // Desplazamiento en latitud
            val dLon = (radio / earthRadius) * (180.0 / PI) * sin(angle) / cos(latRad)  // Desplazamiento en longitud

            // Calculamos los nuevos puntos alrededor del círculo
            val newLat = lat + dLat
            val newLon = lon + dLon

            points.add(GeoPoint(newLat, newLon))
        }

        // Crear el polígono (círculo) con los puntos generados
        val circle = Polygon()
        circle.points = points
        circle.fillColor = 0x330000FF  // Azul con opacidad
        circle.strokeWidth = 2f  // Grosor del borde del círculo

        // Agregar el círculo como overlay en el mapa
        map.overlays.add(circle)
    }



}

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, SettingsFragment())
            .commit()
    }
}


class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}
