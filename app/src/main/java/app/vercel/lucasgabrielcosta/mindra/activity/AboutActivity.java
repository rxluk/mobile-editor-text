package app.vercel.lucasgabrielcosta.mindra.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import java.util.Locale;

import app.vercel.lucasgabrielcosta.mindra.R;

public class AboutActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private SwitchCompat themeSwitch;
    private Spinner spinnerLanguage;
    private SharedPreferences preferences;
    private boolean settingsChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        initializeViews();
        setupToolbar();
        setupThemeSwitch();
        setupLanguageSpinner();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        themeSwitch = findViewById(R.id.switchTheme);
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
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
            settingsChanged = true;

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

            recreate();
        });
    }

    private void setupLanguageSpinner() {
        String[] languageOptions = {
                getString(R.string.idioma_ingles),
                getString(R.string.idioma_portugues)
        };

        // Configurar o adaptador para o spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                languageOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);

        String currentLanguage = preferences.getString("language", "en");
        spinnerLanguage.setSelection(currentLanguage.equals("pt") ? 1 : 0);

        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String languageCode = (position == 0) ? "en" : "pt";
                String currentLanguage = preferences.getString("language", "en");

                if (!currentLanguage.equals(languageCode)) {
                    preferences.edit().putString("language", languageCode).apply();
                    settingsChanged = true;

                    changeAppLanguage(languageCode);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void changeAppLanguage(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);

        resources.updateConfiguration(config, resources.getDisplayMetrics());

        Intent refresh = new Intent(this, AboutActivity.class);
        finish();
        startActivity(refresh);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}