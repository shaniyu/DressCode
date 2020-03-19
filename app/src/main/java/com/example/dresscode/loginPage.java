package com.example.dresscode;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class loginPage extends AppCompatActivity {
    private static final String TAG = "loginPage";
    private Button signIn, signUp;
    private userSettings loggedInUserSettings = new userSettings();
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInAccount mAccount;
    private Boolean didTaskFail = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        getSupportActionBar().hide();

        signIn = (Button)findViewById(R.id.btn_sign_in);
        signUp = (Button)findViewById(R.id.btn_sign_up);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent logOutIntent = getIntent();
        if((mAccount = (GoogleSignInAccount)logOutIntent.getParcelableExtra("log_out")) != null)
        {       //  we got to loginPage from homePage with log out button
            signOut();
            Intent intent = new Intent(loginPage.this, MainActivity.class);
            intent.putExtra("log_out", mAccount);
            startActivity(intent);
        }

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

    }

    @Override
    //this function is called when other activity finish and created an intent
    protected void onActivityResult( int requestCode, int resultCode, Intent dataIntent) {
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(dataIntent);
            handleSignInResult(task);
        }
        else if(requestCode == constants.ERROR_PAGE_CODE){

        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            mAccount = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            //Now we need to check if the user we just logged in with is in the DB
            handleAfterSignInWithGoogle();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Unable to connect. \nPlease check your internet connection", Toast.LENGTH_LONG).show();
        }
    }

    private void handleAfterSignInWithGoogle() {
        String userEmail = mAccount.getEmail();
        String userName = mAccount.getDisplayName();
        findUser();
    }

    private void findUser()
    {
        findUserTask task = new findUserTask();
        task.execute(new String[] {constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=findUser&email="+mAccount.getEmail()});
    }
    //Handle user's Yes-No answer to creating a DressCode account question
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            String userEmail = mAccount.getEmail();
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked- go to sign up activity
                    //Insert the user email in the DB
                    registerUserInDB();
            }
        }
    };

    private void registerUserInDB() {
        //This function needs to insert the user email in the DB
        insertUserToDBTask task = new insertUserToDBTask();
        task.execute(new String[] {constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=newUser&email="+mAccount.getEmail()+"&name=&city=&num_of_shelves="});
    }

    private void getUserDataFromDB(String email) {
        //This function gets all the data for user using the user's email
        getUserSettingsTask task = new getUserSettingsTask();
        task.execute(new String[] {constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=getUserDetails&email="+mAccount.getEmail()});
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                    }
                });
    }

    private class getUserSettingsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String output = null;
            for (String url : urls) {
                output = getOutputFromUrl(url);
            }
            return output;
        }

        private String getOutputFromUrl(String url) {
            StringBuffer output = new StringBuffer("");
            try {
                InputStream stream = getHttpConnection(url);
                if(stream != null) {
                    BufferedReader buffer = new BufferedReader(
                            new InputStreamReader(stream));
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
                Intent intent = new Intent(loginPage.this, ErrorPage.class);
                startActivity(intent);
            }
            else {
                userSettings tempUserSettings = DBResultsConversionFunctions.convertDBReusltToUserSettings(output);
                if (tempUserSettings != null) {
                    //registration completed
                    loggedInUserSettings = tempUserSettings;
                    Intent intent = new Intent(loginPage.this, homePageActivity.class);
                    intent.putExtra("settingsObject", loggedInUserSettings);
                    intent.putExtra("account", mAccount);
                    startActivityForResult(intent, constants.SETTINGS_CODE);

                } else {
                    //If the user didn't complete his registration then we want to move to SignIn activity to complete the process
                    Intent intent = new Intent(loginPage.this, SignUp.class);
                    intent.putExtra("sign_up", loggedInUserSettings);
                    intent.putExtra("account", mAccount);
                    startActivityForResult(intent, constants.SIGN_UP);
                }
            }
        }
    }

    private class findUserTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String output = null;
            for (String url : urls) {
                output = getOutputFromUrl(url);
            }
            return output;
        }

        private String getOutputFromUrl(String url) {
            StringBuffer output = new StringBuffer("");
            try {
                InputStream stream = getHttpConnection(url);
                if(stream != null) {
                    BufferedReader buffer = new BufferedReader(
                            new InputStreamReader(stream));
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
                Intent intent = new Intent(loginPage.this, ErrorPage.class);
                startActivity(intent);
            }
            else {
                boolean isUserInDB = DBResultsConversionFunctions.convertDBResultToIsUserInDB(output);
                if (isUserInDB) {
                    //Get all data from DB and update loggedInUserSettings
                    getUserDataFromDB(mAccount.getEmail());
                } else {
                    // pass user to complete registration ( actually register him)
                    AlertDialog.Builder builder = new AlertDialog.Builder(loginPage.this);
                    builder.setMessage("We created an account for you. Please finish your registration.").setPositiveButton("Ok", dialogClickListener).show();
                }
            }
        }

    }

    //When pressing the "back" button on android- we exit the application
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    private class insertUserToDBTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String output = null;
            for (String url : urls) {
                output = getOutputFromUrl(url);
            }
            return output;
        }

        private String getOutputFromUrl(String url) {
            StringBuffer output = new StringBuffer("");
            try {
                InputStream stream = getHttpConnection(url);
                if(stream != null) {
                    BufferedReader buffer = new BufferedReader(
                            new InputStreamReader(stream));
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
                Intent intent = new Intent(loginPage.this, ErrorPage.class);
                startActivity(intent);
            }
            else {
                loggedInUserSettings.setEmail(mAccount.getEmail());
                Intent intent = new Intent(loginPage.this, SignUp.class);
                intent.putExtra("sign_up", loggedInUserSettings);
                intent.putExtra("account", mAccount);
                startActivityForResult(intent, constants.SIGN_UP);
            }
        }
    }
}


