package edu.ap.WcApp

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.beust.klaxon.JsonObject
import org.json.JSONArray

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("Drop table toilet_database.toilets")
        db.execSQL(CREATE_TABLE_TOILETS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(DELETE_TABLE_TOILETS)
        onCreate(db)
    }

    // loop through all rows and adding to Students list
    @SuppressLint("Range")
    fun allToilets(): ArrayList<String> {
        val studentsArrayList = ArrayList<String>()
        var name: String
        val db = this.readableDatabase
        val cursor = db.rawQuery(SELECT_TOILETS, null)

        //val projection = arrayOf(KEY_ID, STRAAT, HUISNUMMER)
        //val sortOrder = "${STRAAT} ASC"
        //
        //val cursor = db.query(
        //    TABLE_TOILETS,
        //    projection,
        //    null,
        //    null,
        //    null,
        //    null,
        //    sortOrder
        //)

        with(cursor) {
            while (moveToNext()) {
                val street = cursor.getString(cursor.getColumnIndex(STRAAT))
                val huisnr = cursor.getString(cursor.getColumnIndex(HUISNUMMER))
                //val lat = cursor.getString(cursor.getColumnIndex(LAT))
                //val lon = cursor.getString(cursor.getColumnIndex(LONG))
                studentsArrayList.add(street + " " + huisnr+" ")
            }
        }
        cursor.close()

        return studentsArrayList
    }

    fun addToilet(firstName: String, lastName: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(STRAAT, firstName)
        values.put(HUISNUMMER, lastName)

        return db.insert(TABLE_TOILETS, null, values)
    }
    fun addJson(json: JSONArray) {
        val db = this.writableDatabase
        db.execSQL("delete from "+ TABLE_TOILETS);
        val values = ContentValues()
        var i:Int = 0
        var size:Int = json.length()
        for (i in 0 until size) {
            var jsonobject = json.getJSONObject(i)
            var prop = jsonobject.getJSONObject("properties")
            var geometry = jsonobject.getJSONObject("geometry")
            var coord = geometry.getJSONArray("coordinates")
            values.put(HUISNUMMER, prop.getString("HUISNUMMER"))
            values.put(STRAAT, prop.getString("STRAAT"))
            values.put(LAT, coord.getString(0))
            values.put(LONG, coord.getString(1))
            values.put(DOELGROEP, prop.getString("DOELGROEP"))
            values.put(LUIERTAFEL, prop.getString("LUIERTAFEL"))
            db.insert(TABLE_TOILETS, null, values)
        }
        db.close()
    }

    companion object {

        var DATABASE_NAME = "toilet_database"
        private val DATABASE_VERSION = 4
        private val TABLE_TOILETS = "toilets"
        private val KEY_ID = "id"
        private val LAT = "lat"
        private val LONG = "long"
        private val CATEGGORIE = "categorie"
        private val PUBLICEREN = "publiceren"
        private val PRIORITAIR = "prioritair"
        private val OMSCHRIJVING = "omschrijving"
        private val EXTRA_INFO_PUBLIEK ="EXTRA_INFO_PUBLIEK"
        private val VRIJSTAAND ="VRIJSTAAND"
        private val TYPE = "TYPE"
        private val STADSEIGENDOM ="STADSEIGENDOM"
        private val BETALEND = "BETALEND"
        private val STRAAT = "STRAAT"
        private val HUISNUMMER = "HUISNUMMER"
        private val POSTCODE = "POSTCODE"
        private val DISTRICT = "DISTRICT"
        private val DOELGROEP = "DOELGROEP"
        private val LUIERTAFEL = "LUIERTAFEL"
        private val OPENINGSUREN_OPM = "OPENINGSUREN_OPM"
        private val CONTACTPERSOON = "CONTACTPERSOON"
        private val CONTACTGEGEVENS ="CONTACTGEGEVENS"

        private val CREATE_TABLE_TOILETS = ("CREATE TABLE "+ TABLE_TOILETS +
                "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + STRAAT + " TEXT, "
                + HUISNUMMER + " TEXT,"
                + LUIERTAFEL + " TEXT,"
                + LAT + " TEXT,"
                + LONG + " TEXT,"
                + DOELGROEP + " TEXT );")

        private val DELETE_TABLE_TOILETS = "DROP TABLE IF EXISTS $TABLE_TOILETS"

        private val SELECT_TOILETS = "SELECT * FROM $TABLE_TOILETS"
    }
}