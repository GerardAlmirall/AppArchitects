package com.example.ruleta;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ruleta.DB.DBmanager;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private DBmanager dbManager;
    // Constante para el código de solicitud de permiso de ubicación
    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbManager = new DBmanager(this);
        if (PreferenciasUsuario.shouldPlayMusic(this)) {
            Intent musicServiceIntent = new Intent(this, MusicaFondo.class);
            startService(musicServiceIntent);
        }

        Button btnInicioSesion = findViewById(R.id.btnInicioSesion);
        btnInicioSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí se muestra el diálogo para ingresar el nombre de usuario
                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setHint("Nombre de Usuario");

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Iniciar Sesión")
                        .setMessage("Ingresa tu nombre de usuario:")
                        .setView(input)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                final String nombreUsuario = input.getText().toString();
                                if (!nombreUsuario.trim().isEmpty()) {
                                    // Llama al método para obtener y almacenar la ubicación
                                    obtenerYAlmacenarUbicacion(new UbicacionCallback() {
                                        @Override
                                        public void onUbicacionObtenida(String ubicacion) {
                                            // Consultar las monedas totales del usuario
                                            int monedasTotales = obtenerMonedasTotalesDelUsuario(nombreUsuario);

                                            // Llamar a verificarEInsertarUsuario con las monedas totales obtenidas
                                            long usuarioId = dbManager.verificarEInsertarUsuario(nombreUsuario, monedasTotales, ubicacion);

                                            // Guarda el nombre en SharedPreferences
                                            guardarNombreUsuarioEnPrefs(nombreUsuario);
                                            // Guarda el ID del usuario en SharedPreferences
                                            guardarIdUsuarioEnPrefs(usuarioId);
                                            // Actualiza las monedas totales en SharedPreferences
                                            guardarMonedasTotalesEnPrefs(monedasTotales);

                                            // Lanza la actividad Menu
                                            Intent intent = new Intent(MainActivity.this, Menu.class);
                                            startActivity(intent);
                                        }
                                    });
                                } else {
                                    Toast.makeText(MainActivity.this, "Por favor, ingresa un nombre de usuario.", Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });

        Button btnSalir = findViewById(R.id.btnSalir);
        btnSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAffinity();
            }
        });

        Button btnOpciones = findViewById(R.id.btnOpciones);
        btnOpciones.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Opciones.class);
            startActivity(intent);
        });

        Button btnAyuda = findViewById(R.id.btnAyuda);
        btnAyuda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AyudaActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pausar la música cuando la aplicación va a fondo
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

    @Override
    protected void onDestroy() {
        // Detener el servicio de música cuando la app se cierra completamente
        Intent musicServiceIntent = new Intent(this, MusicaFondo.class);
        stopService(musicServiceIntent);
        super.onDestroy();
    }

    // Método para obtener y almacenar la ubicación del usuario
    private void obtenerYAlmacenarUbicacion(final UbicacionCallback callback) {
        // Inicializar el LocationManager
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Verificar si se tienen permisos de ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Obtener la última ubicación conocida del proveedor de ubicación
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            // Verificar si la ubicación no es nula
            if (lastKnownLocation != null) {
                // Obtener la latitud y longitud
                double latitude = lastKnownLocation.getLatitude();
                double longitude = lastKnownLocation.getLongitude();

                // Convertir la latitud y longitud a una cadena de ubicación
                String ubicacion = "Latitud: " + latitude + ", Longitud: " + longitude;

                // Llamar al callback con la ubicación obtenida
                callback.onUbicacionObtenida(ubicacion);
            } else {
                // Manejar el caso en que la ubicación no esté disponible
                Log.e("MainActivity", "No se pudo obtener la ubicación actual.");
                // Llamar al callback con un valor nulo
                callback.onUbicacionObtenida(null);
            }
        } else {
            // Si no se tienen permisos, solicitarlos al usuario
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
        }
    }


    // Interfaz para manejar la ubicación obtenida de manera asíncrona
    private interface UbicacionCallback {
        void onUbicacionObtenida(String ubicacion);
    }

    private int obtenerMonedasTotalesDelUsuario(String nombreUsuario) {
        SQLiteDatabase db = dbManager.dbConexion.obtenerDatabase();

        int monedasTotales = 0;

        // Realiza una consulta a la base de datos para obtener las monedas totales del usuario
        Cursor cursor = db.query("Usuario", new String[]{"monedasTotales"}, "nombreUsuario = ?", new String[]{nombreUsuario}, null, null, null);
        if (cursor.moveToFirst()) {
            int monedasTotalesIndex = cursor.getColumnIndex("monedasTotales");
            if (monedasTotalesIndex != -1) {
                monedasTotales = cursor.getInt(monedasTotalesIndex);
            } else {
                // Manejar el caso en que la columna "monedasTotales" no exista
                Log.e("DBconexion", "La columna 'monedasTotales' no fue encontrada en el resultado de la consulta.");
            }
        } else {
            // Manejar el caso en que no se encuentre el usuario
            Log.e("DBconexion", "Usuario no encontrado: " + nombreUsuario);
        }

        cursor.close();
        db.close(); // Cierra la base de datos para liberar recursos

        return monedasTotales;
    }

    private void guardarNombreUsuarioEnPrefs(String nombreUsuario) {
        SharedPreferences prefs = getSharedPreferences("prefsRuleta", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("nombreUsuario", nombreUsuario);
        editor.apply();
    }

    private void guardarIdUsuarioEnPrefs(long usuarioId) {
        SharedPreferences prefs = getSharedPreferences("prefsRuleta", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("usuarioId", usuarioId);
        editor.apply();
    }

    private void guardarMonedasTotalesEnPrefs(int monedasTotales) {
        SharedPreferences prefs = getSharedPreferences("prefsRuleta", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("monedasTotales", monedasTotales);
        editor.apply();
    }
}
