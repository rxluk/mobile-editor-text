package app.vercel.lucasgabrielcosta.mindra.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import app.vercel.lucasgabrielcosta.mindra.R;
import app.vercel.lucasgabrielcosta.mindra.adapter.NoteAdapter;
import app.vercel.lucasgabrielcosta.mindra.database.NoteDatabase;
import app.vercel.lucasgabrielcosta.mindra.model.Note;

public class NoteListActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ListView listViewNotes;
    private FloatingActionButton fabAddNote;
    private ArrayList<Note> noteList;
    private NoteAdapter noteAdapter;
    private NoteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        initializeViews();
        setupToolbar();
        setupAdapter();
        loadNotesFromDatabase();
        setupListClickListener();
        setupButtonListeners();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(NoteListActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        listViewNotes = findViewById(R.id.listViewNotes);
        fabAddNote = findViewById(R.id.fabAddNote);
        database = NoteDatabase.getDatabase(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.notes_title);
        }
    }

    private void setupAdapter() {
        noteList = new ArrayList<>();
        noteAdapter = new NoteAdapter(this, noteList);
        listViewNotes.setAdapter(noteAdapter);
    }

    private void loadNotesFromDatabase() {
        List<Note> notes = database.noteDao().getAllNotes();
        noteList.clear();
        noteList.addAll(notes);
        noteAdapter.notifyDataSetChanged();
    }

    private void setupListClickListener() {
        listViewNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < noteList.size()) {
                    Note selectedNote = noteList.get(position);
                    openNoteForEditing(selectedNote);
                }
            }
        });
    }

    private void openNoteForEditing(Note note) {
        Intent intent = new Intent(NoteListActivity.this, NoteFormActivity.class);
        intent.putExtra("note_id", note.getId());
        startActivity(intent);
    }

    private void setupButtonListeners() {
        fabAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NoteListActivity.this, NoteFormActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotesFromDatabase();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}