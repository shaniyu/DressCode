package com.example.dresscode;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

public class SignUp extends AppCompatActivity {
    private userSettings loggedInUserSettings;
    private Button Save, openChooser;
    private EditText Name, NumberOfShelves;
    private TextView NameError, CityError, NumberOfShelvesError, HelloUser, city;
    private GoogleSignInAccount mAccount;
    private short mNumberOfShelves;
    private SpinnerDialog spinnerDialogCities;
    private ArrayList<String> cities = new ArrayList<>();
    private Boolean didTaskFail = false;

    //When pressing the "back" button on android- we exit the application
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //Find all buttons, textVies and such- by ID
        createAllButtons();
        cities.addAll(eCity.names());
        //Getting data from the intent that got us here to SignUp activity in order to update loggedInUserSettings with
        //the settings that the user will fill in this activity and to pass mAccount to homePage activity (for log-out purposes)
        Intent intent = getIntent();
        loggedInUserSettings = (userSettings) intent.getSerializableExtra("sign_up");
        mAccount = (GoogleSignInAccount)intent.getParcelableExtra("account"); //GoogleSignInAccount implements Parcable and not Serializeable
        HelloUser.setText("Hello ");

        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(checkValidity())
                {
                    updateUserDetails();
                }
            }
        });

        spinnerDialogCities = new SpinnerDialog(SignUp.this, cities, "Select City");
        spinnerDialogCities.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(String s, int i) {
                city.setText(s);
            }
        });

        openChooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerDialogCities.showSpinerDialog();
            }
        });
    }

    //Find all buttons, textVies and such- by ID
    private void createAllButtons()
    {
        Save = (Button)findViewById(R.id.btn_save);
        Name = (EditText)findViewById(R.id.etxt_name);
        Name.requestFocus();
        city = (TextView) findViewById(R.id.textViewChosenCity);
        NumberOfShelves = (EditText)findViewById(R.id.etxt_num_of_shelves);
        NameError = (TextView)findViewById(R.id.name_error);
        CityError = (TextView)findViewById(R.id.city_error);
        NumberOfShelvesError = (TextView)findViewById(R.id.num_of_shelves_error);
        HelloUser = (TextView)findViewById(R.id.txt_hello_user);
        openChooser = (Button)findViewById(R.id.buttonOpenChooser);
    }

    //Checks validy of the EditText fields- Name, City, NumberofShelves
    private boolean checkValidity() {

        boolean validInput = true;

        if (city.getText().toString().equals("Choose city"))
        {
            validInput = false;
            CityError.setTextColor(Color.RED);
            CityError.setText("Enter a city");
        }
        else
        {
            CityError.setText("");
        }

        if (Name.getText().toString().isEmpty())
        {
            validInput = false;
            NameError.setTextColor(Color.RED);
            NameError.setText("Enter a name");
        }
        else if (Name.getText().toString().length() > constants.maxNameLength)
        {
            validInput = false;
            NameError.setTextColor(Color.RED);
            NameError.setText("Name is too long");
        }
        else
        {
            NameError.setText("");
        }

        //Checking if the value in the NumberOfShleves fields is numeric.
        try
        {
            mNumberOfShelves = Short.parseShort(NumberOfShelves.getText().toString());
            NumberOfShelvesError.setText("");
        }
        catch (NumberFormatException e)
        {
            validInput = false;
            NumberOfShelvesError.setTextColor(Color.RED);
            NumberOfShelvesError.setText("Enter number of shelves");
        }

        return(validInput);
    }

    private void updateUserDetails()
    {
        // email address is no longer initialized because when trying to login with user that didn't finish the registration process
        // the loggedInUserSettings value is updated to null
        loggedInUserSettings.setEmail(mAccount.getEmail());
        loggedInUserSettings.setName(Name.getText().toString());
        loggedInUserSettings.setCity(eCity.fromString(city.getText().toString()));
        loggedInUserSettings.setNumberOfShelves(mNumberOfShelves);
        updateUsersRecordInDBTask task = new updateUsersRecordInDBTask();
        task.execute(new String[] {constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=updateUser&email="+loggedInUserSettings.getEmail()+"&name="+loggedInUserSettings.getName()+"&city="+loggedInUserSettings.getCity()+"&num_of_shelves="+loggedInUserSettings.getNumberOfShelves()});
    }


    private class updateUsersRecordInDBTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String output = null;
            for (String url : urls) {
                url = url.replace(" ","%20");
                output = getOutputFromUrl(url);
            }
            return output;
        }

        private String getOutputFromUrl(String url) {
            StringBuffer output = new StringBuffer("");
            try {
                InputStream stream = getHttpConnection(url);
                if(stream != null) {
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(stream));
                    String s = "";
                    while ((s = buffer.readLine()) != null)
                        output.append(s);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return output.toString();
        }

        // Makes HttpURLConnection and returns InputStream
        private InputStream getHttpConnection(String urlString)
                throws IOException {
            InputStream stream = null;
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();

            try {
                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestMethod("GET");
                httpConnection.connect();

                if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    stream = httpConnection.getInputStream();
                }
            } catch (Exception ex) {
                didTaskFail = true;
                ex.printStackTrace();
            }
            return stream;
        }
        @Override
        protected void onPostExecute(String output) {
            if(didTaskFail || output.equals(constants.DB_EXCEPTION)){
                didTaskFail = false;
                Intent intent = new Intent(SignUp.this, ErrorPage.class);
                startActivity(intent);
            }
            else {
                // done add the user to the db
                Intent intent = new Intent(SignUp.this, homePageActivity.class);
                intent.putExtra("settingsObject", loggedInUserSettings);
                intent.putExtra("account", mAccount);
                startActivityForResult(intent, constants.SETTINGS_CODE);
            }
        }
    }
}
