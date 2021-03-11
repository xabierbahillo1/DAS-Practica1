package com.example.practica1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FotoAdapter extends RecyclerView.Adapter<FotoHolder> {
        private int[] lasimagenes;
        private String[] lassubido;
        public FotoAdapter(int[] imagenes, String[] subido)
        {
            lasimagenes=imagenes;
            lassubido=subido;
        }

        public FotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View elLayoutDeCadaItem= LayoutInflater.from(parent.getContext()).inflate(R.layout.foto_layout,null);
            FotoHolder evh = new FotoHolder(elLayoutDeCadaItem);
            evh.subidos=lassubido;
            return evh;
        }
        @Override
        public void onBindViewHolder(@NonNull FotoHolder holder, int position) {
            holder.laimagen.setImageResource(lasimagenes[position]);
            holder.elTexto.setText("Imagen subida por: "+lassubido[position]);
        }
        @Override
        public int getItemCount() {
            return lasimagenes.length;
        }
}

