package com.example.Mystagram.Dialogs;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.Mystagram.Alarmas.AlarmLanzaNotificacion;
import com.example.Mystagram.R;

public class DialogProgramarMensaje extends DialogFragment {

    public DialogProgramarMensaje(){
        super();
    }

    public static DialogProgramarMensaje newInstance() { //Metodo factoria para evitar fallo al rotar pantalla
        Bundle args = new Bundle();
        DialogProgramarMensaje f = new DialogProgramarMensaje();
        return f;
    }

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.programarNotiTxt));
        builder.setMessage(getString(R.string.programaDialogText));
        builder.setPositiveButton(getString(R.string.programUn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Selecciona solo una vez
                Intent intent = new Intent(getActivity(), AlarmLanzaNotificacion.class);
                PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager gestor= (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                gestor.setExact(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()+ 60000, pi); //Se lanza al minuto
                Toast.makeText(getContext(), getString(R.string.programaNoti), Toast.LENGTH_SHORT).show();
            }

        });

        builder.setNeutralButton(getString(R.string.pCancSubirFoto),new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        builder.setNegativeButton(getString(R.string.programRep),new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Selecciona repeticion
                Intent intent = new Intent(getActivity(), AlarmLanzaNotificacion.class);
                PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager gestor= (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                gestor.setRepeating(AlarmManager.ELAPSED_REALTIME,SystemClock.elapsedRealtime(),  60000, pi); //Se lanza cada minuto
                Toast.makeText(getContext(), getString(R.string.programaNotiRep), Toast.LENGTH_SHORT).show();
            }
        });
        return builder.create();
    }

}
