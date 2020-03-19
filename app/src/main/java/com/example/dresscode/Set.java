package com.example.dresscode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Set implements Serializable {
    private List<Item> items;
    private ESeason season;
    private int id;
    private boolean isChecked = false;

    public boolean getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean checked) {
        isChecked = checked;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Item> getItems() {
        return items;
    }

    public int getId() {
        return id;
    }

    public ESeason getSeason() {
        return season;
    }

    public Set(List<Item> items, ESeason season) {
        this.items = items;
        this.season = season;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public void setSeason(ESeason season) {
        this.season = season;
    }

    public Set() {
        items = null;
    }


    // function to get only sets for current weather
    static public List<Set> filterSetsForWeather(double currentTempratue, List<Set> sets) {
        List<Set> result = new ArrayList<>();
        ESeason currentSeason;
        if (currentTempratue < ESeason.crossingSeason.lowestTemp)
            currentSeason = ESeason.winter;
        else if (currentTempratue < ESeason.summer.lowestTemp)
            currentSeason = ESeason.crossingSeason;
        else
            currentSeason = ESeason.summer;

        for (Set set : sets) {
            if (set.getSeason().equals(currentSeason) && allItemsOfSetInTheCloset(set))
                result.add(set);
        }
        return result;
    }
    static public List<Set> filterSetsBySeason(ESeason i_season, List<Set> i_list)
    {
        List<Set> result = new ArrayList<>();
        for (Set set : i_list)
        {
            if (set.getSeason().equals(i_season) || i_season == null)
                result.add(set);
        }
        return result;
    }

    static public boolean allItemsOfSetInTheCloset(Set i_Set)
    {
        for (Item item : i_Set.getItems())
        {
            if (!item.isInCloset())
                return false;
        }
        return true;
    }

    // for default oredering the sets in the show sets by their id
    public static Comparator<Set> setIdComparator = new Comparator<Set>() {
        public int compare(Set set1, Set set2) {
            // bigger id wins
            return ( Integer.compare(set1.getId(), set2.getId()));
        }
    };
}