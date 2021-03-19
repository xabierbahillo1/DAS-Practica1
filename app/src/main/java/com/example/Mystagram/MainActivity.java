package com.example.Mystagram;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.LocaleList;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Gestiono el idioma
        gestionarIdioma();
        //Definicion de eventos
        TextView textRegister= findViewById(R.id.registerText);
        textRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
            }
        });

        TextView buttonLogin= findViewById(R.id.buttonLogin);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Compruebo el login
                EditText usuarioET=findViewById(R.id.userText);
                EditText passwordET=findViewById(R.id.claveText);
                String usuario=usuarioET.getText().toString();
                String password=passwordET.getText().toString();
                boolean login=false;
                if (usuario.equals("") || password.equals("")){ //Si alguno de los campos esta vacio
                    //Devuelvo dialog indicandolo
                    DialogFragment dialogoFaltanCampos= DialogFalloLogin.newInstance(getString(R.string.rgFaltanCampos));
                    dialogoFaltanCampos.show(getSupportFragmentManager(), "faltanCampos");
                }
                else{ //Procedimiento login
                    //Compruebo si existe el usuario
                    miBD GestorDB = new miBD (getApplicationContext(), "MystragramDB", null, 1);
                    SQLiteDatabase bd = GestorDB.getWritableDatabase();
                    Cursor c = bd.rawQuery("SELECT Usuario FROM Usuarios WHERE Usuario=\'"+usuario+"\'", null);
                    if (c.moveToFirst()){ //Si cursor no esta vacio, existe el usuario, compruebo si la clave es correcta
                        Cursor c1 = bd.rawQuery("SELECT Usuario FROM Usuarios WHERE Usuario=\'"+usuario+"\' AND Clave=\'"+password+"\'", null);
                        if (c1.moveToFirst()){ //Clave correcta
                            login=true;
                        }
                        else{
                            DialogFragment dialogoClaveIncorrecta= DialogFalloLogin.newInstance(getString(R.string.lgClaveIncorrecta));
                            dialogoClaveIncorrecta.show(getSupportFragmentManager(), "claveIncorrecta");
                        }
                        c.close();
                        c1.close();
                    }
                    else { //Si no existe, muestro un dialog para ofrecerle ir a registrar el usuario
                        c.close();
                        DialogFragment dialogoLoginNoExiste = new DialogLoginNoExiste();
                        dialogoLoginNoExiste.show(getSupportFragmentManager(), "loginNoExiste");
                    }
                    bd.close();
                } //Fin procedimiento login
                if (login){ //Si el login ha sido correcto
                    finish();
                    Intent i = new Intent(getApplicationContext(), ActivityPrincipal.class);
                    i.putExtra("usuario",usuario);
                    startActivity(i);
                }
            }
        });
    }

    private void gestionarIdioma(){ //Obtiene el idioma actual que tiene la aplicacion en preferencias
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String idioma=prefs.getString("idiomaApp","DEF"); //Si no hay ningun idioma devuelve def
        if (idioma.equals("DEF")) { //No hay ningun idioma en preferencias, asigno uno para seleccionar idioma de la app
            String idiomaSistema=Locale.getDefault().getLanguage(); //Obtengo el idioma del sistema
            SharedPreferences.Editor editor= prefs.edit();
            switch (idiomaSistema){
                case "en":{ //Si el idioma por defecto es ingles, cambio el idioma de la app a ingles
                    editor.putString("idiomaApp","ENG");
                    cambiarIdioma("ENG");
                    break;
                }
                default:{ //Por defecto pongo esp
                    editor.putString("idiomaApp","ESP");
                    cambiarIdioma("ESP");
                    break;
                }
            }
            editor.apply();
        }
        else{
            LocaleList locale=getBaseContext().getResources().getConfiguration().getLocales(); //Obtengo el idioma actual de la aplicacion
            String idiomaApp=locale.get(0).toString();
            if (idiomaApp.equals("es")){
                idiomaApp="ESP";
            }
            if (idiomaApp.equals("en")){
                idiomaApp="ENG";
            }
            if (idiomaApp.equals("en_GB")){
                idiomaApp="ENG";
            }
            if (!idiomaApp.equals(idioma)){ //Si el idioma de la app no es el mismo que el de las preferencias lo cambio
                cambiarIdioma(idioma);
            }

        }
    }
    private void cambiarIdioma(String idioma){
        Locale nuevaloc= new Locale("es"); //Por defecto español
        if (idioma.equals("ENG")){ //Si el idioma es ingles
            nuevaloc = new Locale("en","GB");
        }
        Locale.setDefault(nuevaloc);
        Configuration configuration =
                getBaseContext().getResources().getConfiguration();
        configuration.setLocale(nuevaloc);
        configuration.setLayoutDirection(nuevaloc);
        Context context =
                getBaseContext().createConfigurationContext(configuration);
        getBaseContext().getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
        finish();
        startActivity(getIntent());
    }
}