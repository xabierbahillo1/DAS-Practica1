package com.example.Mystagram.WS;

import android.util.Log;


import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessageService extends FirebaseMessagingService {
    @Override
    public void onNewToken(String token) {
        //Si se genera un nuevo token
        Log.d("MensajesFirebase", "Nuevo token: " + token);
        //Guardo el token en la BD para poder enviar mensajes a este dispositivo
        guardarToken(token);
    }
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Si llega un mensaje mientras esta la app abierta

        // Si el mensaje es una notificacion
        if (remoteMessage.getNotification() != null) {
            Log.d("MensajesFirebase", "Cuerpo notificacion " + remoteMessage.getNotification().getBody());
        }


    }
    public void guardarToken(String token){
        Data datos = new Data.Builder()
                .putString("token",token)
                .build();
        OneTimeWorkRequest tokenOtwr= new OneTimeWorkRequest.Builder(guardaTokenWS.class).setInputData(datos)
                .build();
        WorkManager.getInstance(getApplicationContext()).enqueue(tokenOtwr);
    }
}
