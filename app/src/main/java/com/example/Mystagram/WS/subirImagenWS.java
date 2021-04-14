package com.example.Mystagram.WS;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.Mystagram.Dialogs.DialogPreviewFoto;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class subirImagenWS extends Worker {
    public subirImagenWS(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }
    DialogPreviewFoto.ListenerdelDialogo miListener;
    public interface ListenerdelDialogo {
        void subirFoto(Uri miUri);
    }

    @NonNull
    @Override
    public Result doWork() {
        String direccion = "http://ec2-54-167-31-169.compute-1.amazonaws.com/xbahillo001/WEB/guardarImagen.php";
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
            String usuario=getInputData().getString("usuario");
            //Obtengo el blob a partir de la uri de la imagen para subirla al servidor
            Uri miUri= Uri.parse(getInputData().getString("uri"));
            Bitmap bitmap=MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(),miUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream(6000);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 10 , baos);
            byte[] blob = baos.toByteArray();
            String fotoen64= Base64.encodeToString(blob, Base64.DEFAULT);

            Uri.Builder builder = new Uri.Builder()
                    .appendQueryParameter("usuario", usuario)
                    .appendQueryParameter("imagen", fotoen64);
            String parametrosURL = builder.build().getEncodedQuery();
            out.print(parametrosURL);
            out.close();
            int statusCode = urlConnection.getResponseCode();
            if (statusCode == 200) { //Si 200 OK, recojo la respuesta del servidor
                BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String line, result = "";
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
                Log.d("subirFotos","Resultado subir foto: "+result);
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
