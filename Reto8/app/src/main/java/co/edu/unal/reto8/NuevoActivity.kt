package co.edu.unal.reto8

import android.content.Context
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import co.edu.unal.reto8.Controladores.EmpresaBD
import co.edu.unal.reto8.Modelos.Empresa

class NuevoActivity : AppCompatActivity() {

    private lateinit var contexto: Context
    private lateinit var empresaBD: EmpresaBD
    private lateinit var nombre: EditText
    private lateinit var URL: EditText
    private lateinit var Telefono: EditText
    private lateinit var Email: EditText
    private lateinit var consultoria: CheckBox
    private lateinit var desarrollo : CheckBox
    private lateinit var fabrica : CheckBox
    private var Clasificacion: MutableList<String> = mutableListOf()

    private lateinit var guardarButton: Button
    private lateinit var cancelarButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_nuevo)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        init()
    }

    fun init(){
        contexto = applicationContext
        nombre = findViewById(R.id.nombreEmpresa)
        URL = findViewById(R.id.URLEmpresa)
        Telefono = findViewById(R.id.telefonoEmpresa)
        Email = findViewById(R.id.correoEmpresa)
        consultoria = findViewById(R.id.consultoriaCheckBox)
        desarrollo = findViewById(R.id.desarrolloCheckBox)
        fabrica = findViewById(R.id.fabricaCheckBox)

        guardarButton = findViewById(R.id.guardar)
        cancelarButton = findViewById(R.id.cancelar)

        guardarButton.setOnClickListener {
            setResult(RESULT_OK)
            guardar()
            finish()
        }

        cancelarButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        val i = intent
        val data = i.extras

        if (data != null) {
            nombre.setText(data.getString("Nombre")?: "")
            URL.setText(data.getString("URL")?: "")
            Telefono.setText(data.getString("Telefono")?: "")
            Email.setText(data.getString("Email")?: "")
        }

    }

    fun crearEmpresa():Empresa{
        var empresaNueva: Empresa
        var nombreEmpresaNueva: String = nombre.text.toString()
        var URLEmpresaNueva: String = URL.text.toString()
        var TelefonoEmpresaNueva: String = Telefono.text.toString()
        var EmailEmpresaNueva: String = Email.text.toString()

        if (consultoria.isChecked) Clasificacion.add("Consultoría")
        if (desarrollo.isChecked) Clasificacion.add("Desarrollo a la medida")
        if (fabrica.isChecked) Clasificacion.add("Fábrica de software")

        empresaNueva = Empresa(
            nombre = nombreEmpresaNueva,
            URL = URLEmpresaNueva,
            Telefono = TelefonoEmpresaNueva,
            Email = EmailEmpresaNueva,
            Clasificacion = Clasificacion
        )
        return empresaNueva
    }

    fun guardar(){
        empresaBD = EmpresaBD(contexto)
        var nuevaEmpresa: Empresa = crearEmpresa()
        empresaBD.insertEmpresa(nuevaEmpresa)
    }
}