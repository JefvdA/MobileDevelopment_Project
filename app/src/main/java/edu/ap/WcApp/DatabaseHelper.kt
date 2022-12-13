package edu.ap.WcApp

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import androidx.lifecycle.ViewModel
import com.beust.klaxon.JsonObject
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {


    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(DELETE_TABLE_TOILETS)
        db.execSQL(CREATE_TABLE_TOILETS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(DELETE_TABLE_TOILETS)
        onCreate(db)
    }

    // loop through all rows and adding to Students list
    @SuppressLint("Range")
    fun allToilets(): ArrayList<ToiletViewModel> {
        val toiletsArrayList = ArrayList<ToiletViewModel>()
        var name: String
        val db = this.readableDatabase
        val cursor = db.rawQuery(SELECT_TOILETS, null)
        with(cursor) {
            while (moveToNext()) {
                val street = cursor.getString(cursor.getColumnIndex(STRAAT))
                val huisnr = cursor.getString(cursor.getColumnIndex(HUISNUMMER))
                val omschrijving = cursor.getString(cursor.getColumnIndex(OMSCHRIJVING))
                val lat = cursor.getString(cursor.getColumnIndex(LAT))
                val lon = cursor.getString(cursor.getColumnIndex(LONG))
                val doelgroep = cursor.getString(cursor.getColumnIndex(DOELGROEP))
                val luiertafel = cursor.getString(cursor.getColumnIndex(LUIERTAFEL))
                val rolstoel = cursor.getString(cursor.getColumnIndex(INTEGRAAL_TOEGANKELIJK))
                toiletsArrayList.add(ToiletViewModel(omschrijving,street+" "+huisnr,rolstoel, lat.toDouble(), lon.toDouble(), doelgroep, luiertafel))
            }
        }
        cursor.close()
        Log.d("database", toiletsArrayList.toString())
        return toiletsArrayList
    }

    fun addToilet(firstName: String, lastName: String): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(STRAAT, firstName)
        values.put(HUISNUMMER, lastName)

        return db.insert(TABLE_TOILETS, null, values)
    }

    fun getData(){
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://geodata.antwerpen.be/arcgissql/rest/services/P_Portal/portal_publiek1/MapServer/8/query?outFields=*&where=1%3D1&f=geojson")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("OUR_APP", e.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    var str_response = response.body!!.string()
                    //creating json object
                    val json_contact: JSONObject = JSONObject(str_response)
                    //creating json array
                    var jsonarray_info: JSONArray = json_contact.getJSONArray("features")
                    Log.d("json",jsonarray_info.toString())
                    addJson(jsonarray_info)
                }
            }
        })
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
            values.put(OMSCHRIJVING, prop.getString("OMSCHRIJVING").trim())
            values.put(HUISNUMMER, prop.getString("HUISNUMMER").trim())
            values.put(STRAAT, prop.getString("STRAAT").trim())
            values.put(LAT, coord.getString(1))
            values.put(LONG, coord.getString(0))
            values.put(DOELGROEP, prop.getString("DOELGROEP").trim())
            values.put(LUIERTAFEL, prop.getString("LUIERTAFEL").trim())
            values.put(INTEGRAAL_TOEGANKELIJK, prop.getString("INTEGRAAL_TOEGANKELIJK").trim())
            db.insert(TABLE_TOILETS, null, values)
        }
        db.close()
    }

    companion object {

        var DATABASE_NAME = "toilets"
        private val DATABASE_VERSION = 5
        private val TABLE_TOILETS = "toilets"
        private val KEY_ID = "id"
        private val LAT = "LAT"
        private val LONG = "LONG"
        private val CATEGGORIE = "categorie"
        private val PUBLICEREN = "publiceren"
        private val PRIORITAIR = "prioritair"
        private val OMSCHRIJVING = "OMSCHRIJVING"
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
        private val INTEGRAAL_TOEGANKELIJK ="INTEGRAAL_TOEGANKELIJK"

        private val CREATE_TABLE_TOILETS = ("CREATE TABLE "+ TABLE_TOILETS +
                "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + STRAAT + " TEXT, "
                + HUISNUMMER + " TEXT,"
                + OMSCHRIJVING + " TEXT,"
                + LUIERTAFEL + " TEXT,"
                + LAT + " TEXT,"
                + LONG + " TEXT,"
                + INTEGRAAL_TOEGANKELIJK + " TEXT,"
                + DOELGROEP + " TEXT );")

        private val DELETE_TABLE_TOILETS = "DROP TABLE IF EXISTS $TABLE_TOILETS"

        private val SELECT_TOILETS = "SELECT * FROM $TABLE_TOILETS"
    }
}