package com.oosubhisara.mm6editor;

public class DetailedItem {
    public String name;
    public String value;
    
    DetailedItem(String name, int value) {
        this.name = name;
        this.value = String.valueOf(value);
    }

    DetailedItem(String name, String value) {
        this.name = name;
        this.value = value;
    }
    
    public String getName() { return name; };
    public String getValue() { return value; };
}
