package com.example.Mystagram;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FotoHolder extends RecyclerView.ViewHolder {
        public ImageView laimagen;
        public TextView elTexto;
        public String[] subidos;
        public FotoHolder(@NonNull View itemView){
            super(itemView);
            laimagen=itemView.findViewById(R.id.cardImg);
            elTexto=itemView.findViewById(R.id.cardText);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { //Accion cuando haces clic
                    getAdapterPosition();

                }
            });
        }

}
