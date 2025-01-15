package co.edu.unal.reto8

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import co.edu.unal.reto8.Controladores.EmpresaBD
import co.edu.unal.reto8.Modelos.Empresa
import org.json.JSONArray

class ActualizarActivity : AppCompatActivity() {

    private lateinit var nombre: EditText
    private lateinit var url: EditText
    private lateinit var telefono: EditText
    private lateinit var email: EditText
    private lateinit var consultoria: CheckBox
    private lateinit var desarrollo: CheckBox
    private lateinit var fabrica: CheckBox
    private lateinit var actualizar: Button
    private lateinit var eliminar: Button
    private lateinit var cancelar: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actualizar)

        // Inicializar vistas
        nombre = findViewById(R.id.nombreEmpresa)
        url = findViewById(R.id.urlEmpresa)
        telefono = findViewById(R.id.telefonoEmpresa)
        email = findViewById(R.id.correoEmpresa)
        consultoria = findViewById(R.id.consultoria)
        desarrollo = findViewById(R.id.desarrollo)
        fabrica = findViewById(R.id.fabrica)
        actualizar = findViewById(R.id.actualizar)
        eliminar = findViewById(R.id.eliminarButton)
        cancelar = findViewById(R.id.cancelar)

        // Obtener datos enviados desde la actividad anterior
        val intent = intent
        val nombreRecibido = intent.getStringExtra("Nombre") ?: ""
        val urlRecibido = intent.getStringExtra("URL") ?: ""
        val telefonoRecibido = intent.getStringExtra("Telefono") ?: ""
        val emailRecibido = intent.getStringExtra("Email") ?: ""
        val clasificacionJson = intent.getStringExtra("Clasificacion") ?: "[]"

        // Llenar campos con los datos recibidos
        nombre.setText(nombreRecibido)
        url.setText(urlRecibido)
        telefono.setText(telefonoRecibido)
        email.setText(emailRecibido)

        // Parsear la clasificación (JSON) y marcar los CheckBoxes correspondientes
        val clasificacionList = JSONArray(clasificacionJson)
        for (i in 0 until clasificacionList.length()) {
            when (clasificacionList.getString(i)) {
                "Consultoría" -> consultoria.isChecked = true
                "Desarrollo a la medida" -> desarrollo.isChecked = true
                "Fábrica de software" -> fabrica.isChecked = true
            }
        }

        actualizar.setOnClickListener {
            // Crear una nueva instancia de Empresa con los datos actualizados
            val empresaActualizada = Empresa(
                nombre = nombre.text.toString(),
                URL = url.text.toString(),
                Telefono = telefono.text.toString(),
                Email = email.text.toString()
            )
            val nuevaClasificacion = mutableListOf<String>()
            if (consultoria.isChecked) nuevaClasificacion.add("Consultoría")
            if (desarrollo.isChecked) nuevaClasificacion.add("Desarrollo a la medida")
            if (fabrica.isChecked) nuevaClasificacion.add("Fábrica de software")
            empresaActualizada.setClasificacionFromJson(JSONArray(nuevaClasificacion).toString())

            // Actualizar la empresa en la base de datos
            val empresaBD = EmpresaBD(this)
            empresaBD.updateEmpresa(empresaActualizada)

            // Devolver el resultado y cerrar la actividad
            setResult(RESULT_OK)
            finish()
        }

        eliminar.setOnClickListener {
            val empresaAEliminar = nombre.text.toString()

            // Crear un AlertDialog para confirmar la eliminación
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirmación")
            builder.setMessage("¿Estás seguro de que deseas eliminar la empresa \"$empresaAEliminar\"?")

            // Botón para confirmar la eliminación
            builder.setPositiveButton("Sí") { dialog, which ->
                // Eliminar la empresa en la base de datos
                val empresaBD = EmpresaBD(this)
                empresaBD.deleteEmpresa(empresaAEliminar)

                // Devolver el resultado y cerrar la actividad
                setResult(RESULT_OK)
                finish()
            }

            // Botón para cancelar la operación
            builder.setNegativeButton("Cancelar") { dialog, which ->
                // Cerrar el diálogo sin hacer nada
                dialog.dismiss()
            }

            // Mostrar el diálogo
            builder.create().show()
        }


        cancelar.setOnClickListener {
            // Cancelar y cerrar la actividad
            setResult(RESULT_CANCELED)
            finish()
        }
    }
}
