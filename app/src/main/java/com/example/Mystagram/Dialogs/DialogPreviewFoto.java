package com.example.Mystagram.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.Mystagram.R;

public class DialogPreviewFoto extends DialogFragment {
    ListenerdelDialogo miListener;
    public interface ListenerdelDialogo {
        void subirFoto(Bitmap bitmap);
    }

    private Uri imgUri;
    public DialogPreviewFoto(){
        super();
    }
    public static DialogPreviewFoto newInstance(String miUri) { //Metodo factoria para evitar fallo al rotar pantalla
        Bundle args = new Bundle();
        args.putString("miUri", miUri);
        DialogPreviewFoto f = new DialogPreviewFoto();
        f.setArguments(args);
        return f;
    }

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        miListener = (ListenerdelDialogo) getActivity();
        imgUri= Uri.parse(getArguments().getString("miUri"));//Cargo la uri

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        //Cargo la imagen a subir
        builder.setTitle(getString(R.string.pSubirFoto));
        LayoutInflater factory = LayoutInflater.from(getActivity().getApplicationContext());
        final View view = factory.inflate(R.layout.previewfoto, null);
        ImageView img= (ImageView)view.findViewById(R.id.previewImg);
        img.setImageURI(imgUri);
        Bitmap bitmap = ((BitmapDrawable)img.getDrawable()).getBitmap();
        builder.setView(view);

        //Metodos botones
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.pAccSubirFoto), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ImageView img= (ImageView)view.findViewById(R.id.previewImg);
                miListener.subirFoto(bitmap);
            }
        });
        builder.setNegativeButton(getString(R.string.pCancSubirFoto),new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        return builder.create();
    }



}
