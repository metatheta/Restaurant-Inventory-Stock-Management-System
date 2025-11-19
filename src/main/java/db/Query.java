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
    public Query()
    {
    }

    public static String stockItemAndSuppliers()
    {
        return "SELECT si.item_name, si.unit_of_measure, si.category, s.name, s.contact_person, s.contact_info\n" +
                "FROM stock_items si\n" +
                "\t\tJOIN supplier_products sp \n" +
                "\t\t\tON si.item_id = sp.item_id\n" +
                "\t\tJOIN suppliers s \n" +
                "\t\t\tON sp.supplier_id = s.supplier_id\n" +
                "WHERE si.visible = 1 AND sp.visible = 1 AND s.visible = 1\n" +
                "ORDER BY si.item_id, s.supplier_id;";
    }

    public static String storedItemAndLocations()
    {
        return "";
    }

    public static String locationAndStoredItems()
    {
        return "SELECT sl.location_id, sl.storage_name, sl.storage_type, sl.address, si.item_id, si.item_name, si.unit_of_measure, si.category\n" +
                "FROM stock_locations sl\n" +
                "\tJOIN inventory i\n" +
                "\t\tON sl.location_id = i.location_id\n" +
                "\tJOIN stock_items si\n" +
                "\t\tON i.item_id = si.item_id\n" +
                "WHERE sl.visible = 1 AND i.visible = 1 AND si.visible = 1\n" +
                "ORDER BY sl.location_id, si.item_id;";
    }

    public static String supplierAndProducts()
    {
        return """
            SELECT
                s.supplier_id,
                s.name AS supplier_name,
                s.contact_person,
                s.contact_info,
                sp.item_id,
                si.item_name,
                sp.amount,
                sp.unit_cost
            FROM suppliers s
            LEFT JOIN supplier_products sp
                   ON s.supplier_id = sp.supplier_id
            LEFT JOIN stock_items si
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

    public static String restockingItem()
    {
        return "";
    }

    public static String buyNewStockItem(String name, String unitOfMeasure, String category)
    {
        return "";
    }

    public static String disposingExpiredItems()
    {
        return "INSERT INTO disposed_items(quantity_disposed, inventory_id)\n" +
                "SELECT running_balance, inventory_id\n" +
                "FROM inventory\n" +
                "WHERE expiry_date < CURRENT_TIMESTAMP() AND running_balance  > 0;";
    }

    public static String recordingDisposedItemsInStockMovement()
    {
        return "INSERT INTO stock_movement(quantity, transaction_type, item_id, inventory_id, location_id)\n" +
                "SELECT d.quantity_disposed, 'DISPOSAL', i.item_id, i.inventory_id, i.location_id\n" +
                "FROM disposed_items d\n" +
                "\tJOIN inventory i\n" +
                "\t\tON d.inventory_id = i.inventory_id\n" +
                "WHERE d.should_update_inventory = 1;";
    }

    public static String updateInventoryAfterDisposing()
    {
        return "UPDATE inventory i\n" +
                "\tJOIN disposed_items d\n" +
                "\t\tON i.inventory_id = d.inventory_id\n" +
                "SET i.running_balance = 0\n" +
                "WHERE d.should_update_inventory = 1;";
    }

    public static String displayAllDisposedItems()
    {
        return "SELECT si.item_name, si.category, sl.storage_name, sl.address\n" +
                "FROM disposed_items d\n" +
                "\tJOIN inventory i\n" +
                "\t\tON d.inventory_id = i.inventory_id\n" +
                "\tJOIN stock_items si\n" +
                "\t\tON i.item_id = si.item_id\n" +
                "\tJOIN stock_locations sl\n" +
                "\t\tON i.location_id = sl.location_id;";
    }

    public static String displayRecentlyDisposedItems()
    {
        return "SELECT si.item_name, si.category, sl.storage_name, sl.address\n" +
                "FROM disposed_items d\n" +
                "\tJOIN inventory i\n" +
                "\t\tON d.inventory_id = i.inventory_id\n" +
                "\tJOIN stock_items si\n" +
                "\t\tON i.item_id = si.item_id\n" +
                "\tJOIN stock_locations sl\n" +
                "\t\tON i.location_id = sl.location_id\n" +
                "WHERE d.should_update_inventory = 1;";
    }

    public static String updateNewlyDisposedToPreviouslyDisposed()
    {
        return "UPDATE disposed_items\n" +
                "SET should_update_inventory = 0\n" +
                "WHERE should_update_inventory = 1;";
    }


    public static String createDish(int quantity)
    {
        return "SELECT " +
           "  dr.item_id, " +
           "  si.item_name, " +
           "  dr.quantity * " + quantity + " AS required_quantity " +
           "FROM dish_requirements dr " +
           "JOIN stock_items si ON dr.item_id = si.item_id " +
           "WHERE dr.dish_id = ?";
    }

        // Get inventory batches for an item at a location, FEFO (earliest expiry first)
    public static String getInventoryForItemAtLocationFEFO()
    {
        return """
            SELECT inventory_id,
                   running_balance,
                   expiry_date
            FROM inventory
            WHERE item_id = ?
              AND location_id = ?
              AND visible = 1
              AND running_balance > 0
            ORDER BY (expiry_date IS NULL), expiry_date, inventory_id
            """;
    }

    // Deduct a quantity from a single inventory row
    public static String deductFromInventoryBatch()
    {
        return """
            UPDATE inventory
            SET running_balance = running_balance - ?
            WHERE inventory_id = ?
              AND visible = 1
            """;
    }

    // Remove inventory rows whose balance is now zero or negative
    public static String deleteZeroInventoryRows()
    {
        return """
            DELETE FROM inventory
            WHERE running_balance <= 0
              AND visible = 1
            """;
    }

        // Log one dish consumption transaction
    public static String recordDishConsumption()
    {
        return """
            INSERT INTO dish_consumption (dish_id, servings, location_id)
            VALUES (?, ?, ?)
            """;
    }

    // Read-only history of dish consumption transactions
    public static String dishConsumptionHistory()
    {
        return """
            SELECT
                dc.consumption_id,
                dc.consumed_at,
                d.dish_name,
                dc.servings,
                sl.storage_name AS location_name
            FROM dish_consumption dc
            JOIN dishes d
              ON dc.dish_id = d.dish_id
            JOIN stock_locations sl
              ON dc.location_id = sl.location_id
            WHERE dc.visible = 1
            ORDER BY dc.consumed_at DESC, dc.consumption_id DESC
            """;
    }

    /*
        ###############
        # section 5.0 #
        ###############
    */

    public static String preferredSuppliersReport()
    {
        return "";
    }

    public static String storageDistributionReport()
    {
        return "";
    }

    public static String seasonalStockReport()
    {
        return "SELECT si.item_id, si.item_name, \n" +
                "\tCASE\n" +
                "\t\tWHEN ins.totalTransactions IS NULL THEN 0 \n" +
                "        ELSE ins.totalTransactions\n" +
                "\tEND AS movementInTotalTransactions,\n" +
                "    CASE\n" +
                "\t\tWHEN ins.averagePerDayTransactions IS NULL THEN 0\n" +
                "        ELSE ins.averagePerDayTransactions\n" +
                "\tEND AS movementInAveragePerDayTransactions,\n" +
                "    CASE \n" +
                "\t\tWHEN outs.totalTransactions IS NULL THEN 0\n" +
                "        ELSE outs.totalTransactions\n" +
                "\tEND AS movementOutTotalTransactions,\n" +
                "    CASE\n" +
                "\t\tWHEN outs.averagePerDayTransactions IS NULL THEN 0\n" +
                "        ELSE outs.averagePerDayTransactions\n" +
                "\tEND AS movementOutAveragePerDayTransactions,\n" +
                "\tCASE \n" +
                "\t\tWHEN MONTH(CURRENT_TIMESTAMP) BETWEEN 6 AND 11 \n" +
                "\t\tTHEN CONCAT(MONTHNAME(CURRENT_TIMESTAMP()), ', ','Rainy Season')\n" +
                "\t\tELSE CONCAT(MONTHNAME(CURRENT_TIMESTAMP()), ', ','Dry Season')\n" +
                "\tEND AS season\n" +
                "FROM stock_items si\n" +
                "\tLEFT JOIN (\n" +
                "\t\t\t\tSELECT si.item_id,\n" +
                "\t\t\t\t\tCOUNT(sm.movement_id) AS totalTransactions,\n" +
                "                    (COUNT(sm.movement_id) / DAY(LAST_DAY(CURRENT_TIMESTAMP))) AS averagePerDayTransactions\n" +
                "                FROM stock_items si\n" +
                "\t\t\t\t\tLEFT JOIN stock_movement sm\n" +
                "\t\t\t\t\t\tON si.item_id = sm.item_id\n" +
                "\t\t\t\t\tAND MONTH(sm.moved_at) = MONTH(CURRENT_TIMESTAMP())\n" +
                "\t\t\t\t\tAND YEAR(sm.moved_at) = YEAR(CURRENT_TIMESTAMP())\n" +
                "\t\t\t\tWHERE si.visible = 1 AND sm.visible = 1 AND sm.transaction_type IN ('RESTOCK','TRANSFER_IN')\n" +
                "\t\t\t\tGROUP BY (sm.item_id)\n" +
                "\t\t\t  ) ins \n" +
                "              ON si.item_id = ins.item_id\n" +
                "\tLEFT JOIN (\n" +
                "\t\t\t\tSELECT si.item_id,\n" +
                "\t\t\t\t\tCOUNT(sm.movement_id) AS totalTransactions,\n" +
                "                    (COUNT(sm.movement_id) / DAY(LAST_DAY(CURRENT_TIMESTAMP))) AS averagePerDayTransactions\n" +
                "                FROM stock_items si\n" +
                "\t\t\t\t\tLEFT JOIN stock_movement sm\n" +
                "\t\t\t\t\t\tON si.item_id = sm.item_id\n" +
                "\t\t\t\t\tAND MONTH(sm.moved_at) = MONTH(CURRENT_TIMESTAMP())\n" +
                "\t\t\t\t\tAND YEAR(sm.moved_at) = YEAR(CURRENT_TIMESTAMP())\n" +
                "\t\t\t\tWHERE si.visible = 1 AND sm.visible = 1 AND sm.transaction_type IN ('CONSUMPTION','TRANSFER_OUT')\n" +
                "\t\t\t\tGROUP BY (sm.item_id)\n" +
                "\t\t\t  ) outs\n" +
                "              ON si.item_id = outs.item_id\n" +
                "ORDER BY si.item_id;";
    }

    public static String expiryReport()
    {
        return "SELECT si.item_name, \n" +
                "\t\tCASE \n" +
                "\t\t\tWHEN d.totalQuantityDisposed IS NULL THEN 0\n" +
                "\t\t\tELSE d.totalQuantityDisposed\n" +
                "\t\tEND AS totalQuantityDisposed,\n" +
                "        CASE\n" +
                "\t\t\tWHEN p.totalQuantityPurchased IS NULL THEN 0\n" +
                "            ELSE p.totalQuantityPurchased\n" +
                "\t\tEND AS totalQuantityPurchased,\n" +
                "        totalQuantityDisposed / totalQuantityPurchased AS disposedPurchasedRatio\n" +
                "FROM stock_items si\n" +
                "\tLEFT JOIN(\n" +
                "\t\t\t\tSELECT SUM(di.quantity_disposed) AS totalQuantityDisposed, i.item_id\n" +
                "                FROM disposed_items di\n" +
                "\t\t\t\t\tJOIN inventory i\n" +
                "\t\t\t\t\t\tON di.inventory_id = i.inventory_id\n" +
                "\t\t\t\tWHERE YEAR(di.disposed_date) = YEAR(CURRENT_TIMESTAMP()) AND MONTH(di.disposed_date) = MONTH(CURRENT_TIMESTAMP())\n" +
                "\t\t\t\t\tAND di.visible = 1 AND i.visible = 1\n" +
                "\t\t\t\tGROUP BY i.item_id\n" +
                "\t\t\t) d\n" +
                "\t\tON si.item_id = d.item_id\n" +
                "\tLEFT JOIN (\n" +
                "\t\t\t\tSELECT SUM(pl.quantity) AS totalQuantityPurchased, i.item_id\n" +
                "                FROM inventory i\n" +
                "\t\t\t\t\tJOIN purchase_line pl\n" +
                "\t\t\t\t\t\tON i.inventory_id = pl.inventory_id\n" +
                "\t\t\t\t\tJOIN purchases pu\n" +
                "\t\t\t\t\t\tON pl.purchase_id = pu.purchase_id\n" +
                "\t\t\t\tWHERE YEAR(pu.receive_date) = YEAR(CURRENT_TIMESTAMP()) AND MONTH(pu.receive_date) = MONTH(CURRENT_TIMESTAMP())\n" +
                "                AND i.visible = 1 AND pl.visible = 1 AND pu.visible = 1\n" +
                "\t\t\t\tGROUP BY i.item_id\n" +
                "\t\t\t ) p\n" +
                "\t\tON si.item_id = p.item_id\n" +
                "\tORDER BY totalQuantityDisposed / totalQuantityPurchased;";
    }
}
