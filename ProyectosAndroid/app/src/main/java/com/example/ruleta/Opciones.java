package com.example.ruleta;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Opciones extends AppCompatActivity {
    private Switch switchMusica;
    private Button btnSelectMusica;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opciones);

        // Obtener referencias de la UI
        switchMusica = findViewById(R.id.switchMusic);
        btnSelectMusica = findViewById(R.id.btnSelectMusic);

        // Configurar el estado inicial del Switch basado en la preferencia guardada
        switchMusica.setChecked(PreferenciasUsuario.shouldPlayMusic(this));

        // Manejar cambios en el Switch
        switchMusica.setOnCheckedChangeListener((buttonView, isChecked) -> {
            PreferenciasUsuario.saveMusicPreference(this, isChecked);
            Intent intent = new Intent(this, MusicaFondo.class);
            if (isChecked) {
                intent.setAction(MusicaFondo.ACTION_PLAY);
            } else {
                intent.setAction(MusicaFondo.ACTION_PAUSE);
            }
            startService(intent);
        });

        btnSelectMusica.setOnClickListener(v -> openFileChooser());
        Button btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(v -> {
            Intent intent = new Intent(Opciones.this, MainActivity.class);
            startActivity(intent);
        });
    }
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 1);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri selectedMusicUri = data.getData();
            if (selectedMusicUri != null) {
                playMusicFromUri(selectedMusicUri);
            }
        }
    }

    private void playMusicFromUri(Uri musicUri) {
        Intent serviceIntent = new Intent(this, MusicaFondo.class);
        serviceIntent.setAction(MusicaFondo.ACTION_PLAY);
        serviceIntent.putExtra(MusicaFondo.EXTRA_MUSIC_URI, musicUri.toString());
        startService(serviceIntent);
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Pausar la música cuando la aplicación esta en segundo plano
        Intent intent = new Intent(this, MusicaFondo.class);
        intent.setAction(MusicaFondo.ACTION_PAUSE);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reanudar la música sólo si está habilitada en las preferencias
        if (PreferenciasUsuario.shouldPlayMusic(this)) {
            Intent intent = new Intent(this, MusicaFondo.class);
            intent.setAction(MusicaFondo.ACTION_PLAY);
            intent.putExtra(MusicaFondo.EXTRA_VOLUME, 1.0f);
            startService(intent);
        }
    }
}