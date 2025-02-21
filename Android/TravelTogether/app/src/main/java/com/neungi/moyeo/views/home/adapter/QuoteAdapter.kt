package com.neungi.moyeo.views.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.neungi.moyeo.R
import com.neungi.moyeo.databinding.ItemQuoteCardBinding
import com.neungi.moyeo.views.home.viewmodel.Quote

class QuoteAdapter : RecyclerView.Adapter<QuoteAdapter.QuoteViewHolder>() {
    private val quotes = mutableListOf<Quote>()

    fun setQuotes(newQuotes: List<Quote>) {
        quotes.clear()
        quotes.addAll(newQuotes)
        notifyDataSetChanged()
    }

    inner class QuoteViewHolder(private val binding: ItemQuoteCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(quote: Quote) {
            binding.apply {
                textQuote.text = quote.text
                textAuthor.text = buildString {
                    quote.source?.let { append("<$it>") }
                    quote.source?.let { append(", ") }
                    append(quote.author)
                }
                imageBackground.load(R.drawable.image_camping)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuoteViewHolder {
        return QuoteViewHolder(
            ItemQuoteCardBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: QuoteViewHolder, position: Int) {
        holder.bind(quotes[position % quotes.size])
    }

    override fun getItemCount() = Int.MAX_VALUE
}
