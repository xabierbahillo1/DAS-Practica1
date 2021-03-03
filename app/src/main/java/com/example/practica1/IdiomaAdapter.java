package com.example.practica1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class IdiomaAdapter extends RecyclerView.Adapter<IdiomaHolder> {
        private int[] lasimagenes;
        public IdiomaAdapter (int[] imagenes)
        {
            lasimagenes=imagenes;
        }

        public IdiomaHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View elLayoutDeCadaItem= LayoutInflater.from(parent.getContext()).inflate(R.layout.idioma_card_layout,null);
            IdiomaHolder evh = new IdiomaHolder(elLayoutDeCadaItem);
            return evh;
        }

        @Override
        public void onBindViewHolder(@NonNull IdiomaHolder holder, int position) {
            holder.laimagen.setImageResource(lasimagenes[position]);
        }
        @Override
        public int getItemCount() {
            return lasimagenes.length;
        }
}

