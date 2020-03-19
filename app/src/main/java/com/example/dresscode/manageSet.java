package com.example.dresscode;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class manageSet extends AppCompatActivity{

    private Set mCurrSet;
    private Spinner seasonSpinner;
    private Button editBtn, addItemsBtn, takeOutSetBtn;
    private TextView setOutOfCloset;
    private String newSeason;
    private ItemWithButtonListAdapter mItemWithButtonListAdapter;
    private List<String> mSeasons = new ArrayList<>();
    private ListView mItemImageList;
    private int index = 0;
    private Boolean didTaskFail = false;
    private ArrayAdapter seasonAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_set);
        setTitle("Manage Set");

        Intent showSetIntent = getIntent();
        mCurrSet = (Set)showSetIntent.getSerializableExtra("setObject");
        initializeViews();

        addItemsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(manageSet.this, ChooseItemsForSet.class);
                intent.putExtra("currSet", mCurrSet);
                intent.putExtra("isNewSet", constants.ADD_ITEMS_TO_EXISTING_SET);
                startActivityForResult(intent, constants.ADD_ITEMS_TO_EXISTING_SET);
            }
        });

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (editBtn.getText() == "Edit") {
                    seasonSpinner.setEnabled(true);
                    editBtn.setText("Save");
                }
                else { // save
                    editBtn.setText("Edit");
                    newSeason = seasonSpinner.getSelectedItem().toString();
                    seasonSpinner.setEnabled(false);
                    seasonSpinner.setClickable(false);
                    if (!newSeason.equals(mCurrSet.getSeason().toString())) // update DB only if season has changed
                    {
                        updateSetSeasonTask task = new updateSetSeasonTask(v);
                        String query = new String(constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=updateSetsSeason&email=" + Closet.getInstance().getUserEmail() + "&set_id=" + mCurrSet.getId() + "&season=" + newSeason);
                        task.execute(query);
                    }
                }
            }
        });

        takeOutSetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateIsInClosetTask task = new updateIsInClosetTask(mCurrSet.getItems().get(index));
                String query = new String(constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=updateIsInCloset&email="+ Closet.getInstance().getUserEmail()+ "&item_id="+mCurrSet.getItems().get(index).getId()+"&is_in_closet=0&shelf_number=");
                task.execute(new String[] {query});

            }
        });
    }

    private void initializeViews() {

        mItemImageList = (ListView) findViewById(R.id.itemListView);
        takeOutSetBtn = (Button)findViewById(R.id.take_out_set_btn);
        setOutOfCloset = (TextView)findViewById(R.id.setOutOfClosetTxt);
        editBtn = (Button) findViewById(R.id.editBtn);
        editBtn.setText("Edit");
        addItemsBtn = (Button) findViewById(R.id.addItems_btn);
        mItemWithButtonListAdapter = new ItemWithButtonListAdapter(this, R.layout.adapter_item_with_button, mCurrSet.getItems(), mCurrSet.getId(), this);
        mItemImageList.setAdapter(mItemWithButtonListAdapter);
        initializeSeasonSpinner();
        setTakeOutSetBtnEnablity();
    }

    private void setTakeOutSetBtnEnablity() {

        boolean isSetInCloset = true;

        for (Item item : mCurrSet.getItems()){

            if(!(item.isInCloset())){
                isSetInCloset = false;
                break;
            }
        }

        if (isSetInCloset){
            takeOutSetBtn.setEnabled(true);
            setOutOfCloset.setVisibility(View.GONE);
        }
        else{
            takeOutSetBtn.setEnabled(false);
            setOutOfCloset.setVisibility(View.VISIBLE);
            setOutOfCloset.setTextColor(Color.parseColor("#c97a90"));
        }
    }

    private void initializeSeasonSpinner() {

        seasonSpinner = (Spinner) findViewById(R.id.season_spinner);
        seasonSpinner.setEnabled(false);
        seasonSpinner.setClickable(false);
        mSeasons.addAll(ESeason.names());
        seasonAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, mSeasons);
        seasonSpinner.setAdapter(seasonAdapter);
        int spinnerPosition = seasonAdapter.getPosition(mCurrSet.getSeason().toString());
        seasonSpinner.setSelection(spinnerPosition);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }

    public void resetCurrentSet(int itemId){
        Item item = Closet.getItemById(itemId);
        Closet.getInstance().getSetById(mCurrSet.getId()).getItems().remove(item);
        mCurrSet = Closet.getInstance().getSetById(mCurrSet.getId());
        setTakeOutSetBtnEnablity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == constants.ADD_ITEMS_TO_EXISTING_SET){
            // if set was changed
            if (!(Closet.getInstance().isSetsUpdated())) {
                GetSetByID task = new GetSetByID(constants.ADD_ITEMS_TO_EXISTING_SET);
                task.execute(new String[]{"http://192.116.98.71:81/DBServlet_war/DBServlet?requestType=getSpecificSet&email=" + Closet.getInstance().getUserSettings().getEmail() + "&set_id=" + mCurrSet.getId()});
            }
        }
    }

    // Class for updating set season
    private class updateSetSeasonTask extends AsyncTask<String, Void, String> {
        private View v;

        public updateSetSeasonTask(View v){
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
                int spinnerPosition = seasonAdapter.getPosition(mCurrSet.getSeason().toString());
                seasonSpinner.setSelection(spinnerPosition);
                popErrorDialog(true);
            }
            else {
                Snackbar.make(v, "Saved", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                seasonSpinner.setEnabled(false);
                mCurrSet.setSeason(ESeason.fromString(newSeason));
                Closet.getInstance().setSetsUpdated(false);
            }
        }
    }

    private void popErrorDialog(Boolean isPost) {
        ErrorDialog errorDialog = new ErrorDialog(manageSet.this, manageSet.this, isPost);
        // do when errorDialog finish its work
        errorDialog.setDialogResult(new ErrorDialog.OnMyDialogResult(){
            public void finish(String result){
            }
        });
        errorDialog.show();
    }

    @Override
    public void onBackPressed(){
        //This result intent is used currently ONLY when going back to "ShowSetsAccordingToWeather".
        // We don't use the intent in other activities we go back to from manageSet
        Intent resultIntent = new Intent();
        //Sending the set to ShowSetsAccordingToWeather, we will check there is the set's items are in the closet
        resultIntent.putExtra("currEditedSet", mCurrSet);
        setResult(constants.EDIT_SET_ITEMS_CODE, resultIntent);
        super.onBackPressed();
    }

    private class updateIsInClosetTask extends AsyncTask<String, Void, String> {

        private Item item;

        @Override
        protected String doInBackground(String... urls) {
            String output = null;
            for (String url : urls) {
                output = getOutputFromUrl(url);
            }
            return output;
        }

        public updateIsInClosetTask(Item item){
            this.item = item;
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
            if(didTaskFail || output.equals(constants.DB_EXCEPTION)) {
                didTaskFail = false;
                popErrorDialog(true);
            }
            else {
                //updating the cache
                Closet.getItemById(item.getId()).setInCloset(false);
                //updating mCurrSet for showSetsAccordingToWeather
                item.setInCloset(false);
                takeOutSetBtn.setEnabled(false);
                //At least one item was successfully taken out of closet - "Take out set" button
                //should be disabled
                setOutOfCloset.setVisibility(View.VISIBLE);
                setOutOfCloset.setTextColor(Color.parseColor("#c97a90"));
                index++;

                if (index < mCurrSet.getItems().size()) {
                    updateIsInClosetTask task = new updateIsInClosetTask(mCurrSet.getItems().get(index));
                    String query = new String(constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=updateIsInCloset&email=" + Closet.getInstance().getUserEmail() + "&item_id=" + mCurrSet.getItems().get(index).getId() + "&is_in_closet=0&shelf_number=");
                    task.execute(new String[]{query});
                }
            }
        }
    }

    private class GetSetByID extends AsyncTask<String, Void, String> {
        private int code;

        public GetSetByID(int code){
            this.code = code;
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
                popErrorDialog(false);
            }
            else {
                //get the set from the db, list with only one set
                List<Set> updatedSetsFromDB = DBResultsConversionFunctions.convertDBResultToSets(output);
                if (!updatedSetsFromDB.isEmpty()) {
                    Set setToRemove = Closet.getInstance().getSetById(mCurrSet.getId());
                    Closet.getInstance().getSets().remove(setToRemove);

                    mCurrSet = updatedSetsFromDB.get(0);
                    Closet.getInstance().getSets().add(mCurrSet);
                    mItemWithButtonListAdapter.updateListToDisplay(mCurrSet.getItems());
                    setTakeOutSetBtnEnablity();
                }
            }
        }
    }
}
