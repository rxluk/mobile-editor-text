package app.vercel.lucasgabrielcosta.mindra.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.vercel.lucasgabrielcosta.mindra.R;
import app.vercel.lucasgabrielcosta.mindra.database.NoteDatabase;
import app.vercel.lucasgabrielcosta.mindra.model.Note;

public class NoteFormActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputEditText etNoteTitle;
    private TextInputEditText etNoteCategory;
    private TextInputEditText etNoteContent;
    private NoteDatabase database;
    private int noteId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_form);

        initializeViews();
        setupToolbar();
        setupTextHighlighting();
        checkForEditMode();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        etNoteTitle = findViewById(R.id.etNoteTitle);
        etNoteCategory = findViewById(R.id.etNoteCategory);
        etNoteContent = findViewById(R.id.etNoteContent);
        database = NoteDatabase.getDatabase(this);
    }

    private void checkForEditMode() {
        if (getIntent().hasExtra("note_id")) {
            noteId = getIntent().getIntExtra("note_id", -1);
            loadNoteData();
        }
    }

    private void loadNoteData() {
        if (noteId > 0) {
            Note note = database.noteDao().getNoteById(noteId);
            if (note != null) {
                etNoteTitle.setText(note.getTitle());
                etNoteCategory.setText(note.getCategory());
                etNoteContent.setText(note.getContent());
            }
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(noteId > 0 ? R.string.title_edit_note : R.string.title_create_note);
        }
    }

    private void setupTextHighlighting() {
        etNoteContent.addTextChangedListener(new TextWatcher() {
            private boolean isHighlighting = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isHighlighting) return;

                isHighlighting = true;

                ForegroundColorSpan[] spans = s.getSpans(0, s.length(), ForegroundColorSpan.class);
                for (ForegroundColorSpan span : spans) {
                    s.removeSpan(span);
                }

                int connectionColor = ContextCompat.getColor(NoteFormActivity.this, R.color.primary_dark);
                Pattern pattern = Pattern.compile("\\[\\[(.*?)\\]\\]");
                Matcher matcher = pattern.matcher(s.toString());

                while (matcher.find()) {
                    s.setSpan(
                            new ForegroundColorSpan(connectionColor),
                            matcher.start(),
                            matcher.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                }

                isHighlighting = false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_form, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            if (validateForm()) {
                saveNote();
            }
            return true;
        } else if (id == R.id.action_clear) {
            clearForm();
            showToast(getString(R.string.toast_form_cleared));
            return true;
        } else if (id == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean validateForm() {
        if (etNoteTitle.getText().toString().trim().isEmpty()) {
            showToast(getString(R.string.error_empty_title));
            etNoteTitle.requestFocus();
            return false;
        }

        if (etNoteCategory.getText().toString().trim().isEmpty()) {
            showToast(getString(R.string.error_empty_category));
            etNoteCategory.requestFocus();
            return false;
        }

        if (etNoteContent.getText().toString().trim().isEmpty()) {
            showToast(getString(R.string.error_empty_content));
            etNoteContent.requestFocus();
            return false;
        }

        return true;
    }

    private void saveNote() {
        String title = etNoteTitle.getText().toString().trim();
        String category = etNoteCategory.getText().toString().trim();
        String content = etNoteContent.getText().toString().trim();

        if (noteId > 0) {
            Note note = database.noteDao().getNoteById(noteId);
            if (note != null) {
                note.setTitle(title);
                note.setCategory(category);
                note.setContent(content);
                database.noteDao().update(note);
                showToast(getString(R.string.toast_note_updated));
            }
        } else {
            Note newNote = new Note();
            newNote.setTitle(title);
            newNote.setCategory(category);
            newNote.setContent(content);

            database.noteDao().insert(newNote);
            showToast(getString(R.string.toast_note_saved));
        }

        Intent resultIntent = new Intent();
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void clearForm() {
        etNoteTitle.setText("");
        etNoteCategory.setText("");
        etNoteContent.setText("");
        etNoteTitle.requestFocus();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}