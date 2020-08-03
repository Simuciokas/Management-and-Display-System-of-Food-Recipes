package com.scraper.recipes.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;

public class Recipe {

    private int id;
    private String title, imageURL = "", desc, sourceURL;
    private HashMap<String, String> ingredients = new HashMap<>();

    public Recipe(int id, String title, String imageURL, String desc, String sourceURL, HashMap<String, String> ingredients) {
        this.id = id;
        this.title = title;
        this.imageURL = imageURL;
        this.desc = desc;
        this.sourceURL = sourceURL;
        this.ingredients = ingredients;
    }

    public Recipe(String title, String desc, String sourceURL, HashMap<String, String> ingredients) {
        this.title = title;
        this.desc = desc;
        this.sourceURL = sourceURL;
        this.ingredients = ingredients;
    }

    @JsonIgnore
    public String getProducts() {
        StringBuilder sb = new StringBuilder();
        for (String k : ingredients.keySet()) {
            if (ingredients.get(k).equals("") || ingredients.get(k) == null)
                sb.append(k + "|");
            else
                sb.append(k + "_" + ingredients.get(k) + "|");
        }
        sb.deleteCharAt(sb.lastIndexOf("|"));
        return sb.toString();
    }

    public HashMap<String, String> getIngredients() {
        return ingredients;
    }

    public int getID() {
        return id;
    }

    public String getDesc() {
        return desc;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getSourceURL() {
        return sourceURL;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "Pavadinimas: " + title + "\n"
                + "Nuotrauka: " + imageURL + "\n"
                + "Aprašymas: " + desc + "\n"
                + "Šaltinis: " + sourceURL + "\n"
                + "Produktai: " + ingredients + "\n";
    }

}
