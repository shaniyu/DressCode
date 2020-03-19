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

public class filterItemsDialog extends Dialog implements
        android.view.View.OnClickListener {
    public Activity c;
    public Button showItemsBtn;
    private Spinner seasonSpinner, categorySpinner, colorSpinner, isInClosetSpinner,typeSpinner;
    private Context mContext;
    private DialogListener mReadyListener ;

    // parameters to filter by
    private EColor colorToFilterBy;
    private ESeason seasonToFilterBy;
    private EType typeToFilterBy;
    private ECategory categoryToFilterBy;
    private int isInClosetToFilterBy;

    // the listener for this dialog is the showAllClothes activity
    public interface DialogListener {
        public void ready(EColor color, ESeason season, EType type,ECategory category, int inCloset);
        public void cancelled();
    }


    public filterItemsDialog(EColor i_color, ESeason i_season, EType i_type, ECategory i_category, int inCloset, Activity a, Context i_Context, DialogListener i_dialogListener)
    {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        mContext = i_Context;
        mReadyListener = i_dialogListener;
        // set the data members of the parameters
        colorToFilterBy = i_color;
        seasonToFilterBy = i_season;
        typeToFilterBy = i_type;
        categoryToFilterBy = i_category;
        isInClosetToFilterBy = inCloset;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.filter_items_dialog);
        // set this dialog animation
        getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;;
        showItemsBtn = (Button) findViewById(R.id.showItemsBtn);
        showItemsBtn.setOnClickListener(this);
        initializeSpinners();
    }

    private void initializeSpinners()
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
        else // user already chose this parameter to filter so show it.
        {
            int index = seasonAdapter.getPosition(seasonToFilterBy.toString());
            seasonSpinner.setSelection(index, false);
        }

        List<String> colors = new ArrayList<>();
        colors.add(0,"All");
        colors.addAll(EColor.names());
        ArrayAdapter colorAdapter = new ArrayAdapter(mContext, android.R.layout.simple_spinner_item,colors);
        colorSpinner = (Spinner) findViewById(R.id.color_spinner);
        colorSpinner.setAdapter(colorAdapter);
        if (colorToFilterBy == null)
        {
            colorSpinner.setSelection(0, false);
        }
        else
        {
            int index = colorAdapter.getPosition(colorToFilterBy.toString());
            colorSpinner.setSelection(index);
        }

        List<String> types = new ArrayList<>();
        types.add(0,"All");
        types.addAll(EType.names());
        ArrayAdapter typesAdapter = new ArrayAdapter(mContext, android.R.layout.simple_spinner_item,types);
        typeSpinner = (Spinner) findViewById(R.id.type_spinner);
        typeSpinner.setAdapter(typesAdapter);
        if (typeToFilterBy == null)
        {
            typeSpinner.setSelection(0, false);
        }
        else
        {
            int index = typesAdapter.getPosition(typeToFilterBy.toString());
            typeSpinner.setSelection(index);
        }

        List<String> categories = new ArrayList<>();
        categories.add(0,"All");
        categories.addAll(ECategory.names());
        ArrayAdapter categoryAdapter = new ArrayAdapter(mContext, android.R.layout.simple_spinner_item,categories);
        categorySpinner = (Spinner) findViewById(R.id.category_spinner);
        categorySpinner.setAdapter(categoryAdapter);
        if (categoryToFilterBy == null)
        {
            categorySpinner.setSelection(0, false);
        }
        else
        {
            int index = categoryAdapter.getPosition(categoryToFilterBy.toString());
            categorySpinner.setSelection(index);
        }

        List<String> isInCloset = new ArrayList<>();
        isInCloset.add(0,"All");
        isInCloset.add(1,"Yes");
        isInCloset.add(2,"No");
        ArrayAdapter isInClosetAdapter = new ArrayAdapter(mContext, android.R.layout.simple_spinner_item,isInCloset);
        isInClosetSpinner = (Spinner) findViewById(R.id.is_in_closet_spinner);
        isInClosetSpinner.setAdapter(isInClosetAdapter);
        if (isInClosetToFilterBy == -1)
        {
            int index = isInClosetAdapter.getPosition("All");
            isInClosetSpinner.setSelection(index);
        }
        else if (isInClosetToFilterBy == 1)
        {
            int index = isInClosetAdapter.getPosition("Yes");
            isInClosetSpinner.setSelection(1, false);
        }
        else
        {
            // it was 0
            int index = isInClosetAdapter.getPosition("No");
            isInClosetSpinner.setSelection(2,false);
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.showItemsBtn:
                String season, color, type, category, inCloset;
                ESeason selectedSeason = null;
                EColor selectedColor = null;
                EType selectedType= null;
                ECategory selectedCategory = null;
                int inClosetChoise = -1;

                season = new String(seasonSpinner.getSelectedItem().toString());
                color = new String(colorSpinner.getSelectedItem().toString());
                type = new String(typeSpinner.getSelectedItem().toString());
                category = new String(categorySpinner.getSelectedItem().toString());
                inCloset  = new String(isInClosetSpinner.getSelectedItem().toString());

                if (! season.equals("All"))
                    selectedSeason = ESeason.fromString(season);
                if (! color.equals("All"))
                 selectedColor = EColor.fromString(color);
                if (! type.equals("All"))
                    selectedType = EType.fromString(type);
                if (! category.equals("All"))
                    selectedCategory = ECategory.fromString(category);
                if (! inCloset.equals("All"))
                {
                    if (inCloset.equals("Yes"))
                        inClosetChoise = 1;
                    else
                        inClosetChoise = 0;
                }
                // pass the filtering parameters to the showAllClothes screen
                mReadyListener.ready(selectedColor, selectedSeason, selectedType, selectedCategory, inClosetChoise);
                filterItemsDialog.this.dismiss(); // finish this dialog
                break;
            default:
                break;
        }
    }
}
