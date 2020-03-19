package com.example.dresscode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Item implements Serializable {
    private EType type;
    private ESeason season;
    private EColor color;
    private ECategory category;
    private Date lastLaundrtDate;
    private Date lastWornDate;
    private short shelfNumber;
    private short rate;
    private int id;
    private boolean isInCloset;
    private String comment;
    private boolean isChecked = false;

    public boolean getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean checked) {
        isChecked = checked;
    }

    public void setColor(EColor color) {
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public void setCategory(ECategory category) {
        this.category = category;
    }

    public void setShelfNumber(short shelfNumber) {
        this.shelfNumber = shelfNumber;
    }

    public void setRate(short rate) {
        this.rate = rate;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }


    public Item(EType type, ESeason season, EColor color,
                ECategory category, Date lastLaundryDate,
                Date lastWornDate, short shelfNumber, short rate,
                boolean isInCloset, String comment, int id) {
        this.type = type;
        this.season = season;
        this.color = color;
        this.category = category;
        this.lastLaundrtDate = lastLaundryDate;
        this.lastWornDate = lastWornDate;
        this.shelfNumber = shelfNumber;
        this.rate = rate;
        this.isInCloset = isInCloset;
        this.comment = comment;
        this.id = id;
    }
    public Item()  // empty ctor for translating query result to an item
    { }

    public void setType(EType type) {
        this.type = type;
    }

    public void setSeason(ESeason season) {
        this.season = season;
    }

    public void setLastLaundrtDate(Date lastLaundrtDate) {
        this.lastLaundrtDate = lastLaundrtDate;
    }

    public void setLastWornDate(Date lastWornDate) {
        this.lastWornDate = lastWornDate;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setInCloset(boolean inCloset) {
        isInCloset = inCloset;
    }

    public EType getType() {
        return type;
    }

    public ESeason getSeason() {
        return season;
    }

    public EColor getColor() {
        return color;
    }

    public ECategory getCategory() {
        return category;
    }

    public Date getLastLaundrtDate() {
        return lastLaundrtDate;
    }

    public Date getLastWornDate() {
        return lastWornDate;
    }

    public short getShelfNumber() {
        return shelfNumber;
    }

    public short getRate() {
        return rate;
    }

    public boolean isInCloset() {
        return isInCloset;
    }

    public String getComment() {
        return comment;
    }

    public static Comparator<Item> itemRateComparator
            = new Comparator<Item>() {
        public int compare(Item item1, Item item2) {

            short firstItemRate = item1.getRate();
            short secondItemRate = item2.getRate();
            return Integer.compare(secondItemRate, firstItemRate);
        }
    };

    public static Comparator<Item> laundryDateComparator
            = new Comparator<Item>() {
        public int compare(Item item1, Item item2) {
            // last date first
            Date firstItemDate = item1.getLastLaundrtDate();
            Date secondItemDate = item2.getLastLaundrtDate();
            if (firstItemDate == null && secondItemDate == null) {
                return 0;
            } else if (firstItemDate == null) {
                return 1;
            } else if (secondItemDate == null) {
                return -1;
            } else {
                return secondItemDate.compareTo(firstItemDate);
            }
        }
    };

    public static Comparator<Item> lastwornDateComparator
            = new Comparator<Item>() {
        public int compare(Item item1, Item item2) {
            // last date first
            Date firstItemDate = item1.getLastWornDate();
            Date secondItemDate = item2.getLastWornDate();
            if (firstItemDate == null && secondItemDate == null) {
                return 0;
            } else if (firstItemDate == null) {
                return 1;
            } else if (secondItemDate == null) {
                return -1;
            } else {
                return secondItemDate.compareTo(firstItemDate);
            }
        }
    };

    public static Comparator<Item> itemIdComparator = new Comparator<Item>() {
        public int compare(Item item1, Item item2) {
            // bigger id wins
            return ( Integer.compare(item1.getId(), item2.getId()));
        }
    };

    public static List<Item> filterItems( List<Item> items,EType type, EColor color, ECategory category, ESeason season, int isInCloset)
    {
        boolean itemInCloset ;
        if (isInCloset == 1)
            itemInCloset = true;
        else
            itemInCloset = false;

        List<Item> result = new ArrayList<>();
        for ( Item item : items )
        {
            if (    (type == (null) || item.getType().equals(type)) &&
                    (color== (null) || item.getColor().equals(color)) &&
                    (category ==(null) || item.getCategory().equals(category)) &&
                    (season== (null) || item.getSeason().equals(season)) &&
                    (isInCloset == -1 || item.isInCloset == itemInCloset))
                result.add(item);
        }
        return result;
    }
}

// Example of order items of the closet : (By yuval)
//         if we want to sort the items of the closet we need to perform:
//         1) create a copy of the list
//         List<Item> currentList = new ArrayList(userCloset.getItems());
//         2) choose the required comparator and use this:
//         Collections.sort(currentList, Item.laundryDateComparator);

// Example of filtering items
//         List<Item> currentList = Item.filterItems(userCloset.getItems(),EType.shorts, null,null,ESeason.summer, 1);
