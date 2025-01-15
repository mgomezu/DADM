package co.edu.unal.reto8

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import co.edu.unal.reto8.Modelos.Empresa

class EmpresaAdapter(context: Context, empresas: List<Empresa>) :
    ArrayAdapter<Empresa>(context, 0, empresas) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val empresa = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.item_list, parent, false
        )

        // Vincular vistas
        val nombre = view.findViewById<TextView>(R.id.nombreEmpresa)
        val url = view.findViewById<TextView>(R.id.urlEmpresa)
        val telefono = view.findViewById<TextView>(R.id.telefonoEmpresa)
        val email = view.findViewById<TextView>(R.id.emailEmpresa)
        val clasificacion = view.findViewById<TextView>(R.id.clasificacionEmpresa)

        // Asignar valores
        nombre.text = empresa?.nombre
        url.text = "URL: ${empresa?.URL}"
        telefono.text = "Teléfono: ${empresa?.Telefono}"
        email.text = "Email: ${empresa?.Email}"
        clasificacion.text = "Clasificación: ${empresa?.Clasificacion?.joinToString(", ")}"

        return view
    }
}
