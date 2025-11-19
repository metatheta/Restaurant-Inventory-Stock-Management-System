package db;


import gui.ScreenManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

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
    private Statement s;

    public DBInteractor()
    {
        try
        {
            s = ScreenManager.getConnection().createStatement();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public ResultSet recordManagement1()
    {
        try
        {
            ResultSet rs = s.executeQuery(Query.stockItemAndSuppliers());
            return rs;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public ResultSet recordManagement2()
    {
        try
        {
            ResultSet rs = s.executeQuery(Query.storedItemAndLocations());
            return rs;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public ResultSet recordManagement3()
    {
        try
        {
            ResultSet rs = s.executeQuery(Query.locationAndStoredItems());
            return rs;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public ResultSet recordManagement4(int supplierId)
    {
        try
        {
            String sql = Query.supplierAndProducts();
            PreparedStatement ps = ScreenManager.getConnection().prepareStatement(sql);
            ps.setInt(1, supplierId);   // binds s.supplier_id = ?
            return ps.executeQuery();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public ResultSet transaction1()
    {
        try
        {
            ResultSet rs = s.executeQuery(Query.restockingItem());
            return rs;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public ResultSet transaction2(String name, String unitOfMeasure, String category)
    {
        try
        {
            ResultSet rs = s.executeQuery(Query.buyNewStockItem(name, unitOfMeasure, category));
            return rs;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public void enteringTransaction3()
    {
        try
        {
            s.executeUpdate(Query.disposingExpiredItems());
            s.executeUpdate(Query.recordingDisposedItemsInStockMovement());
            s.executeUpdate(Query.updateInventoryAfterDisposing());
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    public ResultSet displayAllDisposedItems()
    {
        try
        {
            ResultSet rs = s.executeQuery(Query.displayAllDisposedItems());
            return rs;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public ResultSet displayRecentlyDisposedItems()
    {
        try
        {
            ResultSet rs = s.executeQuery(Query.displayRecentlyDisposedItems());
            return rs;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public void exitingTransaction3()
    {
        try
        {
            s.executeUpdate(Query.updateNewlyDisposedToPreviouslyDisposed());
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    public ResultSet transaction4(int dishId, int numberOfDishes)
    {
        try
        {
            String sql = Query.createDish(numberOfDishes);
            PreparedStatement ps = ScreenManager.getConnection().prepareStatement(sql);
            ps.setInt(1, dishId);   // binds dr.dish_id = ?
            return ps.executeQuery();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public ResultSet report1()
    {
        try
        {
            ResultSet rs = s.executeQuery(Query.preferredSuppliersReport());
            return rs;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public ResultSet report2()
    {
        try
        {
            ResultSet rs = s.executeQuery(Query.storageDistributionReport());
            return rs;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public ResultSet report3()
    {
        try
        {
            ResultSet rs = s.executeQuery(Query.seasonalStockReport());
            return rs;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public ResultSet report4(int year, int month)
    {
        try
        {
            String sql = Query.expiryReport();
            PreparedStatement ps = ScreenManager.getConnection().prepareStatement(sql);
            ps.setInt(1, year);   // purchases.order_year
            ps.setInt(2, month);  // purchases.order_month
            ps.setInt(3, year);   // YEAR(moved_at)
            ps.setInt(4, month);  // MONTH(moved_at)
            return ps.executeQuery();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }


}
