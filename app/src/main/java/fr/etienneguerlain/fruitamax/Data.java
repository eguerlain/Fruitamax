package fr.etienneguerlain.fruitamax;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


// This class holds useful information that needs to be shared across activities, service, ...
public class Data {

    // Data singleton (easy access to data from service and activities)
    public static final Data Instance = new Data();

    // GPS Coordinates (used when posting a new sale)
    public static double lat = 0;
    public static double lng = 0;

    // Constant used when requesting the Fine location permission
    public static final int REQUEST_FINE_LOCATION = 1000;

    // Tells if user has granted the location permission
    public static boolean LOCATION_PERMISSION = false;

    // The application database, holding all the sales that the user posted
    public static AppDatabase DB;

    // The search range (By default, -1 means the value has not been set from the SharedPreferences yet)
    public static int range = -1;

    // List of the nearby sales, retrieved from the server
    private ArrayList<NearbySale> _nearbySales;

    // Constructor
    private Data(){
        _nearbySales = new ArrayList<NearbySale>();
    }

    // Method returning the nearby sales
    public ArrayList<NearbySale> getNearbySales(){
        return _nearbySales;
    }

    // Method setting the nearby sales list from sales provided by the server
    public void setNearbySales(ArrayList<NearbySale> nearbySales){

        // Sorting the list so that the nearest sale appears at the top of the nearby sales
        Collections.sort(nearbySales, NearbySale.getCompByDistance());

        // Storing the sorted list of nearby sales, previously received from the server
        _nearbySales = nearbySales;
    }

}
