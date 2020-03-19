package com.example.dresscode;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
// for qr
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.util.List;

public class homePageActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private Button setsForWeatherBtn, scanBtn, addItemBtn,showClothesBtn, showSetsBtn, removeItemsBtn;
    private TextView helloMsg;
    private boolean scannedForNewItem= false, taskForSetsAccordingToWeather = false, didTaskFail = false;
    private final Activity activity = this;
    private GoogleSignInAccount mAccount;
    private List<Set> setsAccordingToWeather;
    private double weatherValue;
    private int qr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        initializeSideBar();
        initializations();
        createAllButtons();
    }
    private void initializeSideBar()
    {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(""); // empty title on the tool bar of homePage
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.logo2);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_page_with_navigator, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent;
        switch (id)
        {
            case R.id.nav_settings:
                intent = new Intent(homePageActivity.this, SettingsPage.class);
                startActivityForResult(intent, constants.SETTINGS_CODE);
                break;
            case R.id.nav_logout:
                //Passing account to loginPage activity, where we'll check if the account is not null.
                //If it's not null we'll call signOut() in the loginPage activity in order to log out
                //and continue from there to MainActivity
                intent = new Intent(homePageActivity.this, loginPage.class);
                intent.putExtra("log_out", mAccount);
                startActivity(intent);
                break;
            case R.id.nav_aboutus:
                intent = new Intent(homePageActivity.this, aboutUsPage.class);
                startActivity(intent);
                break;
            case R.id.nav_freeNextQr:
                tellUserNextFreeQrNumber();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void tellUserNextFreeQrNumber()
    {
        findNextFreeQRNumberTask task = new findNextFreeQRNumberTask();
        task.execute(new String[] {constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=getNextAvailableItemID&email="+Closet.getInstance().getUserSettings().getEmail()});
    }
    private void initializations()
    {
        Intent intent = getIntent();
        mAccount = (GoogleSignInAccount)intent.getParcelableExtra("account"); //GoogleSignInAccount implements Parcable and not Serializeable
        Closet.getInstance().setUserSettings((userSettings) intent.getSerializableExtra("settingsObject"));
        helloMsg = findViewById(R.id.helloMsg);
        helloMsg.setText("Hello, " + Closet.getInstance().getUserSettings().getName() + "!");
        //shouldGetSets = true;
        //set the list of items
        getAllItemsForUserTask task = new getAllItemsForUserTask();
        task.execute(new String[] {constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=getAllItemsForUser&email="+Closet.getInstance().getUserSettings().getEmail()});
    }
    private void createAllButtons()
    {
        makeAddItemButton();
        makeSetsForWeatherButton();
        makeShowSetsButton();
        makeScanBtn();
        makeShowClothesBtn();
        makeRemoveItemsBtn();

    }

    private void makeRemoveItemsBtn() {
        removeItemsBtn = (Button)findViewById(R.id.removeItemBtn);
        removeItemsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(homePageActivity.this, RemoveItems.class);
                startActivityForResult(intent, constants.REMOVE_ITEMS_CODE);
            }
        });
    }


    private void makeAddItemButton()
    {
        addItemBtn = (Button) findViewById(R.id.addItemBtn);
        addItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scannedForNewItem= true;
                scanQr();
            }
        });
    }
    public void makeSetsForWeatherButton()
    {
        // get sets according to weather
        setsForWeatherBtn = (Button) findViewById(R.id.setForTodayBtn);
        setsForWeatherBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                taskLoadUp(eCity.getStringForWeatherAPI(Closet.getInstance().getUserSettings().getCity().toString()));
            }
        });
    }

    private void makeShowSetsButton()
    {
        showSetsBtn = (Button)findViewById(R.id.showSetsBtn);
        showSetsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent showAllSetsIntent = new Intent(homePageActivity.this, showAllSets.class);
                homePageActivity.this.startActivity(showAllSetsIntent);
            }
        });
    }

    private void makeScanBtn()
    {    // just scans an item
        scanBtn = (Button) findViewById(R.id.scanBtn);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scannedForNewItem = false;
                scanQr();
            }
        });
    }
    private void makeShowClothesBtn()
    {
        showClothesBtn = (Button) findViewById(R.id.showClothesBtn);
        showClothesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(homePageActivity.this, showAllClothes.class);
                // we pass the whole closet to the show clothes screen
                intent.putExtra("closetObject", Closet.getInstance());
                startActivityForResult(intent, constants.SHOW_ALL_CLOTHES_CODE);
            }
        });
    }
    public void scanQr()
    {
        IntentIntegrator integrator = new IntentIntegrator(activity);
        integrator.setCaptureActivity(AnyOrientationCaptureActivity.class);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Scan the item");
        integrator.setOrientationLocked(false);
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    @Override
    //this function is called when other activity finish and created an intent
    protected void onActivityResult ( int requestCode, int resultCode, Intent dataIntent){
        super.onActivityResult(requestCode, resultCode, dataIntent);
        // The returned result data is identified by requestCode.
        // The request code is specified in startActivityForResult method.
        switch (requestCode) {
            case constants.SETTINGS_CODE: // request #1 refers to passing: city+name+number of shelves
                if (resultCode == RESULT_OK) {
                    // closet is updated with the singleton object
                    helloMsg.setText("Hello, " + Closet.getInstance().getUserSettings().getName() + "!");
                }
                break;
            case constants.QR_SCANNER_CODE:
                // if we used qr scanner for new item or just scan
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, dataIntent);
                if(result != null) {
                    if (result.getContents() == null) {
                        scannedForNewItem = false;
                        Toast.makeText(this, "You canceled the scanning", Toast.LENGTH_LONG).show();
                    } else {
                        String qrCode = result.getContents();
                        if (scannedForNewItem == true) // if scanned from add new item
                        {
                            try
                            { // parsing to int might cause exception
                                qr = Integer.parseInt(qrCode);
                                // task for checking if this qr already in the closet, and add it if not
                                findItemByIDTask task = new findItemByIDTask();
                                task.execute(new String[] {constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=findItem&email="+Closet.getInstance().getUserSettings().getEmail()+"&item_id="+qr});
                            }
                            catch (Exception e) {
                                Toast.makeText(getApplicationContext(), "Non valid QR code.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {  // go to view item of the chosen item
                            Intent intent = new Intent(homePageActivity.this, viewItem.class);
                            try { // parsing to int might cause exception
                            int index = getIndexOfItemByQrCode(Integer.parseInt(qrCode));
                            if ( index != -1 ) // item was found for this qr
                                 {
                                    intent.putExtra("itemID", Closet.getInstance().getItems().get(index).getId());
                                    intent.putExtra("settingsObject", Closet.getInstance().getUserSettings());
                                    startActivityForResult(intent, constants.VIEW_ITEM_CODE);
                                }
                                else
                                    Toast.makeText(homePageActivity.this, "You don't have this item in your closet", Toast.LENGTH_LONG).show();
                            }
                            catch (Exception e) {
                                Toast.makeText(getApplicationContext(), "Non valid QR code.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
                else { //some error
                    super.onActivityResult(requestCode, resultCode, dataIntent);
                }
                break;
            case constants.VIEW_ITEM_CODE: // this might changed the items in closet
                updateClosetCacheIfNeeded();
                break;
            case constants.REMOVE_ITEMS_CODE:
                updateClosetCacheIfNeeded();
                break;
            case constants.ADD_ITEM_CODE:
                // item was added to the closet, came here after done adding item in newItemPage
                updateClosetCacheIfNeeded();
                break;
            case constants.VIEW_SETS_CODE:
                updateClosetCacheIfNeeded();
                break;
        }
    }

    public void updateClosetCacheIfNeeded()
    {   // update cache closet items
        if (!Closet.getInstance().isItemsUpdated())
        {
            getAllItemsForUserTask task = new getAllItemsForUserTask();
            task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=getAllItemsForUser&email=" + Closet.getInstance().getUserSettings().getEmail()});
        }
        else if(!Closet.getInstance().isSetsUpdated()){
            getAllSetsForUserTask task = new getAllSetsForUserTask();
            task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=getAllSetsForUser&email=" + mAccount.getEmail()});
        }
    }
    public int getIndexOfItemByQrCode(int qr)
    {
        int i= 0 ;
        for ( Item item : Closet.getInstance().getItems())
        {
            if (item.getId() == qr )
                return i;
            else
                i++;
        }
        return -1; // wasn't found
    }
    // Get weather, then get all sets according to it
    public void taskLoadUp(String query) {
        if (weatherFunctions.isNetworkAvailable(getApplicationContext())) {
            DownloadWeather task = new DownloadWeather();
            task.execute(query);
            // get sets for this weather
        } else {
            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
        }
    }

    //class for getting weather from https://openweathermap.org/ and get all sets according to it
    class DownloadWeather extends AsyncTask< String, Void, String > {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        protected String doInBackground(String...args) {
            String xml = weatherFunctions.excuteGet("http://api.openweathermap.org/data/2.5/weather?q=" + args[0] +
                    "&units=metric&appid=" + constants.OPEN_WEATHER_MAP_API);
            return xml;
        }
        @Override
        protected void onPostExecute(String xml) {
            try {
                JSONObject json = new JSONObject(xml);
                if (json != null) {
                    JSONObject details = json.getJSONArray("weather").getJSONObject(0);
                    JSONObject main = json.getJSONObject("main");
                    DateFormat df = DateFormat.getDateTimeInstance();
                    weatherValue = main.getDouble("temp");
                    // create a list of the sets that fits todays weather
                    // we get the data for the items in a set from the items list in user closet
                    if ( ! Closet.getInstance().isItemsUpdated())
                    {
                        taskForSetsAccordingToWeather = true; // do async task according to show sets for weather
                        // first need to update items
                        getAllItemsForUserTask task = new getAllItemsForUserTask();
                        task.execute(new String[] {constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=getAllItemsForUser&email="+Closet.getInstance().getUserSettings().getEmail()});
                    }
                    // user items already updated
                    else if (! Closet.getInstance().isSetsUpdated())
                    {
                        taskForSetsAccordingToWeather = true; // do async task according to show sets for weather
                        // first, need to update sets
                        getAllSetsForUserTask task = new getAllSetsForUserTask();
                        task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=getAllSetsForUser&email=" + mAccount.getEmail()});
                    }
                    else
                    {
                        // items and sets are updated, can filter sets and go to show sets screen
                        showSetsAccordingToWeather(main.getDouble("temp"), Closet.getInstance().getSets());
                    }
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Error, Check City", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showSetsAccordingToWeather(double temprature,List<Set> i_sets)
    {
        // after sets are updated, need to filter sets that match the weather and go to show sets screen
        setsAccordingToWeather = Set.filterSetsForWeather(temprature,i_sets);
        taskForSetsAccordingToWeather = false;
        // go to show sets with the list setsAccordingToWeather
        Intent intent = new Intent(homePageActivity.this, showSetsAccordingToWeather.class);
        intent.putExtra("setsObject", (Serializable) setsAccordingToWeather);
        intent.putExtra("temprature", temprature);
        startActivityForResult(intent, constants.VIEW_SETS_CODE);
    }

    private class getAllItemsForUserTask extends AsyncTask<String, Void, String> {
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
                List<Item> itemsList = DBResultsConversionFunctions.convertDBResultToItems(output);
                Closet.getInstance().setItems(itemsList);
                Closet.getInstance().setItemsUpdated(true);
                getAllSetsForUserTask task = new getAllSetsForUserTask();
                task.execute(new String[]{constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=getAllSetsForUser&email=" + mAccount.getEmail()});
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
                if (taskForSetsAccordingToWeather) {
                    showSetsAccordingToWeather(weatherValue, Closet.getInstance().getSets());
                }

                if (scannedForNewItem)
                // already added new item, came back from newItemPage, updated the closet
                // and now need to go to show all clothes
                {
                    scannedForNewItem = false;
                    Intent intent = new Intent(homePageActivity.this, showAllClothes.class);
                    intent.putExtra("closetObject", Closet.getInstance());
                    startActivityForResult(intent, constants.SHOW_ALL_CLOTHES_CODE);
                }
            }
        }
    }

    private class findItemByIDTask extends AsyncTask<String, Void, String> {
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
                scannedForNewItem = false;
                popErrorDialog(true);
            }
            else {
                output = output.replace("[", ""); // delete brackets
                output = output.replace("]", "");
                output = output.replace("\"", ""); // delete the opening and closing "

                if (!output.isEmpty()) // the item with this qr is in the closet
                {
                    Toast.makeText(homePageActivity.this, "You already have item with this code in your closet", Toast.LENGTH_LONG).show();
                } else {
                    // add this item
                    Intent intent = new Intent(homePageActivity.this, newItemPage.class);
                    intent.putExtra("qrCode", qr);
                    startActivityForResult(intent, constants.ADD_ITEM_CODE); // go to home page activity
                }
            }
        }
    }

    private class findNextFreeQRNumberTask extends AsyncTask<String, Void, String> {
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
                popErrorDialog(false);
            }
            else {
                output = output.replace("[", "");
                output = output.replace("]", "");
                output = output.replace("\"", "");

                final AlertDialog.Builder freeQrDialog = new AlertDialog.Builder(homePageActivity.this);
                freeQrDialog.setTitle("Next free QR number for your use is:");
                if (output.equals("null") || output.isEmpty()) {
                    freeQrDialog.setMessage("1");
                } else {
                    freeQrDialog.setMessage(output);
                }
                freeQrDialog.setCancelable(true);
                freeQrDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog dialog = freeQrDialog.create();
                dialog.show();
            }
        }
    }

    //When pressing the "back" button on android- we exit the application
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        // if sidebar is open, back should close it
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
        }
    }

    private void popErrorDialog(Boolean isPost) {
        ErrorDialog errorDialog = new ErrorDialog(homePageActivity.this, homePageActivity.this, isPost);
        // do when errorDialog finish its work
        errorDialog.setDialogResult(new ErrorDialog.OnMyDialogResult(){
            public void finish(String result){
                if (taskForSetsAccordingToWeather) {
                    showSetsAccordingToWeather(weatherValue, Closet.getInstance().getSets());
                }

                if (scannedForNewItem)
                // already added new item, came back from newItemPage, updated the closet
                // and now need to go to show all clothes
                {
                    scannedForNewItem = false;
                    Intent intent = new Intent(homePageActivity.this, showAllClothes.class);
                    intent.putExtra("closetObject", Closet.getInstance());
                    startActivityForResult(intent, constants.SHOW_ALL_CLOTHES_CODE);
                }
            }
        });
        errorDialog.show();
    }
}
