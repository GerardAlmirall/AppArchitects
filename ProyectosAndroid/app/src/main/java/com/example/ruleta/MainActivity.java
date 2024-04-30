package com.example.ruleta;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.content.Context;
import com.example.ruleta.DB.DBmanager;
import android.Manifest;
import android.content.pm.PackageManager;

public class MainActivity extends AppCompatActivity {

    private DBmanager dbManager;

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private String nombreUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Verificar y solicitar permisos de ubicación si es necesario
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates();
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            }
        } else {
            requestLocationUpdates();
        }
        // Configuración del botón de inicio de sesión
        Button btnInicioSesion = findViewById(R.id.btnInicioSesion);
        btnInicioSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setHint("Nombre de Usuario");

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Iniciar Sesión")
                        .setMessage("Ingresa tu nombre de usuario:")
                        .setView(input)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String nombreUsuario = input.getText().toString();
                                if (!nombreUsuario.trim().isEmpty()) {
                                    // Obtener la ubicación del jugador
                                    obtenerUbicacion(nombreUsuario);
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
    // Método para solicitar actualizaciones de ubicación
    private void requestLocationUpdates() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                try {
                    locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            // Llamar a la función para iniciar sesión con la ubicación
                            iniciarSesionConUbicacion(nombreUsuario, location);
                        }

                        // Resto de los métodos del LocationListener...
                    }, null);
                } catch (SecurityException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Excepción de seguridad: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "El proveedor de ubicación no está habilitado.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "No se pudo obtener el servicio de ubicación.", Toast.LENGTH_LONG).show();
        }
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

    private void iniciarSesionConUbicacion(String nombreUsuario, Location location) {
        if (location != null) {
            // Aquí puedes almacenar la ubicación del jugador en la base de datos
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            // Luego, puedes pasar la ubicación a tu clase DBmanager para su almacenamiento
            dbManager.insertarUbicacionJugador(nombreUsuario, latitude, longitude);

            // Continuar con el inicio de sesión
            continuarInicioSesion(nombreUsuario);
        } else {
            Toast.makeText(MainActivity.this, "No se pudo obtener la ubicación del jugador.", Toast.LENGTH_LONG).show();
            // Continuar con el inicio de sesión sin almacenar la ubicación
            continuarInicioSesion(nombreUsuario);
        }
    }

    // Agregar método para obtener la ubicación del jugador
    private void obtenerUbicacion(String nombreUsuario) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            // Verificar si el proveedor de ubicación está habilitado
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                try {
                    // Verificar permisos de ubicación
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        // Solicitar actualizaciones de ubicación
                        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                // Llamar a la función para iniciar sesión con la ubicación
                                iniciarSesionConUbicacion(nombreUsuario, location);
                            }

                            @Override
                            public void onStatusChanged(String provider, int status, Bundle extras) {}

                            @Override
                            public void onProviderEnabled(String provider) {}

                            @Override
                            public void onProviderDisabled(String provider) {}
                        }, null);
                    } else {
                        // Si los permisos no están concedidos, solicitarlos al usuario
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                    }
                } catch (SecurityException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Excepción de seguridad: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(MainActivity.this, "El proveedor de ubicación no está habilitado.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "No se pudo obtener el servicio de ubicación.", Toast.LENGTH_LONG).show();
        }
    }


    private void continuarInicioSesion(String nombreUsuario) {
        // Aquí puedes continuar con el proceso de inicio de sesión
        // Por ejemplo, lanzar la actividad del menú
        Intent intent = new Intent(MainActivity.this, Menu.class);
        startActivity(intent);
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

    // Método onRequestPermissionsResult para manejar la respuesta del usuario a la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso de ubicación concedido
                Toast.makeText(this, "Permiso de ubicación concedido.", Toast.LENGTH_SHORT).show();
                requestLocationUpdates(); // Solicitar actualizaciones de ubicación después de obtener permiso
            } else {
                // Permiso de ubicación denegado
                Toast.makeText(this, "Permiso de ubicación denegado.", Toast.LENGTH_SHORT).show();
            }
        }
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
