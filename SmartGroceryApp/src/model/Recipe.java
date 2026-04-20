package SmartGroceryApp.src.model;

import java.util.ArrayList;
import java.util.List;

//Represents a recipe with a name and a list of ingredients.
//Provides a toString() method to print the recipe and convert the object to a string.
public class Recipe {
    public String name;
    public List<Ingredient> ingredients = new ArrayList<>();

    public Recipe() {}
    public Recipe(String name) { this.name = name; }

    public void addIngredient(Ingredient ing) { ingredients.add(ing); }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("\n");
        for (Ingredient i : ingredients) sb.append("  - ").append(i.toString()).append("\n");
        return sb.toString();
    }
}
