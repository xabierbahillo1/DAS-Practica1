package com.example.Mystagram;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.LocaleList;

import java.util.Locale;

public class PreferencesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String idioma=prefs.getString("idiomaApp","DEF"); //Si no hay ningun idioma devuelve def
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
    protected void onRestoreInstanceState(Bundle savedInstanceState){ //Si cambio a horizontal
        super.onRestoreInstanceState(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cambiarIdioma(prefs.getString("idiomaApp","DEF"));
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