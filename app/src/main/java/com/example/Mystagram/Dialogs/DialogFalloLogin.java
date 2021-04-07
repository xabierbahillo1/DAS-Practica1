package com.example.Mystagram.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.Mystagram.R;

public class DialogFalloLogin extends DialogFragment {

    public DialogFalloLogin(){
        super();
    }

    public static DialogFalloLogin newInstance(String message) { //Metodo factoria para evitar fallo al rotar pantalla
        Bundle args = new Bundle();
        args.putString("message", message);
        DialogFalloLogin f = new DialogFalloLogin();
        f.setArguments(args);
        return f;
    }

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.lgAlertTitle));
        builder.setMessage(getArguments().getString("message"));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.rgAlertContinuar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        return builder.create();
    }
}
