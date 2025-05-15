package app.vercel.lucasgabrielcosta.mindra.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import app.vercel.lucasgabrielcosta.mindra.R;

public class AboutActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private SwitchCompat themeSwitch;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        initializeViews();
        setupToolbar();
        setupThemeSwitch();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        themeSwitch = findViewById(R.id.switchTheme); // Aqui usamos o novo ID
        preferences = getSharedPreferences("mindra_preferences", MODE_PRIVATE);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.about_title);
        }
    }

    private void setupThemeSwitch() {
        boolean isDarkTheme = preferences.getBoolean("dark_theme", true);
        themeSwitch.setChecked(isDarkTheme);

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean("dark_theme", isChecked).apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

            recreate();
        });
    }
}