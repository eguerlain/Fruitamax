package fr.etienneguerlain.fruitamax;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


/*

    The Preferences Activity has two missions:

    - Allow the user to set the search range
    - Allow the user to log out

 */

public class PreferencesActivity extends AppCompatActivity {

    Button logout;
    SeekBar rangeBar;
    TextView searchRangeDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        // Components of the layout
        logout = findViewById(R.id.logoutButton);   // Logout button
        rangeBar = findViewById(R.id.searchRange);  // Seekbar to set the search range
        searchRangeDisplay = findViewById(R.id.searchRangeDisplay); // Shows to the user the current value of the search range

        // When the search range has not been defined by the user yet, it is set to 20km by default
        if(Data.range == -1){
            rangeBar.setProgress(20);
            searchRangeDisplay.setText("20 km");
        }else{
            // It the search range was previously set by the user, we display it (with both the seekbar and the value in km)
            rangeBar.setProgress(Data.range);
            searchRangeDisplay.setText(Data.range + " km");
        }



        // If the user clicks on the logout button...
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // ... we erase the token from the Shared Preferences
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                        getString(R.string.sharesPreferencesFile),
                        Context.MODE_PRIVATE
                );
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.remove(getString(R.string.token));
                editor.apply();


                // We send the result back to the sales activity
                // We have to do so, because lauching the Login Activity from here would mess up the activity stack
                // and the history would be inconsistent
                Intent returnIntent = new Intent();

                // We set the value of "logout" to true to indicate the user logged out
                returnIntent.putExtra("logout", true);

                // Setting up the result of the preferences activity
                setResult(Activity.RESULT_OK, returnIntent);

                // As user is logged out, it is time to stop the getNearbySalesService
                stopService(new Intent(getApplicationContext(), getNearbySalesService.class));

                // Finishing the activity brings us back to the Sales Activity
                finish();

            }
        });


        // This listener handles operations on the SeekBar
        rangeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int prog = 0;


            // If the user changes the seekbar, this method is called
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

                // We have to had 2km, because it is impossible to set a minimum other than 0 to a seekbar
                // (And max is 98 km. The range can be set between 2 and 100km)
                prog = progress + 2;

                // We refresh the display
                searchRangeDisplay.setText(prog + " km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}


            // When the user has finished to move the seekbar
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                // We save the new range to the Data singleton...
                Data.range = prog;

                // ... and to the shared preferences
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                        getString(R.string.sharesPreferencesFile),
                        Context.MODE_PRIVATE
                );

                // Second, we have to create a Preferences Editor to write the file
                SharedPreferences.Editor editor = sharedPreferences.edit();

                // Third, we write the token to the editor
                editor.putInt(getString(R.string.range), prog);

                // Fourth, we save (commit) the editor
                editor.commit();
            }
        });
    }
}
