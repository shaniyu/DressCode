package com.example.dresscode;

import android.content.Intent;
import android.view.View;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {
    private TextView txtForDBError, errorHeader;
    private ImageView dbErrorImage;
    private GoogleSignInAccount account;
    private Boolean didTaskFail = false;
    userSettings loggedInUserSettings = new userSettings();

    @Override
     protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtForDBError = findViewById(R.id.txtForDBError);
        errorHeader = findViewById(R.id.errorHeader);
        dbErrorImage = findViewById(R.id.sadClosetImage);
        dbErrorImage.setVisibility(View.INVISIBLE);

        getSupportActionBar().hide();
        try {
            loginAndDirectUser();
        }
        catch (Exception e)
        {
            dbErrorImage.setVisibility(View.VISIBLE);
            errorHeader.setText("Oops...");
            txtForDBError.setText("An error occurred in our database.\nContact us if you want:\n\nyuvalvahaba@gmail.com\n\ninbal.valdman@gmail.com\n\nshaniyunker@gmail.com");

        }
    }

    private void loginAndDirectUser()
    {
        //First check if we reached MainActivity because we logged-out
        //by getting info from the loginPage Activity
        Intent logOutIntent = getIntent();
        account = (GoogleSignInAccount)logOutIntent.getParcelableExtra("log_out");

        if(account != null) //If we got an account from home page with "log_out" tag
        //then it means that we want to log out. Setting the account to null = logging out.
        {
            account = null;
        }
        else // just opened the app
        {
            // Check for existing Google Sign In account
            // if the user is already signed in, the GoogleSignInAccount will be non-null.
            account = GoogleSignIn.getLastSignedInAccount(this);
        }

        if (account != null) {  // logged in successfully
            //Get all data from DB and update loggedInUserSettings
            try {
                getUserDataFromDB(account.getEmail());
            }
            catch (Exception e)
            {
                dbErrorImage.setVisibility(View.VISIBLE);
                errorHeader.setText("Oops...");
                txtForDBError.setText("An error occoured in our database.\nContact us if you want:\n\nyuvalvahaba@gmail.com\n\ninbal.valdman@gmail.com\n\nshaniyunker@gmail.com");
            }
        }
        else //If user isn't logged in then we want to go to loginPage
        {
            Intent intent = new Intent(MainActivity.this, loginPage.class);
            intent.putExtra("login_user", loggedInUserSettings);
            startActivity(intent);

        }
    }

    private void getUserDataFromDB(String email) {
        //This function gets all the data for user using the user's email
        //The assumption is that if the user is already logged in when openning the app
        //then at some point he signed-in with this email and already has it in the DB
        loggedInUserSettings.setEmail(account.getEmail());
        try {
            getUserSettingsTask task = new getUserSettingsTask();
            task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=getUserDetails&email=" + account.getEmail()});
        }
        catch (Exception e) {
            dbErrorImage.setVisibility(View.VISIBLE);
            errorHeader.setText("Oops...");
            txtForDBError.setText("An error occurred in our database.\nContact us if you want:\n\nyuvalvahaba@gmail.com\n\ninbal.valdman@gmail.com\n\nshaniyunker@gmail.com");
        }
    }

    // Class for getting user settings from DB  : email, name. number of shelves, city
    private class getUserSettingsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String output = null;
            for (String url : urls) {
               try { output = getOutputFromUrl(url);}
               catch (Exception e)
               {
                   e.printStackTrace();
               }
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
            } catch (Exception e1) {
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
                dbErrorImage.setVisibility(View.VISIBLE);
                errorHeader.setText("Oops...");
                txtForDBError.setText("An error occurred in our database.\nContact us if you want:\n\nyuvalvahaba@gmail.com\n\ninbal.valdman@gmail.com\n\nshaniyunker@gmail.com");
            }
            else {
                    userSettings tempUserSettings = DBResultsConversionFunctions.convertDBReusltToUserSettings(output);
                    if ((tempUserSettings != null)) // user was in DB and registration completed
                    {
                        loggedInUserSettings = tempUserSettings;
                        //If the user completed his registration then we want to continue to homePageActivity
                        //with the user's details that were updated with getUserDataFromDB function
                        Intent intent = new Intent(MainActivity.this, homePageActivity.class);
                        intent.putExtra("settingsObject", loggedInUserSettings);
                        intent.putExtra("account", account);
                        startActivityForResult(intent, constants.SETTINGS_CODE);
                    } else //user didn't complete his registration, continue to SignUp page
                    {
                        Intent intent = new Intent(MainActivity.this, SignUp.class);
                        intent.putExtra("sign_up", loggedInUserSettings); // loggedInUserSettings only has email address
                        intent.putExtra("account", account);
                        startActivityForResult(intent, constants.SIGN_UP);
                    }
            }
        }
    }
    @Override
    // gets here after using back space from homepage and then enter again to the app
    protected void onActivityResult ( int requestCode, int resultCode, Intent dataIntent) {
        super.onActivityResult(requestCode, resultCode, dataIntent);
        loginAndDirectUser();
    }
}


