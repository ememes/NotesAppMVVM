package com.example.notesapp.Database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.notesapp.Models.Note

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note:Note)
    @Delete
    fun deleteNote(note: Note)
    @Query("Select * from notes_table order by id ASC")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("UPDATE notes_table SET title=:title,note= :note WHERE id=:id")
    suspend fun update(id: Int?, title:String?, note: String?)


}