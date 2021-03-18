package com.example.Mystagram;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.DialogFragment;
//import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Locale;

public class ActivityPrincipal extends AppCompatActivity implements DialogPreviewFoto.ListenerdelDialogo {
    private String usuario; //Usuario que ha iniciado sesion
    private String idioma; //Idioma de la aplicacion
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
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

    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        switch (id){
            case R.id.subirFoto:{
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery, 1); //Codigo 1 para recuperar la imagen
                break;
            }
            case R.id.preferencias:{ //Abre el activity para modificar las preferencias
                Intent i = new Intent(getApplicationContext(), PreferencesActivity.class);
                startActivity(i);
                break;
            }
            case R.id.exportarDatos:{
                exportarDatosATxt(); //Exporta los datos de los usuarios del sistema a un TXT
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1) { //Si se ha seleccionado correctamente una foto, se pregunta si realmente desea subirla.
            Uri imageUri = data.getData();
            String uriString=imageUri.toString();
            DialogFragment dialogoPreviewFoto= DialogPreviewFoto.newInstance(uriString);
            dialogoPreviewFoto.show(getSupportFragmentManager(), "previewFoto");
        }
    }

    public void tratarListaFotos() {
        //Obtiene las fotos subidas desde la base de datos

        RecyclerView rv = (RecyclerView)findViewById(R.id.listaFotos);
        //Obtengo las listas (usuarios: Lista con el nombre de usuario que ha subido la foto, fotos: Lista con las fotos subidas, codusuario= Usuario que ha subido la foto
        //idFotos Id de referencia a la foto
        miBD GestorDB = new miBD (getApplicationContext(), "MystragramDB", null, 1);
        SQLiteDatabase bd = GestorDB.getReadableDatabase();
        Cursor c = bd.rawQuery("SELECT a.NombreCompleto,b.img,a.Usuario,b.fotoid FROM Usuarios a, FotosUsuario b WHERE a.Usuario = b.usuario ORDER BY fotoid DESC", null); //Muestro primero las ultimas fotos subidas
        String[] usuarios=new String[c.getCount()];
        Bitmap[] fotos= new Bitmap[c.getCount()];
        String[] codusuario= new String[c.getCount()];
        int[] idFotos= new int[c.getCount()];
        int i=0;
        while (c.moveToNext()){
            String subido= c.getString(0); //Usuario que ha subido la foto
            byte[] blob= c.getBlob(1); //Foto
            String usuario= c.getString(2); //Codigo de usuario que ha subido la foto
            int idFoto= c.getInt(3); //Id de referencia a la foto
            //Convierto la foto en un bitmap para despues cargarlo en el ImageView
            ByteArrayInputStream bais = new ByteArrayInputStream(blob);
            Bitmap bitmap = BitmapFactory.decodeStream(bais);
            bitmap = Bitmap.createScaledBitmap(bitmap,bitmap.getWidth()/2,bitmap.getHeight()/2,true); //Reescalado para evitar consumir muchos recursos
            usuarios[i]=subido;
            fotos[i]=bitmap;
            codusuario[i]=usuario;
            idFotos[i]=idFoto;
            i++;
            if (i==10){ //Solo muestro las 10 últimas fotos subidas (para evitar consumir muchos recursos)
                break;
            }
        }
        //Cargo los datos en el adaptador del recyclerview
        FotoAdapter eladaptador = new FotoAdapter(this,usuarios,fotos,codusuario,idFotos,usuario);
        c.close();
        bd.close();
        rv.setAdapter(eladaptador);
    }
    public void subirFoto(Bitmap bitmap){
        //Sube un bitmap a BD
        //https://es.stackoverflow.com/questions/74332/guardar-imagenes-en-sqlite-android
        ByteArrayOutputStream baos = new ByteArrayOutputStream(6000);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 10 , baos);
        byte[] blob = baos.toByteArray();
        miBD GestorDB = new miBD (getApplicationContext(), "MystragramDB", null, 1);
        SQLiteDatabase bd = GestorDB.getWritableDatabase();
        ContentValues nuevo = new ContentValues();
        nuevo.put("usuario", usuario);
        nuevo.put("img",blob);
        bd.insert("FotosUsuario", null, nuevo);
        Log.d("subidaFoto","Foto subida correctamente");
        //Cierro la conexion a BD
        bd.close();
        tratarListaFotos(); //Actualizo el recyclerview
        lanzarNotificacionFotoSubida();//Lanzo una notificacion indicando que se ha subido la foto
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
        cambiarIdioma(idioma);
    }

    public void onResume() {

        super.onResume();

        //Si cambia el idioma recargo la pagina
        String nuevoIdioma=obtenerIdioma();
        if (!idioma.equals(nuevoIdioma)){
            cambiarIdioma(nuevoIdioma);
        }
        tratarListaFotos(); //Actualizo la lista de fotos
    }

    public String obtenerIdioma(){ //Obtiene el idioma actual que tiene la aplicacion en preferencias
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
        finish();
        startActivity(getIntent());
    }

    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem exportData = menu.findItem(R.id.exportarDatos);
        if(usuario.equals("admin")) //Si el usuario es el administrador muestro la opcion de exportar datos
        {
            exportData.setVisible(true);
        }
        else //Si no es administrador, oculto la opcion de exportar datos
        {
            exportData.setVisible(false);
        }
        return true;
    }

    private void exportarDatosATxt(){
        //Exporta los datos de los usuarios de la app a un TXT

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
    public void lanzarNotificacionFotoSubida(){
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
}
