package co.edu.unal.reto9;

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
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
import kotlinx.coroutines.delay
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
    private lateinit var spinner: Spinner
    private lateinit var municipios: MutableList<String>
    private val cundinamarca = Cundinamarca()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        setContentView(R.layout.activity_main)

        map = findViewById(R.id.map)
        map.setMultiTouchControls(true)
        cundinamarca.obtenerDatosDesdeAPI()
        while(!cundinamarca.isLoad()){
            //Log.d("Carga","cargando")
        }
        municipios = cundinamarca.obtenerMunicipios()
        municipios.add(0, "TODOS");
        spinner = findViewById(R.id.spinnerMunicipios)

        // Crea un ArrayAdapter para el Spinner
        val adapter = ArrayAdapter(
            this,  // Contexto
            android.R.layout.simple_spinner_item,  // Diseño para el item del Spinner
            municipios  // Lista de elementos
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)  // Diseño para el desplegable

        spinner.adapter = adapter

        spinner.setSelection(0)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Obtener el elemento seleccionado
                val selectedItem = parent?.getItemAtPosition(position).toString()
                seleccionarMunicipio(selectedItem)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No se seleccionó nada
            }
        }

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

                val currentLocation = GeoPoint(currentLat, currentLon)
                map.controller.setCenter(currentLocation)
                map.controller.setZoom(10.0)

                // Cargar los puntos de interés dentro del radio
                cargarPuntosDeInteres(currentLat, currentLon, 0)

                // Agregar un marcador en la ubicación actual
                agregarMarcador(currentLat, currentLon, "Estás aquí","Estás aquí")

            }
        }
    }


    private fun cargarPuntosDeInteres(lat: Double, lon: Double, municipio: Int) {
        val url = "https://services7.arcgis.com/lsxbLWF2l19Rmhqj/arcgis/rest/services/Equipamiento_Cundinamarca/FeatureServer/13/query?where=1%3D1&outFields=*&f=json"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                response.body?.string()?.let { json ->
                    val jsonObject = JSONObject(json)
                    val features = jsonObject.getJSONArray("features")

                    for (i in 0 until features.length()) {
                        if(municipio == 0){
                            val attributes = features.getJSONObject(i).getJSONObject("attributes")
                            val geometry = features.getJSONObject(i).getJSONObject("geometry")

                            val lat = geometry.optDouble("y", 0.0)
                            val lon = geometry.optDouble("x", 0.0)
                            val nombre = attributes.optString("NOMBRE", "Punto de interés")

                            runOnUiThread {
                                agregarMarcador(lat, lon,nombre, "hospital")
                            }
                        }else{
                            val attributes = features.getJSONObject(i).getJSONObject("attributes")
                            val geometry = features.getJSONObject(i).getJSONObject("geometry")

                            val lat = geometry.optDouble("y", 0.0)
                            val lon = geometry.optDouble("x", 0.0)
                            val nombre = attributes.optString("NOMBRE", "Punto de interés")
                            val CODIGO_MUN = attributes.optString("CODIGO_MUN", "CODIGO_MUN")
                            if (CODIGO_MUN.equals(municipio.toString())){
                                runOnUiThread {
                                    agregarMarcador(lat, lon,nombre, "hospital")
                                }
                            }
                        }
                    }
                }
            }
        })

    }

    fun seleccionarMunicipio(municipio: String){
        if(municipio.equals("TODOS")){
            obtenerUbicacionActual()
        }else{
            val codigoMunicipio = cundinamarca.obtenerCodigoPorNombre(municipio)
            val latitud = cundinamarca.obtenerLatitudPorNombre(municipio)
            val longitud = cundinamarca.obtenerLongitudPorNombre(municipio)

            if (latitud != null && longitud != null && codigoMunicipio != null) {
                cargarPuntosDeInteres(latitud,longitud,codigoMunicipio)
            }else{
                Log.e("cargar Puntos","latitud or longitud or codigo is null")
            }

            if (latitud != null && longitud != null) {
                Reposicionar(latitud,longitud)
            }else{
                Log.e("reposicionar","latitud or longitud is null")
            }
        }

    }

    private fun Reposicionar(currentLat:Double, currentLon:Double) {
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

                val currentLocation = GeoPoint(currentLat, currentLon)
                map.controller.setCenter(currentLocation)
                map.controller.setZoom(15.0)
            }
        }
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
