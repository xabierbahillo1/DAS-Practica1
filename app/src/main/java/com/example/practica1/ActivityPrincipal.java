package com.example.practica1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class ActivityPrincipal extends AppCompatActivity implements DialogPreviewFoto.ListenerdelDialogo {
    private String usuario; //Usuario que ha iniciado sesion
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        setSupportActionBar(findViewById(R.id.labarra)); //Incluyo la barra
        //Obtengo el usuario que ha iniciado sesion
        Bundle extras= getIntent().getExtras();
        if (extras!= null){
            usuario=extras.getString("usuario");
        }
        //Cargo el recyclerview con las fotos subidas
        RecyclerView rv = (RecyclerView)findViewById(R.id.listaFotos);
        LinearLayoutManager elLayoutLineal= new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        rv.setLayoutManager(elLayoutLineal);
        tratarListaFotos();

    }
    @Override

    //Metodos barra
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        switch (id){
            case R.id.subirFoto:{
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery, 1); //Codigo 1 para recuperar la imagen
            }
        }
        return super.onOptionsItemSelected(item);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1) { //Si se ha seleccionado correctamente una foto, se pregunta si realmente desea subirla.
            Uri imageUri = data.getData();
            String uriString=imageUri.toString();
            DialogFragment dialogoPreviewFoto= DialogPreviewFoto.newInstance(uriString);
            dialogoPreviewFoto.show(getSupportFragmentManager(), "previewFoto");
        }
    }

    public void tratarListaFotos() {
        //Obtiene las fotos subidas desde la base de datos

        RecyclerView rv = (RecyclerView)findViewById(R.id.listaFotos);
        //Obtengo las dos listas (usuarios: Lista con el usuario que ha subido la foto, fotos: Lista con las fotos subidas)
        miBD GestorDB = new miBD (getApplicationContext(), "MystragramDB", null, 1);
        SQLiteDatabase bd = GestorDB.getReadableDatabase();
        Cursor c = bd.rawQuery("SELECT a.NombreCompleto,b.img FROM Usuarios a, FotosUsuario b WHERE a.Usuario = b.usuario ORDER BY fotoid DESC", null); //Muestro primero las ultimas fotos subidas
        String[] usuarios=new String[c.getCount()];
        Bitmap[] fotos= new Bitmap[c.getCount()];
        int i=0;
        while (c.moveToNext()){
            String subido= c.getString(0); //Usuario que ha subido la foto
            byte[] blob= c.getBlob(1); //Foto
            //Convierto la foto en un bitmap para despues cargarlo en el ImageView
            ByteArrayInputStream bais = new ByteArrayInputStream(blob);
            Bitmap bitmap = BitmapFactory.decodeStream(bais);
            bitmap = Bitmap.createScaledBitmap(bitmap,bitmap.getWidth()/2,bitmap.getHeight()/2,true); //Reescalado para evitar consumir muchos recursos
            usuarios[i]=subido;
            fotos[i]=bitmap;
            i++;
            if (i==10){ //Solo muestro las 10 últimas fotos subidas (para evitar consumir muchos recursos)
                break;
            }
        }
        //Cargo los datos en el adaptador del recyclerview
        FotoAdapter eladaptador = new FotoAdapter(usuarios,fotos);
        c.close();
        bd.close();
        rv.setAdapter(eladaptador);
    }
    public void subirFoto(Bitmap bitmap){
        //Sube un bitmap a BD
        //https://es.stackoverflow.com/questions/74332/guardar-imagenes-en-sqlite-android
        ByteArrayOutputStream baos = new ByteArrayOutputStream(6000);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 10 , baos);
        byte[] blob = baos.toByteArray();
        miBD GestorDB = new miBD (getApplicationContext(), "MystragramDB", null, 1);
        SQLiteDatabase bd = GestorDB.getWritableDatabase();
        ContentValues nuevo = new ContentValues();
        nuevo.put("usuario", usuario);
        nuevo.put("img",blob);
        bd.insert("FotosUsuario", null, nuevo);
        Log.d("subidaFoto","Foto subida correctamente");
        //Cierro la conexion a BD
        bd.close();
        tratarListaFotos(); //Actualizo el recyclerview
    }

    protected void onSaveInstanceState(Bundle outState){ //Guardo los datos del usuario que ha iniciado sesion
        super.onSaveInstanceState(outState);
        outState.putString("usuario", usuario);
    }
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        usuario = savedInstanceState.getString("usuario");
    }
}
