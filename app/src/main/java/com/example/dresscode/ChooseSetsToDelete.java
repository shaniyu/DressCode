package com.example.dresscode;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
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

public class ChooseSetsToDelete extends AppCompatActivity implements android.widget.CompoundButton.OnCheckedChangeListener{
    private ListView mListView;
    private List<Set> mSetList; //Holds items to display in the listView in the activity
    private ArrayList<Set> mSetListForAction; //Holds the items that are checked for further actions (delete items or create set)
    private SetListWithCheckBoxAdapter mSetListAdapter;
    private ImageView appologyImage;
    private TextView noSetsText;
    private boolean shouldDeleteFewSets;
    private int index = 0;
    private Boolean didTaskFail = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_list_to_delete);
        appologyImage = findViewById(R.id.appologyImg);
        noSetsText = findViewById(R.id.noItemsTitle);
        setTitle("Sets to Delete");

        displaySetList();
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        int position = mListView.getPositionForView(buttonView);

        if (position != mListView.INVALID_POSITION){

            Set set = mSetList.get(position);

            if(isChecked)
            {
                mSetListForAction.add(set);
                set.setIsChecked(true);
            }
            else {
                mSetListForAction.remove(set);
                set.setIsChecked(false);
            }
        }

        mSetListAdapter.updateSetList(mSetList);
    }


    private void displaySetList() {

        mListView = (ListView) findViewById(R.id.setListView);
        mSetList = Closet.getInstance().getSets();
        mSetListForAction = new ArrayList<Set>();

        mSetListAdapter = new SetListWithCheckBoxAdapter(this, R.layout.adapter_set_with_chkbox_view_layout, mSetList, this);
        mListView.setAdapter(mSetListAdapter);
        if (mSetList.size()==0)
        {
            // no sets to show.. put a proper message
            noSetsText.setText("You don't have any set yet");
            appologyImage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_delete_sets, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_discard:
                deleteSetFromDB();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteSetFromDB() {

        if(mSetListForAction.size()==0){
            Toast.makeText(this,"No sets were chosen", Toast.LENGTH_SHORT).show();
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to delete these sets?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    shouldDeleteFewSets = true;
                    DeleteSets task = new DeleteSets();
                    task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=deleteSet&email=" + Closet.getInstance().getUserEmail() +"&set_id=" + mSetListForAction.get(index).getId()});
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    private class DeleteSets extends AsyncTask<String, Void, String> {
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
                Closet.getInstance().setSetsUpdated(false);
                if (shouldDeleteFewSets) {
                    index++;
                    if (index < mSetListForAction.size()) {
                        ChooseSetsToDelete.DeleteSets task = new ChooseSetsToDelete.DeleteSets();
                        task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=deleteSet&email=" + Closet.getInstance().getUserEmail() + "&set_id=" + mSetListForAction.get(index).getId()});
                    } else {
                        shouldDeleteFewSets = false;
                        getAllSetsForUserTask task = new getAllSetsForUserTask();
                        task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=getAllSetsForUser&email=" + Closet.getInstance().getUserEmail()});
                    }
                }
            }
        }

        public class getAllSetsForUserTask extends AsyncTask<String, Void, String> {
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
                    finish();
                }
            }
        }

        private void popErrorDialog(Boolean isPost) {
            ErrorDialog errorDialog = new ErrorDialog(ChooseSetsToDelete.this, ChooseSetsToDelete.this, isPost);
            // do when errorDialog finish its work
            errorDialog.setDialogResult(new ErrorDialog.OnMyDialogResult(){
                public void finish(String result){
                    ChooseSetsToDelete.this.finish();
                }
            });
            errorDialog.show();
        }
    }
}
