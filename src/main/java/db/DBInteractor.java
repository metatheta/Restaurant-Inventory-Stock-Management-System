package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBInteractor {

    private Query query;
    private Connection c;
    private Statement s;

    public DBInteractor(Query q)
    {
        query = q;
        c = null;
        s = null;
    }

    public ResultSet recordManagement1()
    {
        createConnection();
        try
        {
            ResultSet rs = s.executeQuery(query.stockItemAndSuppliers());
            return rs;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        closeConnection();

        return null;
    }

    public ResultSet recordManagement2()
    {
        createConnection();
        try
        {
            ResultSet rs = s.executeQuery(query.storedItemAndLocations());
            return rs;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        closeConnection();

        return null;
    }

    public ResultSet recordManagement3()
    {
        createConnection();
        try
        {
            ResultSet rs = s.executeQuery(query.locationAndStoredItems());
            return rs;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        closeConnection();

        return null;
    }

    public ResultSet recordManagement4()
    {
        createConnection();
        try
        {
            ResultSet rs = s.executeQuery(query.supplierAndProducts());
            return rs;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        closeConnection();

        return null;
    }

    public ResultSet transaction1()
    {
        createConnection();
        try
        {
            ResultSet rs = s.executeQuery(query.restockingItem());
            return rs;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        closeConnection();

        return null;
    }

    public ResultSet transaction2(String name, String unitOfMeasure, String category)
    {
        createConnection();
        try
        {
            ResultSet rs = s.executeQuery(query.buyNewStockItem(name, unitOfMeasure, category));
            return rs;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        closeConnection();

        return null;
    }

    public ResultSet transaction3()
    {
        createConnection();
        try
        {
            ResultSet rs = s.executeQuery(query.disposeUnusedStock());
            return rs;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        closeConnection();

        return null;
    }

    public ResultSet transaction4(int numberOfDishes)
    {
        createConnection();
        try
        {
            ResultSet rs = s.executeQuery(query.createDish(numberOfDishes));
            return rs;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        closeConnection();

        return null;
    }

    public ResultSet report1()
    {
        createConnection();
        try
        {
            ResultSet rs = s.executeQuery(query.preferredSuppliersReport());
            return rs;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        closeConnection();

        return null;
    }

    public ResultSet report2()
    {
        createConnection();
        try
        {
            ResultSet rs = s.executeQuery(query.storageDistributionReport());
            return rs;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        closeConnection();

        return null;
    }

    public ResultSet report3()
    {
        createConnection();
        try
        {
            ResultSet rs = s.executeQuery(query.seasonalStockReport());
            return rs;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        closeConnection();

        return null;
    }

    public ResultSet report4()
    {
        createConnection();
        try
        {
            ResultSet rs = s.executeQuery(query.expiryReport());
            return rs;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        closeConnection();

        return null;
    }

    private void createConnection()
    {
        try
        {
            c = DriverManager.getConnection("jdbc:mysql://localhost/crisms_db?" + "user=root&password=p@ssword");
            s = c.createStatement();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    private void closeConnection()
    {
        try
        {
            c.close();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }


}
