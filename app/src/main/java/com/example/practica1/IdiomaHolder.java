package com.example.practica1;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class IdiomaHolder extends RecyclerView.ViewHolder {
        public ImageView laimagen;
        public String[] idiomas;
        public IdiomaHolder (@NonNull View itemView){
            super(itemView);
            laimagen=itemView.findViewById(R.id.idiomaImage);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    getAdapterPosition();
                    String idioma=idiomas[getAdapterPosition()];
                    System.out.println(idioma);
                }
            });
        }

}
