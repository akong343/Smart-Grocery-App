package SmartGroceryApp; 

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import SmartGroceryApp.src.model.GroceryItem;
import SmartGroceryApp.src.model.Ingredient;
import SmartGroceryApp.src.model.Recipe;

/*
  Basic out line for a list focused workflow:
  1. User builds a shopping list manually.
  2. User can add ingredients from recipes to that list.
  3. User can check the final list against their pantry stock.
*/
public class App {
    static final String DATA_DIR = "data"; // Directory to store data
    static final String RECIPES_FILE = DATA_DIR + "/recipes.txt"; // File to store recipes
    static final String PANTRY_FILE = DATA_DIR + "/pantry.txt"; // File to store pantry items
    static final String SHOPPING_FILE = DATA_DIR + "/shopping.txt"; // File to store shopping list

    List<Recipe> recipes; //Recipes available
    List<GroceryItem> pantry; // Represents items you ALREADY HAVE at home.
    List<GroceryItem> shoppingList; // The list of items you plan TO BUY.

    public App() {
        new java.io.File(DATA_DIR).mkdirs(); // Create data directory if it doesn't exist
        recipes = JsonStorage.loadRecipes(RECIPES_FILE); // Load recipes
        pantry = JsonStorage.loadPantry(PANTRY_FILE); // Load pantry
        shoppingList = JsonStorage.loadPantry(SHOPPING_FILE); // Load shopping list
    }

    public void run() {
        System.out.println("\nWelcome to your Smart Grocery List!"); // Welcome message
        while (true) {
            // Display main menu
            //users can input 1 - 9 to control the menu options
            System.out.println("\n--- Main Menu ---");
            System.out.println("--Shopping List (" + shoppingList.size() + " items)--");
            System.out.println("1) View/Edit Shopping List");
            System.out.println("2) Add items to Shopping List manually");
            System.out.println("3) Add ingredients from recipes to Shopping List");
            System.out.println("\n--Pantry (Your stock at home)--");
            System.out.println("4) View Pantry");
            System.out.println("5) Add items to Pantry");
            System.out.println("\n--Recipes--");
            System.out.println("6) View Recipes");
            System.out.println("7) Add a new Recipe");
            System.out.println("\n--Finalize--");
            System.out.println("8) Generate Final Shopping List (check against Pantry)");
            System.out.println("9) Save & Exit");

            int c = InputUtil.readInt("\n> Your choice: ", -1); // Get user input
            switch (c) {
                // Handle menu choices
                case 1: viewOrEditShoppingList(); break;
                case 2: addManualItemsToList(); break;
                case 3: addFromRecipesToList(); break;
                case 4: viewPantry(); break;
                case 5: addPantryItem(); break;
                case 6: viewRecipes(); break;
                case 7: addRecipe(); break;
                case 8: finalizeShoppingList(); break;
                case 9:
                    // Save data before exiting (into Json files)
                    JsonStorage.saveRecipes(recipes, RECIPES_FILE);
                    JsonStorage.savePantry(pantry, PANTRY_FILE);
                    JsonStorage.savePantry(shoppingList, SHOPPING_FILE); 
                    System.out.println("All data saved. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please enter a number from 1 to 9."); // Handle invalid input
            }
        }
    }

    // --- Shopping List Methods ---

    /**
     * Displays the current shopping list and allows the user to remove items or update quantities.
     * The user is presented with a list of items in their shopping list, along with options to remove items or update quantities.
     * The user can enter 'r <number>' to remove the item at the specified number, 'q <number> <new_qty>' to update the quantity 
     * of the item at the specified number, or 'd' or 'done' to exit the loop.
     * The method will loop until the user chooses to exit.
     */
    void viewOrEditShoppingList() {
        // Check if the shopping list is empty
        if (shoppingList.isEmpty()) {
            System.out.println("\nYour shopping list is currently empty.");
            return;
        }

        while (true) {
            // Display the current shopping list
            System.out.println("\n--- Current Shopping List ---");
            for (int i = 0; i < shoppingList.size(); i++) {
                System.out.println((i + 1) + ") " + shoppingList.get(i).toString());
            }

            // Prompt the user for an action
            // r with a number to remove an item, q with a number and a quantity to update the quantity, or d or done to exit
            System.out.println("\nOptions: (r)emove <num>, (q)uantity <num> <new_qty>, or (d)one");
            String cmd = InputUtil.readLine("> ");
            // Exit the loop if the user enters 'd' or 'done'
            if (cmd.trim().equalsIgnoreCase("d") || cmd.trim().equalsIgnoreCase("done")) break;

            // Handle remove updates
            if (cmd.startsWith("r ")) {
                try {
                    int n = Integer.parseInt(cmd.substring(2).trim()) - 1; // Remove the item at the specified number
                    if (n >= 0 && n < shoppingList.size()) { // Check if the number is valid
                        System.out.println("Removed: " + shoppingList.remove(n).name); 
                    } else {
                        System.out.println("Invalid number.");
                    }
                } catch (Exception e) {
                    System.out.println("Invalid command format. Use 'r <number>'.");
                }

            // Handle quantity updates
            } else if (cmd.startsWith("q ")) { // Update the quantity of the item at the specified number
                String[] parts = cmd.substring(2).trim().split(" ");
                if (parts.length >= 2) {
                    try {
                        int n = Integer.parseInt(parts[0]) - 1;
                        double newQty = parseQuantity(parts[1]); 
                        if (n >= 0 && n < shoppingList.size()) { 
                            shoppingList.get(n).qty = newQty;
                            System.out.println("Updated quantity for " + shoppingList.get(n).name); 
                        } else {
                            System.out.println("Invalid number.");
                        }
                    } catch (Exception e) {
                        System.out.println("Invalid command format. Use 'q <number> <quantity>'.");
                    }
                }
            } else {
                System.out.println("Unknown command.");
            }
        }
    }

