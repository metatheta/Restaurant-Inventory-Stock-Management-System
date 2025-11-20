package db;

public class TransactionQueries {
    public static String selectItemsToRestock() {
        return "SELECT inventory_id, item_id, item_name, storage_name, address, running_balance " +
                "FROM inventory " +
                "LEFT JOIN stock_items USING (item_id) " +
                "LEFT JOIN stock_locations USING (location_id) " +
                "WHERE inventory.visible = 1";
    }// params: supplier_id, item_id

    public static String r2_checkIfSupplierProductComboExists() {
        return "SELECT EXISTS (\n" +
                "    SELECT 1 FROM supplier_products\n" +
                "    WHERE supplier_id = ?\n" +
                "    AND item_id = ?\n" +
                "    AND visible = 1\n" +
                ");";
    }// params: supplier_id, item_id

    public static String r2_checkIfInvisibleRecordExists() {
        return "SELECT EXISTS (\n" +
                "    SELECT 1 FROM supplier_products\n" +
                "    WHERE supplier_id = ?\n" +
                "    AND item_id = ?\n" +
                "    AND visible = 0\n" +
                ");";
    }// params: unit_cost, supplier_id, item_id

    public static String r2_updateToDoIfInvisibleRecordExists() {
        return "UPDATE supplier_products\n" +
                "SET unit_cost = ?, visible = 1\n" +
                "WHERE supplier_id = ? AND item_id = ?;";
    }// params: supplier_id, item_id, unit_cost

    public static String r2_updateToDoIfInvisibleRecordDoesNotExist() {
        return "INSERT INTO supplier_products (supplier_id, item_id, unit_cost)\n" +
                "VALUES (?, ?, ?);";
    }// params: supplier_id, item_id, unit_cost

    public static String r2_recordAdditionInTransactionTable() {
        return "INSERT INTO product_registry_history (supplier_id, item_id, updated_cost, category)\n" +
                "VALUES (?, ?, ?, 'addition');";
    }// params: unit_cost, supplier_id, item_id

    public static String r2_updateToDoIfUserSelectsChange() {
        return "UPDATE supplier_products\n" +
                "SET unit_cost = ?\n" +
                "WHERE supplier_id = ? AND item_id = ?;";
    }// params: supplier_id, item_id, unit_cost

    public static String r2_recordChangeInTransactionTable() {
        return "INSERT INTO product_registry_history (supplier_id, item_id, updated_cost, category)\n" +
                "VALUES (?, ?, ?, 'change');";
    }// params: none

    public static String r2_showReadOnlyTable() {
        return "SELECT\n" +
                "    s.name AS supplier_name,\n" +
                "    si.item_name,\n" +
                "    IFNULL(prh.updated_cost, 'N/A') AS updated_cost,\n" +
                "    prh.category,\n" +
                "    prh.changed_at\n" +
                "FROM product_registry_history prh\n" +
                "INNER JOIN suppliers s ON prh.supplier_id = s.supplier_id\n" +
                "INNER JOIN stock_items si ON prh.item_id = si.item_id\n" +
                "ORDER BY prh.changed_at DESC;";
    }

    public static String disposingExpiredItems() {
        return "INSERT INTO disposed_items(quantity_disposed, inventory_id)\n" +
                "SELECT running_balance, inventory_id\n" +
                "FROM inventory\n" +
                "WHERE expiry_date < CURRENT_TIMESTAMP() AND running_balance  > 0;";
    }

    public static String setExpiredToZero() {
        return "UPDATE inventory " +
                "SET running_balance = 0 " +
                "WHERE expiry_date < CURRENT_TIMESTAMP() AND running_balance > 0;";
    }

    public static String getRestockRecords() {
        return "SELECT\n" +
                "    inventory_id,\n" +
                "    item_name,\n" +
                "    supplier_name,\n" +
                "    cost_per_unit,\n" +
                "    quantity,\n" +
                "    total_cost,\n" +
                "    storage_location,\n" +
                "    address,\n" +
                "    restocked_at\n" +
                "FROM\n" +
                "    item_restocks\n" +
                "WHERE\n" +
                "    visible = 1;";
    }

