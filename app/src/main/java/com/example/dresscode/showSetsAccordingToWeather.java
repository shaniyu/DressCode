package com.example.dresscode;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
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

public class showSetsAccordingToWeather extends AppCompatActivity {

    private ListView mListView;
    private ArrayList<Set> mSetList; //Holds items to display in the listView in the activity
    private SetListAdapter mSetListAdapter;
    private TextView noSetsText, weatherTitle;
    private ImageView appologyImage;
    private ESeason currSeason;
    private int mCurrSetIDClicked;
    private Boolean didTaskFail = false;
    private Set mCurrSet;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_sets_according_to_weather);
        setTitle("Sets for this weather");
        noSetsText = findViewById(R.id.noItemsTitle);
        weatherTitle = findViewById(R.id.weatherTitle);
        appologyImage = findViewById(R.id.appologyImg);

        displaySetList();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCurrSetIDClicked = mSetList.get(position).getId();
                mCurrSet = mSetList.get(position);
                Intent manageSetIntent = new Intent(showSetsAccordingToWeather.this, manageSet.class);;
                manageSetIntent.putExtra("setObject", mSetList.get(position));
                startActivityForResult(manageSetIntent, 1); // we don't really need the request code so we put 1
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == constants.EDIT_SET_ITEMS_CODE) {
            Set returnedSet = (Set)data.getSerializableExtra("currEditedSet");

            //Some of the set's items are not in closet - we only need to remove it from mSetList and the closet's sets list will be updated
            //when returning to home page
            if(!Set.allItemsOfSetInTheCloset(returnedSet)){
                removeSetFromSetList(returnedSet.getId());
                checkIfShouldDisplayDoNotHaveSets();
            }
            // if set was changed (its season or items were changed)
            else if (!(Closet.getInstance().isSetsUpdated())) {
                //If the set's season doesn't match the current season
                //then we only need to remove it from mSetList and the closet's sets list will be updated
                //when returning to home page
                if(returnedSet.getSeason() != currSeason){
                    removeSetFromSetList(returnedSet.getId());
                    checkIfShouldDisplayDoNotHaveSets();
                }
                //The set should appear on the list and should be updated
                //removing it from closet and mSetList, getting the updated set and adding it to closet and mSetList
                else {
                    Set setToRemove = Closet.getInstance().getSetById(mCurrSetIDClicked);
                    Closet.getInstance().getSets().remove(setToRemove);
                    GetSetByID task = new GetSetByID();
                    task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=getSpecificSet&email=" + Closet.getInstance().getUserSettings().getEmail() + "&set_id=" + mCurrSetIDClicked});
                }
            }
        }
    }

    private void removeSetFromSetList(int id) {
        List<Set> tempList = (List<Set>)mSetList.clone();
        for (Set set : tempList)
        {
            if (set.getId() == id)
                mSetList.remove(set);
        }
    }

    private void checkIfShouldDisplayDoNotHaveSets(){
        if (mSetList.size()==0) {
            // no sets to show.. put a proper message
            noSetsText.setText("No sets to show");
            appologyImage.setVisibility(View.VISIBLE);
        }
        else {
            noSetsText.setText("");
            appologyImage.setVisibility(View.GONE);
        }
        mSetListAdapter.notifyDataSetChanged();
    }

    private void displaySetList() {
        mListView = (ListView) findViewById(R.id.setListView);
        mSetList = new ArrayList<Set>();
        Intent intent = getIntent(); // get the data that was passed from homepage
        mSetList = (ArrayList<Set>) intent.getSerializableExtra("setsObject");

        double temprature = intent.getDoubleExtra("temprature",0);
        weatherTitle.setText("It is "+temprature+"° right now in "+Closet.getInstance().getUserSettings().getCity().toString());

        currSeason = ESeason.getCurrentSeason(temprature);

        // connect the sets to the list adapter
        mSetListAdapter = new SetListAdapter(this, R.layout.adapter_set_for_weather__layout, mSetList, this);
        mListView.setAdapter(mSetListAdapter);
        if (mSetList.size()==0)
        {
            // no sets to show.. put a proper message
            noSetsText.setText("Sorry, none of your sets match today's weather");
            appologyImage.setVisibility(View.VISIBLE);
        }
    }

    private class GetSetByID extends AsyncTask<String, Void, String> {
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
            if (didTaskFail || output.equals(constants.DB_EXCEPTION)) {
                didTaskFail = false;
                popErrorDialog(false);
            } else {
                //get the set from the db, list with only one set
                List<Set> updatedSetsFromDB = DBResultsConversionFunctions.convertDBResultToSets(output);

                if (!updatedSetsFromDB.isEmpty()) {
                    removeSetFromSetList(mCurrSetIDClicked);
                    mCurrSet = updatedSetsFromDB.get(0);
                    Closet.getInstance().setSetsUpdated(true);

                    // Already deleted the edited set from closet and now add it back
                    Closet.getInstance().getSets().add(mCurrSet);
                    mSetList.add(mCurrSet);
                } else {
                    // the set doesn't exist anymore, last item of it was deleted
                    // need to update mSetList to have only sets from the closet
                    removeSetFromSetList(mCurrSetIDClicked);
                }

                checkIfShouldDisplayDoNotHaveSets();
            }
        }
    }

    private void popErrorDialog(Boolean isPost) {
        ErrorDialog errorDialog = new ErrorDialog(showSetsAccordingToWeather.this, showSetsAccordingToWeather.this, isPost);
        // do when errorDialog finish its work
        errorDialog.setDialogResult(new ErrorDialog.OnMyDialogResult(){
            public void finish(String result){}
        });
        errorDialog.show();
    }
}
