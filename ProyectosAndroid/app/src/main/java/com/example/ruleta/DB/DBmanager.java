package com.example.ruleta.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import com.example.ruleta.TiradaClase;



import io.reactivex.rxjava3.core.Observable;

public class DBmanager {
    public DBconexion dbConexion;

    public DBmanager(Context context) {
        this.dbConexion = new DBconexion(context);
    }

    public long verificarEInsertarUsuario(String nombreUsuario, int monedasTotales, String ubicacion) {
        SQLiteDatabase db = dbConexion.getWritableDatabase();

        // Intenta encontrar el usuario por su nombre
        Cursor cursor = db.query("Usuario", new String[]{"id"}, "nombreUsuario = ?", new String[]{nombreUsuario}, null, null, null);

        long id;
        if (cursor != null && cursor.moveToFirst()) {
            // Usuario encontrado, actualizar las monedas totales del usuario
            int idColumnIndex = cursor.getColumnIndex("id");
            if (idColumnIndex != -1) {
                id = cursor.getLong(idColumnIndex);
                ContentValues updateValues = new ContentValues();
                updateValues.put("monedasTotales", monedasTotales);
                db.update("Usuario", updateValues, "id = ?", new String[]{String.valueOf(id)});
            } else {
                // Manejar el caso en que la columna "id" no existe en el cursor
                Log.e("Error", "La columna 'id' no existe en el cursor.");
                id = -1; // Valor predeterminado para indicar un error
            }
        } else {
            // Usuario no encontrado, insertar uno nuevo
            ContentValues values = new ContentValues();
            values.put("nombreUsuario", nombreUsuario);
            values.put("monedasTotales", monedasTotales);
            values.put("ubicacion", ubicacion);
            id = db.insert("Usuario", null, values);
        }

        if (cursor != null) {
            cursor.close();
        }

        return id;
    }










    public Observable<List<TiradaClase>> obtenerHistorialDeUsuario(long usuarioId) {
        return Observable.create(emitter -> {
            List<TiradaClase> historial = new ArrayList<>();
            SQLiteDatabase db = dbConexion.getReadableDatabase();

            String[] projection = {
                    "id",
                    "resultado",
                    "premioSeleccionado",
                    "apuesta",
                    "usuarioId",
                    "monedasTotales"
            };

            String selection = "usuarioId = ?";
            String[] selectionArgs = {String.valueOf(usuarioId)};
            Log.d("DBmanager", "Iniciando consulta de historial para usuarioId: " + usuarioId);
            Cursor cursor = db.query(
                    "Tirada",
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null);

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                int resultado = cursor.getInt(cursor.getColumnIndexOrThrow("resultado"));
                int premioSeleccionado = cursor.getInt(cursor.getColumnIndexOrThrow("premioSeleccionado"));
                int apuesta = cursor.getInt(cursor.getColumnIndexOrThrow("apuesta"));
                long userId = cursor.getLong(cursor.getColumnIndexOrThrow("usuarioId"));
                int monedasTotales = cursor.getInt(cursor.getColumnIndexOrThrow("monedasTotales"));

                historial.add(new TiradaClase(id, resultado, premioSeleccionado, apuesta, userId, monedasTotales));
                Log.d("DBmanager", "Tirada recuperada: Id=" + id + ", Resultado=" + resultado + ", PremioSeleccionado=" + premioSeleccionado + ", Apuesta=" + apuesta + ", UsuarioId=" + userId + ", MonedasTotales=" + monedasTotales);
            }
            cursor.close();

            if (!emitter.isDisposed()) {
                emitter.onNext(historial);
                emitter.onComplete();
            }
        });
    }
}