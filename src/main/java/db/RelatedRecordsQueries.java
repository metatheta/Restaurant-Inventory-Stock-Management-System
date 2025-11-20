package db;

public class RelatedRecordsQueries {
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
}