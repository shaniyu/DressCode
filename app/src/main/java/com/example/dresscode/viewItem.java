package com.example.dresscode;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class viewItem extends AppCompatActivity {
    private Spinner seasonSpinner, typeSppiner , colorSpinner, categorySpinner, shelveNumberSpinner;
    private RatingBar ratingBar;
    private String personalRating;
    private Item item;
    private userSettings userSettings;
    private TextView isInClosetTxt, lastWornDateTxt,lastLaundryDateTxt;
    private EditText descriptionEditTxt;
    private Button operateItemBtn,editItemBtn, addToSetBtn, cancelEditBtn;
    private ImageView itemImage;
    private Bitmap imageFromServer;
    private Boolean didTaskFail = false;
    private Set setToRemove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_item);
        getSupportActionBar().hide();

        // get parameters from last activity
        Intent intent = getIntent();
        int itemID = intent.getIntExtra("itemID", 0);
        item = Closet.getItemById(itemID);
        userSettings = (userSettings) intent.getSerializableExtra("settingsObject");

        initializeTextFieldsAndButtons();
        initializeSpinners();
        initializeImage();
    }

    private void initializeImage()
    {
        itemImage = (ImageView) findViewById(R.id.imageView);

        GetPictureTask task = new GetPictureTask(itemImage);
        task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=getPicture&email=" + Closet.getInstance().getUserEmail() + "&item_id="+ item.getId()});
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == constants.EDIT_SET_ITEMS_CODE) {
            // if set was changed
            if (!(Closet.getInstance().isSetsUpdated())) {
                int changedSetID = (int)data.getIntExtra("changedSetID", 0);
                setToRemove = Closet.getInstance().getSetById(changedSetID);
                GetSetByID task = new GetSetByID();
                task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=getSpecificSet&email=" + Closet.getInstance().getUserSettings().getEmail() + "&set_id=" + changedSetID});
            }
        }
    }

    private void initializeTextFieldsAndButtons()
    {
        String laundryDateString, laundryDateStringToPrint, lastWornDateString, lastWornDateStringToPrint;
        descriptionEditTxt = findViewById(R.id.descriptionEditTxt);
        descriptionEditTxt.setText(item.getComment());
        descriptionEditTxt.setFocusable(true);
        descriptionEditTxt.setEnabled(false);
        descriptionEditTxt.setCursorVisible(true);
        // limit the edit text length to 40
        descriptionEditTxt.setFilters(new InputFilter[] {new InputFilter.LengthFilter(constants.maxDescriptionLength)});
        isInClosetTxt = findViewById(R.id.isInClosetTxt);

        operateItemBtn = findViewById(R.id.operateItemBtn);
        editItemBtn = findViewById(R.id.editItemBtn);
        addToSetBtn = findViewById(R.id.addToSetBtn);
        cancelEditBtn = (Button) findViewById(R.id.cancelEditBtn);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        if(item.getLastLaundrtDate() != null) {
            laundryDateString = format.format(item.getLastLaundrtDate());
            laundryDateStringToPrint = "Last washed at:     " +laundryDateString;
        }
        else{
            laundryDateStringToPrint = "Wasn't washed yet";
        }

        if(item.getLastWornDate() != null) {
            lastWornDateString = format.format(item.getLastWornDate());
            lastWornDateStringToPrint = "Last worn at:          " +lastWornDateString;
        }
        else{
            lastWornDateStringToPrint = "Wasn't worn yet";
        }

        lastLaundryDateTxt = findViewById(R.id.lastLaundryTxt);
        lastWornDateTxt = findViewById(R.id.lastwornTxt);

        lastLaundryDateTxt.setText(laundryDateStringToPrint);
        lastWornDateTxt.setText(lastWornDateStringToPrint);

        editItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editItemBtn.getText().equals("Edit"))
                {
                    editItemBtn.setText("Save");
                    setAllObjectOnPageVisibility(true);
                    operateItemBtn.setClickable(false); // don't allow operating the item while editing
                    cancelEditBtn.setVisibility(View.VISIBLE);
                }
                else // user edited the item
                {
                    if(checkIsDescriptionValid()) {
                        checkIfChangedCloset();
                    }
                    else {
                        String comment = item.getComment();
                        descriptionEditTxt.setText(item.getComment());
                        Toast.makeText(viewItem.this, "Text length must be up to "+ constants.maxDescriptionLength+" characters", Toast.LENGTH_LONG).show();
                    }

                }
            }
        });

        if (item.isInCloset())
        {
            operateItemBtn.setText("Take out item");
            isInClosetTxt.setText("In Closet");
            isInClosetTxt.setTextColor(Color.parseColor("#008000"));
        }
        else
        {
            operateItemBtn.setText("Return item");
            isInClosetTxt.setText("Not in closet");
            isInClosetTxt.setTextColor(Color.parseColor("#FF0000"));
        }

        operateItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (operateItemBtn.getText().equals("Take out item"))
                {
                    updateIsInClosetTask task = new updateIsInClosetTask();
                    String query = new String(constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=updateIsInCloset&email="+userSettings.getEmail()+"&item_id="+item.getId()+"&is_in_closet=0&shelf_number=");
                    task.execute(new String[] {query});

                }
                else // "Return item"
                {
                    returnItemDialog returnItem = new returnItemDialog(viewItem.this, userSettings,item, viewItem.this);
                    // do when returnItemDialog finish its work
                    returnItem.setDialogResult(new returnItemDialog.OnMyDialogResult(){
                        public void finish(List<String> result){
                            if (result.get(0).equals("returned")) {
                                item.setInCloset(true);
                                isInClosetTxt.setText("In Closet");
                                operateItemBtn.setText("Take out item");
                                isInClosetTxt.setTextColor(Color.parseColor("#008000"));

                                updateViewItemAndCache(result);
                            }
                            else if(result.get(0).equals("task failed")){
                                popErrorDialog(true, false);
                            }
                        }
                    });
                    returnItem.show();
                }
            }
        });

        addToSetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent chooseSetToAddItemToIntent = new Intent(viewItem.this, ChooseSetsToAddItemTo.class);
                chooseSetToAddItemToIntent.putExtra("itemID", item.getId());
                startActivityForResult(chooseSetToAddItemToIntent,1);
            }
        });

        cancelEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelEdit();
            }
        });
    }

    private void cancelEdit() {
        descriptionEditTxt.setText(item.getComment());
        seasonSpinner.setSelection(getIndex(seasonSpinner,item.getSeason().seasonName));
        categorySpinner.setSelection(getIndex(categorySpinner,item.getCategory().categoryName));
        colorSpinner.setSelection(getIndex(colorSpinner,item.getColor().colorName));
        ratingBar.setRating(item.getRate());
        shelveNumberSpinner.setSelection(item.getShelfNumber()-1);

        cancelEditBtn.setVisibility(View.INVISIBLE);
        editItemBtn.setText("Edit");
        setAllObjectOnPageVisibility(false);
    }

    private void updateViewItemAndCache(List<String> result) {
        Item itemInCloset = Closet.getInstance().getItemById(item.getId());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        shelveNumberSpinner.setSelection(Integer.parseInt(result.get(1)) - 1);

        if(!result.get(2).equals("didn't wear") && !result.get(2).equals("task failed")){
            lastWornDateTxt.setText("Last worn at:          " + result.get(2));
            try
            {
                itemInCloset.setLastWornDate(sdf.parse(result.get(2)));
            }
            catch (ParseException e) { // should never happen, parse will always get the date formatted by DB and parse well
            }
        }

        //if the second cell is "task failed" there won't be third cell
        if(!result.get(2).equals("task failed") &&
                !result.get(3).equals("didn't wash") && !result.get(3).equals("task failed")){
            lastLaundryDateTxt.setText("Last washed at:     " + result.get(3));
            try
            {
                itemInCloset.setLastLaundrtDate(sdf.parse(result.get(3)));
            }
            catch (ParseException e)
            {
                // should never happen, parse will always get the date formatted by DB and parse well
            }
        }

        //We updated both the DB and the cache
        // update shelf number in the cache
        if ( result.get(0).equals("returned"))
        {
            itemInCloset.setShelfNumber(Short.parseShort(result.get(1)));
        }
        Closet.getInstance().setItemsUpdated(true);

        if(result.get(2).equals("task failed") || result.get(3).equals("task failed")){
            popErrorDialog(true, false);
        }
    }

    private boolean checkIsDescriptionValid() {

        return(!(descriptionEditTxt.getText().length() > constants.maxDescriptionLength));
    }

    private void setAllObjectOnPageVisibility(boolean val)
    {
        seasonSpinner.setEnabled(val);
        colorSpinner.setEnabled(val);
        shelveNumberSpinner.setEnabled(val);
        categorySpinner.setEnabled(val);

        ratingBar.setIsIndicator(!val);
        descriptionEditTxt.setEnabled(val);
        operateItemBtn.setEnabled(!val);
        addToSetBtn.setEnabled(!val);
    }
    //private method of your class
    private int getIndex(Spinner spinner, String myString){
        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                return i;
            }
        }
        return 0;
    }

    private void initializeSpinners()
    {
        seasonSpinner = (Spinner) findViewById(R.id.spinner_dropdown_season);
        seasonSpinner.setEnabled(false);
        seasonSpinner.setClickable(false);
        seasonSpinner.setAdapter(new ArrayAdapter(this, android.R.layout.simple_spinner_item,ESeason.values()));
        seasonSpinner.setSelection(getIndex(seasonSpinner,item.getSeason().seasonName));


        categorySpinner = (Spinner) findViewById(R.id.spinner_dropdown_category);
        categorySpinner.setEnabled(false);
        categorySpinner.setClickable(false);
        categorySpinner.setAdapter(new ArrayAdapter(this, android.R.layout.simple_spinner_item,ECategory.values()));
        categorySpinner.setSelection(getIndex(categorySpinner,item.getCategory().categoryName));

        colorSpinner = (Spinner) findViewById(R.id.spinner_dropdown_color);
        colorSpinner.setEnabled(false);
        colorSpinner.setClickable(false);
        colorSpinner.setAdapter(new ArrayAdapter(this, android.R.layout.simple_spinner_item,EColor.values()));
        colorSpinner.setSelection(getIndex(colorSpinner,item.getColor().colorName));

        typeSppiner = (Spinner) findViewById(R.id.spinner_dropdown_type);
        typeSppiner.setEnabled(false);
        typeSppiner.setClickable(false);
        typeSppiner.setAdapter(new ArrayAdapter(this, android.R.layout.simple_spinner_item, EType.values()));
        typeSppiner.setSelection(getIndex(typeSppiner,item.getType().typeName));

        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingBar.setMax(5);
        ratingBar.setIsIndicator(true);
        ratingBar.setRating(item.getRate());

        //if rating value is changed, display the current rating value
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {
                personalRating = (String.valueOf(rating));

            }
        });

        List shelveNumberList = new ArrayList();
        for (int i = 1; i <= userSettings.getNumberOfShelves(); i++) {
            shelveNumberList.add(i);
        }
        ArrayAdapter<Integer> shelveSpinnerArrayAdapter = new ArrayAdapter<Integer>(
                this, android.R.layout.simple_spinner_item, shelveNumberList);
        shelveSpinnerArrayAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        shelveNumberSpinner = (Spinner)findViewById(R.id.spinner_dropdown_shelve);
        shelveNumberSpinner.setEnabled(false);
        shelveNumberSpinner.setClickable(false);
        shelveNumberSpinner.setAdapter(shelveSpinnerArrayAdapter);
        shelveNumberSpinner.setSelection(item.getShelfNumber()-1);
    }

    private class updateItemTask extends AsyncTask<String, Void, String> {
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
                else {
                    didTaskFail = true;
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
                popErrorDialog(true, true);
                didTaskFail = false;
            }
            else {
                operateItemBtn.setClickable(true); // allow operating on item
                editItemBtn.setText("Edit");
                cancelEditBtn.setVisibility(View.INVISIBLE);
                setAllObjectOnPageVisibility(false); // done editing
                item.setComment(descriptionEditTxt.getText().toString());
                item.setSeason(ESeason.fromString(seasonSpinner.getSelectedItem().toString()));
                item.setShelfNumber(Short.parseShort(shelveNumberSpinner.getSelectedItem().toString()));
                item.setCategory(ECategory.fromString(categorySpinner.getSelectedItem().toString()));
                item.setColor(EColor.fromString(colorSpinner.getSelectedItem().toString()));
                item.setRate((short) ratingBar.getRating());
            }
        }

    }

    private void popErrorDialog(Boolean isPost, final Boolean isUpdateItemTask) {
        ErrorDialog errorDialog = new ErrorDialog(viewItem.this, viewItem.this, isPost);
        // do when errorDialog finish its work
        errorDialog.setDialogResult(new ErrorDialog.OnMyDialogResult(){
            public void finish(String result){
                if(isUpdateItemTask){
                    cancelEdit();
                }
            }
        });
        errorDialog.show();
    }

    private class updateIsInClosetTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String output = null;
            for (String url : urls) {
                url = url.replace(" ", "%20");
                output = getOutputFromUrl(url);
            }
            return output;
        }

        private String getOutputFromUrl(String url) {
            StringBuffer output = new StringBuffer("");
            try {
                InputStream stream = getHttpConnection(url);
                if (stream != null) {
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
                popErrorDialog(true, false);
            }
            else {
                item.setInCloset(false);
                operateItemBtn.setText("Return item");
                isInClosetTxt.setText("Not in closet");
                isInClosetTxt.setTextColor(Color.parseColor("#FF0000"));
            }
        }
    }
    private void checkIfChangedCloset() {
        // if one of the items details has changed, update the closet boolean value "isItemsUpdated" to false
        boolean changed = false;

        if ( (item.getComment() == null && ! descriptionEditTxt.getText().toString().isEmpty()) || ( item.getComment()!= null && !(item.getComment().equals(descriptionEditTxt.getText().toString()))))
            changed = true;
        if (!(item.getSeason().equals(ESeason.fromString(seasonSpinner.getSelectedItem().toString()))))
            changed = true;
        if (item.getShelfNumber() != Short.parseShort(shelveNumberSpinner.getSelectedItem().toString()))
            changed = true;
        if (!(item.getCategory().equals(ECategory.fromString(categorySpinner.getSelectedItem().toString()))))
            changed = true;
        if (!(item.getColor().equals(EColor.fromString(colorSpinner.getSelectedItem().toString()))))
            changed = true;
        if (!(item.getRate() == (short) ratingBar.getRating()))
            changed = true;
        if (changed)
        {
            // update DB
            updateItemTask task = new updateItemTask();
            String query = new String(constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=updateItem&email="+userSettings.getEmail()+"&item_id="+item.getId()+"&shelf_number="+Short.parseShort(shelveNumberSpinner.getSelectedItem().toString())+"&personal_rate="+(short)ratingBar.getRating()+"&category="+categorySpinner.getSelectedItem().toString()+"&type="+item.getType()+"&season="+seasonSpinner.getSelectedItem().toString()+"&color="+colorSpinner.getSelectedItem().toString()+"&comment="+descriptionEditTxt.getText().toString());
            task.execute(new String[] {query});
        }
        else {
            setAllObjectOnPageVisibility(false);
            editItemBtn.setText("Edit");
            cancelEditBtn.setVisibility(View.INVISIBLE);
        }
    }

    private class GetPictureTask extends AsyncTask<String, Void, byte[]> {
        ImageView imageToPopulate;

        public GetPictureTask(ImageView imageToPopulate) {
            this.imageToPopulate = imageToPopulate;
        }

        @Override
        protected byte[] doInBackground(String... urls) {
            byte[] output = null;
            for (String url : urls) {
                output = getOutputFromUrl(url);
            }
            return output;
        }

        private byte[] getOutputFromUrl(String url) {
            StringBuffer output = new StringBuffer("");
            byte[] imageBytes = null;

            try {
                InputStream stream = getHttpConnection(url);
                if(stream != null) {
                    imageBytes = IOUtils.toByteArray(stream);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return imageBytes;
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
                httpConnection.setDoOutput(true);
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
        protected void onPostExecute(byte[] imageBytes)
        {
            if(didTaskFail){
                didTaskFail = false;
                popErrorDialog(false, false);
            }
            else {
                try {
                    imageFromServer = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    imageToPopulate.setImageBitmap(imageFromServer);

                } catch (Exception ex) {
                    ex.getMessage();
                }
            }

        }
    }

    private class GetSetByID extends AsyncTask<String, Void, String> {

        private Set mUpdatedSet;

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
            //Couldn't get the updated set from DB - the isSetsUpdated flag remains false
            //and the set in cache remains without the new item
            if(didTaskFail || output.equals(constants.DB_EXCEPTION)){
                didTaskFail = false;
                popErrorDialog(false, false);
            }
            else {
                //get the set from the db, list with only one set
                List<Set> updatedSetsFromDB = DBResultsConversionFunctions.convertDBResultToSets(output);
                if (!updatedSetsFromDB.isEmpty()) {
                    Closet.getInstance().getSets().remove(setToRemove);
                    mUpdatedSet = updatedSetsFromDB.get(0);
                    Closet.getInstance().setSetsUpdated(true);
                    Closet.getInstance().getSets().add(mUpdatedSet);
                }
            }
        }
    }
}
