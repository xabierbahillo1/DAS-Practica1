package com.example.practica1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
}