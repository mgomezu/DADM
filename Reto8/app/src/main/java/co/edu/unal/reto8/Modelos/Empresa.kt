package co.edu.unal.reto8.Modelos

import org.json.JSONArray

data class Empresa(
    var nombre: String = "",
    var URL: String = "",
    var Telefono: String = "",
    var Email: String = "",
    var Clasificacion: MutableList<String> = mutableListOf()
) {
    // Conversión de la lista a un formato JSON
    fun getClasificacionAsJson(): String {
        return JSONArray(Clasificacion).toString()
    }

    // Cargar datos desde un formato JSON
    fun setClasificacionFromJson(json: String) {
        Clasificacion = JSONArray(json).let { jsonArray ->
            MutableList(jsonArray.length()) { index -> jsonArray.getString(index) }
        }
    }

    override fun toString(): String {
        return "Empresa(nombre='$nombre', URL='$URL', Telefono='$Telefono', Email='$Email', Clasificacion=$Clasificacion)"
    }
}
