package fr.etienneguerlain.fruitamax;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/*

    This activity should handle operations to register a new user in the platform.

    To do so, it would use the same logic as the loginActivity:
    - Check all fields of the form are filled
    - Get the data and build a JSON Object, url encoded
    - Send a POST request
    - Read and parse the response (it would be a JSON Object)
    - Display a toast to the user accordingly

    This activity has not been implemented yet.

    However, back-end logic has been implemented on server.

    To test the back-end, send a url-encoded POST request to
    http://fruitamax.etienneguerlain.fr/register containing the following data:
    - email
    - password1
    - password2
    - firstname
    - lastname

    (Use the header : "Content-Type" : "application/x-www-form-urlencoded"

 */

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }
}
