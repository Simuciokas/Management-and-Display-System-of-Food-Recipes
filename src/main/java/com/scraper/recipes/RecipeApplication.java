package com.scraper.recipes;


import com.scraper.recipes.objects.Recipe;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;



@SpringBootApplication
public class RecipeApplication {

    private static RecipeManager recipeManager = new RecipeManager();

    private static Connection con = DBConfig.conDB();

    private static PreparedStatement ps = null;

    private static ResultSet rs = null;

    public static RecipeManager getInstance() {
        return recipeManager;
    }

    public static Connection getCon() {
        return con;
    }

    public static void syncFromDB() {
        String sql = "SELECT * FROM receptai";
        int highestID = 0;
        ArrayList<Recipe> recipes = new ArrayList<>();
        try {
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (!rs.getString("products").equals("Prduktai")) {
                    HashMap<String, String> ingredients = new HashMap<>();
                    for (String s : rs.getString("products").split("\\|")) {
                        String[] split = s.split("_");
                        if (split.length == 1)
                            ingredients.put(split[0], "");
                        else
                            ingredients.put(split[0], split[1]);
                    }
                    recipes.add(new Recipe(rs.getInt("id"), rs.getString("title"), rs.getString("image"), rs.getString("desc"), rs.getString("source"), ingredients));
                    highestID = rs.getInt("id");
                }
            }
        }
        catch (Exception e) {
            System.out.println("Blogai: " + e.getMessage());
        }
        recipeManager.setRecipes(recipes);
        recipeManager.setHighestID(highestID);
        System.out.println("Loaded " + recipes.size() + " recipes from DB");
    }

    public static boolean deleteRecipesFromDB() {
        String sql = "DELETE FROM `receptai`";
        try {
            ps = con.prepareStatement(sql);
            int up = ps.executeUpdate();
            if (up >= 1) {
                System.out.println("Sėkmingai ištrinti duomenys iš DB");
            }
            else {
                System.out.println("Ivyko klaida trinant DB");
                return false;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean saveRecipesToDB() {
        deleteRecipesFromDB();
        String sql = "INSERT INTO receptai (id, title, image, receptai.desc, source, products) VALUES (?, ?, ?, ?, ?, ?);";
        try {
            ps = con.prepareStatement(sql);
            for (Recipe r : recipeManager.getRecipes()) {
                ps.setInt(1, r.getID());
                ps.setString(2, r.getTitle());
                ps.setString(3, r.getImageURL());
                ps.setString(4, r.getDesc());
                ps.setString(5, r.getSourceURL());
                ps.setString(6, r.getProducts());
                int up = ps.executeUpdate();
                if (up == 1) {
                    System.out.println("Pridėtas receptas su ID " + r.getID());
                }
                else {
                    System.out.println("Nepavyko pridėti recepto su ID " + r.getID());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void main(String[] args) throws IOException, ParseException {

        // Start Web Service
        SpringApplication.run(RecipeApplication.class, args);

        // Getting json file
        JSONParser parser = new JSONParser();
        JSONObject main = (JSONObject) parser.parse(new FileReader("web scraper regexes.json"));

        // Getting regexes
        int n = 0;
        for (Object o : (JSONArray) main.get("websites")) {
            n++;
            HashMap<String, String> values = new HashMap<>();
            HashMap<String, Pattern> pattern = new HashMap<>();
            JSONObject website = (JSONObject) o;
            values.put("URL", (String) website.get("mainURL"));
            values.put("recipeURL", (String) website.get("recipeURL"));
            JSONObject regex = (JSONObject) website.get("regexes");
            //values.put("iIDs", (String) regex.get("ingredientIDs"));
            values.put("title", (String) regex.get("title"));
            values.put("iNames", (String) regex.get("ingredientNames"));
            values.put("iSizes", (String) regex.get("ingredientSize"));
            values.put("image", (String) regex.get("image"));
            if (regex.containsKey("description")) {
                values.put("desc", (String) regex.get("description"));
                pattern.put("desc", Pattern.compile(values.get("desc"), Pattern.CASE_INSENSITIVE));
            }

            for (String s : "title,iNames,iSizes,image".split(",")) {
                pattern.put(s, Pattern.compile(values.get(s), Pattern.CASE_INSENSITIVE));
            }
            recipeManager.getPatterns().put(n, pattern);
            recipeManager.getWebsites().put(n, values);
        }

        syncFromDB();

        //System.out.println(recipeManager.getRecipe(new URL("https://www.receptai.lt/receptas/blyneliai-su-trintomis-braskemis-11879"), recipeManager.getPatterns().get(1)));
        //System.out.println(recipeManager.getRecipe(new URL("https://www.delfi.lt/1000receptu/receptai/tobulai-skanus-morku-tortas-velykoms.d?id=70766652"), recipeManager.getPatterns().get(2)));
        //System.out.println(recipeManager.getRecipe(new URL("https://www.lamaistas.lt/receptas/vistienos-kepeneles-su-obuoliais-64020"), recipeManager.getPatterns().get(3)));
        //recipeManager.addRecipe("https://www.delfi.lt/1000receptu/receptai/tobulai-skanus-morku-tortas-velykoms.d?id=70766652");


        // 64010
        // Lamaistas.lt
/*
        for (int i = 64010; i > 0; i--) {
            recipeManager.addRecipe(recipeManager.getWebsites().get(3).get("recipeURL").replace("#", ""+i));
        }

        saveRecipesToDB();

        // receptai.lt
        for (int i = 13066; i > 0; i--) {
            recipeManager.addRecipe(recipeManager.getWebsites().get(1).get("recipeURL").replace("#", ""+i));
        }

        //saveRecipesToDB();


        // delfi.lt/1000receptu
        for (int i = 81789215; i > 62211247 ; i--) {
            recipeManager.addRecipe(recipeManager.getWebsites().get(2).get("recipeURL").replace("#", ""+i));
        }

         */

        //saveRecipesToDB();




    }
}