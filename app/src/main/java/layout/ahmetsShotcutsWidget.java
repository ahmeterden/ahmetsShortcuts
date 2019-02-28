package layout;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import android.media.AudioManager;
import android.widget.RemoteViews;

import com.ahmeterden.ahmet.ahmetsShortcuts.MainActivity;
import com.ahmeterden.ahmet.ahmetsShortcuts.R;

public class ahmetsShotcutsWidget extends AppWidgetProvider
{

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,int appWidgetId)
    {
        //CharSequence widgetText = context.getString(R.string.appwidget_text);

        //SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
        //String shortTimeStr = sdf.format(new Date());
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ahmets_shotcuts_widget);

        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int currentMediaVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        int currentRingerVolume = audio.getStreamVolume(AudioManager.STREAM_RING);
        int currentRingerMode = audio.getRingerMode();

        if(currentRingerMode == AudioManager.RINGER_MODE_NORMAL && currentRingerVolume == audio.getStreamMaxVolume(AudioManager.STREAM_RING))
        {
            //max
            views.setInt(R.id.btn_widget, "setBackgroundResource", R.drawable.ic_filter_vintage_black_24dp);
        }
        else if(currentRingerMode == AudioManager.RINGER_MODE_NORMAL && currentRingerVolume == 1)
        {
            //work
            views.setInt(R.id.btn_widget, "setBackgroundResource", R.drawable.ic_work_black_24dp);
        }
        else if(currentRingerMode == AudioManager.RINGER_MODE_VIBRATE && currentRingerVolume == 0)
        {
            //vibrate
            views.setInt(R.id.btn_widget, "setBackgroundResource", R.drawable.ic_vibration_black_24dp);
        }
        else if(currentRingerMode == AudioManager.RINGER_MODE_SILENT && currentRingerVolume == 0 && currentMediaVolume > 0)
        {
            //dead
            views.setInt(R.id.btn_widget, "setBackgroundResource", R.drawable.ic_do_not_disturb_black_24dp);
        }
        else if(currentRingerMode == AudioManager.RINGER_MODE_SILENT && currentRingerVolume == 0 && currentMediaVolume == 0)
        {
            //realDead
            views.setInt(R.id.btn_widget, "setBackgroundResource", R.drawable.ic_do_not_disturb_on_black_24dp);

        }

        //views.setTextViewText(R.id.appwidget_text, shortTimeStr);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds)
        {
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent openAppIntent = PendingIntent.getActivity(context, 0, intent, 0);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.ahmets_shotcuts_widget);
            views.setOnClickPendingIntent(R.id.btn_widget, openAppIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context)
    {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context)
    {
        // Enter relevant functionality for when the last widget is disabled
    }
}

