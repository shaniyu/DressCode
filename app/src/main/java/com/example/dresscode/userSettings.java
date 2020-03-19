package com.example.dresscode;

import java.io.Serializable;

public class userSettings implements Serializable {
    private String email;
    private eCity city;
    private short NumberOfShelves;
    private String name;

    public userSettings(String email, eCity city, short numberOfShelves, String name) {
        this.email = email;
        this.city = city;
        NumberOfShelves = numberOfShelves;
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public void setCity(eCity city) {
        this.city = city;
    }

    public void setNumberOfShelves(short numberOfShelves) {
        NumberOfShelves = numberOfShelves;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public eCity getCity() {
        return city;
    }

    public short getNumberOfShelves() {
        return NumberOfShelves;
    }

    public userSettings() {
    }
}
