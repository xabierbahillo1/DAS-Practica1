package com.example.practica1;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DialogFalloLogin extends DialogFragment {
    private String miMensaje;
    public DialogFalloLogin(String message){
        super();
        miMensaje=message;
    }

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.lgAlertTitle));
        builder.setMessage(miMensaje);
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.rgAlertContinuar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        return builder.create();
    }
}
