package com.example.practica1;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DialogLoginNoExiste extends DialogFragment {
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.lgAlertTitle));
        builder.setMessage(getString(R.string.lgUsuNoExiste));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.rgAlertContinuar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setNegativeButton(getString(R.string.lgAlertRegistrar), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent= new Intent(getActivity(), RegisterActivity.class);
                startActivity(intent);
            }
        });
        return builder.create();
    }
}
