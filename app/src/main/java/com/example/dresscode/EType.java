package com.example.dresscode;

import java.util.ArrayList;

public enum EType {
    accessory("Accessory"),
    activeWear("Activewear"),
    cardigans("Cardigan"),
    dresses("Dress"),
    jackets("Jacket"),
    jeans("Jeans"),
    jumpsuits("Jumpsuit"),
    pants("Pants"),
    shorts("Shorts"),
    skirts("Skirt"),
    sleepWear("Sleepwear"),
    sweaters("Sweater"),
    tankTops("Tank Top"),
    top("Top"),
    tShirts("T-shirt");

    String typeName;

    EType(String type) {
        this.typeName = type;
    }
    @Override public String toString(){
        return typeName;
    }

    public static EType fromString(String text) {
        for (EType type : EType.values()) {
            if (type.toString().equalsIgnoreCase(text)) {
                return type;
            }
        }
        return null;
    }
    public static ArrayList<String> names() {
        EType[] types = values();
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < types.length; i++) {
            names.add(types[i].toString());
        }
        return names;
    }
}
