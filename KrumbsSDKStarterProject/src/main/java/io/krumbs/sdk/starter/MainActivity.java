/*
 * Copyright (c) 2016 Krumbs Inc
 * All rights reserved.
 *
 */
package io.krumbs.sdk.starter;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.MapView;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;

import io.krumbs.sdk.KrumbsSDK;
import io.krumbs.sdk.dashboard.KDashboardFragment;
import io.krumbs.sdk.dashboard.KGadgetDataTimePeriod;
import io.krumbs.sdk.data.model.Event;
import io.krumbs.sdk.krumbscapture.KCaptureCompleteListener;
import io.krumbs.sdk.krumbscapture.settings.KUserPreferences;


public class MainActivity extends AppCompatActivity {
    private KGadgetDataTimePeriod defaultInitialTimePeriod = KGadgetDataTimePeriod.TODAY;
    private KDashboardFragment kDashboard;
    private JSONObject jsonObject;
    private String category,name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preloadMaps();

        setContentView(R.layout.app_bar_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        if (savedInstanceState == null) {
            kDashboard = buildDashboard();
            getSupportFragmentManager().beginTransaction().replace(R.id.content, kDashboard).commit();
        }
        View startCaptureButton = findViewById(R.id.start_report_button);
        KrumbsSDK.setUserPreferences(
                new KUserPreferences.KUserPreferencesBuilder().audioRecordingEnabled(true).build());
        if (startCaptureButton != null) {
            startCaptureButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startCapture();
                }
            });
        }
    }

    private void preloadMaps() {
        // hack to load mapsgadget faster: http://stackoverflow
        // .com/questions/26265526/what-makes-my-map-fragment-loading-slow
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    MapView mv = new MapView(getApplicationContext());
                    mv.onCreate(null);
                    mv.onPause();
                    mv.onDestroy();
                } catch (Exception ignored){
                    Log.e("KRUMBS-ERROR", "error while init maps/ google play serv");
                }
            }
        });
        // alternatively: http://stackoverflow.com/questions/26178212/first-launch-of-activity-with-google-maps-is-very-slow

    }

    private KDashboardFragment buildDashboard() {
        return new KDashboardFragment.KDashboardBuilder()
//                TODO: adding individual gadgets not supported yet
//                TODO: WIll be available in the next SDK release
//                .addGadget(KGadgetType.REPORTS).addGadget(KGadgetType.PEOPLE).addGadget(KGadgetType.TOP_INTENTS).addGadget(KGadgetType.TOP_PLACES)
                .addAllGadgets()
                .timePeriod(defaultInitialTimePeriod).build();

    }


    private void startCapture() {
        int containerId = R.id.camera_container;
// SDK usage step 4 - Start the K-Capture component and add a listener to handle returned images and URLs
        KrumbsSDK.startCapture(containerId, this, new KCaptureCompleteListener() {
            @Override
            public void captureCompleted(CompletionState completionState, boolean audioCaptured,
                                         Map<String, Object> map) {
                if (completionState != null) {
                    Log.i("KRUMBS-CALLBACK", "STATUS" + ": " + completionState.toString());
                }
                if (completionState == CompletionState.CAPTURE_SUCCESS) {
// The local image url for your capture
                    String imagePath = (String) map.get(KCaptureCompleteListener.CAPTURE_MEDIA_IMAGE_PATH);
                    if (audioCaptured) {
// The local audio url for your capture (if user decided to record audio)
                        String audioPath = (String) map.get(KCaptureCompleteListener.CAPTURE_MEDIA_AUDIO_PATH);
                        Log.i("KRUMBS-CALLBACK", audioPath);
                    }
// The mediaJSON url for your capture
                    String mediaJSONUrl = (String) map.get(KCaptureCompleteListener.CAPTURE_MEDIA_JSON_URL);
                    Log.i("KRUMBS-CALLBACK", mediaJSONUrl + ", " + imagePath);
                    if (map.containsKey(KCaptureCompleteListener.CAPTURE_EVENT)) {
                        Event ev = (Event) map.get(KCaptureCompleteListener.CAPTURE_EVENT);
                        Log.i("KRUMBS-CALLBACK", "Event captured =  + " + ev.objectId());
                    }

                    new Task().execute(mediaJSONUrl);

                } else if (completionState == CompletionState.CAPTURE_CANCELLED ||
                        completionState == CompletionState.SDK_NOT_INITIALIZED) {
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        switch (defaultInitialTimePeriod) {
            case TODAY:
                menu.findItem(R.id.last_day).setChecked(true);
                break;
            case LAST_24_HOURS:
                menu.findItem(R.id.last_24h).setChecked(true);
                break;
            case LAST_30_DAYS:
                menu.findItem(R.id.last_month).setChecked(true);
                break;
            case LAST_12_MONTHS:
                menu.findItem(R.id.last_year).setChecked(true);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        item.setChecked(true);
        switch (item.getItemId()) {
            case R.id.last_day:
                defaultInitialTimePeriod = KGadgetDataTimePeriod.TODAY;
                break;
            case R.id.last_24h:
                defaultInitialTimePeriod = KGadgetDataTimePeriod.LAST_24_HOURS;
                break;
            case R.id.last_month:
                defaultInitialTimePeriod = KGadgetDataTimePeriod.LAST_30_DAYS;
                break;
            case R.id.last_year:
                defaultInitialTimePeriod = KGadgetDataTimePeriod.LAST_12_MONTHS;
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        //send notification to the SDK to update the Dashboard
        if (kDashboard != null) {
            kDashboard.refreshDashboard(defaultInitialTimePeriod);
        }
        return true;
    }

    //    http://stackoverflow.com/questions/7469082/getting-exception-illegalstateexception-can-not-perform-this-action-after-onsa
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    private class Task extends AsyncTask<String, Void, Boolean> {

        private String error_str = "";
        double lat, lon;

        protected Boolean doInBackground(String... urls) {

            try {

                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                int status = conn.getResponseCode();
                if (status != 200) {
                    // TODO: show correct error reason based on response code
                    throw new IOException("Post failed with error code " + status);
                }
                InputStream res = conn.getInputStream();
                String response = convertStreamToString(res);

                jsonObject = new JSONObject(response);
                JSONObject geoloc = jsonObject.getJSONArray("media").getJSONObject(0).getJSONObject("where").getJSONObject("geo_location");
                //System.out.println("Response:  " +response);
                JSONObject type = jsonObject.getJSONArray("media").getJSONObject(0).getJSONArray("why").getJSONObject(0);
                category = type.getString("intent_category_name");
                name= type.getString("intent_name");
                lat = geoloc.getDouble("latitude");
                lon = geoloc.getDouble("longitude");

            } catch (Exception E) {

                E.printStackTrace();
                return false;


            }
            return true; //change this
        }

        @Override
        protected void onPostExecute(Boolean s) {

            System.out.println("MMMMMMMMMMMMMMMMMMMMMMMMM");
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            intent.putExtra("Lat", lat);
            intent.putExtra("Lon", lon);
            intent.putExtra("Category",category);
            intent.putExtra("Name",name);
            startActivityForResult(intent, 1234);
        }


        private String convertStreamToString(InputStream is) {

            StringBuilder sb = new StringBuilder();
            Scanner se = new Scanner(is);

            try {
                while (se.hasNextLine()) {
                    sb.append(se.nextLine());
                    sb.append("\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return sb.toString();
        }
    }



}
