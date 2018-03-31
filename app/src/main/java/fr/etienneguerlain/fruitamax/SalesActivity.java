package fr.etienneguerlain.fruitamax;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/*

    This activity is the main activity of the app.

    It displays all the sales that are in a perimeter around the user's location.

 */

public class SalesActivity extends AppCompatActivity {


    Timer timer;
    TimerTask task;


    // This adapter builds up the list of nearby sales from the data stored in the Data object
    // (the Data object is filled up with the "Get Nearby Sales" Service)
    NearbySalesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales);

        // First, we check if the user has set a search range
        checkSearchRange();


        // Then we check if the geolocation is enabled, and if not, we ask for permission
        checkLocationPermission();

        // This call to method builds the list displaying the nearby sales
        inflateNearbySalesList();

        timer = new Timer();

        // A task runs every second to refresh the nearby sales list if there where changes
        final Handler handler = new Handler();
        task = new TimerTask() {
            @Override
            public void run() {


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        adapter.clear();
                        adapter.addAll(Data.Instance.getNearbySales());
                    }
                });
            }
        };

        // We check the data every second
        timer.schedule(task, 0, 1000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sales_activity_actions, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // If user touches the settings icon in the toolbar
            case R.id.preferencesActionButton:

                // We prepare the intent for the Preferences activity...
                Intent intentToPreferencesActivity = new Intent(SalesActivity.this, PreferencesActivity.class);

                // and launch the Preferences Activity
                startActivityForResult(intentToPreferencesActivity, 1);

                break;


            case R.id.mySalesActionButton:

                // If user presses the "+" icon, we launch the My Sales Activity
                Intent intentToMySalesActivity = new Intent(SalesActivity.this, MySalesActivity.class);
                startActivity(intentToMySalesActivity);

                break;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
        return super.onOptionsItemSelected(item);
    }



    // The following method cleanly logs out the user if the result of the Preferences Activity tells
    // us that the user chose to log out
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // RequestCode == 1 means that we are dealing with the result of the Preferences Activity
        if (requestCode == 1) {

            if(resultCode == Activity.RESULT_OK){

                // If the Preferences Activity sends back data with a "logout" key, then we have to log out the user
                if(data.hasExtra("logout")){

                    // User previously logged out, and the token has been removed from the Shared Preferences
                    // We can now go finish this activity and launch the Login Activity

                    Intent intentToLoginActivity = new Intent(SalesActivity.this, LoginActivity.class);
                    startActivity(intentToLoginActivity);

                    // We finish this activity to clean the activity stack
                    finish();

                }
            }

            if (resultCode == Activity.RESULT_CANCELED) {

                // Code if there is nothing returned from the preferences activity

            }
        }
    }


    // This method build the list of nearby sales
    public void inflateNearbySalesList(){

        // Inflate the list of nearby sales each time we load the activity

        // Construct the data source
        ArrayList<NearbySale> sales = Data.Instance.getNearbySales();

        // Create the adapter to convert the array to views
        adapter = new NearbySalesAdapter(this, sales);

        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.nearbySalesList);
        listView.setAdapter(adapter);
    }



    // This method checks if a search range was previously set by user
    // If not, the searh range is set to 20km by default
    public void checkSearchRange(){
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                getString(R.string.sharesPreferencesFile),
                Context.MODE_PRIVATE
        );

        // If the range is not specified in shared preferences, -1 is returned
        int range = sharedPreferences.getInt(getString(R.string.range), -1);

        if(range == -1){
            range = 20;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(getString(R.string.range), range);
            editor.commit();
        }

        Data.range = range;
    }


    // This method check that the user granted us the location permission, and if not, asks him/her
    public void checkLocationPermission(){

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_COARSE_LOCATION);
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Data.REQUEST_FINE_LOCATION);

        }else{
            Data.LOCATION_PERMISSION = true;
        }

    }


    // This methods runs if the user grants us the location permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == Data.REQUEST_FINE_LOCATION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){

                Data.LOCATION_PERMISSION = true;

                // We ask to the System was is the location of the user
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation("gps");

            }else{
                Toast.makeText(getApplicationContext(), "Access to location was not granted", Toast.LENGTH_SHORT).show();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}