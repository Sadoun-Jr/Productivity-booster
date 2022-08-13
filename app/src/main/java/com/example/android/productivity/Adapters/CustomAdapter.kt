package com.example.android.productivity.Adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android.productivity.R
import com.example.android.productivity.database.ProductiveDay
import com.example.android.productivity.productivitytracker.AddGoal
import java.text.DateFormat
import java.util.*

class CustomAdapter(
    private val mList: List<ProductiveDay>
) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    val LOG_TAG = "Holder"
    val MSG_UPDATE = "update whole day"

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

        val dayObject = mList[position]

        holder.ratingImage.setImageResource(setProperImageForRating(dayObject.overallDayRating))

        // sets the text to the textview from our itemHolder class
        holder.descTxt.text = dayObject.desc
        holder.goalTxt.text = dayObject.firstGoal

        //format date clearly
        val calender = Calendar.getInstance()
        calender[Calendar.YEAR] = cutDateFromDbToYrs(dayObject.day).toInt()
        calender[Calendar.DAY_OF_YEAR] = cutDateFromDbToDays(dayObject.day).toInt()
        holder.dateTxt.text = DateFormat.getDateInstance().format(calender.timeInMillis)

        //set on click listener
        holder.itemView.setOnClickListener {
            rateClickedDay(dayObject.dayId, holder.itemView)
        }
    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val ratingImage: ImageView = itemView.findViewById(R.id.imageview)
        val dateTxt: TextView = itemView.findViewById(R.id.dayTxt)
        val goalTxt : TextView = itemView.findViewById(R.id.goalTxt)
        val descTxt : TextView = itemView.findViewById(R.id.descTxt)
        val wholeCard : LinearLayout = itemView.findViewById(R.id.wholeCardLinear)
    }

    private fun setProperImageForRating(rating: Int) : Int{
        var ratingReturned = 0
        when(rating){
            -1 -> ratingReturned = R.drawable.hourglasss         //not rated yet
            1 -> ratingReturned = R.drawable.happy_selected //happy
            2 -> ratingReturned = R.drawable.meh_selected   //meh
            3 -> ratingReturned = R.drawable.sad_selected   //sad
        }
        return ratingReturned
    }

    private fun rateClickedDay(dayId: Long, view:View) {
        val intent = Intent(view.context, AddGoal::class.java)
        //this extra will let us display update UI and functionality instead of add
        intent.putExtra("Add", MSG_UPDATE)
        //notification message will be day id here
        intent.putExtra("NotificationMessage", dayId.toString())
        view.context.startActivity(intent)

    }

    private fun cutDateFromDbToYrs(fullDateFromDb: String): String {
        return fullDateFromDb.substring(
            fullDateFromDb.lastIndexOf(" ") + 1
        ).trimEnd().trimStart()
    }

    private fun cutDateFromDbToDays(day: String): String {
        val i = day.indexOf(' ')
        return day.substring(0, i)
    }

}