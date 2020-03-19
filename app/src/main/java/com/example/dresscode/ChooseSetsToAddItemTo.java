package com.example.dresscode;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
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
import java.util.List;

public class ChooseSetsToAddItemTo extends AppCompatActivity {

    private ListView mListView;
    private List<Set> mSetList; //Holds items to display in the listView in the activity
    private SetListAdapter mSetListAdapter;
    private ImageView appologyImage;
    private TextView noSetsText;
    private int itemID;
    private int mCurrSetIDClicked;
    private Boolean didTaskFail = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_list_to_delete);
        appologyImage = findViewById(R.id.appologyImg);
        noSetsText = findViewById(R.id.noItemsTitle);
        Intent intent = getIntent();
        itemID = (int)intent.getIntExtra("itemID", 0);
        setTitle("Sets to add item to");

        displaySetList();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                mCurrSetIDClicked = mSetList.get(position).getId();
                AddItemToSet task = new AddItemToSet();
                task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=insertItemToSet&email=" + Closet.getInstance().getUserSettings().getEmail() +"&set_id=" + mCurrSetIDClicked + "&item_id=" + itemID});
            }
        });
    }

    private void displaySetList() {

        mListView = (ListView) findViewById(R.id.setListView);
        mSetList = findSetsThatDontContainItem();
        mSetListAdapter = new SetListAdapter(this, R.layout.adapter_set_view_layout, mSetList, this);
        mListView.setAdapter(mSetListAdapter);

        if (mSetList.size()==0)
        {
            // no sets to show.. put a proper message
            noSetsText.setText("You don't have any sets available");
            appologyImage.setVisibility(View.VISIBLE);
        }
    }

    public ArrayList<Set> findSetsThatDontContainItem()
    {
        ArrayList<Set> res = new ArrayList<>();
        ArrayList<Set> closetSets = (ArrayList)Closet.getInstance().getSets();
        boolean shouldAddSetToList;

        for (Set set : closetSets){

            shouldAddSetToList = true;
            for (Item currItem : set.getItems()){

                //if at some point we found an item in the set with the same item ID as the item we want to add
                //then we set the flag to false and break the current loop
                if(!(shouldAddSetToList = !(currItem.getId() == itemID))){
                    break;
                }
            }

            if (shouldAddSetToList){
                res.add(set);
            }
        }

        return res;
    }

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
                Toast.makeText(ChooseSetsToAddItemTo.this, "Item was added to set", Toast.LENGTH_SHORT).show();
                Closet.getInstance().setSetsUpdated(false);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("changedSetID", mCurrSetIDClicked);
                setResult(constants.EDIT_SET_ITEMS_CODE, resultIntent);
                finish();
            }
        }
    }

    private void popErrorDialog(Boolean isPost) {
        ErrorDialog errorDialog = new ErrorDialog(ChooseSetsToAddItemTo.this, ChooseSetsToAddItemTo.this, isPost);
        // do when errorDialog finish its work
        errorDialog.setDialogResult(new ErrorDialog.OnMyDialogResult(){
            public void finish(String result){
                ChooseSetsToAddItemTo.this.finish();
            }
        });
        errorDialog.show();
    }
}
