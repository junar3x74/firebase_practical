package com.example.firebase_practtical;

public class Item {

    private String id;
    private String name;

    // No-argument constructor required for Firebase
    public Item() {}

    public Item(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name; // Used by ArrayAdapter to display in ListView
    }
}
