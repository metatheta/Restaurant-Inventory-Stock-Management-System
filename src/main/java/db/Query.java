package db;

/*
    This is where we will store all out SQL statements
    as strings

    Note:
    - Each SQL statement should have its own method
 */
public class Query {
    /*
        ###############
        # section 3.0 #
        ###############
    */
    public Query() {
    }

    public static String stockItemAndSuppliers() {
        return "SELECT si.item_name, si.unit_of_measure, si.category, s.name, s.contact_person, s.contact_info\n" +
                "FROM stock_items si\n" +
                "\t\tJOIN supplier_products sp \n" +
                "\t\t\tON si.item_id = sp.item_id\n" +
                "\t\tJOIN suppliers s \n" +
                "\t\t\tON sp.supplier_id = s.supplier_id\n" +
                "WHERE si.item_id = ? AND si.visible = 1 AND sp.visible = 1 AND s.visible = 1\n" +
                "ORDER BY si.item_id, s.supplier_id;";
    }

    public static String storedItemAndLocations() {
        return "SELECT s.item_name, running_balance, last_restock_date, expiry_date, storage_name, address, " +
                "storage_type\n" +
                "FROM inventory i\n" +
                "JOIN stock_locations l ON i.location_id = l.location_id\n" +
                "JOIN stock_items s ON i.item_id = s.item_id\n" +
                "WHERE i.inventory_id = ? AND i.visible = 1 AND l.visible = 1\n" +
                "order by i.inventory_id;";
    }

    public static String locationAndStoredItems() {
        return "SELECT sl.storage_name, sl.storage_type, sl.address, si.item_name, si.unit_of_measure, " +
                "i.running_balance, " +
                "si.category\n" +
                "FROM stock_locations sl\n" +
                "\tJOIN inventory i\n" +
                "\t\tON sl.location_id = i.location_id\n" +
                "\tJOIN stock_items si\n" +
                "\t\tON i.item_id = si.item_id\n" +
                "WHERE sl.location_id = ? AND sl.visible = 1 AND i.visible = 1 AND si.visible = 1\n" +
                "ORDER BY sl.location_id, si.item_id;";
    }

    public static String supplierAndProducts() {
        return """
                SELECT
                    s.name,
                    s.contact_person,
                    s.contact_info,
                    si.item_name,
                    sp.amount,
                    sp.unit_cost
                FROM suppliers s
                JOIN supplier_products sp
                       ON s.supplier_id = sp.supplier_id
                JOIN stock_items si
                       ON sp.item_id = si.item_id
                WHERE s.visible = 1
                  AND sp.visible = 1
                  AND si.visible = 1
                  AND s.supplier_id = ?
                 ORDER BY s.supplier_id, si.item_id
                """;
    }

    /*
        ###############
        # section 4.0 #
        ###############
    */

    public static String selectItemsToRestock() {
        return "SELECT inventory_id, item_id, item_name, storage_name, address, running_balance " +
                "FROM inventory " +
                "LEFT JOIN stock_items USING (item_id) " +
                "LEFT JOIN stock_locations USING (location_id) " +
                "WHERE inventory.visible = 1";
    }

    // params: supplier_id, item_id
    public static String r2_checkIfSupplierProductComboExists() {
        return "SELECT EXISTS (\n" +
                "    SELECT 1 FROM supplier_products\n" +
                "    WHERE supplier_id = ?\n" + // supplier id
                "    AND item_id = ?\n" + // item id
                "    AND visible = 1\n" +
                ");";
    }

    // params: supplier_id, item_id
    public static String r2_checkIfInvisibleRecordExists() {
        return "SELECT EXISTS (\n" +
                "    SELECT 1 FROM supplier_products\n" +
                "    WHERE supplier_id = ?\n" + // supplier id
                "    AND item_id = ?\n" + // item id
                "    AND visible = 0\n" +
                ");";
    }

