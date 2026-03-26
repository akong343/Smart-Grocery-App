package SmartGroceryApp.src.model;

public class Ingredient {
    public String name;
    public double qty;
    public String unit;

    public Ingredient() {}
    public Ingredient(String name, double qty, String unit) {
        this.name = name.toLowerCase().trim();
        this.qty = qty;
        this.unit = unit == null ? "" : unit.trim();
    }

    public String key() {
        return name + (unit.isEmpty() ? "" : ("|"+unit));
    }

    @Override
    public String toString() {
        return qty + " " + unit + " " + name;
    }
}
