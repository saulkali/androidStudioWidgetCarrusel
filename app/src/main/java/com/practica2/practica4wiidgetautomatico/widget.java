package com.practica2.practica4wiidgetautomatico;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.RemoteViews;

public class widget extends AppWidgetProvider {


    final static String action = "stop";

    public void onUpdate(Context context, AppWidgetManager widgetManager, int[] widgetIds){
        for(int i =0 ; i < widgetIds.length;i++){
            int Id = widgetIds[i];


            //nuestro primer intent para abrir nuestra aplicacion movil
            Intent intent = new Intent(context, widget.class);
            intent.setAction(action);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            RemoteViews views = new RemoteViews(context.getPackageName(),R.layout.widgetview);
            views.setOnClickPendingIntent(R.id.img_widget,pendingIntent);

            views.setTextViewText(R.id.txt_ison,String.valueOf(MainActivity.update));

            if(MainActivity.update == 0){
                SharedPreferences sharedPreferences = context.getSharedPreferences("img",Context.MODE_PRIVATE);
                String Path = sharedPreferences.getString("img"+String.valueOf(MainActivity.Count),"");
                Bitmap bitmap = dimencion(Path);
                views.setImageViewBitmap(R.id.img_widget,bitmap);
            }
            widgetManager.updateAppWidget(Id,views);
        }
    }

    @Override
    public void onReceive(Context context,Intent intent){
        super.onReceive(context,intent);
        if(intent.getAction().equals("stop")){
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName componentName = new ComponentName(context.getPackageName(),getClass().getName());
            int[] appwidgetids = appWidgetManager.getAppWidgetIds(componentName);
            if(MainActivity.update==0){
                MainActivity.update=1;
            }else{
                MainActivity.update = 0;
            }
            onUpdate(context,appWidgetManager,appwidgetids);
        }

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
