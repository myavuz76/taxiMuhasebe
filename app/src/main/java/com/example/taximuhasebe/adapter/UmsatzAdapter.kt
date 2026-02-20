package com.example.taximuhasebe.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.taximuhasebe.R
import com.example.taximuhasebe.database.Umsatz
import java.util.Locale

class UmsatzAdapter(
    private val onEditClick: (Umsatz) -> Unit,
    private val onDeleteClick: (Umsatz) -> Unit
) : ListAdapter<Umsatz, UmsatzAdapter.UmsatzViewHolder>(UmsatzDiffCallback()) {

    private var totalItemCount: Int = 0

    fun setTotalItemCount(count: Int) {
        totalItemCount = count
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UmsatzViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.umsatz_item, parent, false)
        return UmsatzViewHolder(view, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: UmsatzViewHolder, position: Int) {
        val umsatz = getItem(position)
        holder.bind(umsatz, totalItemCount - position)
    }

    class UmsatzViewHolder(
        itemView: View,
        private val onEditClick: (Umsatz) -> Unit,
        private val onDeleteClick: (Umsatz) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val itemNumberTextView: TextView = itemView.findViewById(R.id.item_number_textview)
        private val umsatzAmountTextView: TextView = itemView.findViewById(R.id.umsatz_amount_textview)
        private val nettoAmountTextView: TextView = itemView.findViewById(R.id.netto_amount_textview)
        private val bahsisAmountTextView: TextView = itemView.findViewById(R.id.bahsis_amount_textview)
        private val faturaAmountTextView: TextView = itemView.findViewById(R.id.fatura_amount_textview)
        private val sourceTextView: TextView = itemView.findViewById(R.id.source_textview)
        private val paymentTypeTextView: TextView = itemView.findViewById(R.id.payment_type_textview)
        private val editButton: ImageButton = itemView.findViewById(R.id.edit_button)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.delete_button)
        private val container: View = itemView.findViewById(R.id.item_container)

        fun bind(umsatz: Umsatz, itemNumber: Int) {
            itemNumberTextView.text = itemNumber.toString()
            umsatzAmountTextView.text = String.format(Locale.GERMANY, "%.2f€", umsatz.umsatzAmount)
            nettoAmountTextView.text = String.format(Locale.GERMANY, "%.2f€", umsatz.nettoAmount)
            bahsisAmountTextView.text = String.format(Locale.GERMANY, "%.2f€", umsatz.bahsisAmount)
            faturaAmountTextView.text = String.format(Locale.GERMANY, "%.2f€", umsatz.faturaAmount)
            sourceTextView.text = umsatz.source.uppercase(Locale.GERMANY)
            paymentTypeTextView.text = umsatz.paymentType.uppercase(Locale.GERMANY)

            applyColors(umsatz)

            editButton.setOnClickListener { onEditClick(umsatz) }
            deleteButton.setOnClickListener { onDeleteClick(umsatz) }
        }

        private fun applyColors(umsatz: Umsatz) {
            val dynamicColor = getDynamicColor(umsatz)
            
            val sourceBackground = GradientDrawable()
            sourceBackground.setColor(dynamicColor)
            sourceBackground.cornerRadius = 8f
            sourceTextView.background = sourceBackground
            sourceTextView.setPadding(8, 4, 8, 4)

            val textColor = if (isColorLight(dynamicColor)) Color.BLACK else Color.WHITE
            sourceTextView.setTextColor(textColor)

            container.setBackgroundColor(Color.parseColor("#2C2C2C"))

            if (umsatz.source.lowercase() == "funk" && umsatz.paymentType.lowercase() != "bar") {
                paymentTypeTextView.text = "[${umsatz.paymentType.uppercase()}]"
            }
        }

        private fun isColorLight(color: Int): Boolean {
            val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
            return darkness < 0.5
        }

        private fun getDynamicColor(umsatz: Umsatz): Int {
            return when (umsatz.source.lowercase()) {
                "bolt" -> if (umsatz.paymentType.lowercase() == "bar") Color.parseColor("#A5D6A7") else Color.parseColor("#2E7D32")
                "uber" -> if (umsatz.paymentType.lowercase() == "bar") Color.parseColor("#9E9E9E") else Color.parseColor("#212121")
                "f-now" -> if (umsatz.paymentType.lowercase() == "bar") Color.parseColor("#EF9A9A") else Color.parseColor("#C62828")
                "funk" -> {
                    if (umsatz.faturaAmount > 0) Color.parseColor("#90CAF9") // bar faturalı -> açık mavi
                    else if(umsatz.paymentType.lowercase() != "bar") Color.parseColor("#1565C0") // karte/inkasso -> koyu mavi
                    else Color.WHITE // bar faturasız -> beyaz
                }
                "einst" -> {
                    if (umsatz.faturaAmount > 0) Color.parseColor("#CE93D8") // bar faturalı -> açık mor
                    else if(umsatz.paymentType.lowercase() == "karte") Color.parseColor("#6A1B9A") // karte -> koyu mor
                    else Color.WHITE // bar faturasız -> beyaz
                }
                else -> Color.LTGRAY
            }
        }
    }
}

class UmsatzDiffCallback : DiffUtil.ItemCallback<Umsatz>() {
    override fun areItemsTheSame(oldItem: Umsatz, newItem: Umsatz): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Umsatz, newItem: Umsatz): Boolean {
        return oldItem == newItem
    }
}
