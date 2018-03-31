
package fr.etienneguerlain.fruitamax;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
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
import java.net.URLEncoder;
import java.util.Iterator;


/*

    This activity is similar to the loginActivity in its principle.

    It displays a form where the user sets pieces of information about a sale he/she wants to had
    to the platform.

    When the user "saves" the sale, it performs a check (that all fields are complete) and send the
    data to the server.

    On the server response (a JSON object), it parses it and displays an error message / a confirmation

    If adding the new sale was a success (saved on the server), this activity saves the sale on the
    local SQLite database, and erase all the fields, so that the user can post another sale

 */

public class NewSaleActivity extends AppCompatActivity {

    Button saveSaleButton;
    TextView title;
    TextView quantity;
    TextView unit;
    TextView price;

    // Holds the loader
    FrameLayout progressBarHolder;

    // Useful for showing/hiding the progress loader
    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_sale);

        getLayoutComponents();

        saveSaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                processNewSaleRegistration();
            }
        });
    }

    // This method retrieve the components from the XML layout file
    public void getLayoutComponents(){
        saveSaleButton = findViewById(R.id.saveNewSaleButton);
        title = findViewById(R.id.newSaleTitle);
        quantity = findViewById(R.id.newSaleQuantity);
        unit = findViewById(R.id.newSaleUnit);
        price = findViewById(R.id.newSalePrice);
        progressBarHolder = findViewById(R.id.progressBarHolder);
    }


    // This method runs when the user press the "Save" button
    // It checks that all fields are complete and send/receive data to/from the server
    public void processNewSaleRegistration(){


        // Reminds the user to turn on geolocation if it is off
        if(Data.lat == 0 && Data.lng == 0){

            Toast.makeText(this, "Please turn on geolocation", Toast.LENGTH_SHORT).show();
            return;
        }

        if(checkAllFieldsAreFilled()){

            // If all fields are filled, we run the asyncTask that queries the server
            // Storing new sale information in appropriate object
            NewSaleInformation newSaleInformation = new NewSaleInformation(
                    title.getText().toString(),
                    quantity.getText().toString(),
                    unit.getText().toString(),
                    price.getText().toString()
            );

            // Performing the query to server asynchronously
            new SaveNewSaleTask().execute(newSaleInformation);

        }else{
            // If one of the fields is empty, we display a toast
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
        }
    }

    // This function tells if all fields are filled
    private boolean checkAllFieldsAreFilled(){

        return
                !title.getText().toString().equals("") &&
                        !quantity.getText().toString().equals("") &&
                        !unit.getText().toString().equals("") &&
                        !price.getText().toString().equals("");
    }







    // This class holds all the newSale informatin
    private class NewSaleInformation{

        public String title;
        public String quantity;
        public String unit;
        public String price;

        public NewSaleInformation(String Title, String Quantity, String Unit, String Price){
            title = Title;
            quantity = Quantity;
            unit = Unit;
            price = Price;
        }

    }




    // This asyncTask sends data to the server, parses the response and displays a message to the user
    private class SaveNewSaleTask extends AsyncTask<NewSaleInformation, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Disabling buttons
            saveSaleButton.setEnabled(false);

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
            saveSaleButton.setEnabled(true);


            try {
                JSONObject res = new JSONObject(response);

                if(res.has("err")){

                    // Response to the login request results in an error.
                    String err = res.getString("err");

                    // We display this error
                    Toast.makeText(getApplicationContext(), err, Toast.LENGTH_SHORT).show();


                }else{

                    // If the request was a success, we have display a confirmation
                    Toast.makeText(NewSaleActivity.this, "Sale added :)", Toast.LENGTH_SHORT).show();

                    // We save the just-added sale into the local DB

                    // First we retriveve the user token (used to identify sales owner)
                    SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                            getString(R.string.sharesPreferencesFile),
                            Context.MODE_PRIVATE
                    );

                    String token = sharedPreferences.getString(getString(R.string.token), null);


                    // We create the sale object
                    Sale newSale = new Sale();

                    // We set the object
                    newSale.user_token = token;
                    newSale.title = title.getText().toString();
                    newSale.quantity = Double.parseDouble(quantity.getText().toString());
                    newSale.price = Double.parseDouble(price.getText().toString());
                    newSale.unit = unit.getText().toString();

                    // We save the object to database
                    Data.DB.saleDao().insertAll(newSale);

                    Log.i("NEW SALE DB", "Sale added to database");


                    // And we reset all fields
                    title.setText("");
                    quantity.setText("");
                    price.setText("");
                    unit.setText("");

                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected String doInBackground(NewSaleInformation... newSales) {


            try {
                URL url = new URL("http://fruitamax.etienneguerlain.fr/api/sale");

                // This JSON Object holds all the information from the fields of the form
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("name", newSales[0].title);
                postDataParams.put("quantity", newSales[0].quantity);
                postDataParams.put("unit", newSales[0].unit);
                postDataParams.put("price", newSales[0].price);
                postDataParams.put("lat", Data.lat);
                postDataParams.put("lng", Data.lng);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // It is a POST request...
                connection.setRequestMethod("POST");

                // ... and a url encoded one
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // Get token from the Shared Preferences
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(
                        getString(R.string.sharesPreferencesFile),
                        Context.MODE_PRIVATE
                );

                String token = sharedPreferences.getString(getString(R.string.token), null);

                // The server has to know who we are to respond to our queries
                connection.setRequestProperty("x-access-token", token);
                connection.setDoInput(true);
                connection.setDoOutput(true);


                // We write data to the server...
                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();


                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {

                    // ... and we read the response
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


    // This method builds up an object that holds all the information from the form
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
}
