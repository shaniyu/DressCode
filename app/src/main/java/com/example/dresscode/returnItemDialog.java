package com.example.dresscode;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
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
import java.util.LinkedList;
import java.util.List;

public class returnItemDialog extends Dialog implements
        android.view.View.OnClickListener {

    public Activity c;
    private Item item;
    private userSettings userSettings;
    public Button continueBtn;
    public CheckBox woreItem, washedItem;
    private Spinner shelfNumberSpinner;
    private short numberOfShelves;
    private Context mContext;
    private Boolean didTaskFail = false, isInClosetTask = true, isUpdateLastWornDate = false, isUpdateLastWashedDate = false, isWornChecked;
    private String wornDate, washedDate;
    private List<String> result;
    private CalendarView washedCalendarView, wornCalendarView;
    private ConstraintLayout washedCalendarLayout, wornCalendarLayout;
    OnMyDialogResult mDialogResult; // the callback

    public returnItemDialog(Activity a, userSettings i_userSettings, Item i_item, Context i_Context) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        item = i_item;
        userSettings = i_userSettings;
        numberOfShelves = i_userSettings.getNumberOfShelves();
        mContext = i_Context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.return_item_dialog);

        washedItem = (CheckBox) findViewById(R.id.washedItemCB);
        woreItem = (CheckBox) findViewById(R.id.woreItemCB);
        continueBtn = (Button) findViewById(R.id.continueBtn);
        wornCalendarLayout = (ConstraintLayout)findViewById(R.id.wornCalendarLayout);
        wornCalendarView = (CalendarView)findViewById(R.id.wornCalendarView);
        washedCalendarLayout = (ConstraintLayout)findViewById(R.id.washedCalendarLayout);
        washedCalendarView = (CalendarView)findViewById(R.id.washedCalendarView);
        washedCalendarView.setMaxDate(System.currentTimeMillis());
        wornCalendarView.setMaxDate(System.currentTimeMillis());
        continueBtn.setOnClickListener(this);

        List shelveNumberList = new ArrayList();
        shelveNumberList.add(0, "Select shelf");
        for (int i = 1; i <= numberOfShelves; i++) {
            shelveNumberList.add(i);
        }
        ArrayAdapter<Integer> shelveSpinnerArrayAdapter = new ArrayAdapter<Integer>(
                mContext, android.R.layout.simple_spinner_item, shelveNumberList);
        shelfNumberSpinner = (Spinner)findViewById(R.id.shelve_number_spinner);
        shelfNumberSpinner.setAdapter(shelveSpinnerArrayAdapter);
        shelfNumberSpinner.setSelection(0, false);

        woreItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(woreItem.isChecked()) {
                    //continueBtn.setText("Choose Date");
                    continueBtn.setVisibility(View.INVISIBLE);
                    isWornChecked = true;
                    wornCalendarLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        washedItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(washedItem.isChecked()) {
                    continueBtn.setVisibility(View.INVISIBLE);
                    isWornChecked = false;
                    washedCalendarLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        wornCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                setDate(year, month, dayOfMonth);
                wornCalendarLayout.setVisibility(View.INVISIBLE);
                continueBtn.setVisibility(View.VISIBLE);
            }
        });

        washedCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                setDate(year, month, dayOfMonth);
                washedCalendarLayout.setVisibility(View.INVISIBLE);
                continueBtn.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setDate(int year, int month, int dayOfMonth) {
        String date;
        month++;
        if (month < 10 && dayOfMonth < 10) {
            date = String.valueOf(year) + "-0" + String.valueOf(month) + "-0" + String.valueOf(dayOfMonth);
        } else if (month < 10) {
            date = String.valueOf(year) + "-0" + String.valueOf(month) + "-" + String.valueOf(dayOfMonth);
        } else if (dayOfMonth < 10) {
            date = String.valueOf(year) + "-" + String.valueOf(month) + "-0" + String.valueOf(dayOfMonth);
        } else {
            date = String.valueOf(year) + "-" + String.valueOf(month) + "-" + String.valueOf(dayOfMonth);
        }

        if(isWornChecked){
            wornDate = date;
        }
        else {
            washedDate = date;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.continueBtn:
                    if (isValidInput()) {
                        this.dismiss();
                        doTasks();
                    } else {
                        TextView spinnerTxt = (TextView) shelfNumberSpinner.getChildAt(0);
                        spinnerTxt.setTextColor(Color.parseColor("#f95757"));
                    }
                break;
            default:
                break;
        }
    }
    private boolean isValidInput() {
        boolean isValid = true;
        if (shelfNumberSpinner.getSelectedItemPosition() == 0)
        {
            isValid = false;

        }
        return isValid;
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
            } catch (Exception ex) {
                didTaskFail = true;
                ex.printStackTrace();
            }
            return stream;
        }
        @Override
        protected void onPostExecute(String output) {
            //TASK_FAILED
            if(didTaskFail || output.equals(constants.DB_EXCEPTION)){
                result.add("task failed");
                didTaskFail = false;
                mDialogResult.finish(result); // dialog finished its work
            }
            else if(isInClosetTask){
                result.add("returned");
                result.add(shelfNumberSpinner.getSelectedItem().toString());
                isInClosetTask = false;
                Closet.getInstance().setItemsUpdated(false); // cache closet isn't updated
                if (woreItem.isChecked())
                {
                    isUpdateLastWornDate = true;
                    updateItemTask updateWoreDateTask = new updateItemTask();
                    String taskUrl = new String(constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=updateItemsLastWornDate&email=" + userSettings.getEmail() + "&item_id=" + item.getId() + "&last_worn_date=" + wornDate);
                    updateWoreDateTask.execute(new String[]{taskUrl});
                }
                else if(washedItem.isChecked()){
                    isUpdateLastWashedDate = true;
                    result.add("didn't wear");
                    updateItemTask updateWashedDateTask = new updateItemTask();
                    String taskUrl = new String(constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=updateItemsLastLaundryDate&email=" + userSettings.getEmail() + "&item_id=" + item.getId() + "&last_laundry_date=" + washedDate);
                    updateWashedDateTask.execute(new String[]{taskUrl});
                }
                else{
                    result.add("didn't wear");
                    result.add("didn't wash");
                    mDialogResult.finish(result); // dialog finished its work
                }
            }
            else if(isUpdateLastWornDate){
                result.add(wornDate);
                isUpdateLastWornDate = false;

                if(washedItem.isChecked()) {
                    isUpdateLastWashedDate = true;
                    updateItemTask updateWashedDateTask = new updateItemTask();
                    String taskUrl = new String(constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=updateItemsLastLaundryDate&email=" + userSettings.getEmail() + "&item_id=" + item.getId() + "&last_laundry_date=" + washedDate);
                    updateWashedDateTask.execute(new String[]{taskUrl});
                }
                else{
                    result.add("didn't wash");
                    mDialogResult.finish(result); // dialog finished its work
                }
            }
            //isUpdateLastWashedDate = true
            else {
                result.add(washedDate);
                isUpdateLastWashedDate = false;
                mDialogResult.finish(result); // dialog finished its work
            }
        }
    }

    private void doTasks() {
        /*Date todaysDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        dateInFormat = format.format(todaysDate);*/
        result = new LinkedList<String>();

        updateItemTask updateInClosetTask = new updateItemTask();
        Short shelfNumber = Short.parseShort(shelfNumberSpinner.getSelectedItem().toString());
        String query = new String(constants.IPandPortOfDB + "/DBServlet_war/DBServlet?requestType=updateIsInCloset&email=" + userSettings.getEmail() + "&item_id=" + item.getId() + "&is_in_closet=1&shelf_number=" + shelfNumber);
        updateInClosetTask.execute(new String[] {query});
    }

    public void setDialogResult(OnMyDialogResult dialogResult){
        mDialogResult = dialogResult;
    }
    public interface OnMyDialogResult{
        void finish(List<String> result);
    }

}