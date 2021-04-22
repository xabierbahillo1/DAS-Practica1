package com.example.Mystagram.WS;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class registroWS extends Worker {
    public registroWS(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String direccion = "http://ec2-54-167-31-169.compute-1.amazonaws.com/xbahillo001/WEB/registrarUsuario.php";
        HttpURLConnection urlConnection = null;
        try {
            URL destino = new URL(direccion);
            urlConnection = (HttpURLConnection) destino.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
            //Paso los datos del usuario a registrar como parametro
            String usuario= getInputData().getString("usuario");
            String telefono= getInputData().getString("telefono");
            String nombrecompleto= getInputData().getString("nombrecompleto");
            String clave= getInputData().getString("clave");
            String latitud=getInputData().getString("latitud");
            String longitud=getInputData().getString("longitud");
            String parametros = "usuario="+usuario+"&telefono="+telefono+"&nombrecompleto="+nombrecompleto+"&clave="+clave+"&latitud="+latitud+"&longitud="+longitud;
            out.print(parametros);
            out.close();
            int statusCode = urlConnection.getResponseCode();
            if (statusCode == 200) { //Si 200 OK, recojo la respuesta del servidor
                BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String line, result = "";
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
                inputStream.close();
                Data resultados = new Data.Builder()
                        .putString("resultado",result)
                        .build();
                return Result.success(resultados);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
