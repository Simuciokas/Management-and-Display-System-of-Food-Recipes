package com.scraper.recipes.webservice;

import java.io.IOException;
import java.util.ArrayList;

import com.scraper.recipes.RecipeApplication;
import com.scraper.recipes.objects.Recipe;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class RecipeController {

    @GetMapping("recipes")
    public ArrayList<Recipe> getRecipes() {
        return RecipeApplication.getInstance().getRecipes();
    }

    @GetMapping("recipe/{id}")
    public Recipe getRecipe(@PathVariable int id) {
        return RecipeApplication.getInstance().getRecipeByID(id);
    }

    @PostMapping("recipe/{url}/{id}")
    public boolean addRecipe(@PathVariable String url, @PathVariable String id) throws IOException {
        String link = "";
        for (int i : RecipeApplication.getInstance().getWebsites().keySet()) {
            if (RecipeApplication.getInstance().getWebsites().get(i).get("URL").contains(url))
                link = RecipeApplication.getInstance().getWebsites().get(i).get("recipeURL");
        }
        if (link.isEmpty())
            return false;
        return RecipeApplication.getInstance().addRecipe(link.replace("#",id));
    }

    @GetMapping("search/{term}")
    public ArrayList<Recipe> searchRecipe(@PathVariable String term) {
        return RecipeApplication.getInstance().findRecipe(term);
    }

    @DeleteMapping("recipe/{id}")
    public boolean deleteRecipe(@PathVariable int id) {
        return RecipeApplication.getInstance().removeRecipeByID(id);
    }

    @PostMapping("recipes/db")
    public boolean saveRecipes() {
        return RecipeApplication.saveRecipesToDB();
    }

    @DeleteMapping("recipes/db")
    public boolean deleteRecipes() {
        return RecipeApplication.deleteRecipesFromDB();
    }

}