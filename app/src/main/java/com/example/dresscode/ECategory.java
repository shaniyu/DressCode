package com.example.dresscode;

import java.util.ArrayList;

public enum ECategory {
    casual("Casual"),
    goingOut("Going out"),
    occasion("Occasion"),
    official("Official");

    String categoryName;

    ECategory(String name) {
        this.categoryName = name;
    }
    @Override public String toString(){
        return categoryName;
    }
    public static ECategory fromString(String text) {
        for (ECategory category : ECategory.values()) {
            if (category.toString().equalsIgnoreCase(text)) {
                return category;
            }
        }
        return null;
    }
    public static ArrayList<String> names() {
        ECategory[] categories = values();
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < categories.length; i++) {
            names.add(categories[i].toString());
        }
        return names;
    }
}
