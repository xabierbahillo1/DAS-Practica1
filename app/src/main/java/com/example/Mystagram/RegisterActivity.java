package com.example.Mystagram;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.Context;

import android.content.SharedPreferences;
import android.content.res.Configuration;

import android.os.Bundle;
import android.os.LocaleList;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.Mystagram.Dialogs.DialogFalloRegistro;
import com.example.Mystagram.Dialogs.DialogFinRegistro;

import com.example.Mystagram.WS.registroWS;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button buttonRegister= findViewById(R.id.buttonRegister);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText correoET=findViewById(R.id.rgEmailText);
                EditText nomComET=findViewById(R.id.rgNomComText);
                EditText usuarioET=findViewById(R.id.rgUsuarioText);
                EditText passwordET=findViewById(R.id.rgPasswordText);
                String correo=correoET.getText().toString();
                String nomCom=nomComET.getText().toString();
                String usuario=usuarioET.getText().toString();
                String password=passwordET.getText().toString();
                boolean registrado=false;
                if (correo.equals("") || nomCom.equals("") || usuario.equals("") || password.equals("")){ //Si alguno de los campos esta vacio
                    //Devuelvo dialog indicandolo
                    DialogFragment dialogoFaltanCampos= DialogFalloRegistro.newInstance(getString(R.string.rgFaltanCampos));
                    dialogoFaltanCampos.show(getSupportFragmentManager(), "faltanCampos");
                }
                else{ //Comienza proceso registrar
                    if (!comprobarCorreo(correo)){ //Comprueba la estructura del correo
                        DialogFragment dialogoCorreoIncorrecto= DialogFalloRegistro.newInstance(getString(R.string.rgCorreoIncorrecto));
                        dialogoCorreoIncorrecto.show(getSupportFragmentManager(), "correoIncorrecto");
                    }
                    else {
                        if (password.length()<4){ //Clave demasiado corta
                            DialogFragment dialogoClaveIncorrecta= DialogFalloRegistro.newInstance(getString(R.string.rgLongitudInc));
                            dialogoClaveIncorrecta.show(getSupportFragmentManager(), "claveIncorrecta");
                        }
                        else{
                            gestionarInicioSesion(usuario,correo,nomCom,password);
                        }
                    }
                } //fin proceso registrar
            }
        });
    }

    private boolean comprobarCorreo(String elCorreo){
        //Compruebo que el correo electronico sea correcto
        Pattern pattern = Pattern
                .compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                        + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
        Matcher mather = pattern.matcher(elCorreo);
        if (mather.find()==true){ //El correo es correcto
           return true;
        }
        else{
            return false;
        }
    }
    protected void onRestoreInstanceState(Bundle savedInstanceState){ //Si roto la pantalla
        super.onRestoreInstanceState(savedInstanceState);
        //Si el idioma de la aplicacion es diferente al que deberia mostrarse lo cambio
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

    private void gestionarInicioSesion(String usuario, String correo, String nombrecompleto, String clave) {
        //Gestiono el registro del usuario en la base de datos externa
        Data datos = new Data.Builder()
                .putString("usuario",usuario)
                .putString("correo",correo)
                .putString("nombrecompleto",nombrecompleto)
                .putString("clave",clave)
                .build();

        OneTimeWorkRequest registerOtwr= new OneTimeWorkRequest.Builder(registroWS.class).setInputData(datos)
                .build();

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(registerOtwr.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        //Trato la respuesta, teniendo en cuenta que: -1 -> Error de BD; 0 -> Usuario y contraseña correctos, 1: Usuario no existe
                        if(workInfo != null && workInfo.getState().isFinished()){

                            String resultado=workInfo.getOutputData().getString("resultado");
                            if (resultado.equals("-1")){ //Fallo de BD
                                DialogFragment dialogoFalloBD= DialogFalloRegistro.newInstance(getString(R.string.falloBD));
                                dialogoFalloBD.show(getSupportFragmentManager(), "usuarioExiste");
                            }
                            else if (resultado.equals("0")){ //Registro correcto
                                DialogFragment dialogoFinRegistro= new DialogFinRegistro();
                                dialogoFinRegistro.show(getSupportFragmentManager(), "finRegistro");
                            }
                            else if (resultado.equals("1")){ //Usuario ya existe
                                DialogFragment dialogoExisteUsuario= DialogFalloRegistro.newInstance(getString(R.string.rgExisteUser));
                                dialogoExisteUsuario.show(getSupportFragmentManager(), "usuarioExiste");
                            }
                        }
                    }
                });
        WorkManager.getInstance(getApplicationContext()).enqueue(registerOtwr);
    }
}