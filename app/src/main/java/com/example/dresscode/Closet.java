package com.example.dresscode;

import java.io.Serializable;
import java.util.List;

public class Closet implements Serializable {
    private userSettings userSettings;
    private static List<Item> items;
    private List<Set> sets;
    private boolean isItemsUpdated;
    private boolean isSetsUpdated;
    private static final Closet userCloset = new Closet();

    public static Closet getInstance() {
        return userCloset;
    }

    private Closet() { }

    public void setSets(List<Set> sets) {
        this.sets = sets;
    }

    public List<Set> getSets() {
        return sets;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public void setItemsUpdated(boolean itemsUpdated) {
        isItemsUpdated = itemsUpdated;
    }

    public void setSetsUpdated(boolean setsUpdated) {
        isSetsUpdated = setsUpdated;
    }

    public void setUserSettings(com.example.dresscode.userSettings userSettings) {
        this.userSettings = userSettings;
    }

    public com.example.dresscode.userSettings getUserSettings() {
        return userSettings;
    }

    public Closet(com.example.dresscode.userSettings userSettings, List<Item> items, boolean isItemsUpdated, boolean isSetsUpdated) {
        this.userSettings = userSettings;
        this.items = items;
        this.isItemsUpdated = isItemsUpdated;
        this.isSetsUpdated = isSetsUpdated;
    }

    public String getUserEmail() {
        return userSettings.getEmail();
    }

    public boolean isItemsUpdated() {
        return isItemsUpdated;
    }

    public boolean isSetsUpdated() {
        return isSetsUpdated;
    }

    public List<Item> getItems() {
        return items;
    }

    // get item by item id
    public static Item getItemById(int id)
    {
        for (Item item : items)
        {
            if ( item.getId() == id )
                return item;
        }
        return null;
    }

    public Set getSetById(int id)
    {
        for (Set set : sets)
        {
            if (set.getId() == id)
                return set;
        }
        return null;
    }
}
