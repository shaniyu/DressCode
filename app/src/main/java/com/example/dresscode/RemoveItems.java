package com.example.dresscode;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
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

public class RemoveItems extends AppCompatActivity implements android.widget.CompoundButton.OnCheckedChangeListener{
    private ListView mListView;
    private ArrayList<Item> mItemList; //Holds items to display in the listView in the activity
    private ArrayList<Item> mItemListForAction; //Holds the items that are checked for further actions (delete items or create set)
    private ItemListWithCheckBoxAdapter mItemListAdapter;
    private Button remove, cancel;
    private int index = 0;
    private ArrayList<Integer> setsToRemove = new ArrayList<>();
    private Boolean didTaskFail = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_items);
        setTitle("Remove Items");
        if(Closet.getInstance().getItems().size() > 0) {
            initializeListView();
        }
        else {
            TextView noItemsTextView = (TextView)findViewById(R.id.noItemsTextView);
            noItemsTextView.setText(constants.NO_ITEMS_IN_CLOSET);
            noItemsTextView.setVisibility(View.VISIBLE);
        }
        createCancelBtn();
        createRemoveBtn();
    }

    private void createRemoveBtn() {
        remove = (Button)findViewById(R.id.removeBtn);
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RemoveItems.this);
                builder.setMessage("Are you sure you want to remove the chosen items?");
                builder.setPositiveButton("YES", dialogClickListener);
                builder.setNegativeButton("NO", dialogClickListener);
                builder.show();
            }
        });
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //YES button was clicked
                    //Getting all the sets that the chosen items belong to
                    GetSetIdsForItemTask task = new GetSetIdsForItemTask();
                    task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=getAllSetsForItem&email=" + Closet.getInstance().getUserSettings().getEmail() + "&item_id=" + mItemListForAction.get(index).getId()});
                case DialogInterface.BUTTON_NEGATIVE:
                    //NO button was clicked
                    break;
            }
        }
    };


    private void createCancelBtn() {
        cancel = (Button) findViewById(R.id.cancelBtn);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initializeListView() {
        mListView = (ListView) findViewById(R.id.itemListView);
        mItemList = new ArrayList<Item>();
        mItemListForAction = new ArrayList<Item>();
        mItemList.addAll(Closet.getInstance().getItems());
        mItemListAdapter = new ItemListWithCheckBoxAdapter(this, R.layout.adapter_item_with_chkbox_view_layout, mItemList, this.getLocalClassName(), this);
        mListView.setAdapter(mItemListAdapter);
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

            mItemListAdapter.updateItemList(mItemList);
        }

        if(mItemListForAction.size() == 0){
            remove.setEnabled(false);
        }
        else{
            remove.setEnabled(true);
        }
    }

    private class GetSetIdsForItemTask extends AsyncTask<String, Void, String> {
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
                //Sending true to popErrorDialog although it's not a post task because it's a post process (the user tries to remove items)
                popErrorDialog(true);
            }
            else {
                //Add the IDs to the list
                ArrayList<Integer> idsFromCurrentOutput = (ArrayList<Integer>) DBResultsConversionFunctions.convertDBResultToListOfSetsIDs(output);
                if (idsFromCurrentOutput != null) {
                    setsToRemove.addAll(idsFromCurrentOutput);
                }

                index++;
                if (index < mItemListForAction.size()) // more items to delete
                {
                    GetSetIdsForItemTask task = new GetSetIdsForItemTask();
                    task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=getAllSetsForItem&email=" + Closet.getInstance().getUserSettings().getEmail() + "&item_id=" + mItemListForAction.get(index).getId()});
                } else if (setsToRemove.size() > 0) {
                    index = 0;
                    RemoveSetTask task = new RemoveSetTask();
                    task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=deleteSet&email=" + Closet.getInstance().getUserSettings().getEmail() + "&set_id=" + setsToRemove.get(index)});
                } else {
                    index = 0;
                    RemoveItemTask task = new RemoveItemTask();
                    task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=deleteItem&email=" + Closet.getInstance().getUserSettings().getEmail() + "&item_id=" + mItemListForAction.get(index).getId()});
                }
            }
        }
    }

    private class RemoveItemTask extends AsyncTask<String, Void, String> {
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
                popErrorDialog(true);
            }
            else {
                index++;
                Closet.getInstance().setItemsUpdated(false);
                if (index < mItemListForAction.size()) {
                    RemoveItemTask task = new RemoveItemTask();
                    task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=deleteItem&email=" + Closet.getInstance().getUserSettings().getEmail() + "&item_id=" + mItemListForAction.get(index).getId()});
                } else {
                    finish();
                }
            }
        }
    }

    private class RemoveSetTask extends AsyncTask<String, Void, String> {
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
                popErrorDialog(true);
            }
            else {
                index++;
                Closet.getInstance().setSetsUpdated(false);
                if (index < setsToRemove.size()) {
                    RemoveSetTask task = new RemoveSetTask();
                    task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=deleteSet&email=" + Closet.getInstance().getUserSettings().getEmail() + "&set_id=" + setsToRemove.get(index)});
                }
                //No more sets to remove, start removing items
                else {
                    index = 0;
                    RemoveItemTask task = new RemoveItemTask();
                    task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=deleteItem&email=" + Closet.getInstance().getUserSettings().getEmail() + "&item_id=" + mItemListForAction.get(index).getId()});
                }
            }
        }
    }

    private void popErrorDialog(Boolean isPost) {
        ErrorDialog errorDialog = new ErrorDialog(RemoveItems.this, RemoveItems.this, isPost);
        // do when errorDialog finish its work
        errorDialog.setDialogResult(new ErrorDialog.OnMyDialogResult(){
            public void finish(String result){
                RemoveItems.this.finish();
            }
        });
        errorDialog.show();
    }
}
