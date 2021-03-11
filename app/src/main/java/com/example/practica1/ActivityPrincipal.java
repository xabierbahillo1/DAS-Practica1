package com.example.practica1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;

public class ActivityPrincipal extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        setSupportActionBar(findViewById(R.id.labarra)); //Incluyo la barra
        //Tratamiento de lista de fotos
        RecyclerView rv = (RecyclerView)findViewById(R.id.listaFotos);
        //Añado el layout
        LinearLayoutManager elLayoutLineal= new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        rv.setLayoutManager(elLayoutLineal);
        String[] personajes= {"Xabier","Prueba"};
        int[] nombres={R.drawable.eus_flag,R.drawable.ing_flag};
        FotoAdapter eladaptador = new FotoAdapter(nombres,personajes);
        rv.setAdapter(eladaptador);
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
}