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

    public long verificarEInsertarUsuario(String nombreUsuario, int monedasTotales) {
        SQLiteDatabase db = dbConexion.getWritableDatabase();

        // Intenta encontrar el usuario por su nombre
        Cursor cursor = db.query("Usuario", new String[]{"id"}, "nombreUsuario = ?", new String[]{nombreUsuario}, null, null, null);

        long id;
        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex("id"); // Obtén el índice de la columna
            if (idIndex != -1) { // Verifica que el índice sea válido
                id = cursor.getLong(idIndex); // Usa el índice para obtener el valor de la columna
                Log.d("DBmanager", "Usuario existente encontrado con ID: " + id);

                // Actualizar las monedas totales del usuario
                ContentValues updateValues = new ContentValues();
                updateValues.put("monedasTotales", monedasTotales);
                db.update("Usuario", updateValues, "id = ?", new String[]{String.valueOf(id)});
            } else {
                // Maneja el caso en que la columna "id" no exista, si es necesario
                Log.e("DBmanager", "La columna 'id' no fue encontrada en el resultado de la consulta.");
                throw new IllegalStateException("La columna 'id' no fue encontrada en el resultado de la consulta.");
            }
        } else {
            // Si el usuario no existe, inserta uno nuevo
            Log.d("DBmanager", "Usuario no encontrado, procediendo a insertar nuevo usuario: " + nombreUsuario);
            ContentValues values = new ContentValues();
            values.put("nombreUsuario", nombreUsuario);
            values.put("monedasTotales", monedasTotales); // Valor de monedas totales proporcionado

            id = db.insert("Usuario", null, values);

            if (id == -1) {
                Log.e("DBmanager", "Error al insertar nuevo usuario: " + nombreUsuario);
            } else {
                Log.d("DBmanager", "Nuevo usuario insertado correctamente con ID: " + id);
            }
        }

        cursor.close(); // Asegúrate de cerrar el cursor después de usarlo
        return id;
    }

    public void insertarUbicacionJugador(String nombreUsuario, double latitud, double longitud) {
        SQLiteDatabase db = dbConexion.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nombreUsuario", nombreUsuario);
        values.put("latitud", latitud);
        values.put("longitud", longitud);

        long resultado = db.insert("UbicacionJugador", null, values);
        if (resultado == -1) {
            Log.e("DBmanager", "Error al insertar ubicación del jugador en la base de datos.");
        } else {
            Log.d("DBmanager", "Ubicación del jugador insertada correctamente en la base de datos.");
        }

        db.close();
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
