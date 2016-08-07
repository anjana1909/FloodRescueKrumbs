package io.krumbs.sdk.starter;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    double lat,lon;
    ArrayList<shelter> shelters = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        System.out.println("############################ Maps #######################");
        lat=getIntent().getDoubleExtra("Lat",0);
        lon=getIntent().getDoubleExtra("Lon",0);
        System.out.println("xxxxxxxxx: "+lat+"   "+lon);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    private class shelter implements Comparable<shelter>
    {
        int id;
        double lat, lon;
        double dist;
        String place;

        public shelter(int id, double lat, double lon, double dist,String place)
        {
            this.id=id;
            this.lat=lat;
            this.lon=lon;
            this.dist=dist;
            this.place=place;
        }

        public double getDist()
        {
            return dist;
        }
        @Override
        public int compareTo(shelter another) {
            if(another.dist<this.dist)
                return 1;
            else if(another.dist>this.dist)
                return -1;
            else
                return 0;
        }
    }

    private String getJSONString(Context context)
    {
        String str = "";
        try
        {
            AssetManager assetManager = getAssets();
            InputStream in = assetManager.open("shelter_data.json");
            InputStreamReader isr = new InputStreamReader(in);
            char [] inputBuffer = new char[100];

            int charRead;
            while((charRead = isr.read(inputBuffer))>0)
            {
                String readString = String.copyValueOf(inputBuffer,0,charRead);
                str += readString;
            }
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }

        return str;
    }



    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng location = new LatLng(lat, lon);
        mMap.addMarker(new MarkerOptions().position(location).title("Your location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));


        try {

            JSONObject json = new JSONObject(getJSONString(getApplicationContext()));
            System.out.println("Length: "+json.getJSONArray("features").length());
            for(int i=0;i<json.getJSONArray("features").length();i++) {
                JSONObject coord = json.getJSONArray("features").getJSONObject(i).getJSONObject("geometry").getJSONObject("coordinates");
                String place=json.getJSONArray("features").getJSONObject(i).getJSONObject("properties").getString("name");
                double lat=coord.getDouble("lat");
                double lon=coord.getDouble("lon");
                double dist=getDistance(location,new LatLng(lat,lon));

               // if(!getIntent().getStringExtra("category").equals("Resource")) {
                    shelter s = new shelter(i, lat, lon, dist, place);
                    shelters.add(s);
                //}

            }

            //if(!getIntent().getStringExtra("category").equals("Resource")) {
                Collections.sort(shelters);

                for (int i = 0; i < 4; i++) {
                    LatLng loc = new LatLng(shelters.get(i).lat, shelters.get(i).lon);
                    mMap.addMarker(new MarkerOptions().position(loc).title("Shelter " + i));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
                }
                displayNotification(shelters.get(0).place);
           // }

            //else
            {

                //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                //if(getIntent().getStringExtra("name").equals("Shelter"))
                //{
                    String place="xxxx";


                //}
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public float getDistance(LatLng my_latlong, LatLng frnd_latlong) {

        Location l1 = new Location("One");
        l1.setLatitude(my_latlong.latitude);
        l1.setLongitude(my_latlong.longitude);

        Location l2 = new Location("Two");
        l2.setLatitude(frnd_latlong.latitude);
        l2.setLongitude(frnd_latlong.longitude);

        float distance = l1.distanceTo(l2);
        String dist = distance + " M";

        if (distance > 1000.0f) {
            distance = distance / 1000.0f;
            dist = distance + " KM";
        }
        return distance;
    }


    public void displayNotification(String name){

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Flood rescue")
                        .setContentText("Closest Shelter: "+name);

        Intent resultIntent = new Intent(this, NotificationView.class);
        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();

        inboxStyle.setBigContentTitle("Shelter Details:");
        mBuilder.setStyle(inboxStyle);

        int mNotificationId = 001;
// Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
// Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());



    }

}
