package com.example.Mystagram;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.DialogFragment;

public class DialogFinRegistro extends DialogFragment {

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        lanzarNotificacionRegistro();
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
    public void lanzarNotificacionRegistro(){
        NotificationManager elManager = (NotificationManager)getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder elBuilder = new NotificationCompat.Builder(getContext(), "NotFotoSubida");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //Si version >= Android Oreo
            NotificationChannel elCanal = new NotificationChannel("NotFotoSubida", "NotificacionFotoubida",
                    NotificationManager.IMPORTANCE_DEFAULT);
            elManager.createNotificationChannel(elCanal);
        }
        elBuilder.setSmallIcon(android.R.drawable.ic_menu_agenda)
                .setContentTitle("Mystagram")
                .setContentText(getContext().getString(R.string.notificacionRegistro))
                .setVibrate(new long[]{0, 1000, 500, 1000})
                .setAutoCancel(true);
        elManager.notify(1, elBuilder.build());
    }
}
