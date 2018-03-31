package fr.etienneguerlain.fruitamax;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;


/*
    The mission of this service is to retrieve data (nearby sales) from the server.
    To do so, it runs a task every 10 seconds that:

    - Gets the user geolocation, and builds a target URL with the latitude, longitude and the
    search range the user set in the Preferences.

    - After sending the request, receives a JSON list of nearby sales

    - Parses the list, and stores it in the Data object

*/


public class getNearbySalesService extends Service {

    Timer timer;
    TimerTask task;

    private final IBinder ib = new getNearbySalesServiceBinder();

    public void onCreate() {

        timer = new Timer();

        final Handler handler = new Handler();

        // The task that queries the server, gets, parses and stores the data
        task = new TimerTask() {
            @Override
            public void run() {

                // If the user has granted location and a search range is defined
                if(Data.LOCATION_PERMISSION && Data.range != -1) {

                    // We get the current location of the user
                    LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation("gps");


                    // Location may be null if there is no last known position
                    if(location != null) {

                        Data.lat = location.getLatitude();
                        Data.lng = location.getLongitude();
                    }

                    // Get token from the Shared Preferences
                    SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                            getString(R.string.sharesPreferencesFile),
                            Context.MODE_PRIVATE
                    );

                    String token = sharedPreferences.getString(getString(R.string.token), null);

                    try {

                        // We build the target URL
                        String target = "http://fruitamax.etienneguerlain.fr/api/sales?lat=";

                        // Adding the latitude
                        target += Data.lat;

                        // Adding longitude
                        target += "&lng=" + Data.lng;

                        // Adding the range
                        target += "&range=" + Data.range;

                        URL url = new URL(target);


                        // Setting a new URL connection
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                        // Setting the x-access-token parameter so that the server knows who is
                        // requesting
                        connection.setRequestProperty("x-access-token", token);
                        connection.setDoInput(true);


                        int responseCode = connection.getResponseCode();


                        if (responseCode == HttpURLConnection.HTTP_OK) {

                            // We build up a string containing the data received
                            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            StringBuffer sb = new StringBuffer("");
                            String line = "";

                            while ((line = in.readLine()) != null) {

                                sb.append(line);
                                break;
                            }

                            in.close();


                            // We "cast" the receive data into at JSON object (received data is JSON)
                            JSONObject response = new JSONObject(sb.toString());
                            Iterator<String> keysIterator = response.keys();

                            // We build up the list of nearby sales
                            ArrayList<NearbySale> nearbySales = new ArrayList<NearbySale>();

                            while (keysIterator.hasNext()) {
                                String key = (String) keysIterator.next();
                                JSONObject object = response.getJSONObject(key);

                                int id = Integer.parseInt(key);
                                double distance = object.getDouble("distance");
                                String title = object.getString("title");
                                double quantity = object.getDouble("quantity");
                                String unit = object.getString("unit");
                                double price = object.getDouble("price");

                                NearbySale nearbySale = new NearbySale(id, distance, title, quantity, unit, price);

                                nearbySales.add(nearbySale);

                            }

                            // Once the data has been parsed, we store the nearby sales list into the Data object
                            Data.Instance.setNearbySales(nearbySales);


                        }

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    // Here, user has not granted access to GPS location
                }
            }
        };

        // Run the service's task every 10 seconds
        timer.schedule(task, 100, 1000 * 10);
    }

    public void onDestroy() {

        // When the service is destroyed, we destroy the timer so that the task doesn't run forever
        timer.cancel();
    }

    public IBinder onBind(Intent arg0) {
        return ib;
    }

    private class getNearbySalesServiceBinder extends Binder implements getNearbySalesServiceInterface{
        public void getInfo(){
            return;
        }
    }

}
