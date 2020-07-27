package com.throwntech.overwatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MyReceiver extends BroadcastReceiver {

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = "SmsBroadcastReceiver";

    String msg, phoneNo;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onReceive(Context context, Intent intent) {

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("data");

        Log.i(TAG, "Intent Received: " + intent.getAction());
        if (Objects.equals(intent.getAction(), SMS_RECEIVED)) {
            Bundle dataBundle = intent.getExtras();

            if(dataBundle != null) {
                Object[] mypdu = (Object[]) dataBundle.get("pdus");
                assert mypdu != null;
                final SmsMessage[] message = new SmsMessage[mypdu.length];

                for (int i = 0; i < mypdu.length; i++) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        String format = dataBundle.getString("format");
                        message[i] = SmsMessage.createFromPdu((byte[]) mypdu[i], format);
                    } else {
                        message[i] = SmsMessage.createFromPdu((byte[]) mypdu[i]);
                    }
                    msg = message[i].getMessageBody();
                    phoneNo = message[i].getOriginatingAddress();
                }

                if (phoneNo.equals("+919913383342")) {
//                    Toast.makeText(context, "Message: " + msg + "\n Number: " + phoneNo, Toast.LENGTH_LONG).show();

                    List<String> allData = Arrays.asList(msg.split(" ", 3));
                    List<String> indData = new ArrayList<>();
                    for (String a : allData) {
                        String[] value = a.split(":", 2);
                        indData.add(value[1]);
                    }
                    String str = indData.get(2);
                    String[] cloc = str.split("=", 2);
                    String[] latlng = cloc[1].split(",", 2);
                    int bpm = Integer.parseInt(indData.get(0));
                    double temp = Double.parseDouble(indData.get(1));
                    double lat = Double.parseDouble(latlng[0]);
                    double lng = Double.parseDouble(latlng[1]);

                    mDatabase.child("Bpm").setValue(bpm);
                    mDatabase.child("Lat").setValue(lat);
                    mDatabase.child("Lng").setValue(lng);
                    mDatabase.child("Temp").setValue(temp);
                }
            }
        }
    }
}
