package com.openclassrooms.realestatemanager.view

import android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
import com.openclassrooms.realestatemanager.R
import androidx.core.app.NotificationCompat.getCategory
import android.content.ClipData.Item
import android.content.Context
import android.view.View
import butterknife.ButterKnife
import android.widget.ImageButton
import butterknife.BindView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.openclassrooms.realestatemanager.models.Property
import java.lang.ref.WeakReference
import kotlinx.android.synthetic.main.fragment_main_item.*
import android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat.getCategory
import com.bumptech.glide.Glide
import com.openclassrooms.realestatemanager.models.Picture
import kotlinx.android.synthetic.main.fragment_main_item.view.*
import java.text.DecimalFormat
import androidx.lifecycle.ViewModelProviders
import com.openclassrooms.realestatemanager.injection.Injection
import com.openclassrooms.realestatemanager.injection.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_display_property.*

class MainFragmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val view: View = view
    val decimalFormat = DecimalFormat("#,###,###")

    fun updateWithProperty(property: Property) {
        view.main_fragment_info.text = property.type + " - " + property.area + " m²" + " - " + property.nbRooms + " rooms"
        view.main_fragment_city.text = property.city + " (" + property.zipCode + ")"
        view.main_fragment_price.text = decimalFormat.format(property.price) + " $"

        if (property.picture != "null"){
            if (view.main_fragment_item_cardView == null) view.fragment_main_item_view.addView(view.main_fragment_item_cardView)
            Glide.with(itemView).load(property.picture).into(view.main_fragment_item_pic)
        } else {
            try {
                (view.main_fragment_item_cardView.parent as ViewGroup).removeView(view.main_fragment_item_cardView)
            }catch (e: Exception){}
        }
    }
}