package com.example.Mystagram.WS;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.net.HttpURLConnection;
import java.net.URL;

public class firebaseMensajeWS extends Worker {

    public firebaseMensajeWS(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String direccion = "http://ec2-54-167-31-169.compute-1.amazonaws.com/xbahillo001/WEB/enviarNotificacion.php";
        HttpURLConnection urlConnection = null;
        try {
            URL destino = new URL(direccion);
            urlConnection = (HttpURLConnection) destino.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            int statusCode = urlConnection.getResponseCode();
            if (statusCode == 200) {
                //BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                //BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                //String line, result = "";
                //while ((line = bufferedReader.readLine()) != null) {
                //    result += line;
                //}
                Log.d("MensajesFirebase", "Mensajes enviados a los dispositivos");
                return Result.success();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
