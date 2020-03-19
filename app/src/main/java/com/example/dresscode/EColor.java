package com.example.dresscode;

import java.util.ArrayList;

public enum EColor {
    black("Black"),
    blue("Blue"),
    brown("Brown"),
    green("Green"),
    grey("Grey"),
    multicolor("Multicolor"),
    orange("Orange"),
    pink("Pink"),
    purple("Purple"),
    red("Red"),
    white("White"),
    yellow("Yellow");

    String colorName;

    EColor(String colorName) {
        this.colorName = colorName;
    }

    @Override public String toString(){
        return colorName;
    }
    public static EColor fromString(String text) {
        for (EColor color : EColor.values()) {
            if (color.toString().equalsIgnoreCase(text)) {
                return color;
            }
        }
        return null;
    }
    public static ArrayList<String> names() {
        EColor[] colors = values();
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < colors.length; i++) {
            names.add(colors[i].toString());
        }
        return names;
    }
}
