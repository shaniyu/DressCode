package com.example.dresscode;

import java.util.ArrayList;

public enum ESeason {
    summer(25,60,"Summer"),
    crossingSeason(18,24,"Transition season"),
    winter(-30,17,"Winter");

    int lowestTemp, highestTemp;
    String seasonName;

    ESeason(int lowest, int highest, String name)
    {
        lowestTemp=lowest;
        highestTemp=highest;
        seasonName= name;
    }

    @Override public String toString(){
        return seasonName;
    }

    public static ESeason fromString(String text) {
        for (ESeason season : ESeason.values()) {
            if (season.toString().equalsIgnoreCase(text)) {
                return season;
            }
        }
        return null;
    }

    public static ArrayList<String> names() {
        ESeason[] seasons = values();
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < seasons.length; i++) {
           names.add(seasons[i].toString());
        }
        return names;
    }

    public static ESeason getCurrentSeason(double currTemperature){
        ESeason currentSeason;

        if (currTemperature < ESeason.crossingSeason.lowestTemp)
            currentSeason = ESeason.winter;
        else if (currTemperature < ESeason.summer.lowestTemp)
            currentSeason = ESeason.crossingSeason;
        else
            currentSeason = ESeason.summer;

        return currentSeason;
    }
}

