package db;

import java.sql.Connection;
import gui.view.ScreenManager;  
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;

/*
    This class performs different SQL statements in its
    different methods

    Note:
    - If a report/transaction needs multiple SQL statements, we
      CANNOT place all the SQL statements in 1 string then pass
      that string as a parameter to executeUpdate() or
      executeQuery()
    - When trying to execute an SQL statement, we have to follow a
      1 method per SQL statement rule
    - The flow of each method should be createConnection(),
      either executeUpdate(*insert SQL statement*) or
      executeQuery(*insert SQL statement*), then closeConnection()
    - executeQuery(*insert SQL statement*) is used to retrieve
      information by returning a ResultSet object
    - executeUpdate(*insert SQL statement*) is used to insert, rewrite,
      or 'delete' a record
 */

public class DBInteractor {

    private final Query query;
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

    public ResultSet recordManagement4(int supplierId)
    {
        createConnection();
        try
        {
            String sql = query.supplierAndProducts();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, supplierId);   // binds s.supplier_id = ?
            return ps.executeQuery();
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

    public ResultSet transaction4(int dishId, int numberOfDishes)
    {
        createConnection();
        try
        {
            String sql = query.createDish(numberOfDishes);
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, dishId);   // binds dr.dish_id = ?
            return ps.executeQuery();
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

    public ResultSet report4(int year, int month)
    {
        createConnection();
        try
        {
            String sql = query.expiryReport();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, year);   // purchases.order_year
            ps.setInt(2, month);  // purchases.order_month
            ps.setInt(3, year);   // YEAR(moved_at)
            ps.setInt(4, month);  // MONTH(moved_at)
            return ps.executeQuery();
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
            c = ScreenManager.getConnection();
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
