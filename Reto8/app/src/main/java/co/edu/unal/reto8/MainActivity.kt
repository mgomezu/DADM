package co.edu.unal.reto8

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.navigation.ui.AppBarConfiguration
import co.edu.unal.reto8.Controladores.EmpresaBD
import co.edu.unal.reto8.Modelos.Empresa
import co.edu.unal.reto8.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var contexto: Context
    private lateinit var listView: ListView
    private lateinit var empresaBD: EmpresaBD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.fab.setOnClickListener { view ->
            // Crear un Intent para iniciar el otro Activity
            val intent = Intent(this, NuevoActivity::class.java)
            startActivityForResult(intent,REQUEST_CODE_ACTUALIZAR)
        }

        init()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun init(){
        contexto = applicationContext
        empresaBD = EmpresaBD(contexto)  // Crear la instancia de EmpresaBD
        listView = findViewById(R.id.lista_empresas)

        val searchField = findViewById<EditText>(R.id.search_field)
        val filterSpinner = findViewById<Spinner>(R.id.filter_spinner)

        configurarFiltro(filterSpinner)
        configurarBusqueda(searchField)

        crearLista()
    }

    fun crearLista(){
        val listaEmpresas: List<Empresa> = empresaBD.getAllEmpresas()  // Obtener empresas de la base de datos

        val adapter = EmpresaAdapter(this, listaEmpresas)
        listView.adapter = adapter


        // Manejar clics en los elementos de la lista
        listView.setOnItemClickListener { parent, view, position, id ->
            // Obtener el nombre de la empresa seleccionada
            val empresaSeleccionada = listaEmpresas[position]

            val intent = Intent(this, ActualizarActivity::class.java)
            intent.putExtra("Nombre", empresaSeleccionada.nombre)
            intent.putExtra("URL", empresaSeleccionada.URL)
            intent.putExtra("Telefono", empresaSeleccionada.Telefono)
            intent.putExtra("Email", empresaSeleccionada.Email)
            intent.putExtra("Clasificacion", empresaSeleccionada.getClasificacionAsJson())

            // Iniciar el nuevo Activity
            //startActivity(intent)
            startActivityForResult(intent,REQUEST_CODE_ACTUALIZAR)

        }
    }

    companion object {
        private const val REQUEST_CODE_ACTUALIZAR = 1
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_ACTUALIZAR && resultCode == RESULT_OK) {
            // Recargar la lista de empresas
            crearLista()
        }
    }

    private fun configurarBusqueda(searchField: EditText) {
        searchField.addTextChangedListener { text ->
            filtrarPorNombre(text.toString())
        }
    }

    private fun configurarFiltro(filterSpinner: Spinner) {
        val clasificaciones = arrayOf("Todas", "Consultoría", "Desarrollo a la medida", "Fábrica de software")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, clasificaciones)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterSpinner.adapter = adapter

        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val seleccion = clasificaciones[position]
                if (seleccion == "Todas") {
                    crearLista() // Mostrar todas las empresas
                } else {
                    filtrarPorClasificacion(seleccion)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun filtrarPorNombre(query: String) {
        val listaEmpresasFiltradas = empresaBD.getAllEmpresas().filter { empresa ->
            empresa.nombre.contains(query, ignoreCase = true)
        }

        val adapter = EmpresaAdapter(this, listaEmpresasFiltradas)
        listView.adapter = adapter
    }

    private fun filtrarPorClasificacion(clasificacion: String) {
        val listaEmpresasFiltradas = empresaBD.getAllEmpresas().filter { empresa ->
            empresa.Clasificacion.contains(clasificacion)
        }

        val adapter = EmpresaAdapter(this, listaEmpresasFiltradas)
        listView.adapter = adapter
    }


}