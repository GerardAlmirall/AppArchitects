package com.example.ruleta;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ruleta.DB.DBconexion;

public class Resultado extends AppCompatActivity {
    private DBconexion dbConexion;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        DBconexion dbConexion = new DBconexion(this);
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
}
