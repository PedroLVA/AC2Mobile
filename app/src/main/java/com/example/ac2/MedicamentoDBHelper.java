package com.example.ac2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MedicamentoDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "medicamentos.db";
    public static final int DATABASE_VERSION = 2;


    public static final String TABLE_MEDICAMENTOS = "medicamentos";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NOME = "nome";
    public static final String COLUMN_DESCRICAO = "descricao";
    public static final String COLUMN_HORARIO = "horario";
    public static final String COLUMN_TOMADO = "tomado";

    public MedicamentoDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_MEDICAMENTOS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NOME + " TEXT, "
                + COLUMN_DESCRICAO + " TEXT, "
                + COLUMN_HORARIO + " TEXT, "
                + COLUMN_TOMADO + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICAMENTOS);
        onCreate(db);
    }


    public long inserirMedicamento(String nome, String descricao, String horario) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOME, nome);
        values.put(COLUMN_DESCRICAO, descricao);
        values.put(COLUMN_HORARIO, horario);
        return db.insert(TABLE_MEDICAMENTOS, null, values);
    }

    public Cursor listarMedicamentos() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_MEDICAMENTOS, null);
    }

    public int atualizarMedicamento(int id, String nome, String descricao, String horario, int tomado) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOME, nome);
        values.put(COLUMN_DESCRICAO, descricao);
        values.put(COLUMN_HORARIO, horario);
        values.put(COLUMN_TOMADO, tomado);
        return db.update(TABLE_MEDICAMENTOS, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public int marcarComoTomado(int id, int tomado) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TOMADO, tomado);
        return db.update(TABLE_MEDICAMENTOS, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    public int excluirMedicamento(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_MEDICAMENTOS, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }
}
