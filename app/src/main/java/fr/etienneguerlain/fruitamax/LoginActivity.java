package fr.etienneguerlain.fruitamax;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

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
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;


/*
    This activity is responsible for allowing the user to log in

    It displays a login form, and error toasts when an error happens (ex: wrong password)

    It is the very first activity to be displayed when the app is launched

 */

public class LoginActivity extends AppCompatActivity{

    Button loginButton;
    Button registerButton;

    EditText login;
    EditText password;

    // Holds the loader
    FrameLayout progressBarHolder;

    // Useful for showing/hiding the progress loader
    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Before doing anything, we read the user preferences to know if the user is already logged
        // If so, a token exists in the Shared Preferences

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                getString(R.string.sharesPreferencesFile),
                Context.MODE_PRIVATE
        );

        String token = sharedPreferences.getString(getString(R.string.token), null);

        // If token exists, then we launch the Sales Activity activity
        if(token != null){
            launchSalesActivity();
        }

        // Gestion of the database
        Data.DB = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "fruitamax-database").allowMainThreadQueries().build();

        setContentView(R.layout.activity_login);

        // Retrieve components from the layout
        getComponentsFromLayout();

        // Sets the onClickListeners on the buttons
        setButtonsListeners();

    }


    // This function loads all the components from the XML layout
    private void getComponentsFromLayout(){

        // Buttons
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        // Text fields
        login = findViewById(R.id.login);
        password = findViewById(R.id.password);

        // Frame Layout
        progressBarHolder = findViewById(R.id.progressBarHolder);
    }


    // This function sets the OnClickListeners for the two buttons of the interface
    private void setButtonsListeners(){

        // Click on LOGIN --> Launches the main activity of the app: sales activity
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                performLoginProcess();

            }
        });

        // Click on REGISTER --> Launches the registrer activity
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intentToRegisterActivity = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intentToRegisterActivity);
            }
        });
    }


    // This function performs initial check and requests server for authentication
    private void performLoginProcess(){

        if(checkAllFieldsAreFilled()){

            // If all fields are filled, we run the asyncTask that queries the server
            // Storing login and password in a Credential object
            Credentials credentials = new Credentials(login.getText().toString(), password.getText().toString());

            // Performing the query to server asynchronously
            new AuthenticationTask().execute(credentials);

        }else{
            // If at least one field is empty, we show a toast
            Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
        }
    }


    // This function tells if both login and password fields are filled
    private boolean checkAllFieldsAreFilled(){

        return !login.getText().toString().equals("") && !password.getText().toString().equals("");
    }





    // This private class holds the login and the password
    // It is used to pass those pieces of information to the AuthenticationTask (AsyncTask)
    private class Credentials{

        private String _login;
        private String _password;

        public Credentials(String login, String password){
            _login = login;
            _password = password;
        }

        public String getLogin(){ return _login; }
        public String getPassword(){ return _password; }

    }






    // This AsyncTask sends the login and the password to the server, and displays an error message
    // if there is an authentication error, or launches the next activity if authentication was successful
    private class AuthenticationTask extends AsyncTask<Credentials, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Disabling buttons
            loginButton.setEnabled(false);
            registerButton.setEnabled(false);

            // Loader animation
            inAnimation = new AlphaAnimation(0f, 1f);
            inAnimation.setDuration(200);
            progressBarHolder.setAnimation(inAnimation);
            progressBarHolder.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);

            // End spinner animation
            outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(200);
            progressBarHolder.setAnimation(outAnimation);
            progressBarHolder.setVisibility(View.GONE);

            // Enable buttons again
            loginButton.setEnabled(true);
            registerButton.setEnabled(true);


            try {

                // The doInBackground function has return a JSON object, containing either:
                // an error message
                // or the authentication token of the user

                JSONObject res = new JSONObject(response);


                // If the JSON contains the "err" key, there were an authentication failure
                if(res.has("err")){

                    // The login request has resulted in an error.
                    String err = res.getString("err");

                    // We display this error
                    Toast.makeText(getApplicationContext(), err, Toast.LENGTH_SHORT).show();

                    // We reset the password field
                    password.setText("");

                }else{

                    // If the login request was a success, we have a token
                    String token = res.getString("token");

                    // We reset both the login and the password fields
                    login.setText("");
                    password.setText("");

                    // We save the received token to the SharedPreferences...

                    // ... first, we get the Shared Preferences file
                    Context context = getApplicationContext();
                    SharedPreferences sharedPreferences = context.getSharedPreferences(
                            getString(R.string.sharesPreferencesFile),
                            Context.MODE_PRIVATE
                    );

                    // ... second, we have to create a Preferences Editor to write the file
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    // ... third, we write the token to the editor
                    editor.putString(getString(R.string.token), token);

                    // ... fourth, we save (commit) the editor
                    editor.commit();


                    // Everything is done, we can now launch the main activity of the app: the SalesActivity
                    launchSalesActivity();
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected String doInBackground(Credentials... credentials) {


            // The background function has to:
            // - build up a target URL where to send the credentials
            // - read the response and return it

            try {
                URL url = new URL("http://fruitamax.etienneguerlain.fr/login");


                // The postDataParams object will hold the credentials of the login form
                JSONObject postDataParams = new JSONObject();

                // Filling the postDataParams object with the information of the login form
                postDataParams.put("login", credentials[0].getLogin());
                postDataParams.put("password", credentials[0].getPassword());

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // This request is set as a POST resquest
                connection.setRequestMethod("POST");

                // Server is able to receive only url encoded form data
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // We both write data to server and read data from it
                connection.setDoInput(true);
                connection.setDoOutput(true);


                // We write the form data to the server
                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();


                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {

                    // We read data from the server response

                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                }
                else {
                    return new String("{\"err\":\"Random error\"}");
                }


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return "{\"err\":\"Random error (Check Internet connection)\"}";

        }
    }


    // This function builds up a JSON Object that holds the information from the form
    // It also URL-encode it
    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }

        return result.toString();
    }


    // This function launches the Sales Activity
    // And also starts the "Get Nearby Sales" Service
    private void launchSalesActivity(){

        
        // First, we start the getNearbySalesService, which gets nearby sales, based on user's location and his search range
        startService(new Intent(this, getNearbySalesService.class));

        // Creation of the intent to Sales Activity
        Intent intentToSalesActivity = new Intent(getApplicationContext(), SalesActivity.class);

        // Launching Sales Activity
        startActivity(intentToSalesActivity);

        // We kill the login activity, since the user is now logged in
        // By doing so, the sales activity will be the first activity in the activities pile
        finish();
    }

}
