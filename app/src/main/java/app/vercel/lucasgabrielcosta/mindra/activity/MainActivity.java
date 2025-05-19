package app.vercel.lucasgabrielcosta.mindra.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import java.util.Locale;

import app.vercel.lucasgabrielcosta.mindra.R;

public class MainActivity extends AppCompatActivity {

    private CardView cardNewNote, cardViewNotes, cardViewNetwork, cardAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyUserLanguage();
        applyUserTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupCardListeners();
    }

    private void applyUserLanguage() {
        SharedPreferences preferences = getSharedPreferences("mindra_preferences", MODE_PRIVATE);
        String languageCode = preferences.getString("language", "en");

        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);

        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    private void applyUserTheme() {
        SharedPreferences preferences = getSharedPreferences("mindra_preferences", MODE_PRIVATE);
        boolean isDarkTheme = preferences.getBoolean("dark_theme", true);

        AppCompatDelegate.setDefaultNightMode(isDarkTheme ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private void initializeViews() {
        cardNewNote = findViewById(R.id.cardNewNote);
        cardViewNotes = findViewById(R.id.cardViewNotes);
        cardViewNetwork = findViewById(R.id.cardViewNetwork);
        cardAbout = findViewById(R.id.cardAbout);
    }

    private void setupCardListeners() {
        cardNewNote.setOnClickListener(v -> openNoteForm());
        cardViewNotes.setOnClickListener(v -> openNoteList());
        cardViewNetwork.setOnClickListener(v -> openNetworkView());
        cardAbout.setOnClickListener(v -> openAboutScreen());
    }

    private void openNoteForm() {
        Intent intent = new Intent(MainActivity.this, NoteFormActivity.class);
        startActivity(intent);
    }

    private void openNoteList() {
        Intent intent = new Intent(MainActivity.this, NoteListActivity.class);
        startActivity(intent);
    }

    private void openNetworkView() {
        Intent intent = new Intent(MainActivity.this, NetworkViewActivity.class);
        startActivity(intent);
    }

    private void openAboutScreen() {
        Intent intent = new Intent(MainActivity.this, AboutActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkThemeChange();
    }

    private void checkThemeChange() {
        SharedPreferences preferences = getSharedPreferences("mindra_preferences", MODE_PRIVATE);
        boolean isDarkTheme = preferences.getBoolean("dark_theme", true);
        int currentNightMode = AppCompatDelegate.getDefaultNightMode();

        if ((isDarkTheme && currentNightMode != AppCompatDelegate.MODE_NIGHT_YES) ||
                (!isDarkTheme && currentNightMode != AppCompatDelegate.MODE_NIGHT_NO)) {
            recreate();
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}