package com.example.practica1;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class miBD extends SQLiteOpenHelper {
    public miBD(@Nullable Context context, @Nullable String name,
                @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE Usuarios ('Usuario' VARCHAR(30) PRIMARY KEY NOT NULL, 'Correo' VARCHAR(120), 'NombreCompleto' VARCHAR(40),'Clave' VARCHAR(30))");
        sqLiteDatabase.execSQL("CREATE TABLE FotosUsuario ('fotoid' INTEGER, 'usuario' VARCHAR(30) NOT NULL,'img' BLOB, PRIMARY KEY('fotoid','usuario'))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

}
