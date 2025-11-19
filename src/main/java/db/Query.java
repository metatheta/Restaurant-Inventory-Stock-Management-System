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

    public String stockItemAndSuppliers()
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

    public String storedItemAndLocations()
    {
        return "";
    }

    public String locationAndStoredItems()
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

    public String supplierAndProducts()
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
                "\t\tCOUNT(sm.movement_id) AS totalTransactions,\n" +
                "        (COUNT(sm.movement_id) / DAY(LAST_DAY(CURRENT_TIMESTAMP))) AS averagePerDayTransactions,\n" +
                "        CASE \n" +
                "        WHEN MONTH(CURRENT_TIMESTAMP) BETWEEN 6 AND 11 \n" +
                "            THEN CONCAT(MONTHNAME(CURRENT_TIMESTAMP()), ', ','Rainy Season')\n" +
                "\t\t\tELSE CONCAT(MONTHNAME(CURRENT_TIMESTAMP()), ', ','Dry Season')\n" +
                "\t\tEND AS season\n" +
                "FROM stock_items si\n" +
                "\tLEFT JOIN stock_movement sm\n" +
                "\t\tON si.item_id = sm.item_id\n" +
                "\tAND MONTH(sm.moved_at) = MONTH(CURRENT_TIMESTAMP()) \n" +
                "    AND YEAR(sm.moved_at) = YEAR(CURRENT_TIMESTAMP())\n" +
                "WHERE si.visible = 1 AND sm.visible = 1\n" +
                "GROUP BY (sm.item_id)\n" +
                "ORDER BY totalTransactions, averagePerDayTransactions;";
    }

    public static String expiryReport()
    {
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
