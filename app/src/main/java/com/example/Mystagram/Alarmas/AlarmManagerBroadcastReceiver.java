package com.example.Mystagram.Alarmas;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.StrictMode;
import android.util.Log;
import android.widget.RemoteViews;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.Mystagram.MyWidget;
import com.example.Mystagram.R;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.HttpURLConnection;
import java.net.URL;


import javax.net.ssl.HttpsURLConnection;

public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.my_widget);
        Log.d("alarmaWidget","Se ejecuta actualizacion del widget en: "+System.currentTimeMillis());
        RequestQueue queue = Volley.newRequestQueue(context);
        if (android.os.Build.VERSION.SDK_INT > 9) //Permito descargas en primer plano
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        ComponentName appWidgetId = new ComponentName(context, MyWidget.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        String url = "http://ec2-54-167-31-169.compute-1.amazonaws.com/xbahillo001/WEB/obtenerImagenAleatoria.php";
        // Pido una respuesta en formato string de la url
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Tengo el JSON con la respuesta
                        try {
                            if (response.equals("-1")){
                                //Es error de BD, pongo valores por defecto
                                views.setTextViewText(R.id.textSubido, "Prueba");
                                Uri path = Uri.parse("android.resource://com.example.Mystagram/" + R.drawable.imagen1);
                                views.setImageViewUri(R.id.imagenWidget,path);
                                appWidgetManager.updateAppWidget(appWidgetId, views);
                            }
                            else {
                                JSONParser parser = new JSONParser();
                                JSONArray json = (JSONArray) parser.parse(response);
                                JSONObject dataJson = (JSONObject) json.get(0);
                                String usuario = (String) dataJson.get("Usuario"); ////Codigo de usuario que ha subido la foto
                                String fotoRuta = (String) dataJson.get("imgruta");
                                String direccion = "http://ec2-54-167-31-169.compute-1.amazonaws.com/xbahillo001/WEB/" + fotoRuta;
                                URL destino = new URL(direccion);
                                HttpURLConnection conn = (HttpURLConnection) destino.openConnection();
                                int responseCode = 0;
                                responseCode = conn.getResponseCode();
                                if (responseCode == HttpsURLConnection.HTTP_OK) {
                                    Bitmap elBitmap = BitmapFactory.decodeStream(conn.getInputStream()); //Obtengo la imagen
                                    elBitmap = Bitmap.createScaledBitmap(elBitmap, 1920/2, 1600/2, true); //Reescalado para evitar consumir muchos recursos o pasar tamaño maximo widget
                                    views.setTextViewText(R.id.textSubido, usuario);
                                    views.setImageViewBitmap(R.id.imagenWidget, elBitmap);
                                    appWidgetManager.updateAppWidget(appWidgetId, views);
                                }
                            }
                        }
                        catch (Exception e){
                            //Fallo al tratar el JSON con la respuesta o obtener la imagen, muestro imagen por defecto
                            views.setTextViewText(R.id.textSubido, "Prueba");
                            Uri path = Uri.parse("android.resource://com.example.Mystagram/" + R.drawable.imagen1);
                            views.setImageViewUri(R.id.imagenWidget,path);
                            appWidgetManager.updateAppWidget(appWidgetId, views);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Devuelve error, pongo imagen por defecto
                views.setTextViewText(R.id.textSubido, "Prueba");
                Uri path = Uri.parse("android.resource://com.example.Mystagram/" + R.drawable.imagen1);
                views.setImageViewUri(R.id.imagenWidget,path);
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        });

        //Añado la peticion
        queue.add(stringRequest);

    }
}
