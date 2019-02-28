package com.ahmeterden.ahmet.ahmetsShortcuts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SmsListener extends BroadcastReceiver {

    private SharedPreferences preferences;

    //mmssms.db yi irdele, tüm SMS ler burada tutuluyor ve SQLite imiş DB türü
    // SMS GELİNCE BUNDAN SATIR VE SİLMEYİ ARAŞTIR Kİ İSTEMEDİĞİN SMS LER DİREKT SİLİNMİŞ OLUR

    //SMS gelince ekrana bas
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED"))
        {
            Bundle myBundle = intent.getExtras();
            SmsMessage [] messages;
            if (myBundle != null)
            {
                Object [] pdus = (Object[]) myBundle.get("pdus");
                String format = myBundle.getString("format");
                messages = new SmsMessage[pdus.length];
                for (int i = 0; i < messages.length; i++)
                {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                    Toast.makeText(context,
                            "SMS came from: " + messages[i].getOriginatingAddress()
                             + "\n\nMessage: " + messages[i].getMessageBody(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}