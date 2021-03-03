package com.example.practica1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Defino el RecyclerView para seleccionar el idioma de la aplicacion
        RecyclerView lalista= findViewById(R.id.listaIdiomas);
        int[] idiomas= {R.drawable.esp_flag,R.drawable.ing_flag,R.drawable.eus_flag};
        IdiomaAdapter eladaptador = new IdiomaAdapter(idiomas);
        lalista.setAdapter(eladaptador);
        LinearLayoutManager elLayoutLineal= new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false); //Para que se muestren las imagenes horizontalmente
        lalista.setLayoutManager(elLayoutLineal);


    }
}