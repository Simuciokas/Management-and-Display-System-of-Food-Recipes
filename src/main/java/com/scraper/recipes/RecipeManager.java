package com.scraper.recipes;

import com.scraper.recipes.objects.Recipe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecipeManager {

    private int highestID;
    private static ArrayList<Recipe> recipes = new ArrayList<>();
    private static HashMap<Integer, HashMap<String, String>> websites = new HashMap<>();
    private static HashMap<Integer, HashMap<String, Pattern>> patterns = new HashMap<>();

    public ArrayList<Recipe> findRecipe(String term) {
        ArrayList<Recipe> recipesByTitle = new ArrayList<>();
        ArrayList<Recipe> recipesByIngredients = new ArrayList<>();
        for (Recipe r : recipes) {
            boolean found = false;
            if (r.getTitle().contains(term)) {
                recipesByTitle.add(r);
                found = true;
            }
            if (!found) {
                for (String s : r.getIngredients().keySet()) {
                    if (s.contains(term)) {
                        recipesByIngredients.add(r);
                        break;
                    }
                }
            }
        }
        recipesByTitle.addAll(recipesByIngredients);
        return recipesByTitle;
    }

    public boolean removeRecipeByID(int id) {
        try {
            recipes.remove(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void setRecipes(ArrayList<Recipe> recipes) {
        RecipeManager.recipes = recipes;
    }

    public Recipe getRecipeByID(int id) {
        for (Recipe r : recipes)
            if (r.getID() == id)
                return r;
        return null;
    }

    public void setHighestID(int highestID) {
        this.highestID = highestID;
    }

    public long getHighestID() {
        return highestID;
    }

    public ArrayList<Recipe> getRecipes() {
        return recipes;
    }

    public HashMap<Integer, HashMap<String, Pattern>> getPatterns() {
        return patterns;
    }

    public HashMap<Integer, HashMap<String, String>> getWebsites() {
        return websites;
    }

    public Recipe getRecipeFromURL(String url) throws IOException {
        return getRecipeFromURL(new URL(url));
    }

    public boolean hasRecipe(Recipe recipe) throws MalformedURLException {
        return hasRecipe(new URL(recipe.getSourceURL()));
    }

    public boolean hasRecipe(URL url) {
        for (Recipe r : recipes) {
            if (r.getSourceURL().equals(""+url))
                return true;
        }
        return false;
    }

    public boolean addRecipe(String url) throws IOException {
        return addRecipe(new URL(url));
    }

    public boolean addRecipe(URL url) throws IOException {
        Recipe recipe = getRecipeFromURL(url);
        if (recipe == null)
            return false;
        if (!hasRecipe(recipe)) {
            recipes.add(recipe);
            return true;
        }
        return false;
    }

    public Recipe getRecipeFromURL(URL url) throws IOException {
        Recipe recipe = null;
        for (Integer i : websites.keySet()) {
            if (url.toString().contains(websites.get(i).get("URL"))) {
                return getRecipe(url, patterns.get(i));
            }
        }
        return recipe;
    }

    public Recipe getRecipe(URL url, HashMap<String, Pattern> patternMap) throws IOException {
        Recipe recipe = null;
        URLConnection uc = url.openConnection();
        InputStream is = null;
        try {
            is = uc.getInputStream();
        } catch (Exception e) {
            System.out.println(uc.getURL());
            System.out.println("404");

        }

        // If connected
        if (is != null) {

            // Checking if path exists, so that it's not a homepage
            if (uc.getURL().getPath() != null && !uc.getURL().getPath().isEmpty()) {
                //System.out.println(uc.getURL().getPath());

                // Printing URL
                System.out.println(uc.getURL().toString());

                if (uc.getURL().toString().contains("delfi.lt") && !uc.getURL().getPath().contains("1000receptu"))
                    return recipe;

                if (hasRecipe(uc.getURL())) {
                    System.out.println("\nJau egzistuoja");
                    return recipe;
                }

                // Errors
                StringBuilder errors = new StringBuilder("");

                // <Name, Size>
                HashMap<String, String> names = new HashMap<>();
                String size = "";

                // Instructions
                StringBuilder desc = new StringBuilder("");

                // Image
                String image = "";

                // Title
                String title = "";

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String line = null;

                boolean found = false;
                while ((line = reader.readLine()) != null) {
                    for (String s : patternMap.keySet()) {
                        Matcher m = patternMap.get(s).matcher(line);
                        if (m.matches()) {
                            //System.out.println(s);

                            if (s.contains("desc")) {

                                // delfi.lt/1000receptu
                                if (("" + url).contains("delfi.lt")) {
                                    //System.out.println(line);
                                    for (String trunc : line.replace("/", "").split("<p>")) {
                                        String l = trunc.replaceAll("<a href.*\">", "").replace("<a>", "");
                                        //System.out.println(l);
                                        if (!l.contains("<") && !l.contains(">") && !l.contains("Būkite įvykių sūkuryje ir svarb") && !l.contains("DELFI") && l.trim().length() > 0)
                                            desc.append(l.replace(". ", ".").replace(".", ". "));
                                    }
                                }

                                // lamaistas.lt
                                if ((""+url).contains("lamaistas.lt") && !m.group(1).equals("Daugiau") && !m.group(1).equals("Kraunama...")) {
                                    desc.append(m.group(1).replace(". ", ".").replace(".", ". "));
                                }

                            }

                            if (s.contains("Name")) {
                                //System.out.println(line);
                                if (!m.group(1).contains("<") && !m.group(1).trim().isEmpty()) {
                                    // If size is found first
                                    if (found) {
                                        found = false;
                                        names.put(m.group(1), size);
                                    } else
                                        names.put(m.group(1), "");
                                    //System.out.println("Name: " + m.group(1));
                                }

                            }
                            if (s.contains("Size")) {
                                //System.out.println(line);
                                if (!m.group(1).contains("<") && !m.group(1).trim().isEmpty()) {
                                    if (found) {
                                        System.err.println("Found two sizes in a row");
                                        return null;
                                    }
                                    found = true;
                                    size = m.group(1);
                                    //System.out.println("Size: " + m.group(1));
                                }
                            }
                            if (s.contains("image")) {
                                //System.out.println(m.group(1));
                                if (("" + url).contains("lamaistas.lt"))
                                    image = "https://www.lamaistas.lt/uploads/modules/recipes/fullsize/" + m.group(1);
                                else
                                    image = m.group(1);
                                //System.out.println("Image: " + m.group(1));
                            }
                            if (s.contains("title")) {
                                title = m.group(1);
                                //System.out.println("Title: " + m.group(1));
                            }
                        }
                    }
                    // For receptai.lt to get Instructions
                    if (("" + url).contains("receptai.lt") && (line.contains("recipeInstructions"))) {
                        //desc.add(line.replace("                \"recipeInstructions\": \"","").replace("\"",""));
                        desc.append(line.replace("                \"recipeInstructions\": \"", "").replace("\"", ""));
                        if (!line.endsWith("\"")) {
                            while ((line = reader.readLine()) != null) {
                                if (line.endsWith("\"")) {
                                    desc.append(line.replace("\"", ""));
                                    //desc.add(line.replace("\"",""));
                                    break;
                                }
                                else
                                    desc.append(line);
                            }
                        }
                    }
                }

                if (title.isEmpty())
                    errors.append("[HIGH] Nera pavadinimo\n");
                if (image.isEmpty())
                    errors.append("[LOW] Nera nuotraukos\n");
                if (desc.toString().isEmpty())
                    errors.append("[HIGH] Nera aprasymo\n");
                if (names.isEmpty())
                    errors.append("[HIGH] Nera produktu\n");
                if (!errors.toString().equals(""))
                    System.out.println("Klaidos: " + errors);
                if (!title.isEmpty() && !desc.toString().isEmpty() && !names.isEmpty()) {
                    recipe = new Recipe(highestID + 1, title, image, desc.toString(), uc.getURL().toString(), names);
                    System.out.println("Pridėtas receptas: " + recipe);
                    highestID += 1;
                }

            } else {
                System.out.println(url);
            }
        }
        return recipe;
    }

}
