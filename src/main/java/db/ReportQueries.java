package db;

/*
    This is where we will store all out SQL statements
    as strings

    Note:
    - Each SQL statement should have its own method
 */
public class ReportQueries {
    public static String preferredSuppliersReport(int year, int month) {
        return "SELECT \n" +
                "    s.name,\n" +
                "    COUNT(r.restock_id) AS numOrders,\n" +
                "    COALESCE(AVG(r.total_cost), 0) AS averageOrderCost,\n" +
                "    COALESCE(SUM(r.total_cost), 0) AS totalOrderCost\n" +
                "FROM\n" +
                "    suppliers s\n" +
                "\tLEFT JOIN\n" +
                "    item_restocks r ON s.supplier_id = r.supplier_id\n" +
                "    AND YEAR(r.restocked_at) = " + year + "\n AND MONTH(r.restocked_at) = " + month +
                "\nGROUP BY s.supplier_id\n" +
                "ORDER BY numOrders DESC;";
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

    public static String seasonalStockReport(int startMonth, int endMonth, int startYear) {
        return "SELECT si.item_name, " +
                "COALESCE(b.y, 0) AS totalMovementInTransactions, " +
                "COALESCE(b.y, 0)/30 AS averageMovementInTransactions, " +
                "COALESCE(a.x, 0) + COALESCE(c.z, 0) AS totalMovementOutTransactions, " +
                "(COALESCE(a.x, 0) + COALESCE(c.z, 0))/30 AS averageMovementOutTransactions " +
                "FROM stock_items si " +
                "LEFT JOIN ( " +
                "    SELECT si.item_id, COUNT(di.disposed_id) AS x " +
                "    FROM stock_items si " +
                "    LEFT JOIN inventory i ON si.item_id = i.item_id " +
                "    LEFT JOIN disposed_items di ON i.inventory_id = di.inventory_id " +
                "    WHERE si.visible = 1 AND i.visible = 1 AND di.visible = 1 " +
                "      AND YEAR(di.disposed_date) = " + startYear + " " +
                "      AND MONTH(di.disposed_date) BETWEEN " + startMonth + " AND " + endMonth + " " +
                "    GROUP BY si.item_id " +
                ") a ON si.item_id = a.item_id " +
                "LEFT JOIN ( " +
                "    SELECT si.item_id, COUNT(rs.restock_id) AS y " +
                "    FROM stock_items si " +
                "    LEFT JOIN item_restocks rs ON si.item_id = rs.item_id " +
                "    WHERE si.visible = 1 AND rs.visible = 1 " +
                "      AND YEAR(rs.restocked_at) = " + startYear + " " +
                "      AND MONTH(rs.restocked_at) BETWEEN " + startMonth + " AND " + endMonth + " " +
                "    GROUP BY si.item_id " +
                ") b ON si.item_id = b.item_id " +
                "LEFT JOIN ( " +
                "    SELECT si.item_id, COUNT(dc.consumption_id) AS z " +
                "    FROM stock_items si " +
                "    LEFT JOIN dish_requirements dr ON si.item_id = dr.item_id " +
                "    LEFT JOIN dishes d ON dr.dish_id = d.dish_id " +
                "    LEFT JOIN dish_consumption dc ON d.dish_id = dc.dish_id " +
                "    WHERE si.visible = 1 AND dr.visible = 1 AND d.visible = 1 AND dc.visible = 1 " +
                "      AND YEAR(dc.consumed_at) = " + startYear + " " +
                "      AND MONTH(dc.consumed_at) BETWEEN " + startMonth + " AND " + endMonth + " " +
                "    GROUP BY si.item_id " +
                ") c ON si.item_id = c.item_id " +
                "WHERE si.visible = 1 " +
                "ORDER BY (totalMovementInTransactions + totalMovementOutTransactions) DESC;";
    }

    public static String expiryReport(int year, int month) {
        return "SELECT si.item_name,\n"
                + "       COALESCE(d.totalQuantityDisposed, 0) AS totalQuantityDisposed,\n"
                + "       COALESCE(p.totalQuantityPurchased, 0) AS totalQuantityPurchased,\n"
                + "       CASE\n"
                + "           WHEN COALESCE(p.totalQuantityPurchased, 0) = 0 THEN 0.00\n"
                + "           WHEN COALESCE(d.totalQuantityDisposed, 0) = 0 THEN 0.00\n"
                + "           ELSE ROUND(CAST(COALESCE(d.totalQuantityDisposed, 0) AS DECIMAL(10,2)) / p.totalQuantityPurchased, 2)\n"
                // Guarding against div by zero
                + "       END AS disposedPurchasedRatio\n"
                + "FROM stock_items si\n"
                + "LEFT JOIN (\n"
                + "       SELECT i.item_id,\n"
                + "              SUM(di.quantity_disposed) AS totalQuantityDisposed\n"
                + "       FROM disposed_items di\n"
                + "       JOIN inventory i\n"
                + "         ON di.inventory_id = i.inventory_id\n"
                + "       WHERE di.visible = 1\n"
                + "         AND i.visible = 1\n"
                + "         AND YEAR(di.disposed_date) = " + year + "\n"
                + "         AND MONTH(di.disposed_date) = " + month + "\n"
                + "       GROUP BY i.item_id\n"
                + ") d ON si.item_id = d.item_id\n"
                + "LEFT JOIN (\n"
                + "       SELECT ir.item_id,\n"
                + "              SUM(ir.quantity) AS totalQuantityPurchased\n"
                + "       FROM item_restocks ir\n"
                + "       WHERE ir.visible = 1\n"
                + "         AND YEAR(ir.restocked_at) = " + year + "\n"
                + "         AND MONTH(ir.restocked_at) = " + month + "\n"
                + "       GROUP BY ir.item_id\n"
                + ") p ON si.item_id = p.item_id\n"
                + "WHERE si.visible = 1\n"
                + "ORDER BY disposedPurchasedRatio DESC;";
    }
}
