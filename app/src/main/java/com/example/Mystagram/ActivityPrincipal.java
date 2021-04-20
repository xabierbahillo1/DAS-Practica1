package com.example.Mystagram;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.os.StrictMode;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.Mystagram.Alarmas.AlarmLanzaNotificacion;
import com.example.Mystagram.Alarmas.AlarmManagerBroadcastReceiver;
import com.example.Mystagram.Dialogs.DialogPreviewFoto;
import com.example.Mystagram.Dialogs.DialogAnadirContacto;
import com.example.Mystagram.Dialogs.DialogProgramarMensaje;
import com.example.Mystagram.GestorBD.miBD;
import com.example.Mystagram.GestorFotos.FotoAdapter;
import com.example.Mystagram.WS.firebaseMensajeWS;
import com.example.Mystagram.WS.obtenerImagenWS;
import com.example.Mystagram.WS.subirImagenWS;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class ActivityPrincipal extends AppCompatActivity implements DialogPreviewFoto.ListenerdelDialogo, DialogAnadirContacto.ListenerdelDialogo{
    private String usuario; //Usuario que ha iniciado sesion
    private String idioma; //Idioma de la aplicacion

    private Uri photoURI; //Uri de la foto sacada por la camara

    private String contactUser; //Usuario a añadir a contactos
    private String telUser; //Telefono a añadir a contactos
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        if (android.os.Build.VERSION.SDK_INT > 9) { //Permito descargas en primer plano
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        setSupportActionBar(findViewById(R.id.labarra)); //Incluyo la barra
        //Obtengo el usuario que ha iniciado sesion
        Bundle extras= getIntent().getExtras();
        if (extras!= null){
            usuario=extras.getString("usuario");
        }
        //Obtengo el idioma actual de la aplicacion
        idioma=obtenerIdioma();
        //Cargo el recyclerview con las fotos subidas
        RecyclerView rv = (RecyclerView)findViewById(R.id.listaFotos);
        LinearLayoutManager elLayoutLineal= new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        rv.setLayoutManager(elLayoutLineal);
        tratarListaFotos();
    }
    @Override

    //Metodos barra
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) { //Metodos barra
        int id=item.getItemId();
        switch (id){
            case R.id.subirFoto:{ //Si ha pulsado subir foto
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery, 1); //Codigo 1 para recuperar la imagen
                break;
            }
            case R.id.preferencias:{ //Ha pulsado preferencias, Abre el activity para modificar las preferencias
                Intent i = new Intent(getApplicationContext(), PreferencesActivity.class);
                startActivity(i);
                break;
            }
            case R.id.sacarFoto:{ //Ha pulsado Sacar Foto
                Intent i= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (i.resolveActivity(getPackageManager()) != null) { //Evita fallo si no tiene ninguna app para lanzar la camara
                    File eldirectorio = this.getFilesDir(); //Obtengo el directorio de la aplicacion
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                    String nombrefichero = "IMG_" + timeStamp + "_";
                    File photoFile = new File(eldirectorio, nombrefichero + ".jpg");
                    // Si el fichero se ha creado correctamente
                    if (photoFile != null) {
                        photoURI = FileProvider.getUriForFile(this,
                                "com.example.android.fileprovider",
                                photoFile);
                        i.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(i, 2);
                    }
                }
                break;
            }
            case R.id.lanzarNotificacion:{ //Ha pulsado preferencias, Abre el activity para modificar las preferencias
                lanzarNotificacion();
                break;
            }
            case R.id.programarNotificacion:{
                DialogFragment dialogoPrograma= DialogProgramarMensaje.newInstance(); //Muestro en un dialog las opciones de programacion de mensaje
                dialogoPrograma.show(getSupportFragmentManager(), "programaMensaje");
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem lanzarNot = menu.findItem(R.id.lanzarNotificacion);
        MenuItem programNot = menu.findItem(R.id.programarNotificacion);
        if(usuario.equals("admin")) //Si el usuario es el administrador muestro la opcion de lanzar y programar notificacion
        {
            lanzarNot.setVisible(true);
            programNot.setVisible(true);
        }
        else //Si no es administrador, oculto la opcion de lanzar y programar notificacion
        {
            lanzarNot.setVisible(false);
            programNot.setVisible(false);
        }
        return true;
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Ha finalizado un ActivityForResult, recupero la informacion
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1) { //Si se ha seleccionado correctamente una foto, se pregunta si realmente desea subirla.
            Uri imageUri = data.getData();
            String uriString = imageUri.toString();
            DialogFragment dialogoPreviewFoto = DialogPreviewFoto.newInstance(uriString); //Muestro en un dialog la foto a subir
            dialogoPreviewFoto.show(getSupportFragmentManager(), "previewFoto");
        }
        if (resultCode == RESULT_OK && requestCode == 2){
            //Si se ha sacado correctamente una foto, se pregunta si realmente desea subirla
            String uriString = photoURI.toString();
            DialogFragment dialogoPreviewFoto = DialogPreviewFoto.newInstance(uriString); //Muestro en un dialog la foto a subir
            dialogoPreviewFoto.show(getSupportFragmentManager(), "previewFoto");
        }
    }

    public void tratarListaFotos() {
        //Obtiene las fotos subidas desde la base de datos
        AppCompatActivity myActivity= this; //Guardo la actividad para enviarla al FotoAdapter
        RecyclerView rv = (RecyclerView)findViewById(R.id.listaFotos);
        //Obtengo las listas (usuarios: Lista con el nombre de usuario que ha subido la foto, fotos: Lista con las fotos subidas, codusuario= Usuario que ha subido la foto
        //idFotos Id de referencia a la foto
        OneTimeWorkRequest obtenerFotosOtwr= new OneTimeWorkRequest.Builder(obtenerImagenWS.class)
                .build();
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(obtenerFotosOtwr.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        //Trato la respuesta
                        if(workInfo != null && workInfo.getState().isFinished()){
                            String resultado=workInfo.getOutputData().getString("resultado");
                            if (resultado.equals("-1") ){ //Fallo de BD
                                Log.d("obtenerFotos","Error al obtener imagenes del servidor");
                            }
                            else{ //Tengo el JSON con los datos

                                try {
                                    //Obtengo el JSON con los datos de las imagenes
                                    JSONParser parser = new JSONParser();
                                    JSONArray json = (JSONArray) parser.parse(resultado);
                                    //Inicializo arrays con datos de las imagenes para enviar al recyclerview
                                    String[] usuarios=new String[json.size()];
                                    Bitmap[] fotos= new Bitmap[json.size()];
                                    String[] codusuario= new String[json.size()];
                                    int[] idFotos= new int[json.size()];

                                    for (int i=0;i<json.size();i++){ //Recorro el json
                                        JSONObject dataJson= (JSONObject) json.get(i);
                                        String nombre= (String) dataJson.get("NombreCompleto"); //Usuario que ha subido la foto
                                        String usuario= (String) dataJson.get("Usuario"); ////Codigo de usuario que ha subido la foto
                                        String fotoid=  (String)dataJson.get("fotoid"); //Id de referencia a la foto
                                        String fotoRuta= (String)dataJson.get("imgruta");
                                        //Descargo la foto del servidor
                                        String direccion = "http://ec2-54-167-31-169.compute-1.amazonaws.com/xbahillo001/WEB/"+fotoRuta;
                                        URL destino = null;
                                        try {
                                            destino = new URL(direccion);
                                            HttpURLConnection conn = (HttpURLConnection) destino.openConnection();
                                            int responseCode = 0;
                                            responseCode = conn.getResponseCode();
                                            if (responseCode == HttpsURLConnection.HTTP_OK) {
                                               Bitmap elBitmap = BitmapFactory.decodeStream(conn.getInputStream()); //Obtengo la imagen
                                               elBitmap = Bitmap.createScaledBitmap(elBitmap,elBitmap.getWidth()/2,elBitmap.getHeight()/2,true); //Reescalado para evitar consumir muchos recursos
                                               usuarios[i]=nombre;
                                               fotos[i]=elBitmap;
                                               codusuario[i]=usuario;
                                               idFotos[i]=Integer.parseInt(fotoid); //Siempre es un numero
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } //Fin for recorre json
                                    //Cargo los datos en el adaptador del recyclerview
                                    FotoAdapter eladaptador = new FotoAdapter(usuarios,fotos,codusuario,idFotos,usuario,myActivity);
                                    rv.setAdapter(eladaptador);
                                } catch (ParseException e) {
                                    Log.d("obtenerFotos","Error al obtener el JSON con los datos de las imagenes");
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
        WorkManager.getInstance(getApplicationContext()).enqueue(obtenerFotosOtwr);
    }

    public void subirFoto(Uri imgUri){
        //Sube una foto al servidor
        Data datos = new Data.Builder()
                .putString("usuario",usuario)
                .putString("uri",imgUri.toString())
                .build();

        OneTimeWorkRequest subirFotoOtwr= new OneTimeWorkRequest.Builder(subirImagenWS.class).setInputData(datos)
                .build();

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(subirFotoOtwr.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        //Trato la respuesta, teniendo en cuenta que: -1 -> Error de BD; 0 -> Foto subida
                        if(workInfo != null && workInfo.getState().isFinished()){
                            String resultado=workInfo.getOutputData().getString("resultado");
                            if (resultado.equals("-1")){ //Fallo de BD
                                Log.d("subidaFoto","Error al subir la foto");
                            }
                            else if (resultado.equals("0")){ //Foto subida
                                Log.d("subidaFoto","Foto subida correctamente");
                                tratarListaFotos(); //Actualizo el recyclerview
                                lanzarNotificacionFotoSubida();//Lanzo una notificacion indicando que se ha subido la foto
                            }
                        }
                    }
                });
        WorkManager.getInstance(getApplicationContext()).enqueue(subirFotoOtwr);
    }

    protected void onSaveInstanceState(Bundle outState){ //Guardo los datos del usuario que ha iniciado sesion
        super.onSaveInstanceState(outState);
        outState.putString("usuario", usuario);
        outState.putString("idioma", idioma);
    }
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        usuario = savedInstanceState.getString("usuario");
        idioma = savedInstanceState.getString("idioma");
        SharedPreferences prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
        String miidioma=prefs.getString("idiomaApp","DEF"); //Si no hay ningun idioma devuelve def
        LocaleList locale=getBaseContext().getResources().getConfiguration().getLocales(); //Obtengo el idioma actual de la aplicacion
        String idiomaApp=locale.get(0).toString();
        if (idiomaApp.equals("es")){
            idiomaApp="ESP";
        }
        if (idiomaApp.equals("en")){
            idiomaApp="ENG";
        }
        if (idiomaApp.equals("en_GB")){
            idiomaApp="ENG";
        }
        if (!idiomaApp.equals(miidioma)){ //Si el idioma de la app no es el mismo que el de las preferencias lo cambio
            cambiarIdioma(miidioma);
            idioma=miidioma;
        }

    }


    protected void onResume() {

        super.onResume();

        //Si cambia el idioma recargo la pagina
        String nuevoIdioma=obtenerIdioma();
        if (!idioma.equals(nuevoIdioma)){
            recreate();
        }
    }

    private String obtenerIdioma(){ //Obtiene el idioma actual que tiene la aplicacion en preferencias
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getString("idiomaApp","DEF"); //Si no hay ningun idioma devuelve def
    }
    private void cambiarIdioma(String idioma){
        Locale nuevaloc= new Locale("es"); //Por defecto español
        if (idioma.equals("ENG")){ //Si el idioma es ingles
            nuevaloc = new Locale("en","GB");
        }
        Locale.setDefault(nuevaloc);
        Configuration configuration =
                getBaseContext().getResources().getConfiguration();
        configuration.setLocale(nuevaloc);
        configuration.setLayoutDirection(nuevaloc);
        Context context =
                getBaseContext().createConfigurationContext(configuration);
        getBaseContext().getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
        recreate();
    }

    private void exportarDatosATxt(){
        //Exporta los datos de los usuarios de la app a un TXT (BD LOCAL, ya no sirve, opcion desactivada)
        String texto="CodigoUsuario;CorreoElectronico;NombreCompleto;NumeroFotos"; //Escribo la cabecera del fichero
        //Obtengo los datos de los usuarios
        miBD GestorDB = new miBD (getApplicationContext(), "MystragramDB", null, 1);
        SQLiteDatabase bd = GestorDB.getReadableDatabase();
        Cursor c = bd.rawQuery("SELECT a.Usuario,a.Correo, a.NombreCompleto, IFNULL(b.numFotos,0) " +
                                    "FROM Usuarios a LEFT JOIN (SELECT b.usuario, COUNT(1) numFotos " +
                                                        "FROM FotosUsuario b GROUP BY b.usuario) b " +
                                    "ON a.Usuario = b.usuario " +
                                    "ORDER BY a.Usuario", null); //Obtengo los datos de todos los usuarios
        while (c.moveToNext()) {
            String codUsuario = c.getString(0); //Usuario
            String correoUsurio = c.getString(1); //Correo electronico del usuario
            String nombreusuario = c.getString(2); //NombreCompleto del usuario
            int numFotos = c.getInt(3); //Numero de fotos subidas por el usuario
            texto+="\n"+codUsuario+";"+correoUsurio+";"+nombreusuario+";"+numFotos;
        }
        //Escribo en el fichero
        try {
            OutputStreamWriter fichero = new OutputStreamWriter(openFileOutput("datosUsuarios.txt",
                    Context.MODE_PRIVATE));
            fichero.write(texto);
            fichero.close();
            //Escritura correcta, informo con un toast
            Toast toastOK =
                    Toast.makeText(this,
                            getString((R.string.exportarDatosOK)), Toast.LENGTH_SHORT);
            toastOK.show();
        } catch (IOException e){
            //Algun error, informo con un toast
            Toast toastError =
                    Toast.makeText(this,
                            getString((R.string.exportarDatosErr)), Toast.LENGTH_SHORT);

            toastError.show();
            }
    }
    private void lanzarNotificacionFotoSubida(){
        //Lanza la notificacion de foto subida
        NotificationManager elManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder elBuilder = new NotificationCompat.Builder(this, "NotFotoSubida");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //Si version >= Android Oreo
            NotificationChannel elCanal = new NotificationChannel("NotFotoSubida", "NotificacionFotoubida",
                    NotificationManager.IMPORTANCE_DEFAULT);
            elManager.createNotificationChannel(elCanal);
        }
        elBuilder.setSmallIcon(android.R.drawable.ic_menu_camera)
                .setContentTitle("Mystagram")
                .setContentText(getString(R.string.notificacionFotoSubidaTxt))
                .setVibrate(new long[]{0, 1000, 500, 1000})
                .setAutoCancel(true);
        elManager.notify(1, elBuilder.build());

    }

    @Override
    public void anadirAContactos(String usuario, String telefono) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && this.checkSelfPermission(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            //Añado los parametros para volver a hacer la llamada despues de pedir permisos
            contactUser=usuario;
            telUser=telefono;
            requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS}, 100); //Pido los permisos de escribir
        } else {
            // Ya tiene el permiso o es <=Android 6, añado a contactos

            //La forma recomendada por Android para añadir un contacto es mediante un proceso de lotes:
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME,null)
                    .build());
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, usuario)
                            .build());
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                             .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, telefono)
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                             .build());
            try {
                getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                Toast.makeText(getApplicationContext(), getString(R.string.anadirContactoOK), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), getString(R.string.anadirContactoFallo), Toast.LENGTH_SHORT).show();
                Log.e("Creacionusuario", "Error al crear un usuario: " + e);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        //Gestion de permisos
        if (requestCode == 100) { //Recojo la respuesta del permiso de escritura en contactos
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) { //Si se ha dado permiso
                anadirAContactos(contactUser,telUser);
            } else { //No se ha dado permiso, muestro un toast indicando que no se ha podido añadir el usuario
                Toast.makeText(this, getString(R.string.anadirContactoFallo), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void lanzarNotificacion(){
        OneTimeWorkRequest lanzarNotiOtwr= new OneTimeWorkRequest.Builder(firebaseMensajeWS.class)
                .build();
        WorkManager.getInstance(getApplicationContext()).enqueue(lanzarNotiOtwr);
        Toast.makeText(getApplicationContext(), getString(R.string.lanzadaNoti), Toast.LENGTH_SHORT).show();
    }

}


