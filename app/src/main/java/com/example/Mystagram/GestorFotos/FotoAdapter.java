package com.example.Mystagram.GestorFotos;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.Mystagram.Dialogs.DialogAnadirContacto;
import com.example.Mystagram.R;
import com.example.Mystagram.WS.borrarImagenWS;
import com.example.Mystagram.WS.obtenerTelefonoWS;

public class FotoAdapter extends RecyclerView.Adapter<FotoHolder>{

        private Bitmap[] lasimagenes; //Bitmap con la imagen
        private String[] lassubido; // Nombre del usuario que subio la imagen
        private String[] codigosUsuarios; //Codigo del usuario que subio la imagen
        private int[] ids; //Id de la imagen para identificarla
        private String miUsuario; //Usuario que ha iniciado
        private Context context; //Contexto (para lanzar toast)
        private AppCompatActivity laActivity; //Actividad
        public FotoAdapter(String[] subido, Bitmap[] fotos,String[] usuarios, int[] idFotos, String elUsuario, AppCompatActivity activity)
        {
            //Guardo informacion de las imagenes
            lasimagenes=fotos;
            lassubido=subido;
            codigosUsuarios=usuarios;
            ids=idFotos;
            miUsuario=elUsuario;
            context=activity.getApplicationContext(); //Guardo el contexto para poder lanzar BD y Toast
            laActivity=activity; //Guardo la actividad para borrar de BD externa
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
            holder.itemView.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    String subidoPor=codigosUsuarios[position];

                    if (!miUsuario.equals(subidoPor)){ //Si el usuario de la foto no eres tu, se ofrece añadirlo a contactos
                        //Obtengo el nombre y telefono del contacto
                        String nombreSubido=lassubido[position];
                        Data datos = new Data.Builder()
                                .putString("usuario",String.valueOf(subidoPor))
                                .build();
                        OneTimeWorkRequest obtenerTelefonoOtwr= new OneTimeWorkRequest.Builder(obtenerTelefonoWS.class).setInputData(datos)
                                .build();
                        WorkManager.getInstance(context).getWorkInfoByIdLiveData(obtenerTelefonoOtwr.getId())
                                .observe(laActivity, new Observer<WorkInfo>() {
                                    @Override
                                    public void onChanged(WorkInfo workInfo) {
                                        if(workInfo != null && workInfo.getState().isFinished()){
                                            String resultado=workInfo.getOutputData().getString("resultado");
                                            if (resultado.equals("-1")){ //Fallo de BD
                                                Log.d("obtenerTelefono","Error obtener el telefono");
                                            }
                                            else if (resultado.length()==9){ //Es telefono
                                                String telefono=resultado;
                                                //Creo un dialog para informar si desea agregar al usuario
                                                DialogFragment dialogoAnadirContacto = DialogAnadirContacto.newInstance(nombreSubido,telefono); //Muestro en un dialog la foto a subir
                                                dialogoAnadirContacto.show(laActivity.getSupportFragmentManager(), "anadirContacto");
                                            }
                                        }
                                    }
                                });
                        WorkManager.getInstance(context).enqueue(obtenerTelefonoOtwr);
                    }

                }
            });
            //Defino accion de pulsación larga (borrar la imagen si el usuario que pulsa de forma larga es el que subio la foto)
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    String subidoPor=codigosUsuarios[position];
                    if (miUsuario.equals(subidoPor)){ //Si el usuario que ha iniciado sesion es el mismo que ha subido la foto
                        int idFoto=ids[position]; //Id de referencia a la foto
                        //Borro el elemento en BD
                        Data datos = new Data.Builder()
                                .putString("fotoid",String.valueOf(idFoto))
                                .build();
                        OneTimeWorkRequest borrarFotoOtwr= new OneTimeWorkRequest.Builder(borrarImagenWS.class).setInputData(datos)
                                .build();
                        WorkManager.getInstance(context).getWorkInfoByIdLiveData(borrarFotoOtwr.getId())
                                .observe(laActivity, new Observer<WorkInfo>() {
                                    @Override
                                    public void onChanged(WorkInfo workInfo) {
                                        if(workInfo != null && workInfo.getState().isFinished()){
                                            String resultado=workInfo.getOutputData().getString("resultado");
                                            if (resultado.equals("-1")){ //Fallo de BD
                                                Log.d("borradaFoto","Error al borrar la foto");
                                            }
                                            else if (resultado.equals("0")){ //Borrado correcto
                                                Log.d("borradaFoto","Foto borrada correctamente");
                                                //Borro el elemento en el recyclerview
                                                eliminarElementoEn(position);
                                                notifyItemRemoved(position);
                                                notifyItemRangeChanged(position, getItemCount());
                                                //Lanzo un toast informando de la accion
                                                Toast toastBorrado =
                                                        Toast.makeText(context,
                                                                context.getString((R.string.pToastBorrarFoto)), Toast.LENGTH_SHORT);

                                                Log.d("borradoFoto","Foto "+idFoto+ " borrada correctamente");
                                                toastBorrado.show();
                                            }
                                        }
                                    }
                                });
                        WorkManager.getInstance(context).enqueue(borrarFotoOtwr);
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

