package edu.ap.WcApp

import android.util.Log
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint

class MyMapEventsReceiver(private val longPressCallback: ((p: GeoPoint) -> Unit)): MapEventsReceiver {

    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        return true;
    }

    override fun longPressHelper(p: GeoPoint?): Boolean {
        longPressCallback(p!!)
        return true
    }
}