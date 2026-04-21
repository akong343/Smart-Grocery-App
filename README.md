# Smart Grocery App

## Project Description
The Smart Grocery List App is a Java-based console application designed to help users efficiently manage their grocery shopping. It allows users to build a shopping list manually or automatically from recipes, while also keeping track of items already available in their pantry.

The app compares the shopping list against the pantry stock to determine exactly what you need to buy. It helps to reduce waste, save money, and make meal planning easier.

## Features
### Shoping List Mangement
- Create and edit a shopping list
- Add items manually with quantity and unit
- Update item quantities or remove items
- Automatically merge duplicate items (same name + unit)

### Recipe Integration
- Create and store custom recipes
- Add ingredients from selected recipes directly to your shopping list
- Automatically aggregates ingredient quantities across multiple recipes

### Pantry Tracking
- Maintain a list of items you already have at home
- Add or update pantry items with quantities and units

### Smart Finalization
- Compare shopping list with pantry inventory
- Calculate how much of each item you still need to buy
- Display a clear breakdown:
- Needed amount
- Available in pantry
- Final amount to purchase

### Data Persistence
- Recipes and pantry data are saved locally in text files
- Data is loaded automatically when the app starts

## How to run
### 1. Compile the Project
Navigate to the root directory and run:

```bash
javac SmartGroceryApp/**/*.java
```

### Run the Application
```bash
  java SmartGroceryApp.App
```
### 3. Use the Menu
Follow the on-screen menu to:
- Build your shopping list
- Manage pantry items
- Add recipes
- Generate your final shopping list
