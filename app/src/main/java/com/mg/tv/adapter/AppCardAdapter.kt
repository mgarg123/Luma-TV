package com.mg.tv.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.mg.tv.R
import com.mg.tv.model.AppInfo

class AppCardAdapter(
    private val context: Context,
    private val onAppFocused: (AppInfo) -> Unit = {},
    private val onFavoriteToggled: (AppInfo) -> Unit = {}
) : RecyclerView.Adapter<AppCardAdapter.CardViewHolder>() {

    private val apps = ArrayList<AppInfo>()

    fun submitList(newList: List<AppInfo>) {
        apps.clear()
        apps.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_card, parent, false)
        return CardViewHolder(view)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(apps[position])
    }

    override fun getItemCount(): Int = apps.size

    inner class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val cardContainer: FrameLayout = itemView.findViewById(R.id.cardContainer)
        private val ivAppBanner: ImageView = itemView.findViewById(R.id.ivAppBanner)
        private val ivAppIcon: ImageView = itemView.findViewById(R.id.ivAppIcon)
        private val viewFocusBorder: View = itemView.findViewById(R.id.viewFocusBorder)

        init {
            cardContainer.outlineProvider = ViewOutlineProvider.BACKGROUND
            cardContainer.clipToOutline = true
        }

        fun bind(app: AppInfo) {
            if (app.banner != null) {
                ivAppBanner.visibility = View.VISIBLE
                ivAppBanner.setImageDrawable(app.banner)
                ivAppIcon.visibility = View.GONE

                // Netflix and YouTube banners have transparent backgrounds designed for white cards
                if (app.packageName == "com.netflix.ninja" ||
                    app.packageName == "com.google.android.youtube.tv" ||
                    app.packageName == "com.google.android.youtube.tvmusic"
                ) {
                    cardContainer.setBackgroundResource(R.drawable.bg_card_white)
                } else {
                    cardContainer.setBackgroundResource(R.drawable.bg_card_dark)
                }
            } else {
                ivAppBanner.visibility = View.GONE
                ivAppIcon.visibility = View.VISIBLE
                ivAppIcon.setImageDrawable(app.icon)
                cardContainer.setBackgroundResource(R.drawable.bg_card_dark)
            }

            // Remote D-pad focus state: 1.06x scale + glowing white outline + elevation
            cardContainer.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    cardContainer.animate()
                        .scaleX(1.06f)
                        .scaleY(1.06f)
                        .setDuration(140)
                        .start()
                    cardContainer.elevation = 12f
                    viewFocusBorder.visibility = View.VISIBLE
                    onAppFocused(app)
                } else {
                    cardContainer.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(120)
                        .start()
                    cardContainer.elevation = 0f
                    viewFocusBorder.visibility = View.GONE
                }
            }

            // Launch app on D-pad OK
            cardContainer.setOnClickListener {
                launchApp(app)
            }

            // Toggle favorite on long click
            cardContainer.setOnLongClickListener {
                onFavoriteToggled(app)
                true
            }
        }

        private fun launchApp(app: AppInfo) {
            try {
                val launchIntent = if (app.isLeanback) {
                    context.packageManager.getLeanbackLaunchIntentForPackage(app.packageName)
                        ?: context.packageManager.getLaunchIntentForPackage(app.packageName)
                } else {
                    context.packageManager.getLaunchIntentForPackage(app.packageName)
                }

                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                } else {
                    Toast.makeText(context, "Cannot launch ${app.label}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error launching ${app.label}: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
