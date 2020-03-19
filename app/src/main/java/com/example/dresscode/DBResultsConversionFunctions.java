package com.example.dresscode;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DBResultsConversionFunctions {

    // this function convert DB result ( string) of all items of a user to a items list
    public static List<Item> convertDBResultToItems(String str)
    { // FIX - need to take care of the setsList of each item, in another query
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List <Item> itemList = new ArrayList<Item>();
        Item resultItem;
        str= str.replace("[",""); // delete brackets
        str = str.replace("]","");
        str= str.replace("\"",""); // delete the opening and closing "
        String[] queryResults=str.split(";,"); // split complete result to separated answers

        if(!queryResults[0].equals("")) {
            for (String result : queryResults) {
                resultItem = new Item();
                result = result.replace(";", ""); // delete last ";"
                String[] fields = result.split(",");//array for single DB record columns (one item)

                resultItem.setId(Integer.parseInt(fields[0]));
                resultItem.setType(EType.fromString(fields[1]));
                if(fields[2].equals("") || fields[2].equals("null"))
                {
                    resultItem.setLastLaundrtDate(null);
                }
                else {
                    try {
                        resultItem.setLastLaundrtDate(sdf.parse(fields[2]));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                if(fields[3].equals("") || fields[3].equals("null"))
                {
                    resultItem.setShelfNumber((short)0);
                }
                else {
                    resultItem.setShelfNumber(Short.parseShort(fields[3]));
                }
                resultItem.setInCloset("1".equals(fields[4]));
                if(fields[5].equals("") || fields[5].equals("null"))
                {
                    resultItem.setRate((short)0);
                }
                else {
                    resultItem.setRate(Short.parseShort(fields[5]));
                }
                // DB dates formats is YYYY-MM-DD
                // so we convert this to a Date class (which has different format) that can be ordered
                if(fields[6].equals("") || fields[6].equals("null"))
                {
                    resultItem.setLastWornDate(null);
                }
                else {
                    try {
                        resultItem.setLastWornDate(sdf.parse(fields[6]));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                resultItem.setSeason(ESeason.fromString(fields[7]));
                resultItem.setCategory(ECategory.fromString(fields[8]));
                ;
                resultItem.setColor(EColor.fromString(fields[9]));
                if (fields.length == 11 && fields[10]!="") //there is a comment
                    resultItem.setComment(new String(fields[10]));
                itemList.add(resultItem);
            }
        }
        return  itemList;
    }

    public static List<Set> convertDBResultToSets(String str)
    {
        List<Set> setsList = new ArrayList<>();
        Set currentSet= new Set();
        Item currentItem;

        str = str.replace("[",""); // delete brackets
        str = str.replace("]","");
        str = str.replace("\"",""); // delete the opening and closing "
        String[] queryResults = str.split(";,"); // split complete result to separated answers

        if (!queryResults[0].equals(""))
        {
            for ( String result : queryResults)
            {
                result = result.replace(";", ""); // delete last ";"
                String[] fields = result.split(",");//array for single DB record columns (one item)

                if (currentSet.getItems() == null ) // empty set
                {
                    currentSet.setItems(new ArrayList<Item>()); // empty items list for a new set
                    currentSet.setSeason(ESeason.fromString(fields[2])); // set season for nwe set
                    currentSet.setId(Integer.parseInt(fields[0]));
                }
                else if (Integer.parseInt(fields[0]) != currentSet.getId()) // new set
                {
                    setsList.add(currentSet);
                    currentSet = new Set();
                    currentSet.setItems(new ArrayList<Item>()); // empty items list for a new set
                    currentSet.setId(Integer.parseInt(fields[0]));
                    currentSet.setSeason(ESeason.fromString(fields[2]));
                }

                currentItem = Closet.getItemById(Integer.parseInt(fields[1]));
                currentSet.getItems().add(currentItem);
            }
            setsList.add(currentSet);
        }
        return setsList;
    }

    public static userSettings convertDBReusltToUserSettings(String str)
    {
        userSettings userSettings;
        String name, email, city;
        short numberOfShelves;

        str= str.replace("[",""); // delete brackets
        str = str.replace("]","");
        str= str.replace("\"",""); // delete the opening and closing "
        String[] fields =str.split(","); // split complete result to separated answers

        if (!fields[0].equals("null")) // user exist in db
        {
            name = new String(fields[0]);
            email = new String(fields[1]);
            numberOfShelves = Short.parseShort(fields[2]);
            city = new String(fields[3]);
            userSettings = new userSettings(email, eCity.fromString(city), numberOfShelves, name);
        }
        else
            userSettings = null;
        return userSettings;
    }

    public static boolean convertDBResultToIsUserInDB(String str)
    {
        str= str.replace("[",""); // delete brackets
        str = str.replace("]","");

        //The original string was [] and after deleting it empty string remains
        if(str.equals(""))
        {
            return false;
        }
        return true; // user in DB - where his registration completed or not.
    }

    public static Collection<? extends Integer> convertDBResultToListOfSetsIDs(String str) {
        ArrayList<Integer> res = new ArrayList<>();

        str = str.replace("[","");
        str = str.replace("]","");

        if(str.equals("") || str.isEmpty())
        {
            return null;
        }

        String[] setsIDs = str.split(","); // split complete result to separated answers
        for(String setID : setsIDs){
            setID = setID.replace("\"", "");
            res.add(Integer.parseInt(setID));
        }

        return res;
    }
}