    public static String recordingDisposedItemsInStockMovement() {
        return "INSERT INTO stock_movement(quantity, transaction_type, item_id, inventory_id, location_id)\n" +
                "SELECT d.quantity_disposed, 'DISPOSAL', i.item_id, i.inventory_id, i.location_id\n" +
                "FROM disposed_items d\n" +
                "\tJOIN inventory i\n" +
                "\t\tON d.inventory_id = i.inventory_id\n" +
                "WHERE d.should_update_inventory = 1;";
    }

    public static String updateInventoryAfterDisposing() {
        return "UPDATE inventory i\n" +
                "\tJOIN disposed_items d\n" +
                "\t\tON i.inventory_id = d.inventory_id\n" +
                "SET i.running_balance = 0\n" +
                "WHERE d.should_update_inventory = 1;";
    }

    public static String displayAllDisposedItems() {
        return "SELECT si.item_name, si.category, sl.storage_name, sl.address\n" +
                "\tFROM disposed_items d\n" +
                "\t\tJOIN inventory i\n" +
                "\t\t\tON d.inventory_id = i.inventory_id\n" +
                "\t\tJOIN stock_items si\n" +
                "\t\t\tON i.item_id = si.item_id\n" +
                "\t\tJOIN stock_locations sl\n" +
                "\t\t\tON i.location_id = sl.location_id\n" +
                "\tWHERE d.visible = 1 AND i.visible = 1 AND si.visible = 1 AND sl.visible = 1;";
    }

    public static String displayRecentlyDisposedItems() {
        return "SELECT si.item_name, si.category, sl.storage_name, sl.address\n" +
                "\tFROM disposed_items d\n" +
                "\t\tJOIN inventory i\n" +
                "\t\t\tON d.inventory_id = i.inventory_id\n" +
                "\t\tJOIN stock_items si\n" +
                "\t\t\tON i.item_id = si.item_id\n" +
                "\t\tJOIN stock_locations sl\n" +
                "\t\t\tON i.location_id = sl.location_id\n" +
                "\tWHERE d.should_update_inventory = 1 AND d.visible = 1 AND i.visible = 1 AND si.visible = 1 AND sl" +
                ".visible = 1;";
    }

    public static String updateNewlyDisposedToPreviouslyDisposed() {
        return "UPDATE disposed_items\n" +
                "SET should_update_inventory = 0\n" +
                "WHERE should_update_inventory = 1;";
    }

    public static String getDishIngredients() {
        return """
                SELECT 
                    dish_name,
                    item_name,
                    quantity,
                    unit_of_measure
                FROM dishes d
                JOIN dish_requirements dr ON d.dish_id = dr.dish_id
                JOIN stock_items s ON dr.item_id = s.item_id;
                """;
    }

    public static String getAllDishes() {
        return "SELECT dish_id, dish_name FROM dishes WHERE visible = 1 ORDER BY dish_name ASC";
    }

    public static String getDishRequirements() {
        return "SELECT item_id, quantity FROM dish_requirements WHERE dish_id = ?";
    }

    public static String getAllLocations() {
        return "SELECT location_id, storage_name, address FROM stock_locations WHERE visible = 1";
    }

    public static String deductFromInventoryBatch() {
        return "UPDATE inventory SET running_balance = running_balance - ? WHERE inventory_id = ?";
    }

    public static String recordDishConsumption() {
        return "INSERT INTO dish_consumption (dish_id, servings, location_id) VALUES (?, ?, ?)";
    }

    public static String dishConsumptionHistory() {
        return """
                SELECT dc.consumption_id, dc.consumed_at, d.dish_name, dc.servings, sl.storage_name AS location_name, sl.address
                FROM dish_consumption dc
                JOIN dishes d ON dc.dish_id = d.dish_id
                JOIN stock_locations sl ON dc.location_id = sl.location_id
                WHERE dc.visible = 1
                ORDER BY dc.consumed_at DESC, dc.consumption_id DESC
                """;
    }
}