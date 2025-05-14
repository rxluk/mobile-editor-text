package app.vercel.lucasgabrielcosta.mindra.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import app.vercel.lucasgabrielcosta.mindra.model.Note;

@Dao
public interface NoteDao {

    @Insert
    long insert(Note note);

    @Update
    void update(Note note);

    @Delete
    void delete(Note note);

    @Query("SELECT * FROM notes ORDER BY creationDate DESC")
    List<Note> getAllNotes();

    @Query("SELECT * FROM notes WHERE id = :id")
    Note getNoteById(int id);

    @Query("SELECT * FROM notes WHERE category = :category ORDER BY creationDate DESC")
    List<Note> getNotesByCategory(String category);

    @Query("DELETE FROM notes")
    void deleteAll();
}