package com.practica2.practica4wiidgetautomatico;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static int Count = 0;
    public static int update = 0;
    Button btn_save,btn_create;

    ImageView imgView;

    ListView lst_img;

    Thread hilo;
    Handler handler = new Handler();


    final static int REQUEST_IMG = 100;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imgView = findViewById(R.id.imageView);
        btn_save = findViewById(R.id.buttonSave);
        btn_create = findViewById(R.id.buttoncreate);

        lst_img = findViewById(R.id.ListImg);



        btn_create.setOnClickListener(v -> {
            AppWidgetManager widgetManager = getApplication().getSystemService(AppWidgetManager.class);
            ComponentName componentName = new ComponentName(getApplicationContext(),widget.class);
            if (widgetManager.isRequestPinAppWidgetSupported()){
                Count=0;
                my_thread();
                widgetManager.requestPinAppWidget(componentName,null,null);
            }
        });

        imgView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(intent,REQUEST_IMG);
        });

        //metodos iniciales
        refreshList();
    }





    //refresacr lista de las rutas
    public void refreshList(){
        SharedPreferences count_preference = getSharedPreferences("count",Context.MODE_PRIVATE);
        Count = Integer.parseInt(count_preference.getString("count","0"));
        String[] data = new String[Count];


        SharedPreferences sharedPreferences = getSharedPreferences("img",Context.MODE_PRIVATE);

        for (int i = 0; i < Count ;i++){
            data[i] = String.valueOf(i)+": "+ sharedPreferences.getString("img"+i,"");
        }

        ArrayAdapter<String> itemAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,data);
        lst_img.setAdapter(itemAdapter);
    }


    public void my_thread(){
        hilo = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void run() {
                while(true){

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(update==0){
                        handler.post(()->{
                            SharedPreferences sharedPreferences = getSharedPreferences("count",Context.MODE_PRIVATE);
                            int tope = Integer.parseInt(sharedPreferences.getString("count","0"));
                            if(Count<tope){
                                AppWidgetManager widgetManager = getApplicationContext().getSystemService(AppWidgetManager.class);
                                Intent intent = new Intent(getApplication(),widget.class);
                                intent.setAction(widgetManager.ACTION_APPWIDGET_UPDATE);
                                int[] ids = widgetManager.getAppWidgetIds(new ComponentName(getApplication(),widget.class));
                                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
                                sendBroadcast(intent);
                                Count++;
                            }else{
                                Count=0;
                            }
                        });
                    }

                }
            }
        });
        hilo.start();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK && requestCode == REQUEST_IMG){
            Uri uri = data.getData();
            String path_absolute = getRealPathFromURI(uri);
            Bitmap bitmap = dimencion(path_absolute);
            imgView.setImageBitmap(bitmap);
            SharedPreferences sharedPreferences = getSharedPreferences("img",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("img"+Count,path_absolute);
            editor.commit();
            Count++;

            SharedPreferences sharedPreferences1 = getSharedPreferences("count",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor1 = sharedPreferences1.edit();
            editor1.putString("count",String.valueOf(Count));
            editor1.commit();

            refreshList();
        }
        if (resultCode == RESULT_CANCELED) {
            //Write your code if there's no result
        }
    }







    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else
        {
            cursor.moveToFirst(); int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA); result = cursor.getString(idx); cursor.close();
        }
        return result;
    }

    public Bitmap dimencion(String path){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);
        int img_width=options.outWidth;
        int img_height = options.outHeight;
        int ratio;
        options.inJustDecodeBounds = false;
        if(img_width>img_height){
            ratio = img_width/250;
        }else{
            ratio = img_height/250;
        }
        options.inSampleSize = ratio;
        return BitmapFactory.decodeFile(path,options);
    }


}
