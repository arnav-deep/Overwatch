package com.throwntech.overwatch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final String TAG = "MapActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int MY_PERMISSIONS_REQUEST_RECEIVE_SMS = 0;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;

    private double currLat, currLng;
    private String bpm, temperature, preTime;
    private LatLng currLoc;

    TextView lastUpdated, lastBpm, lastTemp;
    private CardView cardView;

    Button sos, getData;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECEIVE_SMS)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, MY_PERMISSIONS_REQUEST_RECEIVE_SMS);
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }

        lastUpdated = (TextView)findViewById(R.id.last_updated);
        lastBpm = (TextView)findViewById(R.id.last_bpm);
        lastTemp = (TextView)findViewById(R.id.last_temp);

        cardView = findViewById(R.id.card_view);

        initDetail();
        showCurrData();
        cardView.setVisibility(View.VISIBLE);

        sos = findViewById(R.id.sos_button);

        sos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sms = "sos";
                String phoneNum = "+919664624488";

                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNum, null, sms, null, null);
            }
        });

        getData = findViewById(R.id.getData);

        getData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sms = "update";
                String phoneNum = "+919664624488";

                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNum, null, sms, null, null);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        float zoom = 5f;
        currLoc = new LatLng(20.5937, 78.9629);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currLoc, zoom));

        initDetail();
        updateDetail();
    }

    private void goToCurrLatLng(double currLat, double currLng) {
        currLoc = new LatLng(currLat, currLng);
        mMap.addMarker(new MarkerOptions().position(currLoc));
        float zoom = 12f;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currLoc, zoom));
    }

    public void initDetail(){
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mDatabase = firebaseDatabase.getReference("data");
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                bpm = dataSnapshot.child("Bpm").getValue().toString();
                currLat = (Double)dataSnapshot.child("Lat").getValue();
                currLng = (Double)dataSnapshot.child("Lng").getValue();
                temperature = dataSnapshot.child("Temp").getValue().toString();
                goToCurrLatLng(currLat, currLng);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateDetail() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mDatabase = firebaseDatabase.getReference("data");
        mDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                bpm = dataSnapshot.child("Bpm").getValue().toString();
                currLat = (Double) dataSnapshot.child("Lat").getValue();
                currLng = (Double) dataSnapshot.child("Lng").getValue();
                temperature = dataSnapshot.child("Temp").getValue().toString();
                goToCurrLatLng(currLat, currLng);
                showCurrData();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void showCurrData() {

        SimpleDateFormat formatter= new SimpleDateFormat("HH:mm");
        Date date = new Date(System.currentTimeMillis());

        preTime = formatter.format(date).toString();
        Log.i(TAG, "time is: " +  preTime);
        lastUpdated.setText(preTime);
        lastBpm.setText(bpm);
        lastTemp.setText(temperature);
    }
}