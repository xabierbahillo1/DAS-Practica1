package com.example.Mystagram;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DialogFinRegistro extends DialogFragment {

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.rgFinRgTitle));
        builder.setMessage(getString(R.string.rgFinMsg));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.rgFinAction), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getActivity().finish();
            }
        });
        return builder.create();
    }
}
