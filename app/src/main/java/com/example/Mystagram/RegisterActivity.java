package com.example.Mystagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.Manifest;
import android.content.Context;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;

import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.Mystagram.Dialogs.DialogFalloRegistro;
import com.example.Mystagram.Dialogs.DialogFinRegistro;

import com.example.Mystagram.WS.registroWS;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private String latitud;
    private String longitud;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        obtenerGeolocalizacion(); //Obtengo la ubicacion
        Button buttonRegister= findViewById(R.id.buttonRegister);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText telefonoET=findViewById(R.id.rgTelefonoText);
                EditText nomComET=findViewById(R.id.rgNomComText);
                EditText usuarioET=findViewById(R.id.rgUsuarioText);
                EditText passwordET=findViewById(R.id.rgPasswordText);
                String telefono=telefonoET.getText().toString();
                String nomCom=nomComET.getText().toString();
                String usuario=usuarioET.getText().toString();
                String password=passwordET.getText().toString();
                boolean registrado=false;
                if (telefono.equals("") || nomCom.equals("") || usuario.equals("") || password.equals("")){ //Si alguno de los campos esta vacio
                    //Devuelvo dialog indicandolo
                    DialogFragment dialogoFaltanCampos= DialogFalloRegistro.newInstance(getString(R.string.rgFaltanCampos));
                    dialogoFaltanCampos.show(getSupportFragmentManager(), "faltanCampos");
                }
                else{ //Comienza proceso registrar
                    if (telefono.length()!=9){ //Si no tiene nueve digitos no es un telefono correcto
                        DialogFragment dialogoTelefonoIncorrecto= DialogFalloRegistro.newInstance(getString(R.string.rgTelefonoIncorrecto));
                        dialogoTelefonoIncorrecto.show(getSupportFragmentManager(), "telefonoIncorrecto");
                    }
                    else {
                        if (password.length()<4){ //Clave demasiado corta
                            DialogFragment dialogoClaveIncorrecta= DialogFalloRegistro.newInstance(getString(R.string.rgLongitudInc));
                            dialogoClaveIncorrecta.show(getSupportFragmentManager(), "claveIncorrecta");
                        }
                        else{
                            gestionarRegistro(usuario,telefono,nomCom,password);
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

    private void gestionarRegistro(String usuario, String telefono, String nombrecompleto, String clave) {
        //Gestiono el registro del usuario en la base de datos externa
        Data datos = new Data.Builder()
                .putString("usuario",usuario)
                .putString("telefono",telefono)
                .putString("nombrecompleto",nombrecompleto)
                .putString("clave",clave)
                .putString("latitud",latitud)
                .putString("longitud",longitud)
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

    private void obtenerGeolocalizacion(){
        longitud="Desconocida";
        latitud="Desconocida";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100); //Pido los permisos de acceder a la ubicacion
        } else {
            //Ya tiene permisos o android<=6

            FusedLocationProviderClient proveedordelocalizacion =
                    LocationServices.getFusedLocationProviderClient(this);
            proveedordelocalizacion.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                latitud = String.valueOf(location.getLatitude());
                                longitud = String.valueOf(location.getLongitude());
                            }
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            System.out.println("No puede conseguir la localizacion");
                        }
                    });
        }
    }
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        //Gestion de permisos
        if (requestCode == 100) { //Recojo la respuesta del permiso de geolocalizacion
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) { //Si se ha dado permiso
                obtenerGeolocalizacion();
            } else { //No se ha dado permiso, muestro un toast indicando que no se guardará la geolocalizacion
                Toast.makeText(this, getString(R.string.rgNoUbicacion), Toast.LENGTH_SHORT).show();
            }
        }
    }
}