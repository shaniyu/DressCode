package com.example.dresscode;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class filterSetsDialog extends Dialog implements
        android.view.View.OnClickListener {

    public Activity c;
    public Button showSetsBtn;
    private Spinner seasonSpinner;
    private Context mContext;
    private filterSetsDialog.DialogListener mReadyListener ;
    // parameter to filter by
    private ESeason seasonToFilterBy;

    // the listener for this dialog is the showAllClothes activity
    public interface DialogListener {
        public void ready(ESeason season);
        public void cancelled();
    }

    public filterSetsDialog(ESeason i_season, Activity a, Context i_Context, filterSetsDialog.DialogListener i_dialogListener)
    {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        mContext = i_Context;
        mReadyListener = i_dialogListener;
        seasonToFilterBy = i_season;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.filter_sets_dialog);
        // set this dialog animation
        getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;;
        showSetsBtn = (Button) findViewById(R.id.showSetsBtn);
        showSetsBtn.setOnClickListener(this);
        initializeSpinner();
    }
    private void initializeSpinner()
    {
        List<String> seasons = new ArrayList<>();
        seasons.add(0,"All");
        seasons.addAll(ESeason.names());
        ArrayAdapter seasonAdapter = new ArrayAdapter(mContext, android.R.layout.simple_spinner_item,seasons);
        seasonSpinner = (Spinner) findViewById(R.id.season_spinner);
        seasonSpinner.setAdapter(seasonAdapter);
        // no need to show selected season in the filtering params
        if (seasonToFilterBy == null)
        {
            seasonSpinner.setSelection(0, false);
        }
        else // user already chose season to filter so show it.
        {
            int index = seasonAdapter.getPosition(seasonToFilterBy.toString());
            seasonSpinner.setSelection(index, false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.showSetsBtn:
                String season;
                ESeason selectedSeason = null;
                season = new String(seasonSpinner.getSelectedItem().toString());

                if (! season.equals("All"))
                    selectedSeason = ESeason.fromString(season);

                // pass the filtering parameters to the showAllClothes screen
                mReadyListener.ready(selectedSeason);
                filterSetsDialog.this.dismiss(); // finish this dialog
                break;
            default:
                break;
        }
    }
}
