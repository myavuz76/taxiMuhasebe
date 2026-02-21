package com.example.taximuhasebe.adapter

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.taximuhasebe.R
import com.example.taximuhasebe.model.WeeklyReportRow
import java.util.Locale

class WeeklySummaryAdapter : ListAdapter<WeeklyReportRow, WeeklySummaryAdapter.WeeklyViewHolder>(WeeklyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeeklyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.weekly_summary_item, parent, false)
        return WeeklyViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeeklyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class WeeklyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayTextView: TextView = itemView.findViewById(R.id.dayTextView)
        private val bruttoTextView: TextView = itemView.findViewById(R.id.bruttoTextView)
        private val nettoTextView: TextView = itemView.findViewById(R.id.nettoTextView)
        private val bahsisTextView: TextView = itemView.findViewById(R.id.bahsisTextView)
        private val nakitTextView: TextView = itemView.findViewById(R.id.nakitTextView)
        private val faturaTextView: TextView = itemView.findViewById(R.id.faturaTextView)
        private val tmstrTextView: TextView = itemView.findViewById(R.id.tmstrTextView)
        private val tappTextView: TextView = itemView.findViewById(R.id.tappTextView)
        private val tkarteTextView: TextView = itemView.findViewById(R.id.tkarteTextView)

        fun bind(row: WeeklyReportRow) {
            dayTextView.text = row.dayName
            bruttoTextView.text = String.format(Locale.GERMANY, "%.2f€", row.brutto)
            nettoTextView.text = String.format(Locale.GERMANY, "%.2f€", row.netto)
            bahsisTextView.text = String.format(Locale.GERMANY, "%.1f€", row.bahsis)
            nakitTextView.text = String.format(Locale.GERMANY, "%.2f€", row.nakit)
            faturaTextView.text = String.format(Locale.GERMANY, "%.2f€", row.fatura)
            tmstrTextView.text = row.tmstr.toString()
            tappTextView.text = String.format(Locale.GERMANY, "%.2f€", row.tapp)
            tkarteTextView.text = String.format(Locale.GERMANY, "%.2f€", row.tkarte)

            if (row.isTotalRow) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.total_row_background))
                val yellowColor = ContextCompat.getColor(itemView.context, R.color.yellow)
                dayTextView.setTextColor(yellowColor)
                bruttoTextView.setTextColor(yellowColor)
                nettoTextView.setTextColor(yellowColor)
                bahsisTextView.setTextColor(yellowColor)
                nakitTextView.setTextColor(yellowColor)
                faturaTextView.setTextColor(yellowColor)
                tmstrTextView.setTextColor(yellowColor)
                tappTextView.setTextColor(yellowColor)
                tkarteTextView.setTextColor(yellowColor)

                dayTextView.setTypeface(null, Typeface.BOLD)
                bruttoTextView.setTypeface(null, Typeface.BOLD)
                nettoTextView.setTypeface(null, Typeface.BOLD)
                bahsisTextView.setTypeface(null, Typeface.BOLD)
                nakitTextView.setTypeface(null, Typeface.BOLD)
                faturaTextView.setTypeface(null, Typeface.BOLD)
                tmstrTextView.setTypeface(null, Typeface.BOLD)
                tappTextView.setTypeface(null, Typeface.BOLD)
                tkarteTextView.setTypeface(null, Typeface.BOLD)
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.default_item_background))
                val whiteColor = ContextCompat.getColor(itemView.context, android.R.color.white)
                dayTextView.setTextColor(whiteColor)
                bruttoTextView.setTextColor(whiteColor)
                nettoTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.green))
                bahsisTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.yellow))
                nakitTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.green))
                faturaTextView.setTextColor(ContextCompat.getColor(itemView.context, R.color.blue))
                tmstrTextView.setTextColor(whiteColor)
                tappTextView.setTextColor(whiteColor)
                tkarteTextView.setTextColor(whiteColor)

                dayTextView.setTypeface(null, Typeface.NORMAL)
                bruttoTextView.setTypeface(null, Typeface.NORMAL)
                nettoTextView.setTypeface(null, Typeface.NORMAL)
                bahsisTextView.setTypeface(null, Typeface.NORMAL)
                nakitTextView.setTypeface(null, Typeface.NORMAL)
                faturaTextView.setTypeface(null, Typeface.NORMAL)
                tmstrTextView.setTypeface(null, Typeface.NORMAL)
                tappTextView.setTypeface(null, Typeface.NORMAL)
                tkarteTextView.setTypeface(null, Typeface.NORMAL)
            }
        }
    }
}

class WeeklyDiffCallback : DiffUtil.ItemCallback<WeeklyReportRow>() {
    override fun areItemsTheSame(oldItem: WeeklyReportRow, newItem: WeeklyReportRow): Boolean {
        return oldItem.dayName == newItem.dayName
    }

    override fun areContentsTheSame(oldItem: WeeklyReportRow, newItem: WeeklyReportRow): Boolean {
        return oldItem == newItem
    }
}
