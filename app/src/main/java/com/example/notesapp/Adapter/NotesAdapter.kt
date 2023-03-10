package com.example.notesapp.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp.Models.Note
import com.example.notesapp.R
import java.util.*


import kotlin.collections.ArrayList


class NotesAdapter(private val context:Context,val listener:NotesClickListener) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    private val NotesList = ArrayList<Note>()
    private val fullList =ArrayList<Note>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(
            LayoutInflater.from(context).inflate(R.layout.list_item,parent,false)
        )
    }


//    @RequiresApi(Build.VERSION_CODES.M)
//    @SuppressLint("NewApi")
override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
    val currentNote = NotesList[position]

    holder.title.text = currentNote.title
    holder.title.isSelected = true

    holder.Note_tv.text = currentNote.note
    holder.date.text = currentNote.date
    holder.date.isSelected = true

    val color = randomColor(currentNote.id ?: -1)
    holder.notes_layout.setCardBackgroundColor(color)

    holder.notes_layout.setOnClickListener {
        listener.onItemClicked(NotesList[holder.adapterPosition])
    }
    holder.notes_layout.setOnLongClickListener {
        listener.onLongItemClicked(NotesList[holder.adapterPosition], holder.notes_layout)
        true
    }
}

    override fun getItemCount(): Int {
       return NotesList.size
    }

    fun updateList(newList:List<Note>)
    {
        fullList.clear()
        fullList.addAll(newList)
        NotesList.clear()
        NotesList.addAll(fullList)
        notifyDataSetChanged()
    }
    fun filterList(search :String)
    {
        NotesList.clear()
        for(item in fullList)
        {
            if(item.title?.lowercase()?.contains(search.lowercase())==true||
                item.note?.lowercase()?.contains(search.lowercase())==true){
                NotesList.add(item)
            }
        }
        notifyDataSetChanged()
    }
    fun randomColor(id: Int): Int {
        // Eşleştirme tablosunu SharedPreferences kullanarak kaydet
        val sharedPref = context.getSharedPreferences("ColorTable", Context.MODE_PRIVATE)
        var color = sharedPref.getInt(id.toString(), -1)
        if (color == -1) {
            // Eşleştirme yok, yeni bir renk ata

            val colors = context.resources.getIntArray(R.array.note_colors)
            color = colors[Random().nextInt(colors.size)]
            // Eşleştirme tablosuna kaydet
            sharedPref.edit().putInt(id.toString(), color).apply()
        }
        return color
    }
    inner class NoteViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){

        val notes_layout =itemView.findViewById<CardView>(R.id.card_layout)
        val title =itemView.findViewById<TextView>(R.id.tv_title)
        val Note_tv=itemView.findViewById<TextView>(R.id.tv_note)
        val date=itemView.findViewById<TextView>(R.id.tv_date)
    }
    interface NotesClickListener{
        fun onItemClicked(note:Note)
        fun onLongItemClicked(note:Note,cardView: CardView)
    }
}