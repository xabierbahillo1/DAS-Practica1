package com.example.Mystagram;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
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
import com.example.Mystagram.Alarmas.AlarmManagerBroadcastReceiver;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Implementation of App Widget functionality.
 */
public class MyWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
    @Override
    public void onEnabled(Context context) {
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 7475, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Log.d("alarmaWidget","Se ha inicializado la alarma");
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+ 1000 * 3, 60000 , pi);
    }
    public void onDisabled(Context context){
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 7475, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am.cancel(pi);
    }
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.my_widget);
        RequestQueue queue = Volley.newRequestQueue(context);
        if (android.os.Build.VERSION.SDK_INT > 9) //Permito descargas en primer plano
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

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
                                    elBitmap = Bitmap.createScaledBitmap(elBitmap,elBitmap.getWidth()/4,elBitmap.getHeight()/4,true); //Reescalado para evitar consumir muchos recursos
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