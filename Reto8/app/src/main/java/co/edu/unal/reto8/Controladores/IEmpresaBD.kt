package co.edu.unal.reto8.Controladores

import co.edu.unal.reto8.Modelos.Empresa


interface IEmpresaBD {

    fun nombre(nombre:String): Empresa

    fun lista(): MutableList<String>

    fun listaClasificacion(clasificacion:String): MutableList<String>

    fun create(empresa: Empresa)

    fun update(nombre: String, empresa: Empresa)

    fun delete(nombre: String)
}