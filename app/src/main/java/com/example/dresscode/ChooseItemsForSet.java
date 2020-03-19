package com.example.dresscode;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
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

public class ChooseItemsForSet extends AppCompatActivity implements android.widget.CompoundButton.OnCheckedChangeListener{

    private ListView mListView;
    private ArrayList<Item> mItemList; //Holds items to display in the listView in the activity
    private ArrayList<Item> mItemListForAction; //Holds the items that are checked for further actions (delete items or create set)
    private ItemListWithCheckBoxAdapter mItemListAdapter;
    private Button save, cancel;
    private TextView noItemsText;
    private ESeason chosenSeason;
    private Set currSet;
    private int isNewSet;
    private int nextAvailableSetId, index = 0;
    private boolean shouldAddFewItems = false, didTaskFail = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_items_for_set);
        setTitle("Choose Items");

        //Getting the chosen Season
        noItemsText = (TextView)findViewById(R.id.noItemsTitle);
        Intent intent = getIntent();
        String chosen = intent.getStringExtra("season");
        chosenSeason = ESeason.fromString(chosen);
        isNewSet = intent.getIntExtra("isNewSet", 0);
        currSet = (Set)intent.getSerializableExtra("currSet");

        createSaveButton();
        createCancelButton();
        displayItemList();
        save.setEnabled(false);
    }

    private void createCancelButton() {
        cancel = (Button) findViewById(R.id.buttonCancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void createSaveButton() {
        save = (Button) findViewById(R.id.buttonSave);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                index = 0;
                if(isNewSet == constants.CREATE_NEW_SET_CODE){
                    //Getting next available set_id from DB and not from cache
                    getNextAvailableSetID task = new getNextAvailableSetID();
                    task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=getNextAvailableSetID&email=" + Closet.getInstance().getUserSettings().getEmail()});
                }
                else if(isNewSet == constants.ADD_ITEMS_TO_EXISTING_SET){

                    if(currSet != null){
                        shouldAddFewItems = true;
                        AddItemToSet task = new AddItemToSet();
                        task.execute(new String[]{constants.IPandPortOfDB+ "/DBServlet_war/DBServlet?requestType=insertItemToSet&email=" + Closet.getInstance().getUserSettings().getEmail() +"&set_id=" + currSet.getId() + "&item_id=" + mItemListForAction.get(index).getId()});
                    }
                    else{
                        Toast.makeText(ChooseItemsForSet.this, "Oops! Something went wrong :(", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

                else{ //if isNewSet == 0
                    Toast.makeText(ChooseItemsForSet.this, "Oops! Something went wrong :(", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    private void displayItemList() {

        mListView = (ListView) findViewById(R.id.itemListView);
        mItemList = new ArrayList<Item>();
        mItemListForAction = new ArrayList<Item>();
        if(isNewSet == constants.ADD_ITEMS_TO_EXISTING_SET){
            if(currSet != null){
                getItemsThatDontExistInSet(currSet);
            }
        }
        else{ // isNewSet == constants.EDIT_SET_ITEMS_CODE
            mItemList.addAll(Closet.getInstance().getItems());
        }

        mItemListAdapter = new ItemListWithCheckBoxAdapter(this, R.layout.adapter_item_with_chkbox_view_layout, mItemList, this.getLocalClassName(), this);
        mListView.setAdapter(mItemListAdapter);
        if (mItemList.size()==0)
        {
            save.setEnabled(false);
            noItemsText.setText(constants.NO_ITEMS_TO_ADD);
        }
        else
        {
            save.setEnabled(true);
            noItemsText.setText("");
        }
    }

    public void getItemsThatDontExistInSet(Set set){

        ArrayList<Item> res = new ArrayList<>();
        boolean shouldAddToSet;

        for(Item item : Closet.getInstance().getItems()){
            shouldAddToSet = true;
            for(Item itemToCheck : currSet.getItems()){
                if(item.getId() == itemToCheck.getId()){
                    shouldAddToSet = false;
                    break;
                }
            }

            if(shouldAddToSet) {
                mItemList.add(item);
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        int position = mListView.getPositionForView(buttonView);

        if (position != mListView.INVALID_POSITION){

            Item item = mItemList.get(position);

            if(isChecked)
            {
                mItemListForAction.add(item);
                item.setIsChecked(true);

            }
            else {
                mItemListForAction.remove(item);
                item.setIsChecked(false);
            }
        }

        if(mItemListForAction.size() == 0){
            save.setEnabled(false);
        }
        else{
            save.setEnabled(true);
        }

        mItemListAdapter.updateItemList(mItemList);
    }

    //Adding new set to Sets_Seasons table
    private class AddNewSet extends AsyncTask<String, Void, String> {
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
                httpConnection.setRequestMethod("POST");
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
                //Add all items to set
                shouldAddFewItems = true;
                Closet.getInstance().setSetsUpdated(false);
                AddItemToSet task = new AddItemToSet();
                task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=insertItemToSet&email=" + Closet.getInstance().getUserSettings().getEmail() + "&set_id=" + nextAvailableSetId + "&item_id=" + mItemListForAction.get(index).getId()});
            }
        }
    }

    //Adding items to the new set that was added to Sets_Seasons table
    private class AddItemToSet extends AsyncTask<String, Void, String> {
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
                httpConnection.setRequestMethod("POST");
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
                if (shouldAddFewItems) {
                    index++;
                    if (index < mItemListForAction.size()) {
                        AddItemToSet task = new AddItemToSet();

                        if (isNewSet == constants.CREATE_NEW_SET_CODE) {
                            task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=insertItemToSet&email=" + Closet.getInstance().getUserSettings().getEmail() + "&set_id=" + nextAvailableSetId + "&item_id=" + mItemListForAction.get(index).getId()});
                        } else if (isNewSet == constants.ADD_ITEMS_TO_EXISTING_SET) {
                            //In case more than one item was added to an existing set
                            Closet.getInstance().setSetsUpdated(false);
                            task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=insertItemToSet&email=" + Closet.getInstance().getUserSettings().getEmail() + "&set_id=" + currSet.getId() + "&item_id=" + mItemListForAction.get(index).getId()});
                        }
                    } else {
                        shouldAddFewItems = false;
                        Closet.getInstance().setSetsUpdated(false);
                        finish();
                    }
                }
            }
        }
    }

    private void popErrorDialog(Boolean isPost) {
        ErrorDialog errorDialog = new ErrorDialog(ChooseItemsForSet.this, ChooseItemsForSet.this, isPost);
        // do when errorDialog finish its work
        errorDialog.setDialogResult(new ErrorDialog.OnMyDialogResult(){
            public void finish(String result){
                ChooseItemsForSet.this.finish();
            }
        });
        errorDialog.show();
    }

    private class getNextAvailableSetID extends AsyncTask<String, Void, String> {
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
        protected void onPostExecute(String output)
        {
            if(didTaskFail || output.equals(constants.DB_EXCEPTION)){
                didTaskFail = false;
                popErrorDialog(true);
            }
            else {
                output = output.replace("[", "");
                output = output.replace("]", "");
                output = output.replace("\"", "");

                try
                {
                    nextAvailableSetId = Integer.parseInt(output);
                }
                catch (Exception e)
                {
                    // we won't be able to parse null to int, and we get null if there are no sets yet
                    nextAvailableSetId = 1;
                }

                //Creating new set with set_id and season
                AddNewSet task = new AddNewSet();
                task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=newSet&email=" + Closet.getInstance().getUserSettings().getEmail() +"&set_id=" + nextAvailableSetId + "&season=" + chosenSeason.toString()});
            }
        }
    }
}
