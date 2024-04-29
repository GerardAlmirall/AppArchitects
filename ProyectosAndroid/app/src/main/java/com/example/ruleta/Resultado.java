package com.example.ruleta;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.ContentValues;
import android.provider.MediaStore;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ruleta.DB.DBconexion;
import java.io.OutputStream;
import android.os.AsyncTask;
import android.provider.CalendarContract;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;




public class Resultado extends AppCompatActivity {
    private static final String NOTIFICATION_CHANNEL_ID = "channel_id";
    private static final int NOTIFICATION_ID = 1234;
    public static final String[] NECESSARY_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_CALENDAR
    };
    private DBconexion dbConexion;

    public class SaveImageTask extends AsyncTask<Bitmap, Void, String> {
        private Context context;

        public SaveImageTask(Context context) {
            this.context = context;
        }
        // Declaración de los permisos necesarios

        @Override
        protected String doInBackground(Bitmap... bitmaps) {
            Bitmap bitmap = bitmaps[0];
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "image_" + System.currentTimeMillis() + ".jpg");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

            Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                return "Imagen guardada en la galería.";
            } catch (Exception e) {
                return "Error al guardar la imagen: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Solicitar permisos si es necesario
        requestPermissionsIfNeeded();
        setContentView(R.layout.activity_resultado);
        dbConexion = new DBconexion(this);

        // Recupera datos pasados de la actividad Tirada
        Intent intent = getIntent();
        int monedasGanadas = intent.getIntExtra("monedasGanadas", 0);
        int monedasApostadas = intent.getIntExtra("monedasApostadas", 10);

        // Recupera el nombre de usuario de SharedPreferences
        SharedPreferences prefs = getSharedPreferences("prefsRuleta", MODE_PRIVATE);
        String nombreUsuario = prefs.getString("nombreUsuario", "");
        long usuarioId = obtenerUsuarioIdPorNombre(nombreUsuario);
        TextView textUser = findViewById(R.id.textUser);
        textUser.setText(String.valueOf(nombreUsuario));

        // Agregar esta línea para obtener monedasTotales
        int monedasTotales = prefs.getInt(nombreUsuario + "_monedas", 100);

        TiradaClase nuevaTirada = new TiradaClase(
                0, // Pasamos 0 si el ID es autoincrementable
                monedasGanadas,
                intent.getIntExtra("premioSeleccionado", 0),
                intent.getIntExtra("monedasApostadas", 0),
                usuarioId,
                monedasTotales
        );

        // Guarda la nueva tirada en la base de datos
        dbConexion.insertarTirada(nuevaTirada);

        // Configura los elementos de la UI con los datos recuperados
        TextView txtGanado = findViewById(R.id.txtGanado);
        TextView txtHasGanado = findViewById(R.id.txtHasGanado);
        TextView txtHasApostado = findViewById(R.id.txtHasApostado);
        TextView txtMonedasApostadas = findViewById(R.id.txtMonedasApostadas);
        TextView txtTotalMonedas2 = findViewById(R.id.txtTotalMonedas2);
        TextView txtTotalMonedas = findViewById(R.id.txtMonedasTotales);

        txtGanado.setText(String.valueOf(monedasGanadas));
        txtMonedasApostadas.setText(String.valueOf(monedasApostadas));
        txtTotalMonedas2.setText(String.valueOf(monedasTotales));
        txtTotalMonedas.setText(String.valueOf(monedasTotales));

        // Configura el texto dependiendo si el usuario ha ganado o perdido
        if (monedasGanadas > 0) {
            txtHasGanado.setText("Has ganado:");
        } else {
            txtHasGanado.setText("Has perdido:");
        }

        Button btnRetirarse = findViewById(R.id.btnRetirarse);
        btnRetirarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent para regresar al menú principal
                Intent menuIntent = new Intent(Resultado.this, Menu.class);

                // Limpia la pila de actividades y lleva al usuario al menú principal
                menuIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                startActivity(menuIntent);
            }
        });

        //Botón para volver a tirar
        Button btnVolverTirar = findViewById(R.id.btnVolverTirar);
        btnVolverTirar.setOnClickListener(v -> {
            // Iniciar la actividad Tirada de nuevo
            Intent tiradaIntent = new Intent(Resultado.this, Tirada.class);
            startActivity(tiradaIntent);
        });
    }


    // Método para solicitar permisos si es necesario
    private void requestPermissionsIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                // Postergar la solicitud de permisos con un retardo de 1400 milisegundos
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Solicitar permisos
                        ActivityCompat.requestPermissions(Resultado.this, NECESSARY_PERMISSIONS, 123);
                    }
                }, 1400);
            }
        }
    }

    // Método invocado cuando se otorgan o deniegan permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            // Verificar si el permiso WRITE_CALENDAR ha sido otorgado
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso otorgado, realizar acciones necesarias aquí si lo deseas
            } else {
                // Permiso denegado, puedes informar al usuario o realizar acciones adicionales aquí si lo deseas
                Toast.makeText(this, "Permiso WRITE_CALENDAR denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public long obtenerUsuarioIdPorNombre(String nombreUsuario) {
        SQLiteDatabase db = dbConexion.getReadableDatabase();
        Cursor cursor = db.query("Usuario", new String[]{"id"}, "nombreUsuario = ?", new String[]{nombreUsuario}, null, null, null);
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndex("id");
            if (columnIndex != -1) {
                long id = cursor.getLong(columnIndex);
                cursor.close();
                return id;
            }
        }
        cursor.close();
        return -1;
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Reanuda la música sólo si está habilitada en las preferencias
        if (PreferenciasUsuario.shouldPlayMusic(this)) {
            Intent intent = new Intent(this, MusicaFondo.class);
            intent.setAction(MusicaFondo.ACTION_PLAY);
            intent.putExtra(MusicaFondo.EXTRA_VOLUME, 0.3f);
            startService(intent);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Pausa la música cuando la aplicación esta en segundo plano
        Intent intent = new Intent(this, MusicaFondo.class);
        intent.setAction(MusicaFondo.ACTION_PAUSE);
        startService(intent);
    }
    public void captureAndSaveDisplay() {
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        rootView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(rootView.getDrawingCache());
        rootView.setDrawingCacheEnabled(false);

        // Guardar la imagen
        new SaveImageTask(this).execute(bitmap);

        // Recupera el nombre de usuario de SharedPreferences
        SharedPreferences prefs = getSharedPreferences("prefsRuleta", MODE_PRIVATE);
        String nombreUsuario = prefs.getString("nombreUsuario", "");

    }
    private void guardarEventoEnCalendario(String nombreUsuario, int monedasGanadas) {
        // Obtener la fecha y hora actual en milisegundos
        long currentTimeMillis = System.currentTimeMillis();

        // Configurar los valores para el nuevo evento en el calendario
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, currentTimeMillis);
        values.put(CalendarContract.Events.DTEND, currentTimeMillis);
        values.put(CalendarContract.Events.TITLE, "Victoria de " + nombreUsuario + " (+" + monedasGanadas + " monedas)");
        values.put(CalendarContract.Events.DESCRIPTION, nombreUsuario + " ha ganado " + monedasGanadas + " monedas");
        values.put(CalendarContract.Events.EVENT_LOCATION, "Casino");
        values.put(CalendarContract.Events.CALENDAR_ID, 1); // ID del calendario por defecto
        values.put(CalendarContract.Events.EVENT_TIMEZONE, "GMT");


        // Insertar el evento en el calendario mediante el proveedor de contenido
        Uri uri = getContentResolver().insert(CalendarContract.Events.CONTENT_URI, values);
        if (uri != null) {
            // Si se inserta correctamente, mostrar mensaje de éxito
            Toast.makeText(this, "Evento agregado al calendario", Toast.LENGTH_SHORT).show();
        } else {
            // Si hay un error al insertar, mostrar mensaje de error
            Toast.makeText(this, "Error al agregar evento al calendario", Toast.LENGTH_SHORT).show();
        }
    }


    private String obtenerIdCalendarioGoogle() {
        String idCalendarioGoogle = null;
        Cursor cursor = getContentResolver().query(
                CalendarContract.Calendars.CONTENT_URI,
                new String[] {CalendarContract.Calendars._ID}, // Obtener solo la columna _ID
                null,
                null,
                null
        );

        if (cursor != null) {
            try {
                // Verificar si el cursor tiene al menos una fila
                if (cursor.moveToFirst()) {
                    // Intentar obtener el valor del _ID
                    int columnIndex = cursor.getColumnIndex(CalendarContract.Calendars._ID);
                    if (columnIndex != -1) {
                        idCalendarioGoogle = cursor.getString(columnIndex);
                    } else {
                        Log.e("Error", "No se encontró la columna _ID en el cursor");
                    }
                }
            } finally {
                cursor.close();
            }
        }

        return idCalendarioGoogle;
    }



    private boolean isScreenshotTaken = false;
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && !isScreenshotTaken) {
            Intent intent = getIntent();
            int monedasGanadas = intent.getIntExtra("monedasGanadas", 0);
            if (monedasGanadas > 0) {
                captureAndSaveDisplay();
                // Recuperar el nombre de usuario de las preferencias
                SharedPreferences prefs = getSharedPreferences("prefsRuleta", MODE_PRIVATE);
                String nombreUsuario = prefs.getString("nombreUsuario", "");

                // Verificar si hay permisos WRITE_CALENDAR otorgados
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
                    // Intentar agregar el evento al calendario
                    guardarEventoEnCalendario(nombreUsuario, monedasGanadas);
                } else {
                    // Permiso denegado, mostrar mensaje al usuario
                    Toast.makeText(this, "Permiso WRITE_CALENDAR denegado", Toast.LENGTH_SHORT).show();
                }
            }
            isScreenshotTaken = true;
        }
    }


}