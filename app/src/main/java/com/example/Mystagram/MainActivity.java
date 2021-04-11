package com.example.Mystagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import android.os.Bundle;
import android.os.LocaleList;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.Mystagram.Dialogs.DialogFalloLogin;
import com.example.Mystagram.Dialogs.DialogLoginNoExiste;

import com.example.Mystagram.WS.inicioSesionWS;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

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
                    gestionarInicioSesion(usuario,password);
                } //Fin procedimiento login
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
        recreate();
    }
    private void gestionarInicioSesion(String usuario, String clave){
        //Gestiono el inicio de sesion buscando el usuario y contraseña contra la base de datos remota
        Data datos = new Data.Builder()
                .putString("usuario",usuario)
                .putString("clave",clave)
                .build();
        OneTimeWorkRequest loginOtwr= new OneTimeWorkRequest.Builder(inicioSesionWS.class).setInputData(datos)
                .build();

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(loginOtwr.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        //Trato la respuesta, teniendo en cuenta que: -1 -> Error de BD; 0 -> Usuario y contraseña correctos, 1: Usuario no existe, 2: Password incorrecta
                        if(workInfo != null && workInfo.getState().isFinished()){
                            String resultado=workInfo.getOutputData().getString("resultado");
                            if (resultado.equals("-1")){ //Fallo de BD
                                DialogFragment dialogoFalloBD= DialogFalloLogin.newInstance(getString(R.string.falloBD));
                                dialogoFalloBD.show(getSupportFragmentManager(), "falloBD");
                            }
                            else if (resultado.equals("0")){ //Login correcto
                                finish();
                                Intent i = new Intent(getApplicationContext(), ActivityPrincipal.class);
                                i.putExtra("usuario",usuario);
                                startActivity(i);
                            }
                            else if (resultado.equals("1")){ //Usuario no existe
                                DialogFragment dialogoLoginNoExiste = new DialogLoginNoExiste();
                                dialogoLoginNoExiste.show(getSupportFragmentManager(), "loginNoExiste");
                            }
                            else if (resultado.equals("2")){ //Contraseña no existe
                                DialogFragment dialogoClaveIncorrecta= DialogFalloLogin.newInstance(getString(R.string.lgClaveIncorrecta));
                                dialogoClaveIncorrecta.show(getSupportFragmentManager(), "claveIncorrecta");
                            }

                        }
                    }
                });
        WorkManager.getInstance(getApplicationContext()).enqueue(loginOtwr);
    }
}