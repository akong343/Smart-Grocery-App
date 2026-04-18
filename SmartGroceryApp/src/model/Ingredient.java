package SmartGroceryApp.src.model;

// Represents a single grocery item with a normalized name, quantity, and unit.
// Name is stored lowercase and trimmed to make comparisons and lookups consistent. 

public class Ingredient {
    // The ingredient name in lowercase and trimmed of extra spaces.

    public String name; // The numeric quantity for this ingredient (e.g., 1.5).
    public double qty; // The unit for the quantity (e.g., "g", "cups"). Empty string if none.
    public String unit;

    public Ingredient() {}
    // Create an ingredient; normalizes name and ensures unit is non-null.
    public Ingredient(String name, double qty, String unit) {
        this.name = name.toLowerCase().trim(); // lowercase and trimmed
        this.qty = qty; // Ensure quantity is non-negative
        this.unit = unit == null ? "" : unit.trim(); // Ensure unit is non-null and trimmed
    }

    public String key() {
        // Unique key used for grouping/lookup: "name|unit" if unit present, otherwise just "name".
        return name + (unit.isEmpty() ? "" : ("|"+unit)); // Empty string if unit is empty
    }

    // Returns a string representation of the item in the format: "[quantity] [unit] [name]"
    @Override
    public String toString() {
        return qty + " " + unit + " " + name;
    }
}
