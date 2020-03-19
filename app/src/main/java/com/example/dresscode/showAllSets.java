package com.example.dresscode;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;


public class showAllSets extends AppCompatActivity {

    private ListView mListView;
    private List<Set> mSetList;
    private SetListAdapter mSetListAdapter;
    private ESeason seasonToFilterBy, chosenSeason;
    private ImageView appologyImage;
    private TextView noSetsText;
    private int mCurrSetIDClicked;
    private Set mUpdatedSet;
    private Boolean didTaskFail = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_sets);
        appologyImage = findViewById(R.id.appologyImg);
        noSetsText = findViewById(R.id.noItemsTitle);
        seasonToFilterBy = null;
        setTitle("All Sets");
        displaySetList();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent manageSetIntent = new Intent(showAllSets.this, manageSet.class);;
                mCurrSetIDClicked = mSetList.get(position).getId();
                manageSetIntent.putExtra("setObject", mSetList.get(position));
                startActivityForResult(manageSetIntent,1);
            }
        });
    }

    private void createChooseSeasonDialog() {
        AlertDialog.Builder chooseSeasonDialogBuilder = new AlertDialog.Builder(showAllSets.this);
        chooseSeasonDialogBuilder.setTitle("Choose one of the following season for your new set");

        final ArrayAdapter<ESeason> arrayAdapter = new ArrayAdapter<ESeason>(showAllSets.this, android.R.layout.simple_list_item_1);
        arrayAdapter.add(ESeason.summer);
        arrayAdapter.add(ESeason.winter);
        arrayAdapter.add(ESeason.crossingSeason);

        chooseSeasonDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        chooseSeasonDialogBuilder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    chosenSeason = arrayAdapter.getItem(which);
                    Intent intent = new Intent(showAllSets.this, ChooseItemsForSet.class);
                    intent.putExtra("season", chosenSeason.toString());
                    intent.putExtra("isNewSet", constants.CREATE_NEW_SET_CODE);
                    startActivityForResult(intent, constants.CREATE_NEW_SET_CODE);
                }
                catch (Exception e)
                {
                    e.getMessage();
                }
            }
        });
        chooseSeasonDialogBuilder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == constants.CREATE_NEW_SET_CODE){
            //User came back from ChooseItemsForSet page and the isSetsUpdated flag is false - new set was added
            if(!Closet.getInstance().isSetsUpdated()) {
                GetLastSetTask task = new GetLastSetTask();
                task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=getLastSet&email=" + Closet.getInstance().getUserSettings().getEmail()});
            }
        }
        else if (resultCode == constants.EDIT_SET_ITEMS_CODE) {
            // if set was changed
            if (!(Closet.getInstance().isSetsUpdated())) {
                Set setToRemove = Closet.getInstance().getSetById(mCurrSetIDClicked);
                Closet.getInstance().getSets().remove(setToRemove);
                GetSetByID task = new GetSetByID(constants.EDIT_SET_ITEMS_CODE);
                task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=getSpecificSet&email=" + Closet.getInstance().getUserSettings().getEmail() + "&set_id=" + mCurrSetIDClicked});
            }
        }
        else if (requestCode == constants.DELETE_SET_CODE){
            if(!Closet.getInstance().isSetsUpdated()){
                getAllSetsForUserTask task = new getAllSetsForUserTask(this);
                task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=getAllSetsForUser&email=" + Closet.getInstance().getUserEmail()});
            }
            else {
                //Getting updated set list from cache
                mSetList.clear();
                mSetList.addAll(Closet.getInstance().getSets());
                mSetList = Set.filterSetsBySeason(seasonToFilterBy, mSetList);
                Collections.sort(mSetList, Set.setIdComparator); // sort the list by set id
                mSetListAdapter = new SetListAdapter(this, R.layout.adapter_set_view_layout, mSetList, this);
                mListView.setAdapter(mSetListAdapter);
                checkIfShouldDisplayDoNotHaveSets();
            }
        }
    }

    private void displaySetList() {

        mListView = (ListView) findViewById(R.id.setListView);
        mSetList = new ArrayList<>();
        mSetList.addAll(Closet.getInstance().getSets());
        Collections.sort(mSetList,Set.setIdComparator); // sort the list by set id

        //Change the set list given to the mSetListAdapter to Closet.getSets or something list that
        mSetListAdapter = new SetListAdapter(this, R.layout.adapter_set_view_layout, mSetList, this);
        mListView.setAdapter(mSetListAdapter);
        checkIfShouldDisplayDoNotHaveSets();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_show_all_sets, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_discard:
                deleteSet();
                return true;
            case R.id.action_filter_sets:
                filterSetListBySeason();
                return true;
            case R.id.action_new_set:
                createNewSet();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteSet() {

        Intent deleteSetsIntent = new Intent(showAllSets.this, ChooseSetsToDelete.class);
        startActivityForResult(deleteSetsIntent, constants.DELETE_SET_CODE);
    }

    private void filterSetListBySeason()
    {
        filterSetsDialog filterSets = new filterSetsDialog(seasonToFilterBy, this, this, new filterSetsDialog.DialogListener() {
            @Override
            public void ready(ESeason i_season) {
                // this function is called by the filterSetsDialog when it finish its job (view sets btn was clicked)
                // we got the parameters from the dialog, now filter by them
                List<Set> filteredList = Set.filterSetsBySeason(i_season, Closet.getInstance().getSets());
                //update the list
                mSetList.clear(); // clean items list
                mSetList.addAll(filteredList); // add all items according to filtering
                mSetListAdapter.notifyDataSetChanged(); // notify adapter that the list has changed
                if (mSetList.isEmpty())
                {
                    noSetsText.setText("No sets to show");
                    appologyImage.setVisibility(View.VISIBLE);
                }
                else
                {
                    noSetsText.setText("");
                    appologyImage.setVisibility(View.INVISIBLE);
                }
                // save the chosen parameters for the next filter
                seasonToFilterBy = i_season;
            }

            @Override
            public void cancelled() {
                // canceled the filtering, do nothing.
            }
        });
        filterSets.show();
    }

    private void createNewSet()
    {
        createChooseSeasonDialog();
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
            if (didTaskFail || output.equals(constants.DB_EXCEPTION)) {
                didTaskFail = false;
                popErrorDialog(false);
            } else {
                //get the set from the db, list with only one set
                List<Set> updatedSetsFromDB = DBResultsConversionFunctions.convertDBResultToSets(output);
                if (!updatedSetsFromDB.isEmpty()) {
                    mUpdatedSet = updatedSetsFromDB.get(0);
                    Closet.getInstance().setSetsUpdated(true);

                    if (code == constants.CREATE_NEW_SET_CODE) {
                        mSetList.add(mUpdatedSet);
                        Closet.getInstance().getSets().add(mUpdatedSet);
                    } else if (code == constants.EDIT_SET_ITEMS_CODE) {
                        // came here from manage set, already deleted the edited set and now add it back
                        Closet.getInstance().getSets().add(mUpdatedSet);
                        mSetList.clear();
                        mSetList.addAll(Closet.getInstance().getSets());
                    }
                } else {
                    // the set doesn't exist anymore, last item of it was deleted
                    // need to update mSetList to have only sets from the closet
                    mSetList.clear();
                    mSetList.addAll(Closet.getInstance().getSets());
                }
                List<Set> filteredList = Set.filterSetsBySeason(seasonToFilterBy, mSetList);
                mSetList.clear(); // clean items list
                mSetList.addAll(filteredList); // add all items according to filtering
                Collections.sort(mSetList, Set.setIdComparator); // sort the list by set id
                checkIfShouldDisplayDoNotHaveSets();
            }
        }
    }

    private class GetLastSetTask extends AsyncTask<String, Void, String> {
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
                //get the last set from the db, list with only one set
                List<Set> updatedSetsFromDB = DBResultsConversionFunctions.convertDBResultToSets(output);
                if (!updatedSetsFromDB.isEmpty()) {
                    mUpdatedSet = updatedSetsFromDB.get(0);
                    Closet.getInstance().setSetsUpdated(true);
                    mSetList.add(mUpdatedSet);
                    Closet.getInstance().getSets().add(mUpdatedSet);
                } else {
                    // the set doesn't exist anymore, last item of it was deleted
                    // need to update mSetList to have only sets from the closet
                    mSetList.clear();
                    mSetList.addAll(Closet.getInstance().getSets());
                }
                List<Set> filteredList = Set.filterSetsBySeason(seasonToFilterBy, mSetList);
                mSetList.clear(); // clean items list
                mSetList.addAll(filteredList); // add all items according to filtering
                Collections.sort(mSetList, Set.setIdComparator); // sort the list by set id
                checkIfShouldDisplayDoNotHaveSets();
            }
        }
    }

    private void popErrorDialog(Boolean isPost) {
        ErrorDialog errorDialog = new ErrorDialog(showAllSets.this, showAllSets.this, isPost);
        // do when errorDialog finish its work
        errorDialog.setDialogResult(new ErrorDialog.OnMyDialogResult(){
            public void finish(String result){}
        });
        errorDialog.show();
    }

    void checkIfShouldDisplayDoNotHaveSets(){
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

    //using this task in case only some of the sets were deleted and then the DeleteSets task failed
    private class getAllSetsForUserTask extends AsyncTask<String, Void, String> {
        Context context;

        getAllSetsForUserTask(Context context){
            this.context = context;
        }

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
                popErrorDialog(false);
            }
            else {
                // set the items list of the closet
                List<Set> setsList = DBResultsConversionFunctions.convertDBResultToSets(output);
                Closet.getInstance().setSets(setsList);
                Closet.getInstance().setSetsUpdated(true);

                //Getting updated set list from cache
                mSetList.clear();
                mSetList.addAll(Closet.getInstance().getSets());
                mSetList = Set.filterSetsBySeason(seasonToFilterBy, mSetList);
                Collections.sort(mSetList, Set.setIdComparator); // sort the list by set id
                mSetListAdapter = new SetListAdapter(context, R.layout.adapter_set_view_layout, mSetList, (Activity)context);
                mListView.setAdapter(mSetListAdapter);
                checkIfShouldDisplayDoNotHaveSets();
            }
        }
    }
}