    // Add items to the shopping list
    void addManualItemsToList() {
        System.out.println("Add items to your shopping list. Leave name blank or type 'done' to finish.");
        while (true) {
            String name = InputUtil.readLine("Item name: ").trim(); // Get the item name
            if (name.isEmpty() || name.equalsIgnoreCase("done")) break; // Exit the loop if the name is empty or 'done'

            double qty = parseQuantityWithDefault(InputUtil.readLine("Quantity [1]: "), 1.0); // Get the quantity
            String unit = InputUtil.readLine("Unit [each]: ").trim(); // Get the unit
            if (unit.isEmpty()) unit = "each"; // Default to 'each' if the unit is empty

            // Merge with existing items in the shopping list
            mergeItemIntoList(shoppingList, new GroceryItem(name, qty, unit));
            JsonStorage.savePantry(shoppingList, SHOPPING_FILE); // auto save changes

            System.out.println("Added/updated: " + name);
        }
    }

    // Add ingredients from recipes to the shopping list
    void addFromRecipesToList() {
        if (recipes.isEmpty()) {
            System.out.println("No recipes available to add from.");
            return;
        }
        System.out.println("Select recipes to add to your list (e.g., 1,3):"); // Prompt the user to select recipes
        for (int i = 0; i < recipes.size(); i++) {
            System.out.println((i + 1) + ") " + recipes.get(i).name);
        }
        String sel = InputUtil.readLine("Your selection: "); // Get the user's selection
        String[] parts = sel.split(",");
        int itemsAdded = 0;
        for (String s : parts) {
            try {
                int idx = Integer.parseInt(s.trim()) - 1; // Get the index of the recipe
                if (idx >= 0 && idx < recipes.size()) { // Check if the index is valid
                    // Add ingredients from the selected recipe to the shopping list
                    Recipe r = recipes.get(idx);
                    System.out.println("Adding ingredients from '" + r.name + "'..."); // Print the recipe name
                    for (Ingredient ing : r.ingredients) { // Loop through the ingredients
                        mergeItemIntoList(shoppingList, new GroceryItem(ing.name, ing.qty, ing.unit)); // Add the ingredient
                        itemsAdded++;
                    }
                }
            } catch (Exception ignored) {}
        }
        if (itemsAdded > 0) { // Print the number of items added
            System.out.println("Added " + itemsAdded + " ingredient(s) to your shopping list."); // Print the number of items added
        } else {
            System.out.println("No ingredients were added."); // Print a message if no ingredients were added
        }
    }

    // Finalize the shopping list
    void finalizeShoppingList() {
        if (shoppingList.isEmpty()) {
            System.out.println("Your shopping list is empty. Nothing to finalize.");
            return;
        }

        System.out.println("\n =================================================");
        System.out.println("   Final Shopping List (Checked Against Pantry)"); // Print the final shopping list
        System.out.println(" =================================================");
        System.out.printf("%-20s | %10s | %10s | %10s%n", "Item", "Need", "In Pantry", "TO BUY"); // Print the header
        System.out.println("----------------------------------------------------------------");

        int itemsToBuyCount = 0; // Count the number of items to buy
        for (GroceryItem needed : shoppingList) { // Loop through the shopping list
            double inPantryQty = 0; // The quantity of the item in the pantry
            for (GroceryItem p : pantry) { // Loop through the pantry
                if (p.key().equals(needed.key())) { // Check if the item is in the pantry
                    inPantryQty = p.qty; // Set the quantity
                    break; // Exit the loop
                }
            }

            double toBuyQty = Math.max(0, needed.qty - inPantryQty); // Calculate the quantity to buy

            System.out.printf("%-20s | %9.2f | %9.2f | %9.2f %s%n", // Print the item for the final shopping list
                needed.name, needed.qty, inPantryQty, toBuyQty, needed.unit);


            if (toBuyQty > 0) { // If the quantity to buy is greater than 0
                itemsToBuyCount++; // Increment the items to buy count
            }
        }
        System.out.println("----------------------------------------------------------------"); // Print the footer of the final shopping list
        System.out.println("You need to buy " + itemsToBuyCount + " item(s)."); // Print the number of items to buy
    }

