package com.example.Mystagram;

import androidx.annotation.NonNull;

import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ActividadMapa extends FragmentActivity implements OnMapReadyCallback {
    private String resultado;
    private String latitud;
    private String longitud;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividad_mapa);
        obtenerGeolocalizacion(); //Obtengo la localizacion actual del usuario
        Bundle extras= getIntent().getExtras();
        if (extras!= null){
            resultado=extras.getString("datos");
        }

    }
    private void lanzarMapa(){
        SupportMapFragment elfragmento =

                (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentoMapa);



        elfragmento.getMapAsync(this);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (resultado!=null && !resultado.equals("")){ //Si tengo json con datos
            try {
                JSONParser parser = new JSONParser();
                JSONArray json = (JSONArray) parser.parse(resultado);
                String latitudU = null;
                String longitudU= null;
                for (int i=0;i<json.size();i++) { //Recorro el json
                    JSONObject dataJson= (JSONObject) json.get(i);
                    String usuario= (String) dataJson.get("usuario"); ////Codigo de usuario que ha subido la foto
                    latitudU=  (String)dataJson.get("latitud"); //Latitud del usuario
                    longitudU= (String)dataJson.get("longitud"); //Longitud del usuario
                    //Añado un marcador
                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(Double.parseDouble(latitudU), Double.parseDouble(longitudU)))
                            .title(usuario));

                }
                //Posiciono el mapa

                //Si se conoce la última ubicacion del usuario, posiciono el mapa en su ubicacion, si no, posiciono el mapa en la latitud y longitud del ultimo usuario recorrido
                if (latitud!= null && !latitud.equals("Desconocida") && longitud!=null && !longitud.equals("Desconocida")){ //Conozco longitud y latitud
                    CameraUpdate actualizar = CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(latitud), Double.parseDouble(longitud)),9);
                    googleMap.moveCamera(actualizar);
                }
                else{
                    CameraUpdate actualizar = CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(latitudU), Double.parseDouble(longitudU)),9);
                    googleMap.moveCamera(actualizar);
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

    } //fin onMapReady



    private void obtenerGeolocalizacion(){
        longitud="Desconocida";
        latitud="Desconocida";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100); //Pido los permisos de acceder a la ubicacion
        } else {
            //Ya tiene permisos o android<=6

            FusedLocationProviderClient proveedordelocalizacion =
                    LocationServices.getFusedLocationProviderClient(this);
            proveedordelocalizacion.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                latitud = String.valueOf(location.getLatitude());
                                longitud = String.valueOf(location.getLongitude());
                                lanzarMapa();
                            }
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            System.out.println("No puede conseguir la localizacion");
                            lanzarMapa();
                        }
                    });
        }
    }
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        //Gestion de permisos
        if (requestCode == 100) { //Recojo la respuesta del permiso de geolocalizacion
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) { //Si se ha dado permiso
                obtenerGeolocalizacion();
            } else { //No se ha dado permiso, muestro un toast indicando que no se guardará la geolocalizacion
                Toast.makeText(this, getString(R.string.rgNoUbicacion), Toast.LENGTH_SHORT).show();
                lanzarMapa();
            }
        }
    }
}