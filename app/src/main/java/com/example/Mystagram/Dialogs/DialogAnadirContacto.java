package com.example.Mystagram.Dialogs;


import android.app.AlertDialog;
import android.app.Dialog;

import android.content.DialogInterface;

import android.os.Bundle;


import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.Mystagram.R;

public class DialogAnadirContacto extends DialogFragment {

    public DialogAnadirContacto(){
        super();
    }

    ListenerdelDialogo miListener;
    public interface ListenerdelDialogo {
        void anadirAContactos(String usuario,String telefono);
    }

    public static DialogAnadirContacto newInstance(String usuario,String telefono) { //Metodo factoria para evitar fallo al rotar pantalla
        Bundle args = new Bundle();
        args.putString("usuario", usuario);
        args.putString("telefono", telefono);
        DialogAnadirContacto f = new DialogAnadirContacto();
        f.setArguments(args);
        return f;
    }

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        miListener = (ListenerdelDialogo) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.anadirContactoTitle));
        builder.setMessage(getString(R.string.anadirContactoText1)+" "+getArguments().getString("usuario")+" "+getString(R.string.anadirContactoText2));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.anadirContactoOption1), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Añado a contactos
                miListener.anadirAContactos(getArguments().getString("usuario"),getArguments().getString("telefono"));
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
