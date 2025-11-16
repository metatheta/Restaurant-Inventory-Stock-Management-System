package db;

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
        return "";
    }

    public String storedItemAndLocations()
    {
        return "";
    }

    public String locationAndStoredItems()
    {
        return "SELECT sl.storage_name, sl.storage_type, si.item_name, i.running_balance\n" +
                "FROM stock_locations sl\n" +
                "JOIN inventory i \n" +
                "\tON sl.location_id = i.location_id\n" +
                "JOIN stock_items si\n" +
                "\tON i.item_id = si.item_id;";
    }

    public String supplierAndProducts()
    {
        return "";
    }

    /*
        ###############
        # section 4.0 #
        ###############
    */

    public String restockingItem()
    {
        return "";
    }

    public String buyNewStockItem(String name, String unitOfMeasure, String category)
    {
        return "";
    }

    public String disposeUnusedStock()
    {
        return "";
    }

    public String createDish(int quantity)
    {
        return "";
    }

    /*
        ###############
        # section 5.0 #
        ###############
    */

    public String preferredSuppliersReport()
    {
        return "";
    }

    public String storageDistributionReport()
    {
        return "";
    }

    public String seasonalStockReport()
    {
        return "";
    }

    public String expiryReport()
    {
        return "";
    }
}
