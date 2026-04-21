package SmartGroceryApp.src.model;

// Represents a single grocery item with a normalized name, quantity, and unit.
// Name is stored lowercase and trimmed to make comparisons and lookups consistent.
public class GroceryItem {
    // Item name
    public String name;
    // Quantity of the item
    public double qty;
    // Unit for quantity
    public String unit;

    public GroceryItem() {}
    // Create an item -> normalizes name and ensures unit is non-null.
    public GroceryItem(String name, double qty, String unit) {
        this.name = name.toLowerCase().trim();
        this.qty = qty;
        this.unit = unit == null ? "" : unit.trim();
    }
    // Unique key used for grouping/lookup: "name|unit" if unit present, otherwise just "name".
    public String key() { return name + (unit.isEmpty() ? "" : ("|"+unit)); }

    
     //Returns a string representation of the item in the format: "[quantity] [unit] [name]"
    @Override
    public String toString() { return qty + " " + unit + " " + name; }
}
