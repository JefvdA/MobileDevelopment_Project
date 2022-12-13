package edu.ap.WcApp

import android.app.Application
import android.content.ClipData.Item
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class SharedViewModel(application: Application) : AndroidViewModel(application) {
    private val selected = MutableLiveData<List<ToiletViewModel>>()
    val databaseHelper:DatabaseHelper = DatabaseHelper(application.applicationContext)

    fun select(item: List<ToiletViewModel>) {
        selected.value = item
    }

    fun getSelected(): LiveData<List<ToiletViewModel>> {
        return selected
    }

    fun getDataFromDb(){
        var output = databaseHelper.allToilets()
        if(man){
            output = output.filter { it.doelgroep.contains("man") }  as ArrayList<ToiletViewModel>
        }
        if(vrouw){
            output = output.filter { it.doelgroep.contains("vrouw") } as ArrayList<ToiletViewModel>
        }
        if(luiertafel){
            output = output.filter{ it.luiertafel.contains(("ja"))} as ArrayList<ToiletViewModel>
        }
        if(rolstoel){
            output = output.filter{ it.rolstoel.contains(("ja"))} as ArrayList<ToiletViewModel>
        }
        select(output)
    }
    companion object {
        var man:Boolean=false
        var vrouw:Boolean=false
        var rolstoel:Boolean=false
        var luiertafel:Boolean=false
    }
}