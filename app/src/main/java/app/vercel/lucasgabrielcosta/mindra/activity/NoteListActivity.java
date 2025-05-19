package app.vercel.lucasgabrielcosta.mindra.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;

import app.vercel.lucasgabrielcosta.mindra.R;
import app.vercel.lucasgabrielcosta.mindra.adapter.NoteAdapter;
import app.vercel.lucasgabrielcosta.mindra.database.NoteDatabase;
import app.vercel.lucasgabrielcosta.mindra.model.Note;
import app.vercel.lucasgabrielcosta.mindra.util.SwipeActionsTouchListener;

public class NoteListActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ListView listViewNotes;
    private ArrayList<Note> noteList;
    private NoteAdapter noteAdapter;
    private NoteDatabase database;
    private ActionMode actionMode;
    private int selectedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        initializeViews();
        setupToolbar();
        setupAdapter();
        loadNotesFromDatabase();
        setupListClickListener();
        setupSwipeActions();

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

        listViewNotes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (actionMode != null) {
                    return false;
                }

                selectedPosition = position;
                actionMode = startActionMode(actionModeCallback);
                view.setSelected(true);
                return true;
            }
        });
    }

    private void setupSwipeActions() {
        SwipeActionsTouchListener touchListener = new SwipeActionsTouchListener(
                listViewNotes,
                new SwipeActionsTouchListener.SwipeActionsCallback() {
                    @Override
                    public boolean canSwipe(int position) {
                        return position < noteList.size();
                    }

                    @Override
                    public void onSwipeRight(int position) {
                        // Swipe para a direita - Editar
                        if (position < noteList.size()) {
                            Note selectedNote = noteList.get(position);
                            openNoteForEditing(selectedNote);
                        }
                    }

                    @Override
                    public void onSwipeLeft(int position, Context context) {
                        // Swipe para a esquerda - Excluir (com confirmação)
                        if (position < noteList.size()) {
                            Note noteToDelete = noteList.get(position);
                            showDeleteConfirmationDialog(noteToDelete, position);
                        }
                    }
                });

        listViewNotes.setOnTouchListener(touchListener);
        listViewNotes.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // Desativa o swipe quando a lista está sendo rolada
                touchListener.setPaused(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
    }

    private void showDeleteConfirmationDialog(final Note noteToDelete, final int position) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar exclusão")
                .setMessage("Tem certeza que deseja excluir a nota \"" + noteToDelete.getTitle() + "\"?")
                .setPositiveButton("Excluir", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        database.noteDao().delete(noteToDelete);
                        noteList.remove(position);
                        noteAdapter.notifyDataSetChanged();
                        Toast.makeText(NoteListActivity.this, "Nota excluída", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void openNoteForEditing(Note note) {
        Intent intent = new Intent(NoteListActivity.this, NoteFormActivity.class);
        intent.putExtra("note_id", note.getId());
        startActivityForResult(intent, 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add) {
            Intent intent = new Intent(NoteListActivity.this, NoteFormActivity.class);
            startActivityForResult(intent, 1);
            return true;
        } else if (id == R.id.action_about) {
            Intent intent = new Intent(NoteListActivity.this, AboutActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadNotesFromDatabase();
        }
    }

    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.context_menu_note, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (selectedPosition < 0 || selectedPosition >= noteList.size()) {
                mode.finish();
                return false;
            }

            Note selectedNote = noteList.get(selectedPosition);
            int itemId = item.getItemId();

            if (itemId == R.id.action_edit) {
                Intent intent = new Intent(NoteListActivity.this, NoteFormActivity.class);
                intent.putExtra("note_id", selectedNote.getId());
                startActivityForResult(intent, 1);
                mode.finish();
                return true;
            } else if (itemId == R.id.action_delete) {
                showDeleteConfirmationDialog(selectedNote, selectedPosition);
                mode.finish();
                return true;
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            selectedPosition = -1;
        }
    };
}