    // params: amount, unit_cost, supplier_id, item_id
    public static String r2_updateToDoIfInvisibleRecordExists() {
        return "UPDATE supplier_products\n" +
                "SET amount = ?, unit_cost = ?, visible = 1\n" +
                "WHERE supplier_id = ? AND item_id = ?;";
    }

    // params: supplier_id, item_id, amount, unit_cost
    public static String r2_updateToDoIfInvisibleRecordDoesNotExist() {
        return "INSERT INTO supplier_products (supplier_id, item_id, amount, unit_cost)\n" +
                "VALUES (?, ?, ?, ?);";
    }

    // params: supplier_id, item_id, amount, unit_cost
    public static String r2_recordAdditionInTransactionTable() {
        return "INSERT INTO product_registry_history (supplier_id, item_id, updated_amount, updated_cost, category)\n" +
                "VALUES (?, ?, ?, ?, 'addition');";
    }

    // params: amount, unit_cost, supplier_id, item_id
    public static String r2_updateToDoIfUserSelectsChange() {
        return "UPDATE supplier_products\n" +
                "SET amount = ?, unit_cost = ?\n" +
                "WHERE supplier_id = ? AND item_id = ?;";
    }

    // params: supplier_id, item_id, amount, unit_cost
    public static String r2_recordChangeInTransactionTable() {
        return "INSERT INTO product_registry_history (supplier_id, item_id, updated_amount, updated_cost, category)\n" +
                "VALUES (?, ?, ?, ?, 'change');";
    }

    // params: supplier_id, item_id
    public static String r2_updateToDoIfUserSelectsDelete() {
        return "UPDATE supplier_products\n" +
                "SET visible = 0\n" +
                "WHERE supplier_id = ? AND item_id = ?;";
    }

    // params: supplier_id, item_id
    public static String r2_recordDeletionInTransactionTable() {
        return "INSERT INTO product_registry_history (supplier_id, item_id, category)\n" +
                "VALUES (?, ?, 'deletion');";
    }

    // params: none
    public static String r2_showReadOnlyTable() {
        return "SELECT\n" +
                "    s.name AS supplier_name,\n" +
                "    si.item_name,\n" +
                "    IFNULL(prh.updated_amount, 'N/A') AS updated_amount,\n" +
                "    IFNULL(prh.updated_cost, 'N/A') AS updated_cost,\n" +
                "    prh.category,\n" +
                "    prh.changed_at\n" +
                "FROM product_registry_history prh\n" +
                "INNER JOIN suppliers s ON prh.supplier_id = s.supplier_id\n" +
                "INNER JOIN stock_items si ON prh.item_id = si.item_id\n" +
                "ORDER BY prh.changed_at DESC;"; // prioritize showing newest?
    }

