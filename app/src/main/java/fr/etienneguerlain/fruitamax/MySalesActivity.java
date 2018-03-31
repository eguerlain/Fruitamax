package fr.etienneguerlain.fruitamax;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


/*

    This activty shows to the user the list of all the sales he/she posted to the platform

    It takes the data from the local SQLite database

 */

public class MySalesActivity extends AppCompatActivity {

    // Adapter to build the listView from the list of sales
    MySalesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_sales);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // When the activiy is (re)displayed, we read the data from the DB and build the ListView of sales
        inflateMySalesList();
    }


    // The next method is a method that builds the action button (the "+" add the far right of the top bar)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_sales_activity_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // If user touches the "+" icon in the toolbar
            case R.id.newSaleActionButton:

                // We prepare the intent for the New Sale activity...
                Intent intentToNewSaleActivity = new Intent(MySalesActivity.this, NewSaleActivity.class);

                // and launch the Preferences Activity
                startActivity(intentToNewSaleActivity);

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    public void inflateMySalesList(){

        // Inflate the list of sales posted by the logged user


        // Construct the data source
        List<Sale> sales = Data.DB.saleDao().getAll();

        // Create the adapter to convert the array to views
        adapter = new MySalesAdapter(this, (ArrayList<Sale>) sales);

        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.mySalesList);
        listView.setAdapter(adapter);
    }
}
