package com.example.dresscode;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

public class SettingsPage extends AppCompatActivity {
    private Intent intent;
    private Closet userCloset;
    private Button saveButton, openChooser;
    private ImageButton back;
    private EditText nameInput;
    private TextView cityInput, nameError, cityError;
    private userSettings userSettings;
    private SpinnerDialog spinnerDialogCities;
    private ArrayList<String> cities = new ArrayList<>();
    private Boolean didTaskFail = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_page);
        getSupportActionBar().hide();
        Intent intent = getIntent(); // maybe dont need it

        userCloset = Closet.getInstance();
        userSettings = userCloset.getUserSettings();

        setEditText();
        makeSubmitBtn();
        handleSpinnerDialog();
        makeBackBtn();
    }

    private void setEditText()
    {
        cityInput = findViewById(R.id.textViewCurrCity);
        cityInput.setHint(userSettings.getCity().toString()); // set default city string to the current user city
        cityInput.setText(userSettings.getCity().toString());
        nameInput = findViewById(R.id.nameField);
        nameInput.setHint(userSettings.getName()); // set default city string to the current user city
        nameInput.setText(userSettings.getName());
        // non edible text
        nameError = findViewById(R.id.nameErrorTxt);
        cityError = findViewById(R.id.cityErrorTxt);
    }
    private void makeSubmitBtn()
    {
        saveButton =findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean validInput = true;
                if (cityInput.getText().toString().isEmpty())
                {
                    validInput = false;
                    cityError.setText("Enter a city");
                }
                else
                {
                    cityError.setText("");
                }

                if (nameInput.getText().toString().isEmpty())
                {
                    validInput = false;
                    nameError.setText("Enter a name");
                }
                else if (nameInput.getText().toString().length() > constants.maxNameLength)
                {
                    validInput = false;
                    nameError.setText("Name is too long");
                }
                else
                {
                    nameError.setText("");
                }
                if (validInput == true)
                {
                    updateUserSettingsTask task = new updateUserSettingsTask(v);
                    String url =(constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=updateUser&email="+userCloset.getUserSettings().getEmail()+ "&name="+nameInput.getText().toString()+"&city="+cityInput.getText().toString()+"&num_of_shelves="+userCloset.getUserSettings().getNumberOfShelves());
                    task.execute(new String[] {url});

                }
            }
        });
    }

    private void handleSpinnerDialog()
    {
        openChooser = (Button)findViewById(R.id.buttonOpenChooser);
        cities.addAll(eCity.names());

        spinnerDialogCities = new SpinnerDialog(SettingsPage.this, cities, "Select City");
        spinnerDialogCities.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(String s, int i) {
                cityInput.setText(s);
            }
        });

        openChooser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerDialogCities.showSpinerDialog();
            }
        });
    }

    private void makeBackBtn()
    {
        back = findViewById(R.id.backButton);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });
    }

    private class updateUserSettingsTask extends AsyncTask<String, Void, String> {
        private View v;

        updateUserSettingsTask(View v) {
            this.v = v;
        }
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
                popErrorDialog(true);
            }
            else {
                Snackbar.make(v ,"Saved", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                userCloset.getUserSettings().setCity(eCity.fromString(cityInput.getText().toString()));
                userCloset.getUserSettings().setName(nameInput.getText().toString());
                // tell the user that information is saved
                setResult(RESULT_OK, intent);
            }
        }
    }

    private void popErrorDialog(Boolean isPost) {
        ErrorDialog errorDialog = new ErrorDialog(SettingsPage.this, SettingsPage.this, isPost);
        // do when errorDialog finish its work
        errorDialog.setDialogResult(new ErrorDialog.OnMyDialogResult(){
            public void finish(String result){ }
        });
        errorDialog.show();
    }
}
