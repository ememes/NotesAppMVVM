package com.example.notesapp

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat

import com.example.notesapp.Models.Note
import com.example.notesapp.databinding.ActivityAddNoteBinding

import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class  AddNote : AppCompatActivity() {
    private lateinit var binding: ActivityAddNoteBinding
    private lateinit var note : Note
    private lateinit var old_note : Note
    var isUpdate=false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=ActivityAddNoteBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val isDarkMode = (this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        val drawable = ContextCompat.getDrawable(this, R.drawable.ic_baseline_check_24)
        if (isDarkMode) {
            drawable?.setColorFilter(ContextCompat.getColor(this, R.color.white), PorterDuff.Mode.SRC_IN)
        } else {
            // Eğer telefon karanlık modda değilse, vektörün rengi bozulmamalı
            drawable?.clearColorFilter()
        }

        val nightModeFlags = this.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            binding.toolbar.setBackgroundColor(Color.parseColor("#292929"))
        }
        try{
            old_note=intent.getSerializableExtra("current_note") as Note
            binding.etTitle.setText(old_note.title)
            binding.etNote.setText(old_note.note)
            isUpdate=true

        }
        catch (e:Exception)
        {
            e.printStackTrace()
        }
        binding.imgcheck.setOnClickListener {
            val title=binding.etTitle.text.toString()
            val note_desc=binding.etNote.text.toString()
            if(title.isNotEmpty() || note_desc.isNotEmpty() ){
                val formatter=SimpleDateFormat("EEE,d MMM yyyy HH:mm")
                if(isUpdate)
                {
                    note=Note(
                        old_note.id,title,note_desc,formatter.format(Date())
                    )
                }
                else{
                    note=Note(
                        null,title,note_desc,formatter.format(Date())
                    )
                }
                val intent= Intent()
                intent.putExtra("note",note)
                setResult(Activity.RESULT_OK,intent)
                finish()

            }
            else
            {
                Toast.makeText(this@AddNote,"Please enter data.",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }
        binding.imgbackarrow.setOnClickListener{
            onBackPressed()
        }
    }
}