package com.transistorsoft.cordova.bggeo;

import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.transistorsoft.locationmanager.adapter.BackgroundGeolocation;
import com.transistorsoft.locationmanager.adapter.TSConfig;
import com.transistorsoft.locationmanager.event.ActivityChangeEvent;
import com.transistorsoft.locationmanager.event.GeofenceEvent;
import com.transistorsoft.locationmanager.event.GeofencesChangeEvent;
import com.transistorsoft.locationmanager.event.ConnectivityChangeEvent;
import com.transistorsoft.locationmanager.event.HeadlessEvent;
import com.transistorsoft.locationmanager.event.HeartbeatEvent;
import com.transistorsoft.locationmanager.event.MotionChangeEvent;
import com.transistorsoft.locationmanager.event.LocationProviderChangeEvent;
import com.transistorsoft.locationmanager.http.HttpResponse;
import com.transistorsoft.locationmanager.location.TSLocation;
import com.transistorsoft.locationmanager.logger.TSLog;


import android.content.Context;
import android.util.Log;


import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;



/**
 * BackgroundGeolocationHeadlessTask
 * This component allows you to receive events from the BackgroundGeolocation plugin in the native Android environment while your app has been *terminated*,
 * where the plugin is configured for stopOnTerminate: false.  In this context, only the plugin's service is running.  This component will receive all the same
 * events you'd listen to in the Javascript API.
 *
 * You might use this component to:
 * - fetch / post information to your server (eg: request new API key)
 * - execute BackgroundGeolocation API methods (eg: #getCurrentPosition, #setConfig, #addGeofence, #stop, etc -- you can execute ANY method of the Javascript API)
 */

public class BackgroundGeolocationHeadlessTask {

    private Context context;

    @Subscribe
    public void onHeadlessTask(HeadlessEvent event) throws JSONException {

        String name = event.getName();
        Log.v("MyApp", "BackgroundGeolocationHeadlessTask Event: " + event.getName());
        TSLog.logger.debug("\uD83D\uDC80  event: " + event.getName());
        TSLog.logger.debug("- event: " + event.getEvent());


        if (name.equals(BackgroundGeolocation.EVENT_TERMINATE)) {
            JSONObject state = event.getTerminateEvent();
        } else if (name.equals(BackgroundGeolocation.EVENT_LOCATION)) {
            TSLocation location = event.getLocationEvent();
        } else if (name.equals(BackgroundGeolocation.EVENT_MOTIONCHANGE)) {
            MotionChangeEvent motionChangeEvent = event.getMotionChangeEvent();
            TSLocation location = motionChangeEvent.getLocation();
        } else if (name.equals(BackgroundGeolocation.EVENT_HTTP)) {
            HttpResponse response = event.getHttpEvent();
        } else if (name.equals(BackgroundGeolocation.EVENT_PROVIDERCHANGE)) {
            LocationProviderChangeEvent providerChange = event.getProviderChangeEvent();
        } else if (name.equals(BackgroundGeolocation.EVENT_ACTIVITYCHANGE)) {
            ActivityChangeEvent activityChange = event.getActivityChangeEvent();
        } else if (name.equals(BackgroundGeolocation.EVENT_SCHEDULE)) {
            JSONObject state = event.getScheduleEvent();
        } else if (name.equals(BackgroundGeolocation.EVENT_BOOT)) {
            JSONObject state = event.getBootEvent();
        } else if (name.equals(BackgroundGeolocation.EVENT_GEOFENCE)) {
            GeofenceEvent geofenceEvent = event.getGeofenceEvent();
        } else if (name.equals(BackgroundGeolocation.EVENT_GEOFENCESCHANGE)) {
            GeofencesChangeEvent geofencesChangeEvent = event.getGeofencesChangeEvent();
        } else if (name.equals(BackgroundGeolocation.EVENT_HEARTBEAT)) {
            HeartbeatEvent heartbeatEvent = event.getHeartbeatEvent();




            /* Get Config */
            TSConfig config = TSConfig.getInstance(context);
            String url = config.getUrl();



            /* Get last registred location (to improve)*/
            JSONObject options = null;
            JSONArray data = new JSONArray();
            TSLocation location = heartbeatEvent.getLocation();

            if (location != null) {
                data.put(location.toJson());
            }

            JSONObject params = new JSONObject();
            params.put("heartbeat", data);


            /* Open new thread to send a post request to the API with the data */
            sendPost(url,params.toString());


        } else if (name.equals(BackgroundGeolocation.EVENT_NOTIFICATIONACTION)) {
            String buttonId = event.getNotificationEvent();
        } else if (name.equals(BackgroundGeolocation.EVENT_CONNECTIVITYCHANGE)) {
            ConnectivityChangeEvent connectivityChangeEvent = event.getConnectivityChangeEvent();
        } else if (name.equals(BackgroundGeolocation.EVENT_ENABLEDCHANGE)) {
            boolean enabled = event.getEnabledChangeEvent();
        } else {
            TSLog.logger.warn(TSLog.warn("Unknown Headless Event: " + name));
            Log.v("MyApp", "BackgroundGeolocationHeadlessTask Unknown Headless Event: " + event.getName() + "BK name: " + BackgroundGeolocation.EVENT_HEARTBEAT);
        }
    }


    public void sendPost(String urlAddress, String jsonString) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlAddress);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept","application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);

                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    os.writeBytes(jsonString);

                    os.flush();
                    os.close();

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG" , conn.getResponseMessage());

                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }
}
