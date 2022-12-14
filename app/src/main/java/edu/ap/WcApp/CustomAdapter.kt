package edu.ap.WcApp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CustomAdapter(private val mList: List<ToiletViewModel>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_design, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ItemsViewModel = mList[position]

        // sets the text to the textview from our itemHolder class
        if(ItemsViewModel.addres == "null "){
            holder.textView.text = ItemsViewModel.omschrijving
        }
        else{
            holder.textView.text = ItemsViewModel.addres
        }
        if(ItemsViewModel.distance < 1000){
            holder.afstandView.text =String.format("%.0f", ItemsViewModel.distance)+" m"
        }
        else{
            holder.afstandView.text = String.format("%.2f",ItemsViewModel.distance/1000)+" km"
        }
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val textView: TextView = itemView.findViewById(R.id.omschrijving)
        val afstandView: TextView = itemView.findViewById(R.id.afstand)
    }
}
