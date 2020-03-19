package com.example.dresscode;

import com.google.zxing.integration.android.IntentIntegrator;

public class constants {
    public static final int SETTINGS_CODE = 1;
    public static final int ADD_ITEM_CODE = 2;
    public static final int LOGIN_PAGE_CODE = 3;
    public static final int ERROR_PAGE_CODE = 4;
    public static final int QR_SCANNER_CODE = IntentIntegrator.REQUEST_CODE;
    public static final int PICK_IMAGE_CODE = 5;
    public static final int maxNameLength = 12;
    public static final int maxDescriptionLength = 40;
    public static final int SHOW_ALL_CLOTHES_CODE = 6;
    public static final int LOG_IN = 7;
    public static final int SIGN_UP = 8;
    public static final int VIEW_ITEM_CODE = 9;
    public static final int VIEW_SETS_CODE = 11;
    public static final int REMOVE_ITEMS_CODE = 10;
    public static final int CREATE_NEW_SET_CODE = 13;
    public static final int EDIT_SET_ITEMS_CODE = 14;
    public static final int DELETE_SET_CODE = 15;
    public static final int ADD_ITEMS_TO_EXISTING_SET = 16;
    public static String OPEN_WEATHER_MAP_API = "8c17f4542e4e9ef73ec0b5d175d43603";
    public static String IMAGE_FAILED = "@drawable/image_failed";
    public static String NO_ITEMS_IN_CLOSET = "You don't have any items yet";
    public static String NO_ITEMS_TO_ADD = "You don't have items to add to this set";
    public static final int smallImageViewSize = 450;
    public static final int imageCompressingSizeInPixels = 350;
    public static final String aboutUsText = "DressCode application is our degree final project.\n" +
            "We created it to enable people \nto manage their closet with their smart phones.\n" +
            "You can use this app to view your clothing items,\n create your favourite sets, and" +
            " even\n get recommendation of set that fits today's weather!\n"+
            "Hope you'll enjoy our app, and have a great experience.";
    public static final String ourNames = "Team DressCode,\n Yuval, Inbal and Shani.";
    public static final String IPandPortOfDB = "http://192.116.98.71:81";
    public static final String DB_EXCEPTION = "No access to DB";
}