    // --- Pantry and Shopping List Methods ---

    // View the pantry stock
    void viewPantry() {
        System.out.println("\n--- Your Pantry Stock ---"); // Print the header for the pantry stock
        if (pantry.isEmpty()) { // If the pantry is empty it will print a message
            System.out.println("Pantry is empty. Add items you have at home.");
            return;
        }
        for (int i = 0; i < pantry.size(); i++) { // Loop through the pantry and print the items
            System.out.println((i + 1) + ") " + pantry.get(i).toString());
        }
    }

    // Add items to the pantry
    void addPantryItem() {
        System.out.println("Add items you have at home to your pantry. Leave name blank or 'done' to finish."); // Print the instructions for adding items to the pantry
        while (true) {
            String name = InputUtil.readLine("Item name: ").trim(); // Get the item name if it its true
            if (name.isEmpty() || name.equalsIgnoreCase("done")) break; // Exit the loop if the name is empty or 'done'

            double qty = parseQuantityWithDefault(InputUtil.readLine("Quantity [1]: "), 1.0); // Get the quantity of the item if it its true
            String unit = InputUtil.readLine("Unit [each]: ").trim(); // Get the unit of the item if it its true
            if (unit.isEmpty()) unit = "each"; // Default to 'each' if the unit is empty

            // Merge with existing items in pantry
            mergeItemIntoList(pantry, new GroceryItem(name, qty, unit));
            System.out.println("Pantry updated for: " + name);
        }
    }

    // View recipes
    void viewRecipes() { 
         if (recipes.isEmpty()) { // If the recipes is empty it will print a message
        System.out.println("No recipes available.");
        return;
    }

    System.out.println("\n--- Recipes ---"); // Print the header for the recipes   
    for (int i = 0; i < recipes.size(); i++) { // Loop through the recipes and print the recipes
        System.out.println((i + 1) + ") " + recipes.get(i)); 
    }
}

    // Add a recipe
    void addRecipe() { // Add a recipe to the list of recipes
        String name = InputUtil.readLine("Recipe name: ").trim(); // Get the recipe name if it its true
    if (name.isEmpty()) { // If the recipe name is empty it will print a message
        System.out.println("Recipe name cannot be empty.");
        return;
    }

    Recipe recipe = new Recipe(name);

    // Add ingredients to the recipe
    System.out.println("Add ingredients (type 'done' to finish):");
    while (true) { // If it its true, continue the loop untill the user types 'done' or enter an empty string 
        String ingName = InputUtil.readLine("Ingredient name: ").trim();
        if (ingName.equalsIgnoreCase("done") || ingName.isEmpty()) break;

        double qty = parseQuantityWithDefault( // Get the quantity of the ingredient if it its true
            InputUtil.readLine("Quantity [1]: "), 1.0
        );

        String unit = InputUtil.readLine("Unit [each]: ").trim(); // Get the unit of the ingredient if it its true
        if (unit.isEmpty()) unit = "each";

        recipe.addIngredient(new Ingredient(ingName, qty, unit)); // Add the ingredient to the recipe
    }

    recipes.add(recipe); // Add the recipe to the list of recipes
    System.out.println("Recipe added: " + recipe.name);
}

    // --- Utility Methods ---

    /**
     * Merges a GroceryItem into a list. If an item with the same key exists,
     * its quantity is increased. Otherwise, the new item is added.
     */
    private void mergeItemIntoList(List<GroceryItem> list, GroceryItem itemToAdd) { // Merge the item into the list of items 
        for (GroceryItem existingItem : list) { // Loop through the list of items and check if an item with the same key exists
            if (existingItem.key().equals(itemToAdd.key())) { // If the keys are the same, update the quantity
                existingItem.qty += itemToAdd.qty; // Add to quantity
                return;
            }
        }
        list.add(itemToAdd); // If no item with the same key exists, add the new item
    }

    // Parse quantity from string, handling fractions
    private double parseQuantity(String qtyStr) throws NumberFormatException { // Parse the quantity from the string
        if (qtyStr.contains("/")) { // If the string contains a fraction, split it and return the result
            String[] fr = qtyStr.split("/");
            return Double.parseDouble(fr[0].trim()) / Double.parseDouble(fr[1].trim());
        } else {
            return Double.parseDouble(qtyStr); // Otherwise, return the parsed double
        }
    }
    
