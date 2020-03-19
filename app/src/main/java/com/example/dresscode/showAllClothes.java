package com.example.dresscode;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class showAllClothes extends AppCompatActivity {

    private ListView mListView;
    private List<Item> mItemList; //Holds items to display in the listView in the activity
    private ArrayList<Item> mItemListForAction; //Holds the items that are checked for further actions (delete items or create set)
    private ItemListAdapter mItemListAdapter;
    private TextView noItemsText;

    // parameters to filter by the clothing list
    private EColor colorToFilterBy;
    private ESeason seasonToFilterBy;
    private ECategory categoryToFilterBy;
    private EType typeToFilterBy;
    int inClosetFilterBy;
    // order parameter
    int parameterToOrderBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_all_clothes);
        noItemsText = findViewById(R.id.noItemsTitle);
        initializeFilteringParams();
        setTitle("All Clothes");
        mItemList = new ArrayList<>();
        mItemList.addAll(Closet.getInstance().getItems()); //Holds a copy of all the items, to display in the listView in the activity
        displayItemList();


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent viewItemIntent = new Intent(showAllClothes.this, viewItem.class);;
                Item item = mItemList.get(position);
                viewItemIntent.putExtra("settingsObject", Closet.getInstance().getUserSettings());
                viewItemIntent.putExtra("itemID", item.getId());
                startActivityForResult(viewItemIntent, constants.VIEW_ITEM_CODE);
            }
        });

    }

    protected void onActivityResult ( int requestCode, int resultCode, Intent dataIntent){
        super.onActivityResult(requestCode, resultCode, dataIntent);
        // The returned result data is identified by requestCode.
        // The request code is specified in startActivityForResult method.
        List<Item> filteredList = Item.filterItems(Closet.getInstance().getItems(),typeToFilterBy, colorToFilterBy, categoryToFilterBy, seasonToFilterBy, inClosetFilterBy);
        //order the filtered list according to the current order request type
        if (parameterToOrderBy == 0)
            Collections.sort(filteredList, Item.itemIdComparator);
        else if (parameterToOrderBy == 1)
            Collections.sort(filteredList, Item.laundryDateComparator);
        else if (parameterToOrderBy ==2)
            Collections.sort(filteredList, Item.lastwornDateComparator);
        else
            Collections.sort(filteredList, Item.itemRateComparator);

        //update the list
        mItemList.clear(); // clean items list
        mItemList.addAll(filteredList); // add all items according to filtering
        mItemListAdapter.notifyDataSetChanged(); // notify adapter that the list has changed
        if (mItemList.isEmpty())
        {
            noItemsText.setText("No items under this filter");
            ConstraintLayout c = findViewById(R.id.noItemsLayout);
            c.getLayoutParams().height = ConstraintLayout.LayoutParams.WRAP_CONTENT;

        }
        else
        {
            noItemsText.setText("");
            ConstraintLayout c = findViewById(R.id.noItemsLayout);
            c.getLayoutParams().height = 0;
        }

    }

    private void initializeFilteringParams()
    {
        // in the creation of this page, there are no parameters to filter by.
        // just show all of them.
          colorToFilterBy = null;
          seasonToFilterBy = null;
          categoryToFilterBy = null;
          typeToFilterBy = null;
         inClosetFilterBy = -1;
    }

    private void displayItemList() {

        mListView = (ListView) findViewById(R.id.itemListView);
        mItemListForAction = new ArrayList<Item>();


        mItemListAdapter = new ItemListAdapter(this, R.layout.adapter_item_no_chkbox_view_layout, mItemList, this);
        mListView.setAdapter(mItemListAdapter);
        // if closet is empty, tell the user there is no items to show
        if (mItemList.size()==0)
        {
            noItemsText.setText(constants.NO_ITEMS_IN_CLOSET);
            ConstraintLayout c = findViewById(R.id.noItemsLayout);
            c.getLayoutParams().height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
        }
    }

    // this function is called when activity is born and initialize the menu in res/menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_show_all_clothes, menu);
        MenuItem item = menu.findItem(R.id.order_spinner);
        final Spinner orderingSpinner = (Spinner) item.getActionView();

        List<String> orderOptions = new ArrayList<>();
        orderOptions.add("Order");
        orderOptions.add("Last washed");
        orderOptions.add("Last worn");
        orderOptions.add("High rate");
        ArrayAdapter orderOptionsAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item,orderOptions);
        orderingSpinner.setAdapter(orderOptionsAdapter);

        // set the default value of the order spinner to none.
        // this means the list is sorted by item id, the async task gets it sorted from the db at first.
        parameterToOrderBy = orderOptionsAdapter.getPosition("Order");
        orderingSpinner.setSelection(parameterToOrderBy);

        orderingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            // it always sorts the list that is already filtered
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String orderParameterRequest = new String(orderingSpinner.getSelectedItem().toString()); // get selected choise
                List<Item> orderedList = new ArrayList<>(mItemList); // copy the list that is shown

                if (orderParameterRequest.equals("Order") && parameterToOrderBy != 0)
                {
                    parameterToOrderBy = 0; // order by item id if it wasn't already
                    Collections.sort(orderedList, Item.itemIdComparator);
                }
                else if (orderParameterRequest.equals("Last washed") && parameterToOrderBy != 1)
                {
                   parameterToOrderBy = 1;
                   Collections.sort(orderedList, Item.laundryDateComparator);
                 }
                 else if (orderParameterRequest.equals("Last worn") && parameterToOrderBy != 2)
                 {
                    parameterToOrderBy =2 ;
                    Collections.sort(orderedList, Item.lastwornDateComparator);
                 }
                 else if (orderParameterRequest.equals("High rate") && parameterToOrderBy != 3)
                 {
                    parameterToOrderBy = 3;
                    Collections.sort(orderedList, Item.itemRateComparator);
                 }

                 // update the shown list
                 mItemList.clear(); // clean items list
                 mItemList.addAll(orderedList); // add all items according to filtering
                 mItemListAdapter.notifyDataSetChanged(); // notify adapter that the list has changed
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent)
                {
                // then do nothing
                }
            });
         return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_filter_items:
                filterItems();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void filterItems()
    {
        // dialog for setting filter parameters
        filterItemsDialog filterItems = new filterItemsDialog(colorToFilterBy,seasonToFilterBy, typeToFilterBy, categoryToFilterBy,inClosetFilterBy,this, this, new filterItemsDialog.DialogListener() {
            @Override
            public void ready(EColor i_color, ESeason i_season,  EType i_type, ECategory i_category, int i_inCloset)
            {
                // this function is called by the filterItemsDialog when it finish its job (view items btn was clicked)
                // we got the parameters from the dialog, now filter by them
                List<Item> filteredList = Item.filterItems(Closet.getInstance().getItems(),i_type, i_color, i_category, i_season, i_inCloset);

                //order the filtered list according to the current order request type
                if (parameterToOrderBy == 0)
                    Collections.sort(filteredList, Item.itemIdComparator);
                else if (parameterToOrderBy == 1)
                    Collections.sort(filteredList, Item.laundryDateComparator);
                else if (parameterToOrderBy ==2)
                    Collections.sort(filteredList, Item.lastwornDateComparator);
                else
                    Collections.sort(filteredList, Item.itemRateComparator);

                //update the list
                mItemList.clear(); // clean items list
                mItemList.addAll(filteredList); // add all items according to filtering
                mItemListAdapter.notifyDataSetChanged(); // notify adapter that the list has changed
                if (mItemList.isEmpty())
                {
                    noItemsText.setText("No items under this filter");
                    ConstraintLayout c = findViewById(R.id.noItemsLayout);
                    c.getLayoutParams().height = ConstraintLayout.LayoutParams.WRAP_CONTENT;
                }
                else
                {
                    noItemsText.setText("");
                    ConstraintLayout c = findViewById(R.id.noItemsLayout);
                    c.getLayoutParams().height = 0;
                }
                // save the chosen parameters for the next filter
                updateParametersToFilterBy(i_color, i_season, i_type, i_category, i_inCloset);
            }
            @Override
            public void cancelled() {
                // canceled the filtering, do nothing.
            }
        });
        filterItems.show();
    }

    // this function gets parameters of filtering that what chosen in the filterItemsDialog and update
    // the parameters of this activity, so that next call to the filter\order will match those parameters
    // for the next times, unless they are changed again in the filterItemsDialog
    private void updateParametersToFilterBy(EColor i_color, ESeason i_season,  EType i_type, ECategory i_category, int i_inCloset)
    {
        colorToFilterBy = i_color;
        seasonToFilterBy = i_season;
        categoryToFilterBy = i_category;
        typeToFilterBy = i_type;
        inClosetFilterBy = i_inCloset;
    }
}
