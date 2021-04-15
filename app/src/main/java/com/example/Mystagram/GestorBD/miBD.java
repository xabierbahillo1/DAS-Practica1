package com.example.Mystagram.GestorBD;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

public class miBD extends SQLiteOpenHelper {
    //Gestor BD local
    public miBD(@Nullable Context context, @Nullable String name,
                @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //Se crean las tablas Usuarios y Fotousuario
        sqLiteDatabase.execSQL("CREATE TABLE Usuarios ('Usuario' VARCHAR(30) PRIMARY KEY NOT NULL, 'Correo' VARCHAR(120), 'NombreCompleto' VARCHAR(40),'Clave' VARCHAR(30))");
        sqLiteDatabase.execSQL("CREATE TABLE FotosUsuario ('fotoid' INTEGER PRIMARY KEY AUTOINCREMENT, 'usuario' VARCHAR(30) NOT NULL,'img' BLOB)");
        sqLiteDatabase.execSQL("INSERT INTO Usuarios(Usuario,Correo,NombreCompleto,Clave) VALUES ('prueba','prueba@correo.com','Usuario Pruebas','prueba')"); //Usuario por defecto para pruebas
        sqLiteDatabase.execSQL("INSERT INTO Usuarios(Usuario,Correo,NombreCompleto,Clave) VALUES ('admin','admin@correo.com','Administrador','password')"); //Usuario administrador
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

}
