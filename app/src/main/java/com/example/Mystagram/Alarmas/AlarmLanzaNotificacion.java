package com.example.Mystagram.Alarmas;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.Observer;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.example.Mystagram.WS.firebaseMensajeWS;
import com.example.Mystagram.WS.subirImagenWS;

public class AlarmLanzaNotificacion extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmaMensaje","Se ha lanzado un mensaje por alarma");
        OneTimeWorkRequest lanzarNotiOtwr= new OneTimeWorkRequest.Builder(firebaseMensajeWS.class)
                .build();
        WorkManager.getInstance(context).enqueue(lanzarNotiOtwr);
    }
}
