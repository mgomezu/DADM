package co.edu.unal.reto9

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class Cundinamarca {

    /*val codigosMunicipios = mapOf(
        25001 to "AGUA DE DIOS",
        25019 to "ALBÁN",
        25035 to "ANAPOIMA",
        25040 to "ANOLAIMA",
        25053 to "ARBELÁEZ",
        25086 to "BELTRÁN",
        25095 to "BITUIMA",
        25099 to "BOJACÁ",
        25120 to "CABRERA",
        25123 to "CACHIPAY",
        25126 to "CAJICÁ",
        25148 to "CAPARRAPÍ",
        25151 to "CÁQUEZA",
        25154 to "CARMEN DE CARUPA",
        25168 to "CHAGUANÍ",
        25175 to "CHÍA",
        25178 to "CHIPAQUE",
        25181 to "CHOACHÍ",
        25183 to "CHOCONTÁ",
        25200 to "COGUA",
        25214 to "COTA",
        25224 to "CUCUNUBÁ",
        25245 to "EL COLEGIO",
        25258 to "EL PEÑÓN",
        25260 to "EL ROSAL",
        25269 to "FACATATIVÁ",
        25279 to "FÓMEQUE",
        25281 to "FOSCA",
        25286 to "FUNZA",
        25288 to "FÚQUENE",
        25290 to "FUSAGASUGÁ",
        25293 to "GACHALÁ",
        25295 to "GACHANCIPÁ",
        25297 to "GACHETÁ",
        25299 to "GAMA",
        25307 to "GIRARDOT",
        25312 to "GRANADA",
        25317 to "GUACHETÁ",
        25320 to "GUADUAS",
        25322 to "GUASCA",
        25324 to "GUATAQUÍ",
        25326 to "GUATAVITA",
        25328 to "GUAYABAL DE SÍQUIMA",
        25335 to "GUAYABETAL",
        25339 to "GUTIÉRREZ",
        25368 to "JERUSALÉN",
        25372 to "JUNÍN",
        25377 to "LA CALERA",
        25386 to "LA MESA",
        25394 to "LA PALMA",
        25398 to "LA PEÑA",
        25402 to "LA VEGA",
        25407 to "LENGUAZAQUE",
        25426 to "MACHETÁ",
        25430 to "MADRID",
        25436 to "MANTA",
        25438 to "MEDINA",
        25473 to "MOSQUERA",
        25483 to "NARIÑO",
        25486 to "NEMOCÓN",
        25488 to "NILO",
        25489 to "NIMAIMA",
        25491 to "NOCAIMA",
        25506 to "VENECIA",
        25513 to "PACHO",
        25518 to "PAIME",
        25524 to "PANDI",
        25530 to "PARATEBUENO",
        25535 to "PASCA",
        25572 to "PUERTO SALGAR",
        25580 to "PULÍ",
        25592 to "QUEBRADANEGRA",
        25594 to "QUETAME",
        25596 to "QUIPILE",
        25599 to "APULO",
        25612 to "RICAURTE",
        25645 to "SAN ANTONIO DEL TEQUENDAMA",
        25649 to "SAN BERNARDO",
        25653 to "SAN CAYETANO",
        25658 to "SAN FRANCISCO",
        25662 to "SAN JUAN DE RIOSECO",
        25718 to "SASAIMA",
        25736 to "SESQUILÉ",
        25740 to "SIBATÉ",
        25743 to "SILVANIA",
        25745 to "SIMIJACA",
        25754 to "SOACHA",
        25758 to "SOPÓ",
        25769 to "SUBACHOQUE",
        25772 to "SUESCA",
        25777 to "SUPATÁ",
        25779 to "SUSA",
        25781 to "SUTATAUSA",
        25785 to "TABIO",
        25793 to "TAUSA",
        25797 to "TENA",
        25799 to "TENJO",
        25805 to "TIBACUY",
        25807 to "TIBIRITA",
        25815 to "TOCAIMA",
        25817 to "TOCANCIPÁ",
        25823 to "TOPAIPÍ",
        25839 to "UBALÁ",
        25841 to "UBAQUE",
        25843 to "VILLA DE SAN DIEGO DE UBATÉ",
        25845 to "UNE",
        25851 to "ÚTICA",
        25862 to "VERGARA",
        25867 to "VIANÍ",
        25871 to "VILLAGÓMEZ",
        25873 to "VILLAPINZÓN",
        25875 to "VILLETA",
        25878 to "VIOTÁ",
        25885 to "YACOPÍ",
        25898 to "ZIPACÓN",
        25899 to "ZIPAQUIRÁ"
    )*/

    var municipios: MutableList<String> = mutableListOf()
    val codigosMunicipios = mutableMapOf<Int, String>()  // Mapa mutable para almacenar los códigos
    val nombreMunicipios = mutableMapOf<String, Int>()
    val latitudMunicipios = mutableMapOf<String, Double>()
    val longitudMunicipios = mutableMapOf<String, Double>()
    var load = false

    init {
        obtenerDatosDesdeAPI()
    }

    fun obtenerDatosDesdeAPI() {
        val url = "https://www.datos.gov.co/resource/gdxc-w37w.json"
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                val jsonData = response.body?.string()

                if (jsonData != null) {
                    val jsonArray = JSONArray(jsonData)

                    for (i in 458 until 573) {
                        val item = jsonArray.getJSONObject(i)
                        val dpto = item.optString("cod_dpto", "-1")
                        val dptoInt = dpto.toInt()

                        if (dptoInt==25){
                            val codigo = item.optString("cod_mpio", "-1")  // Obtiene el código (valor entero)
                            val municipio = item.optString("nom_mpio", "Desconocido")
                            val lat = item.optString("latitud", "-1")
                            val lon = item.optString("longitud", "-1")

                            val codigoInt = codigo.toInt()
                            val latInt = lat.replace(",", ".").toDouble()
                            val lonInt = lon.replace(",", ".").toDouble()
                            if (codigoInt != -1) {  // Solo guarda si el código es válido
                                municipios.add(municipio)
                                codigosMunicipios[codigoInt] = municipio
                                nombreMunicipios[municipio] = codigoInt
                                latitudMunicipios[municipio] = latInt
                                longitudMunicipios[municipio] = lonInt
                            }
                        }

                    }

                    load = true
                }
            } catch (e: Exception) {
                Log.e("Cundinamarca", e.toString())
            }
        }.start()

    }

    fun isLoad():Boolean{
        return load
    }
    fun obtenerNombrePorCodigo(codigo: Int): String? {
        return codigosMunicipios[codigo]
    }

    fun obtenerCodigoPorNombre(nombre: String): Int? {
        return nombreMunicipios[nombre]
    }

    fun obtenerLatitudPorNombre(nombre: String): Double? {
        return latitudMunicipios[nombre]
    }

    fun obtenerLongitudPorNombre(nombre: String): Double? {
        return longitudMunicipios[nombre]
    }

    // Función para obtener todos los datos por nombre
    data class DatosMunicipio(
        val codigo: Int?,
        val nombre: String?,
        val latitud: Double?,
        val longitud: Double?
    )

    fun obtenerMunicipios(): MutableList<String> {
        Log.d("Cundinamarca 2", "Municipios: ${municipios.size}")
        return municipios
    }

    fun obtenerDatosCompletosPorNombre(nombre: String): DatosMunicipio {
        return DatosMunicipio(
            codigo = nombreMunicipios[nombre],
            nombre = nombre,
            latitud = latitudMunicipios[nombre],
            longitud = longitudMunicipios[nombre]
        )
    }

    // Función para obtener todos los datos por código
    fun obtenerDatosCompletosPorCodigo(codigo: Int): DatosMunicipio? {
        val nombre = codigosMunicipios[codigo] ?: return null
        return DatosMunicipio(
            codigo = codigo,
            nombre = nombre,
            latitud = latitudMunicipios[nombre],
            longitud = longitudMunicipios[nombre]
        )
    }

}