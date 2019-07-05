package com.ahmeterden.ahmet.ahmetsShortcuts;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Instrumentation;
import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.Toast;
import android.location.LocationManager;
import android.content.Context;
import java.io.*;
import java.lang.reflect.Method;

import layout.ahmetsShotcutsWidget;

public class MainActivity extends AppCompatActivity {

    boolean flashState;
    boolean mobileDataEnabled = false;
    boolean isWifiAPenabled = false;
    boolean hideOnToggle = false;
    boolean hideOnProfileActivation = true;


    CountDownTimer myCountDownTimer = new CountDownTimer(8640000 * 1000, 500) //100 gün boyunca 0,5 saniyede bir yap
    {
        public void onTick(long millisUntilFinished)
        {
            refreshScreen();
            //Toast.makeText(MainActivity.this, "onTick " + tempSayi, Toast.LENGTH_SHORT).show();
            //tempSayi++;
        }
        public  void onFinish()
        {
            //Toast.makeText(MainActivity.this, "onFinish " + tempSayi, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //nougat ve üzerinde ringer sesi ayarlamak için bu izin şart
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !notificationManager.isNotificationPolicyAccessGranted())
        {
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }

        //android.permission.WRITE_SETTINGS olayına çözüm
        if (!Settings.System.canWrite(getApplicationContext()))
        {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 200);
        }
        //android.permission.GET_ACCOUNTS olayına çözüm
        final int REQUEST_CODE_ASK_PERMISSIONS = 123;
        int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.GET_ACCOUNTS);
        if (hasWriteContactsPermission != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[] {Manifest.permission.GET_ACCOUNTS},REQUEST_CODE_ASK_PERMISSIONS);
        }


        //BURADA TUŞLARIN LONG CLİCK İLE SETTİNGS'DEKİ YERİNE AÇMA OLAYINI YAP
        Button buttonWifi = (Button) findViewById(R.id.btn_wifi);
        Button buttonData = (Button) findViewById(R.id.btn_data);
        Button buttonGps = (Button) findViewById(R.id.btn_gps);
        Button buttonBluetooth = (Button) findViewById(R.id.btn_bluetooth);
        Button buttonHostSpot = (Button) findViewById(R.id.btn_hotspot);
        Button buttonAutoBrightness = (Button) findViewById(R.id.btn_autoBrightness);

        buttonWifi.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                return true;
            }
        });
        buttonData.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS));
                return true;
            }
        });
        buttonGps.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                return true;
            }
        });
        buttonBluetooth.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                return true;
            }
        });
        buttonHostSpot.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent tetherSettings = new Intent();
                tetherSettings.setClassName("com.android.settings", "com.android.settings.TetherSettings");
                startActivity(tetherSettings);
                return true;
            }
        });
        buttonAutoBrightness.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startActivity(new Intent(Settings.ACTION_DISPLAY_SETTINGS));
                return true;
            }
        });


        final SeekBar sk=(SeekBar) findViewById(R.id.seekBar_autoBrightness);
        sk.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Toast.makeText(getApplicationContext(), String.valueOf(progress),Toast.LENGTH_SHORT).show();

                int brightnessmode = 0;
                try
                {
                    brightnessmode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);

                    if( brightnessmode == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
                    {
                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, sk.getProgress());
                    }
                    else
                    {
                        float value = (((float)sk.getProgress()*2)/256) - 1.0f;
                        //Button buttonCalc = (Button) findViewById(R.id.btn_calculator);buttonCalc.setText(String.valueOf(value));
                        //Settings.System.putFloat(getContentResolver(), "screen_auto_brightness_adj", 0.0f);

                        Process su = Runtime.getRuntime().exec("su");
                        DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
                        outputStream.writeBytes("settings put system screen_auto_brightness_adj " + value  + "\n");
                        outputStream.flush();
                        outputStream.writeBytes("exit\n");
                        outputStream.flush();
                        su.waitFor();
                    }
                }
                catch (Exception e)
                {
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                // TODO Auto-generated method stub
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();

        //Toast.makeText(MainActivity.this, "onResume'e girdi", Toast.LENGTH_SHORT).show();

        myCountDownTimer.start();

        refreshScreen();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        //Toast.makeText(MainActivity.this, "onPause'a girdi", Toast.LENGTH_SHORT).show();

        myCountDownTimer.cancel();

        //refreshScreen();
    }

    public void refreshScreen()
    {
        //burada GPS'in durumunu bul ve ona göre tuşun rengini değiş
        Button gpsButton = (Button)MainActivity.this.findViewById(R.id.btn_gps);
        LocationManager locationManager = (LocationManager)getApplicationContext().getSystemService(getApplicationContext().LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == true)
        {
            gpsButton.setBackgroundTintList(getColorStateList(android.R.color.holo_green_light));
        }
        else
        {
            gpsButton.setBackgroundTintList(getColorStateList(android.R.color.holo_red_light));
        }

        //burada bluetooth durum kontrol et
        Button bluetoothButton = (Button)MainActivity.this.findViewById(R.id.btn_bluetooth);
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null)
        {
            bluetoothButton.setEnabled(false);
        }
        else
        {
            if (mBluetoothAdapter.isEnabled())
            {
                //bluetoothButton.setBackgroundColor(getColor(android.R.color.holo_green_light));
                bluetoothButton.setBackgroundTintList(getColorStateList(android.R.color.holo_green_light));

            }
            else
            {
                bluetoothButton.setBackgroundTintList(getColorStateList(android.R.color.holo_red_light));
                //bluetoothButton.setBackgroundColor(getColor(android.R.color.holo_red_light));
            }
        }

        //burada wifi durum kontrol et
        Button wifiButton = (Button)MainActivity.this.findViewById(R.id.btn_wifi);
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled())
        {
            wifiButton.setBackgroundTintList(getColorStateList(android.R.color.holo_green_light));
        }
        else
        {
            wifiButton.setBackgroundTintList(getColorStateList(android.R.color.holo_red_light));
        }


        //burada data durum kontrol et
        Button dataButton = (Button)MainActivity.this.findViewById(R.id.btn_data);
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Class cmClass = Class.forName(cm.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true); // Make the method callable
            // get the setting for "mobile data"
            mobileDataEnabled = (Boolean)method.invoke(cm);
        } catch (Exception e) {
            // Some problem accessible private API
            // TODO do whatever error handling you want here
        }
        if ( mobileDataEnabled == true)
        {
            dataButton.setBackgroundTintList(getColorStateList(android.R.color.holo_green_light));
        }
        else
        {
            dataButton.setBackgroundTintList(getColorStateList(android.R.color.holo_red_light));
        }

        //burada rotation durum kontrol et
        Button orientationButton = (Button)MainActivity.this.findViewById(R.id.btn_orientation);
        if( Settings.System.getInt(getContentResolver(),Settings.System.ACCELEROMETER_ROTATION, 0) == 1 )
        {
            orientationButton.setBackgroundTintList(getColorStateList(android.R.color.holo_green_light));
        }
        else
        {
            orientationButton.setBackgroundTintList(getColorStateList(android.R.color.holo_red_light));
        }

        //burada hotspot durum kontrol et
        Button hotspotButton = (Button)MainActivity.this.findViewById(R.id.btn_hotspot);
        Method[] wmMethods = wifi.getClass().getDeclaredMethods();
        for (Method method: wmMethods)
        {
            if (method.getName().equals("isWifiApEnabled"))
            {
                try
                {
                    isWifiAPenabled = (boolean) method.invoke(wifi);
                }
                catch (Exception e)
                {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
        if( isWifiAPenabled == true )
        {
            hotspotButton.setBackgroundTintList(getColorStateList(android.R.color.holo_green_light));
        }
        else
        {
            hotspotButton.setBackgroundTintList(getColorStateList(android.R.color.holo_red_light));
        }


        //burada torch durum kontrol et
        //BU BENCE İSTEDİĞİM ŞEY DEĞİL; ÇÜNKÜ BU YAPI İLE SADECE DEĞİŞİM OLURSA BEN DURUMU ÖĞRENEBİLİYORUM, AMA DEĞİŞİM OLMASA DA BİLMEM LAZIM
        CameraManager manager = (CameraManager) getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
        manager.registerTorchCallback(torchCallback, null);


        //burada AutoBrightness'ın durumunu bul ve ona göre tuşun rengini değiş
        Button autoBrightnessButton = (Button)MainActivity.this.findViewById(R.id.btn_autoBrightness);
        int brightnessmode = 0;
        try
        {
            brightnessmode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
        }
        catch (Settings.SettingNotFoundException e)
        {
        }
        //burada seekBar'ın rakamını ayarla
        int brightness = 0; //auto brigtness açıksa her durumda "autobrightness önceki son değer" gelir, onun harici 1 ve 255 arası (dahiller) gelir cevap,
        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar_autoBrightness);
        try
        {
            if( brightnessmode == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
            {
                autoBrightnessButton.setBackgroundTintList(getColorStateList(android.R.color.holo_red_light));
                brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            }
            else
            {
                autoBrightnessButton.setBackgroundTintList(getColorStateList(android.R.color.holo_green_light));
                brightness = (int)(((float)(Settings.System.getFloat(getContentResolver(), "screen_auto_brightness_adj") + 1.0f)/2)*256);

                if(brightness == 0)
                    brightness = 1;
                else if(brightness == 256)
                    brightness = 255;
            }
        }
        catch (Settings.SettingNotFoundException e)
        {
        }
        seekBar.setProgress(brightness);


        //burada ringer vs durum kontrol et
        Button ring_maxButton = (Button)MainActivity.this.findViewById(R.id.btn_ring_max);
        Button ring_workButton = (Button)MainActivity.this.findViewById(R.id.btn_ring_work);
        Button ring_vibrateButton = (Button)MainActivity.this.findViewById(R.id.btn_ring_vibrate);
        Button ring_deadButton = (Button)MainActivity.this.findViewById(R.id.btn_ring_dead);
        Button ring_realDeadButton = (Button)MainActivity.this.findViewById(R.id.btn_ring_realdead);
        //önce hepsini kırmızı yap
        ring_maxButton.setBackgroundTintList(getColorStateList(android.R.color.holo_red_light));
        ring_workButton.setBackgroundTintList(getColorStateList(android.R.color.holo_red_light));
        ring_vibrateButton.setBackgroundTintList(getColorStateList(android.R.color.holo_red_light));
        ring_deadButton.setBackgroundTintList(getColorStateList(android.R.color.holo_red_light));
        ring_realDeadButton.setBackgroundTintList(getColorStateList(android.R.color.holo_red_light));

        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int currentMediaVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        int currentRingerVolume = audio.getStreamVolume(AudioManager.STREAM_RING);
        int currentRingerMode = audio.getRingerMode();

        if(currentRingerMode == AudioManager.RINGER_MODE_NORMAL && currentRingerVolume == audio.getStreamMaxVolume(AudioManager.STREAM_RING))
        {
            //max
            ring_maxButton.setBackgroundTintList(getColorStateList(android.R.color.holo_green_light));
            updateWidget(R.drawable.ic_filter_vintage_black_24dp);
        }
        else if(currentRingerMode == AudioManager.RINGER_MODE_NORMAL && currentRingerVolume == 1)
        {
            //work
            ring_workButton.setBackgroundTintList(getColorStateList(android.R.color.holo_green_light));
            updateWidget(R.drawable.ic_work_black_24dp);
        }
        else if(currentRingerMode == AudioManager.RINGER_MODE_VIBRATE && currentRingerVolume == 0)
        {
            //vibrate
            ring_vibrateButton.setBackgroundTintList(getColorStateList(android.R.color.holo_green_light));
            updateWidget(R.drawable.ic_vibration_black_24dp);
        }
        else if(currentRingerMode == AudioManager.RINGER_MODE_SILENT && currentRingerVolume == 0 && currentMediaVolume > 0)
        {
            //dead
            ring_deadButton.setBackgroundTintList(getColorStateList(android.R.color.holo_green_light));
            updateWidget(R.drawable.ic_do_not_disturb_black_24dp);
        }
        else if(currentRingerMode == AudioManager.RINGER_MODE_SILENT && currentRingerVolume == 0 && currentMediaVolume == 0)
        {
            //realDead
            ring_realDeadButton.setBackgroundTintList(getColorStateList(android.R.color.holo_green_light));
            updateWidget(R.drawable.ic_do_not_disturb_on_black_24dp);
        }
    }

    CameraManager.TorchCallback torchCallback = new CameraManager.TorchCallback()
    {
        @Override
        public void onTorchModeUnavailable(String cameraId)
        {
            super.onTorchModeUnavailable(cameraId);
        }

        @Override
        public void onTorchModeChanged(String cameraId, boolean enabled)
        {
            super.onTorchModeChanged(cameraId, enabled);
            flashState = enabled;
            Button torchButton = (Button)MainActivity.this.findViewById(R.id.btn_torch);
            if( flashState == true )
            {
                torchButton.setBackgroundTintList(getColorStateList(android.R.color.holo_green_light));
            }
            else
            {
                torchButton.setBackgroundTintList(getColorStateList(android.R.color.holo_red_light));
            }
        }
    };

    public void updateWidget(int durum)
    {
        Context context = this;
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.ahmets_shotcuts_widget);
        ComponentName thisWidget = new ComponentName(context, ahmetsShotcutsWidget.class);

        remoteViews.setInt(R.id.btn_widget, "setBackgroundResource", durum);

        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
    }



    public void toggleRingRealDead(View view)
    {
        changeRingerMode("RealDead");

        //changeInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY);
        minimizeApp("forProfileActivation");
        updateWidget(R.drawable.ic_do_not_disturb_on_black_24dp);
    }
    public void toggleRingDead(View view)
    {
        changeRingerMode("Dead");

        //changeInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY);
        minimizeApp("forProfileActivation");
        updateWidget(R.drawable.ic_do_not_disturb_black_24dp);
    }
    public void toggleRingVibrate(View view)
    {
        changeRingerMode("Vibrate");

        //changeInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
        minimizeApp("forProfileActivation");
        updateWidget(R.drawable.ic_vibration_black_24dp);
    }

    public void toggleRingWork(View view)
    {
        changeRingerMode("Work");

        //changeInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
        minimizeApp("forProfileActivation");
        updateWidget(R.drawable.ic_work_black_24dp);
    }
    public void toggleRingMax(View view)
    {
        changeRingerMode("Max");

        //changeInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
        minimizeApp("forProfileActivation");
        updateWidget(R.drawable.ic_filter_vintage_black_24dp);
    }

    private void changeRingerMode(String myMode)
    {
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int previousMediaVolume = 0;

        switch (myMode)
        {
            case "RealDead":
                audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);//set media volume to 0
                break;
            case "Dead":
                previousMediaVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
                audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, previousMediaVolume, 0);
                break;
            case "Vibrate":
                previousMediaVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
                audio.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, previousMediaVolume, 0);
                break;
            case "Work":
                audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                audio.setStreamVolume(AudioManager.STREAM_RING, 1, 0);
                break;
            case "Max":
                audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                audio.setStreamVolume(AudioManager.STREAM_RING, audio.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
                break;
            default:
                Toast.makeText(MainActivity.this, myMode, Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void changeInterruptionFilter(int interruptionFilter)
    {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.setInterruptionFilter(interruptionFilter);
    }

    private void minimizeApp(String forWhat)
    {
        switch (forWhat)
        {
            case "forProfileActivation":
                if(hideOnProfileActivation)
                {
                    MainActivity.this.finish(); //programı kapat et
                    //MainActivity.this.moveTaskToBack(true); //programı minimize et
                }
                break;
            case "forToggle":
                if(hideOnToggle)
                {
                    MainActivity.this.finish(); //programı kapat et
                    //MainActivity.this.moveTaskToBack(true); //programı minimize et
                }
                break;
        }
    }

    public void myTestAction(View view)
    {
        String resultAsString = "";

        int myCurrentInterruptionFilter = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).getCurrentInterruptionFilter();//get current DND mode
        int myRingerMode = ((AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE)).getRingerMode(); //get current ringer mode
        int myVibrateWhenRingingSetting = Settings.System.getInt(getContentResolver(), Settings.System.VIBRATE_WHEN_RINGING, 0);

        switch (myCurrentInterruptionFilter)
        {
            case NotificationManager.INTERRUPTION_FILTER_UNKNOWN:
                resultAsString += "INTERRUPTION_FILTER is UNKNOWN";
                break;
            case NotificationManager.INTERRUPTION_FILTER_ALL:
                resultAsString += "INTERRUPTION_FILTER is ALL";
                break;
            case NotificationManager.INTERRUPTION_FILTER_PRIORITY:
                resultAsString += "INTERRUPTION_FILTER is PRIORITY";
                break;
            case NotificationManager.INTERRUPTION_FILTER_NONE:
                resultAsString += "INTERRUPTION_FILTER is NONE";
                break;
            case NotificationManager.INTERRUPTION_FILTER_ALARMS:
                resultAsString += "INTERRUPTION_FILTER is ALARMS";
                break;
            default:
                resultAsString += "INTERRUPTION_FILTER is(" + myCurrentInterruptionFilter + ")";
                break;
        }

        switch (myRingerMode)
        {
            case AudioManager.RINGER_MODE_SILENT:
                resultAsString += "\nRINGER_MODE is SILENT";
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                resultAsString += "\nRINGER_MODE is VIBRATE";
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                resultAsString += "\nRINGER_MODE is NORMAL";
                break;
            default:
                resultAsString += "\nRINGER_MODE is UNKNOWN(" + myRingerMode + ")";
                break;
        }

        switch (myVibrateWhenRingingSetting)
        {
            case 1:
                resultAsString += "\nVIBRATE_WHEN_RINGING is ON";
                break;
            case 0:
                resultAsString += "\nVIBRATE_WHEN_RINGING is ON";
                break;
            default:
                resultAsString += "\nVIBRATE_WHEN_RINGING is UNKNOWN(" + myVibrateWhenRingingSetting + ")";
                break;
        }

        Toast.makeText(MainActivity.this, resultAsString, Toast.LENGTH_LONG).show();
    }

    public void toggleAutoBrightness(View view)
    {
        int brightnessmode;
        try
        {
            brightnessmode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);

            if( brightnessmode == Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)
                android.provider.Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            else
                android.provider.Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        }
        catch (Settings.SettingNotFoundException e)
        {
        }

    }

    public void syncGoogle(View view)
    {
        Account[] accounts = AccountManager.get(this).getAccountsByType("com.google");
        ContentResolver.setMasterSyncAutomatically(true);
        for(int i=0; i<accounts.length; i++)
        {
            Toast.makeText(MainActivity.this, "Yapılan senkronlar: contacs, calendar, ", Toast.LENGTH_SHORT).show();

            ContentResolver.requestSync(accounts[i], "com.android.contacts", new Bundle());
            ContentResolver.requestSync(accounts[i], "com.android.calendar", new Bundle());
        }
    }

    // BU ALTTAKİ ROOT İSTİYOR YA, YERİNE NORMAL KOD ARAŞTIR
    // SONUÇ: MM'larda ROOT'SUZ DATA AÇMA KAPAMA İMKANI YOK KESİNLİKLE
    public void toggleWifiData(View view)
    {
        try{
            if(hideOnToggle)
                MainActivity.this.moveTaskToBack(true); //programı minimize et

            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

            outputStream.writeBytes("if dumpsys wifi | grep -q \"Wi-Fi is enabled\"; then\nsvc wifi disable\nsvc data enable\nelse\nsvc wifi enable\nsvc data disable\nfi\n");
            outputStream.flush();

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            su.waitFor();
        }
        catch(Exception e)
        {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }

    public void openRecentsMenu(View view)
    {
        try {
            minimizeApp("forToggle");

            /*Class serviceManagerClass = Class.forName("android.os.ServiceManager");
            Method getService = serviceManagerClass.getMethod("getService", String.class);
            IBinder retbinder = (IBinder) getService.invoke(serviceManagerClass, "statusbar");
            Class statusBarClass = Class.forName(retbinder.getInterfaceDescriptor());
            Object statusBarObject = statusBarClass.getClasses()[0].getMethod("asInterface", IBinder.class).invoke(null, new Object[]{retbinder});
            Method clearAll = statusBarClass.getMethod("toggleRecentApps");
            clearAll.setAccessible(true);
            clearAll.invoke(statusBarObject);*/

            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
            outputStream.writeBytes("input keyevent KEYCODE_APP_SWITCH\n");
            outputStream.flush();
            outputStream.writeBytes("exit\n");
            outputStream.flush();
            su.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toggleBluetooth(View view)
    {
        minimizeApp("forToggle");

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null)
        {
            // Device does not support Bluetooth
        }
        else
        {
            if (mBluetoothAdapter.isEnabled())
            {
                mBluetoothAdapter.disable();
            }
            else
            {
                mBluetoothAdapter.enable();
            }
        }
    }

    public void toggleGps(View view) {
        try {
            minimizeApp("forToggle");

            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

            outputStream.writeBytes("if settings get secure location_providers_allowed | grep -q \"network,gps\"; then\n" +
                    "settings put secure location_providers_allowed -gps\n" +
                    "else\n" +
                    "settings put secure location_providers_allowed +gps\n" +
                    "fi\n");
            outputStream.flush();

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            su.waitFor();
        } catch (Exception e)
        {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void openSettings(View view)
    {
        //bu alttaki satır işe yaramadı
        minimizeApp("forToggle");

        startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
    }

    public void toggleTorch(View view)
    {
        CameraManager manager = (CameraManager) getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
        String mCameraId = "";
        try
        {
            mCameraId = manager.getCameraIdList()[0];
            if( flashState == false )
            {
                manager.setTorchMode(mCameraId,true);
            }
            else
            {
                manager.setTorchMode(mCameraId,false);
            }

        }
        catch(Exception e)
        {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    public void toggleData(View view)
    {
        minimizeApp("forToggle");

        try{
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());


            if ( mobileDataEnabled == true)
            {
                outputStream.writeBytes("svc data disable\n");
                outputStream.flush();
            }
            else
            {
                outputStream.writeBytes("svc data enable\n");
                outputStream.flush();
            }

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            su.waitFor();
        }
        catch(Exception e)
        {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    public void toggleOrientation(View view)
    {
        minimizeApp("forToggle");

        if  (android.provider.Settings.System.getInt(getContentResolver(),Settings.System.ACCELEROMETER_ROTATION, 0) == 1)
        {
            android.provider.Settings.System.putInt(getContentResolver(),Settings.System.ACCELEROMETER_ROTATION, 0);
        }
        else
        {
            android.provider.Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
        }
    }

    public void toggleWifi(View view)
    {
        minimizeApp("forToggle");

        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled())
        {
            wifi.setWifiEnabled(false);
        }
        else
        {
            wifi.setWifiEnabled(true);
        }
    }

    public void toggleHotspot(View view)
    {
        minimizeApp("forToggle");

        try
        {
            WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiConfiguration wifi_configuration = null;
            Method[] wmMethods = wifi.getClass().getDeclaredMethods();
            for (Method method: wmMethods)
            {
                if (method.getName().equals("setWifiApEnabled"))
                {
                    if( isWifiAPenabled == true )
                    {

                        method.invoke(wifi, wifi_configuration, false);

                    }
                    else
                    {
                        method.invoke(wifi, wifi_configuration, true);
                    }
                }
            }
        }
        catch (Exception e)
        {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void openCalculator(View view)
    {
        minimizeApp("forToggle");

        try
        {
            /*Intent i = new Intent();
            i.setAction(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_APP_CALCULATOR);
            startActivity(i);*/

            /*Intent intent = new Intent();
            intent.setClassName("com.android.calculator2", "com.android.calculator2.Calculator");
            startActivity(intent);*/

            Intent intent = new Intent();
            intent.setClassName("com.miui.calculator", "com.miui.calculator.cal.CalculatorTabActivity");
            startActivity(intent);

        }
        catch (Exception e)
        {
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }


    //mmssms.db yi irdele, tüm SMS ler burada tutuluyor ve SQLite imiş DB türü
    // SMS GELİNCE BUNDAN SATIR VE SİLMEYİ ARAŞTIR Kİ İSTEMEDİĞİN SMS LER DİREKT SİLİNMİŞ OLUR
    public void readSms(View view)
    {
        Toast.makeText(MainActivity.this, "SMS okuma başladı", Toast.LENGTH_SHORT).show();

        Uri uriSms = Uri.parse("content://sms/inbox");
        Cursor cursor = getContentResolver().query(uriSms, new String[]{"_id", "address", "date", "body"},null,null,null);

        cursor.moveToFirst();
        while  (cursor.moveToNext())
        {
            String address = cursor.getString(1);
            //System.out.println("Mobile number: "+address); //SEFAMERVE, F1_SMS, DiYANETVAKF
        }

        Toast.makeText(MainActivity.this, "SMS okuma bitti", Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
