package co.edu.unal.reto8.Controladores

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import co.edu.unal.reto8.Modelos.Empresa

class EmpresaBD(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "Empresas.db"
        private const val DATABASE_VERSION = 1

        // Nombre de la tabla y columnas
        private const val TABLE_EMPRESAS = "Empresas"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NOMBRE = "nombre"
        private const val COLUMN_URL = "URL"
        private const val COLUMN_TELEFONO = "Telefono"
        private const val COLUMN_EMAIL = "Email"
        private const val COLUMN_CLASIFICACION = "Clasificacion"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Crear tabla
        val createTableQuery = """
            CREATE TABLE $TABLE_EMPRESAS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NOMBRE TEXT NOT NULL,
                $COLUMN_URL TEXT NOT NULL,
                $COLUMN_TELEFONO TEXT NOT NULL,
                $COLUMN_EMAIL TEXT NOT NULL,
                $COLUMN_CLASIFICACION TEXT NOT NULL
            )
        """.trimIndent()


        Log.d("DB_INSERT", "empezado")
        db?.execSQL(createTableQuery)

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Actualizar esquema
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_EMPRESAS")
        onCreate(db)
    }

    // Insertar empresa
    fun insertEmpresa(empresa: Empresa): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE, empresa.nombre)
            put(COLUMN_URL, empresa.URL)
            put(COLUMN_TELEFONO, empresa.Telefono)
            put(COLUMN_EMAIL, empresa.Email)
            put(COLUMN_CLASIFICACION, empresa.getClasificacionAsJson())
        }

        val result = db.insert(TABLE_EMPRESAS, null, values)
        Log.d("DB_INSERT", "Empresa insertada: ${empresa.nombre}, ID: $result")
        return result
    }

    // Obtener todas las empresas
    fun getAllEmpresas(): List<Empresa> {
        val empresas = mutableListOf<Empresa>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_EMPRESAS, null, null, null, null, null, null
        )

        if (cursor.moveToFirst()) {
            do {
                val empresa = Empresa(
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE)),
                    URL = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_URL)),
                    Telefono = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TELEFONO)),
                    Email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL))
                )
                empresa.setClasificacionFromJson(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASIFICACION))
                )
                empresas.add(empresa)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return empresas
    }

    // Actualizar empresa
    fun updateEmpresa(empresa: Empresa): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOMBRE, empresa.nombre)
            put(COLUMN_URL, empresa.URL)
            put(COLUMN_TELEFONO, empresa.Telefono)
            put(COLUMN_EMAIL, empresa.Email)
            put(COLUMN_CLASIFICACION, empresa.getClasificacionAsJson())
        }
        return db.update(TABLE_EMPRESAS, values, "$COLUMN_NOMBRE = ?", arrayOf(empresa.nombre))
    }

    // Eliminar empresa
    fun deleteEmpresa(nombre: String): Int {
        val db = writableDatabase
        return db.delete(TABLE_EMPRESAS, "$COLUMN_NOMBRE = ?", arrayOf(nombre))
    }

    // Obtener empresa por nombre
    fun getEmpresaByName(nombre: String): Empresa? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_EMPRESAS,
            null,
            "$COLUMN_NOMBRE = ?",
            arrayOf(nombre),
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            val empresa = Empresa(
                nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE)),
                URL = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_URL)),
                Telefono = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TELEFONO)),
                Email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL))
            )
            empresa.setClasificacionFromJson(
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASIFICACION))
            )
            cursor.close()
            return empresa
        }
        cursor.close()
        return null
    }

    // Obtener lista de empresas por clasificación
    fun getEmpresasByClasificacion(clasificacion: String): List<Empresa> {
        val empresas = mutableListOf<Empresa>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_EMPRESAS,
            null,
            "$COLUMN_CLASIFICACION = ?",
            arrayOf(clasificacion),
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            do {
                val empresa = Empresa(
                    nombre = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOMBRE)),
                    URL = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_URL)),
                    Telefono = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TELEFONO)),
                    Email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL))
                )
                empresa.setClasificacionFromJson(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASIFICACION))
                )
                empresas.add(empresa)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return empresas
    }

}