    public static String buyNewStockItem(String name, String unitOfMeasure, String category) {
        return "";
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
        return "SELECT * from item_restocks";
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

    public static String getLocationAddress() {
        return "SELECT address FROM stock_locations WHERE location_id = ?";
    }

    public static String getStockAtAddress() {
        return """
                SELECT SUM(i.running_balance) as total_stock
                FROM inventory i
                JOIN stock_locations sl ON i.location_id = sl.location_id
                WHERE i.item_id = ?
                  AND sl.address = ?
                  AND i.visible = 1
                  AND i.running_balance > 0
                """;
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

    /*
        ###############
        # section 5.0 #
        ###############
    */

    public static String preferredSuppliersReport() {
        return "";
    }

    public static String storageDistributionReport(int year, int month) {
        return "SELECT\n" +
                "    si.item_name,\n" +
                "    sl.storage_name,\n" +
                "    sl.address,\n" +
                "    SUM(t.restocked) AS total_restocked,\n" +
                "    SUM(t.consumed_or_disposed) AS total_consumed_or_disposed\n" +
                "FROM (\n" +
                "    -- 1. DISH CONSUMPTION\n" +
                "    SELECT\n" +
                "        dr.item_id,\n" +
                "        dc.location_id,\n" +
                "        0 AS restocked,\n" +
                "        SUM(dr.quantity * dc.servings) AS consumed_or_disposed\n" +
                "    FROM dish_consumption dc\n" +
                "    INNER JOIN dish_requirements dr ON dc.dish_id = dr.dish_id\n" +
                "    WHERE YEAR(dc.consumed_at) = " + year + " AND MONTH(dc.consumed_at) = " + month + "\n" +
                "    GROUP BY dr.item_id, dc.location_id\n" +
                "    UNION ALL\n" +
                "    -- 2. DISPOSED ITEMS\n" +
                "    SELECT\n" +
                "        i.item_id,\n" +
                "        i.location_id,\n" +
                "        0 AS restocked,\n" +
                "        SUM(di.quantity_disposed) AS consumed_or_disposed\n" +
                "    FROM disposed_items di\n" +
                "    INNER JOIN inventory i ON di.inventory_id = i.inventory_id\n" +
                "    WHERE YEAR(di.disposed_date) = " + year + " AND MONTH(di.disposed_date) = " + month + "\n" +
                "    GROUP BY i.item_id, i.location_id\n" +
                "    UNION ALL\n" +
                "    -- 3. ITEM RESTOCKS (FIXED: Joined with Inventory to get Location)\n" +
                "    SELECT\n" +
                "        i.item_id,\n" +
                "        i.location_id,\n" +
                "        SUM(ir.quantity) AS restocked,\n" +
                "        0 AS consumed_or_disposed\n" +
                "    FROM item_restocks ir\n" +
                "    INNER JOIN inventory i ON ir.inventory_id = i.inventory_id\n" +
                "    WHERE YEAR(ir.restocked_at) = " + year + " AND MONTH(ir.restocked_at) = " + month + "\n" +
                "    GROUP BY i.item_id, i.location_id\n" +
                ") t\n" +
                "INNER JOIN stock_items si ON t.item_id = si.item_id\n" +
                "INNER JOIN stock_locations sl ON t.location_id = sl.location_id\n" +
                "GROUP BY t.item_id, t.location_id\n" +
                "ORDER BY total_restocked DESC;";
    }

    public static String seasonalStockReport() {
        return "";
    }

    public static String expiryReport() {
        return """
                SELECT
                    si.item_id,
                    si.item_name,
                    COALESCE(p.total_purchased, 0) AS total_purchased,
                    COALESCE(d.total_disposed, 0) AS total_disposed,
                    CASE
                        WHEN COALESCE(p.total_purchased, 0) = 0 THEN NULL
                        ELSE (COALESCE(d.total_disposed, 0) / p.total_purchased) * 100
                    END AS waste_percentage
                FROM stock_items si
                LEFT JOIN (
                    SELECT
                        pl.item_id,
                        SUM(pl.quantity) AS total_purchased
                    FROM purchase_line pl
                    JOIN purchases pu
                      ON pl.purchase_id = pu.purchase_id
                    WHERE pu.order_year  = ?
                      AND pu.order_month = ?
                      AND pu.visible     = 1
                      AND pl.visible     = 1
                    GROUP BY pl.item_id
                ) p
                  ON si.item_id = p.item_id
                LEFT JOIN (
                    SELECT
                        sm.item_id,
                        SUM(sm.quantity) AS total_disposed
                    FROM stock_movement sm
                    WHERE sm.transaction_type = 'DISPOSAL'
                      AND YEAR(sm.moved_at)  = ?
                      AND MONTH(sm.moved_at) = ?
                      AND sm.visible         = 1
                    GROUP BY sm.item_id
                ) d
                  ON si.item_id = d.item_id
                WHERE si.visible = 1
                ORDER BY waste_percentage DESC IS NULL, waste_percentage DESC
                """;
    }
}
