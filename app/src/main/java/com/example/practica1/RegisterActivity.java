package com.example.practica1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
                    DialogFragment dialogoFaltanCampos= new DialogFalloRegistro(getString(R.string.rgFaltanCampos));
                    dialogoFaltanCampos.show(getSupportFragmentManager(), "faltanCampos");
                }
                else{ //Comienza proceso registrar
                    if (!comprobarCorreo(correo)){ //Comprueba la estructura del correo
                        DialogFragment dialogoCorreoIncorrecto= new DialogFalloRegistro(getString(R.string.rgCorreoIncorrecto));
                        dialogoCorreoIncorrecto.show(getSupportFragmentManager(), "correoIncorrecto");
                    }
                    else {
                        if (password.length()<4){ //Clave demasiado corta
                            DialogFragment dialogoClaveIncorrecta= new DialogFalloRegistro(getString(R.string.rgLongitudInc));
                            dialogoClaveIncorrecta.show(getSupportFragmentManager(), "claveIncorrecta");
                        }
                        else{
                            miBD GestorDB = new miBD (getApplicationContext(), "MystragramDB", null, 1);
                            SQLiteDatabase bd = GestorDB.getWritableDatabase();
                            Cursor c = bd.rawQuery("SELECT Usuario FROM Usuarios WHERE Usuario=\'"+usuario+"\'", null);
                            if (c.moveToFirst()){ //Si cursor no esta vacio, existe el usuario
                                DialogFragment dialogoExisteUsuario= new DialogFalloRegistro(getString(R.string.rgExisteUser));
                                dialogoExisteUsuario.show(getSupportFragmentManager(), "usuarioExiste");
                            }
                            else{ //No existe el usuario
                                //Hago el insert del usuario
                                ContentValues nuevo = new ContentValues();
                                nuevo.put("Usuario",usuario);
                                nuevo.put("Correo",correo);
                                nuevo.put("NombreCompleto",nomCom);
                                nuevo.put("Clave",password);
                                bd.insert("Usuarios",null,nuevo);
                                Log.d("registroUsuario","Usuario "+usuario+ "registrado correctamente");
                                registrado=true;
                            }
                            //Cierre de conexiones y curores
                            c.close();
                            bd.close();
                        }
                    }
                } //fin proceso registrar
                if (registrado){
                    DialogFragment dialogoFinRegistro= new DialogFinRegistro();
                    dialogoFinRegistro.show(getSupportFragmentManager(), "finRegistro");
                }
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
}