package com.example.Mystagram.GestorFotos;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Mystagram.R;
import com.example.Mystagram.GestorBD.miBD;

public class FotoAdapter extends RecyclerView.Adapter<FotoHolder>{

        private Bitmap[] lasimagenes; //Bitmap con la imagen
        private String[] lassubido; // Nombre del usuario que subio la imagen
        private String[] codigosUsuarios; //Codigo del usuario que subio la imagen
        private int[] ids; //Id de la imagen para identificarla
        private String miUsuario; //Usuario que ha iniciado
        private Context context; //Contexto (para lanzar toast)

        public FotoAdapter(Context appContext, String[] subido, Bitmap[] fotos,String[] usuarios, int[] idFotos, String elUsuario)
        {
            //Guardo informacion de las imagenes
            lasimagenes=fotos;
            lassubido=subido;
            codigosUsuarios=usuarios;
            ids=idFotos;
            miUsuario=elUsuario;
            context=appContext; //Guardo el contexto para poder lanzar BD y Toast
        }

        public FotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View elLayoutDeCadaItem= LayoutInflater.from(parent.getContext()).inflate(R.layout.foto_layout,null);
            FotoHolder evh = new FotoHolder(elLayoutDeCadaItem);
            return evh;
        }
        @Override
        public void onBindViewHolder(@NonNull FotoHolder holder, int position) {
            //Muestro la imagen y texto correspondientes
            holder.laimagen.setImageBitmap(lasimagenes[position]);
            String texto=" "+lassubido[position];
            //Obtengo las preferencias para saber si hay que mostrar el nombre de usuario o no
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            Boolean mostrarUsuario = prefs.getBoolean("mostrarUsuarios", false);
            if (mostrarUsuario){ //Se muestra el nombre de usuario entre parentesis
                texto+=" ("+codigosUsuarios[position]+")";
            }
            holder.elTexto.setText(texto);
            //Defino accion de pulsación larga (borrar la imagen si el usuario que pulsa de forma larga es el que subio la foto)
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    String subidoPor=codigosUsuarios[position];
                    if (miUsuario.equals(subidoPor)){ //Si el usuario que ha iniciado sesion es el mismo que ha subido la foto
                        int idFoto=ids[position]; //Id de referencia a la foto
                        //Borro el elemento en BD
                        miBD GestorDB = new miBD (context, "MystragramDB", null, 1);
                        SQLiteDatabase bd = GestorDB.getWritableDatabase();
                        bd.delete("FotosUsuario","fotoid=?",new String[]{Integer.toString(idFoto)});
                        bd.close();
                        //Borro el elemento en el recyclerview
                        eliminarElementoEn(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, getItemCount());
                        //Lanzo un toast informando de la accion
                        Toast toastBorrado =
                                Toast.makeText(context,
                                        context.getString((R.string.pToastBorrarFoto)), Toast.LENGTH_SHORT);

                        Log.d("borradoFoto","Foto "+idFoto+ "borrada correctamente");
                        toastBorrado.show();
                    }
                    return false;
                }
            });
        }
        @Override
        public int getItemCount() {
            return lasimagenes.length;
        }
        private void eliminarElementoEn(int pos){
            //Metodo que elimina el elemento en la posicion i
            Bitmap[] nuevasimagenes= new Bitmap[lasimagenes.length-1];
            String[] nuevalassubido= new String[lassubido.length-1];
            String[] nuevocodigosUsuarios= new String[codigosUsuarios.length-1];
            int[] nuevosIds= new int[ids.length-1];
            int posInsert=0;
            for (int i=0;i<lasimagenes.length;i++){
                if (i!=pos){
                    nuevasimagenes[posInsert]=lasimagenes[i];
                    nuevalassubido[posInsert]=lassubido[i];
                    nuevocodigosUsuarios[posInsert]=codigosUsuarios[i];
                    nuevosIds[posInsert]=ids[i];
                    posInsert++;
                }
            }
            lasimagenes=nuevasimagenes;
            lassubido=nuevalassubido;
            codigosUsuarios=nuevocodigosUsuarios;
            ids=nuevosIds;
        }
}

