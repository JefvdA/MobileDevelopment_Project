package edu.ap.WcApp

data class ToiletViewModel(val omschrijving: String, val addres: String,val rolstoel: String, val lat: Double, val lon: Double, val doelgroep: String, val luiertafel: String, var distance:Float=0f) {
}