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

/*
  Refactored for a list-centric workflow:
  1. User builds a shopping list manually.
  2. User can add ingredients from recipes to that list.
  3. User can check the final list against their pantry stock.
*/
public class App {
    static final String DATA_DIR = "data";
    static final String RECIPES_FILE = DATA_DIR + "/recipes.txt";
    static final String PANTRY_FILE = DATA_DIR + "/pantry.txt";

    List<Recipe> recipes;
    List<GroceryItem> pantry; // Represents items you ALREADY HAVE at home.
    List<GroceryItem> shoppingList; // The list of items you plan TO BUY.

    public App() {
        new java.io.File(DATA_DIR).mkdirs();
        recipes = JsonStorage.loadRecipes(RECIPES_FILE);
        pantry = JsonStorage.loadPantry(PANTRY_FILE);
        shoppingList = new ArrayList<>(); // Start with an empty shopping list for the session.
    }

    public void run() {
        System.out.println("\nWelcome to your Smart Grocery List!");
        while (true) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("--Shopping List (" + shoppingList.size() + " items)--");
            System.out.println("1) View/Edit Shopping List");
            System.out.println("2) Add items to Shopping List manually");
            System.out.println("3) Add ingredients from recipes to Shopping List");
            System.out.println("\n--Pantry (Your stock at home)--");
            System.out.println("4) View Pantry");
            System.out.println("5) Add items to Pantry");
            System.out.println("\n--Recipes--");
            System.out.println("6) View ");
            System.out.println("6) View Recipes");
            System.out.println("7) Add a new Recipe");
            System.out.println("\n--Finalize--");
            System.out.println("8) Generate Final Shopping List (check against Pantry)");
            System.out.println("9) Save & Exit");

            int c = InputUtil.readInt("\n> Your choice: ", -1);
            switch (c) {
                case 1: viewOrEditShoppingList(); break;
                case 2: addManualItemsToList(); break;
                case 3: addFromRecipesToList(); break;
                case 4: viewPantry(); break;
                case 5: addPantryItem(); break;
                case 6: viewRecipes(); break;
                case 7: addRecipe(); break;
                case 8: finalizeShoppingList(); break;
                case 9:
                    // Note: We only save recipes and pantry, not the temporary shopping list.
                    JsonStorage.saveRecipes(recipes, RECIPES_FILE);
                    JsonStorage.savePantry(pantry, PANTRY_FILE);
                    System.out.println("Recipes and pantry saved. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please enter a number from 1 to 9.");
            }
        }
    }

    // --- 🛒 SHOPPING LIST METHODS ---

    void viewOrEditShoppingList() {
        if (shoppingList.isEmpty()) {
            System.out.println("\nYour shopping list is currently empty.");
            return;
        }

        while (true) {
            System.out.println("\n--- 🛒 Current Shopping List ---");
            for (int i = 0; i < shoppingList.size(); i++) {
                System.out.println((i + 1) + ") " + shoppingList.get(i).toString());
            }

            System.out.println("\nOptions: (r)emove <num>, (q)uantity <num> <new_qty>, or (d)one");
            String cmd = InputUtil.readLine("> ");
            if (cmd.trim().equalsIgnoreCase("d") || cmd.trim().equalsIgnoreCase("done")) break;

            if (cmd.startsWith("r ")) {
                try {
                    int n = Integer.parseInt(cmd.substring(2).trim()) - 1;
                    if (n >= 0 && n < shoppingList.size()) {
                        System.out.println("Removed: " + shoppingList.remove(n).name);
                    } else {
                        System.out.println("Invalid number.");
                    }
                } catch (Exception e) {
                    System.out.println("Invalid command format. Use 'r <number>'.");
                }
            } else if (cmd.startsWith("q ")) {
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

    void addManualItemsToList() {
        System.out.println("Add items to your shopping list. Leave name blank or type 'done' to finish.");
        while (true) {
            String name = InputUtil.readLine("Item name: ").trim();
            if (name.isEmpty() || name.equalsIgnoreCase("done")) break;

            double qty = parseQuantityWithDefault(InputUtil.readLine("Quantity [1]: "), 1.0);
            String unit = InputUtil.readLine("Unit [each]: ").trim();
            if (unit.isEmpty()) unit = "each";

            // Merge with existing items in the shopping list
            mergeItemIntoList(shoppingList, new GroceryItem(name, qty, unit));
            System.out.println("Added/updated: " + name);
        }
    }

    void addFromRecipesToList() {
        if (recipes.isEmpty()) {
            System.out.println("No recipes available to add from.");
            return;
        }
        System.out.println("Select recipes to add to your list (e.g., 1,3):");
        for (int i = 0; i < recipes.size(); i++) {
            System.out.println((i + 1) + ") " + recipes.get(i).name);
        }
        String sel = InputUtil.readLine("Your selection: ");
        String[] parts = sel.split(",");
        int itemsAdded = 0;
        for (String s : parts) {
            try {
                int idx = Integer.parseInt(s.trim()) - 1;
                if (idx >= 0 && idx < recipes.size()) {
                    Recipe r = recipes.get(idx);
                    System.out.println("Adding ingredients from '" + r.name + "'...");
                    for (Ingredient ing : r.ingredients) {
                        mergeItemIntoList(shoppingList, new GroceryItem(ing.name, ing.qty, ing.unit));
                        itemsAdded++;
                    }
                }
            } catch (Exception ignored) {}
        }
        if (itemsAdded > 0) {
            System.out.println("Added " + itemsAdded + " ingredient(s) to your shopping list.");
        } else {
            System.out.println("No ingredients were added.");
        }
    }

    void finalizeShoppingList() {
        if (shoppingList.isEmpty()) {
            System.out.println("Your shopping list is empty. Nothing to finalize.");
            return;
        }

        System.out.println("\n--- ✅ Final Shopping List (Checked Against Pantry) ---");
        System.out.printf("%-20s | %10s | %10s | %10s%n", "Item", "Need", "In Pantry", "TO BUY");
        System.out.println("----------------------------------------------------------------");

        int itemsToBuyCount = 0;
        for (GroceryItem needed : shoppingList) {
            double inPantryQty = 0;
            for (GroceryItem p : pantry) {
                if (p.key().equals(needed.key())) {
                    inPantryQty = p.qty;
                    break;
                }
            }

            double toBuyQty = Math.max(0, needed.qty - inPantryQty);

            System.out.printf("%-20s | %9.2f | %9.2f | %9.2f %s%n",
                needed.name, needed.qty, inPantryQty, toBuyQty, needed.unit);

            if (toBuyQty > 0) {
                itemsToBuyCount++;
            }
        }
        System.out.println("----------------------------------------------------------------");
        System.out.println("You need to buy " + itemsToBuyCount + " item(s).");
    }

    // --- 🏠 PANTRY & 📖 RECIPE METHODS (Largely unchanged, but used differently) ---

    void viewPantry() {
        System.out.println("\n--- 🏠 Your Pantry Stock ---");
        if (pantry.isEmpty()) {
            System.out.println("Pantry is empty. Add items you have at home.");
            return;
        }
        for (int i = 0; i < pantry.size(); i++) {
            System.out.println((i + 1) + ") " + pantry.get(i).toString());
        }
    }

    void addPantryItem() {
        System.out.println("Add items you have at home to your pantry. Leave name blank or 'done' to finish.");
        while (true) {
            String name = InputUtil.readLine("Item name: ").trim();
            if (name.isEmpty() || name.equalsIgnoreCase("done")) break;

            double qty = parseQuantityWithDefault(InputUtil.readLine("Quantity [1]: "), 1.0);
            String unit = InputUtil.readLine("Unit [each]: ").trim();
            if (unit.isEmpty()) unit = "each";

            // Merge with existing items in pantry
            mergeItemIntoList(pantry, new GroceryItem(name, qty, unit));
            System.out.println("Pantry updated for: " + name);
        }
    }

    void viewRecipes() { /* ... unchanged ... */ }
    void addRecipe() { /* ... unchanged ... */ }


    // --- UTILITY METHODS ---

    /**
     * Merges a GroceryItem into a list. If an item with the same key exists,
     * its quantity is increased. Otherwise, the new item is added.
     */
    private void mergeItemIntoList(List<GroceryItem> list, GroceryItem itemToAdd) {
        for (GroceryItem existingItem : list) {
            if (existingItem.key().equals(itemToAdd.key())) {
                existingItem.qty += itemToAdd.qty; // Add to quantity
                return;
            }
        }
        list.add(itemToAdd); // Not found, so add as a new item
    }

    private double parseQuantity(String qtyStr) throws NumberFormatException {
        if (qtyStr.contains("/")) {
            String[] fr = qtyStr.split("/");
            return Double.parseDouble(fr[0].trim()) / Double.parseDouble(fr[1].trim());
        } else {
            return Double.parseDouble(qtyStr);
        }
    }
    
    private double parseQuantityWithDefault(String qtyStr, double defaultValue) {
        if (qtyStr.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return parseQuantity(qtyStr);
        } catch (Exception e) {
            System.out.println("Invalid quantity format. Using default.");
            return defaultValue;
        }
    }

    public static void main(String[] args) {
        new App().run();
    }

}

    class Ingredient {
        String name;
        double qty;
        String unit;
        Ingredient(String name, double qty, String unit) { this.name = name; this.qty = qty; this.unit = unit; }
        String key() { return name.toLowerCase().trim() + "|" + unit.toLowerCase().trim(); }
        public String toString() { return name + ": " + qty + " " + unit; }
    }

    class Recipe{
        String name;
        List<Ingredient> ingredients = new ArrayList<>();
        Recipe() {}
        Recipe(String name) { this.name = name; }
        void addIngredient(Ingredient i) { ingredients.add(i); }
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Recipe: ").append(name).append("\n");
            for (Ingredient i : ingredients) sb.append(" - ").append(i.toString()).append("\n");
            return sb.toString();
        }
    }

    class GroceryItem{
        String name;
        double qty;
        String unit;
        GroceryItem() {}
        GroceryItem(String name, double qty, String unit) { this.name = name; this.qty = qty; this.unit = unit; }
        String key() { return name.toLowerCase().trim() + "|" + unit.toLowerCase().trim(); }
        public String toString() { return name + ": " + qty + " " + unit; }
    }

    class JsonStorage {
        // Very small, robust enough: one record per line, fields separated by tab; escape tabs/newlines
        static String esc(String s) {
            if (s == null) return "";
            return s.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n");
        }
        static String unesc(String s) {
            if (s == null) return "";
            return s.replace("\\t", "\t").replace("\\n", "\n").replace("\\\\", "\\");
        }

        static List<Recipe> loadRecipes(String path) {
            List<Recipe> out = new ArrayList<>();
            Path p = Paths.get(path);
            if (!Files.exists(p)) return out;
            try (BufferedReader r = Files.newBufferedReader(p)) {
                String line;
                while ((line = r.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    // format: name \t ing1|qty|unit ;; ing2...
                    String[] parts = line.split("\t", 2);
                    String name = unesc(parts[0]);
                    Recipe rec = new Recipe(name);
                    if (parts.length > 1) {
                        String ings = parts[1];
                        if (!ings.isEmpty()) {
                            String[] items = ings.split(";;");
                            for (String it : items) {
                                String[] p2 = it.split("\\|", 3);
                                if (p2.length==3) {
                                    try {
                                        rec.addIngredient(new Ingredient(unesc(p2[0]), Double.parseDouble(p2[1]), unesc(p2[2])));
                                    } catch (Exception ignored) {}
                                }
                            }
                        }
                    }
                    out.add(rec);
                }
            } catch (IOException e) { /* ignore, return what we have */ }
            return out;
        }

        static void saveRecipes(List<Recipe> recipes, String path) {
            Path p = Paths.get(path);
            try (BufferedWriter w = Files.newBufferedWriter(p)) {
                for (Recipe r : recipes) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(esc(r.name)).append("\t");
                    String ings = r.ingredients.stream()
                        .map(i -> esc(i.name) + "|" + i.qty + "|" + esc(i.unit))
                        .collect(Collectors.joining(";;"));
                    sb.append(ings);
                    w.write(sb.toString());
                    w.newLine();
                }
            } catch (IOException e) { System.err.println("Failed saving recipes: " + e.getMessage()); }
        }

        static List<GroceryItem> loadPantry(String path) {
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
                            out.add(new GroceryItem(unesc(parts[0]), Double.parseDouble(parts[1]), unesc(parts[2])));
                        } catch (Exception ignored) {}
                    }
                }
            } catch (IOException e) {}
            return out;
        }

        static void savePantry(List<GroceryItem> pantry, String path) {
            Path p = Paths.get(path);
            try (BufferedWriter w = Files.newBufferedWriter(p)) {
                for (GroceryItem g : pantry) {
                    w.write(esc(g.name) + "\t" + g.qty + "\t" + esc(g.unit));
                    w.newLine();
                }
            } catch (IOException e) { System.err.println("Failed saving pantry: " + e.getMessage()); }
        }
    }

    class InputUtil {
        static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        static String readLine(String prompt) {
            try {
                System.out.print(prompt);
                String s = br.readLine();
                return s == null ? "" : s;
            } catch (IOException e) { return ""; }
        }
        static int readInt(String prompt, int def) {
            String s = readLine(prompt);
            try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
        }
    }