    private double parseQuantityWithDefault(String qtyStr, double defaultValue) { // Parse the quantity from the string with a default value
        if (qtyStr.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return parseQuantity(qtyStr);
        } catch (Exception e) {
            System.out.println("Invalid quantity format. Using default."); // Print an error message and return the default value
            return defaultValue;
        }
    }

    public static void main(String[] args) { // Main method to run the application
        new App().run();
    }

}

    // --- Storage Classes ---
    // Storage classes for the pantry and recipes
    
      class JsonStorage { 
        static String esc(String s) {
            if (s == null) return "";
            return s.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n");
        }

        static String unesc(String s) {
            if (s == null) return "";
            return s.replace("\\t", "\t").replace("\\n", "\n").replace("\\\\", "\\");
        }

        // Load recipes from a file into a list of recipes
        static List<Recipe> loadRecipes(String path) { 
            List<Recipe> out = new ArrayList<>();
            Path p = Paths.get(path);
            if (!Files.exists(p)) return out;
            try (BufferedReader r = Files.newBufferedReader(p)) { // Read the file line by line
                String line;
                while ((line = r.readLine()) != null) { // Loop through the line
                    if (line.trim().isEmpty()) continue; // Ignore empty lines
                    // format: name \t ing1|qty|unit ;; ing2...
                    String[] parts = line.split("\t", 2); // Split the line into name and ingredients
                    String name = unesc(parts[0]);
                    Recipe rec = new Recipe(name);
                    if (parts.length > 1) {
                        String ings = parts[1];
                        if (!ings.isEmpty()) {
                            String[] items = ings.split(";;"); // Split the ingredients into individual items
                            for (String it : items) { // Loop through the items and add them to the recipe
                                String[] p2 = it.split("\\|", 3); // Split the item into name, quantity, and unit
                                if (p2.length==3) { // If the item has a valid format name|qty|unit , add it to the recipe
                                    try {
                                        rec.addIngredient(new Ingredient(unesc(p2[0]), Double.parseDouble(p2[1]), unesc(p2[2]))); 
                                    } catch (Exception ignored) {}
                                }
                            }
                        }
                    }
                    out.add(rec);
                }
            } catch (IOException e) {} // Ignore any IO errors and return what we have
            return out;
        }

        /**
         * Save the given list of recipes to the given file path.
         * Each recipe is written as a single line with the format:
         * name \t ing1|qty|unit
         */
        static void saveRecipes(List<Recipe> recipes, String path) { // Save the recipes to a file
            Path p = Paths.get(path); // Get the file path
            try (BufferedWriter w = Files.newBufferedWriter(p)) { // Write the recipes to the file
                for (Recipe r : recipes) { // Loop through the recipes and write them to the file
                    StringBuilder sb = new StringBuilder();
                    sb.append(esc(r.name)).append("\t");
                    String ings = r.ingredients.stream()
                        .map(i -> esc(i.name) + "|" + i.qty + "|" + esc(i.unit))
                        .collect(Collectors.joining(";;"));
                    sb.append(ings);
                    w.write(sb.toString());
                    w.newLine();
                }
            } catch (IOException e) { System.err.println("Failed saving recipes: " + e.getMessage()); } // Ignore any IO errors
        }

        static List<GroceryItem> loadPantry(String path) { // Load the pantry from a file into a list of grocery items
            List<GroceryItem> out = new ArrayList<>();
            Path p = Paths.get(path);
            if (!Files.exists(p)) return out;
            try (BufferedReader r = Files.newBufferedReader(p)) {
                String line;
                while ((line = r.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split("\t");
                    if (parts.length >= 3) {
                        try {
                            out.add(new GroceryItem(unesc(parts[0]), Double.parseDouble(parts[1]), unesc(parts[2]))); // Add the grocery item to the list
                        } catch (Exception ignored) {}
                    }
                }
            } catch (IOException e) {}
            return out;
        }

        static void savePantry(List<GroceryItem> pantry, String path) { // Save the pantry to a file
            Path p = Paths.get(path); 
            try (BufferedWriter w = Files.newBufferedWriter(p)) { // Write the pantry to the file
                for (GroceryItem g : pantry) {
                    w.write(esc(g.name) + "\t" + g.qty + "\t" + esc(g.unit));
                    w.newLine();
                }
            } catch (IOException e) { System.err.println("Failed saving pantry: " + e.getMessage()); } // Ignore any IO errors
        }
    }

    // --- Input Utility Class---
    // Utility class for reading user input
    class InputUtil {
        static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        static String readLine(String prompt) { 
            try {
                System.out.print(prompt); 
                String s = br.readLine(); 
                return s == null ? "" : s;
            } catch (IOException e) { return ""; } // Ignore any IO errors
        }
        static int readInt(String prompt, int def) { // Read an integer from the user
            String s = readLine(prompt); // Read the input
            try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; } // Return the default value if the input is invalid
        }
    }
