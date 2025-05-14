package app.vercel.lucasgabrielcosta.mindra.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.List;

import app.vercel.lucasgabrielcosta.mindra.R;
import app.vercel.lucasgabrielcosta.mindra.database.NoteDatabase;
import app.vercel.lucasgabrielcosta.mindra.model.Note;
import app.vercel.lucasgabrielcosta.mindra.view.NetworkView;

public class NetworkViewActivity extends AppCompatActivity implements NetworkView.OnNodeSelectedListener {

    private Toolbar toolbar;
    private NetworkView networkView;
    private List<Note> allNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_view);

        initializeViews();
        setupToolbar();
        loadAllNotes();

        if (allNotes.isEmpty()) {
            showEmptyNotesMessage();
            return;
        }

        networkView.setNotes(allNotes);
        networkView.setOnNodeSelectedListener(this);
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        networkView = findViewById(R.id.networkView);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.network_view_title);
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadAllNotes() {
        NoteDatabase db = NoteDatabase.getDatabase(this);
        allNotes = db.noteDao().getAllNotes();
    }

    private void showEmptyNotesMessage() {
        Toast.makeText(this, R.string.no_notes, Toast.LENGTH_SHORT).show();
        View emptyView = findViewById(R.id.tvInstructions);
        if (emptyView != null) {
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNodeSelected(Note note) {
        Toast.makeText(this, getString(R.string.note_clicked, note.getTitle()), Toast.LENGTH_SHORT).show();

        // Abre a nota para edição quando um nó é clicado
        Intent intent = new Intent(this, NoteFormActivity.class);
        intent.putExtra("note_id", note.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarregar as notas se o usuário voltar para esta tela após editar uma nota
        loadAllNotes();
        if (!allNotes.isEmpty()) {
            networkView.setNotes(allNotes);
        }
    }
}