package db;


import gui.ScreenManager;

import java.sql.*;

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

    public DBInteractor() {
        try {
            s = ScreenManager.getConnection().createStatement();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public ResultSet recordManagement1() {
        try {
            ResultSet rs = s.executeQuery(Query.stockItemAndSuppliers());
            return rs;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public ResultSet recordManagement2() {
        try {
            ResultSet rs = s.executeQuery(Query.storedItemAndLocations());
            return rs;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public ResultSet recordManagement3() {
        try {
            ResultSet rs = s.executeQuery(Query.locationAndStoredItems());
            return rs;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public ResultSet recordManagement4(int supplierId) {
        try {
            String sql = Query.supplierAndProducts();
            PreparedStatement ps = ScreenManager.getConnection().prepareStatement(sql);
            ps.setInt(1, supplierId);   // binds s.supplier_id = ?
            return ps.executeQuery();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public ResultSet getItemsToRestock() {
        try {
            ResultSet rs = s.executeQuery(Query.selectItemsToRestock());
            return rs;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public ResultSet getRestockRecords() {
        try {
            ResultSet rs = s.executeQuery(Query.getRestockRecords());
            return rs;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    // TODO potentially refactor methods into diff classes

    public boolean checkSupplierProductExists(int supplierId, int itemId) {
        try (Connection conn = ScreenManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(Query.r2_checkIfSupplierProductComboExists())) {
            ps.setInt(1, supplierId);
            ps.setInt(2, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkInvisibleRecordExists(int supplierId, int itemId) {
        try (Connection conn = ScreenManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(Query.r2_checkIfInvisibleRecordExists())) {
            ps.setInt(1, supplierId);
            ps.setInt(2, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public ResultSet getRegistryHistory() {
        try {
            Connection conn = ScreenManager.getConnection();
            Statement stmt = conn.createStatement();
            return stmt.executeQuery(Query.r2_showReadOnlyTable());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public void registerSupplierProduct(int supplierId, int itemId, double cost, boolean isReactivation) {
        Connection conn = null;
        try {
            conn = ScreenManager.getConnection();
            conn.setAutoCommit(false);

            String actionSql = isReactivation ? Query.r2_updateToDoIfInvisibleRecordExists() : Query.r2_updateToDoIfInvisibleRecordDoesNotExist();

            try (PreparedStatement ps = conn.prepareStatement(actionSql)) {
                if (isReactivation) {
                    // Update existing invisible record
                    ps.setDouble(1, cost);       // unit_cost
                    ps.setInt(2, supplierId);    // WHERE supplier_id
                    ps.setInt(3, itemId);        // AND item_id
                } else {
                    // Insert new record
                    ps.setInt(1, supplierId);
                    ps.setInt(2, itemId);
                    ps.setDouble(3, cost);
                }
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(Query.r2_recordAdditionInTransactionTable())) {
                ps.setInt(1, supplierId);
                ps.setInt(2, itemId);
                ps.setDouble(3, cost);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void updateSupplierProduct(int supplierId, int itemId, double cost) {
        Connection conn = null;
        try {
            conn = ScreenManager.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(Query.r2_updateToDoIfUserSelectsChange())) {
                ps.setDouble(1, cost);        // SET unit_cost
                ps.setInt(2, supplierId);     // WHERE supplier_id
                ps.setInt(3, itemId);         // AND item_id
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(Query.r2_recordChangeInTransactionTable())) {
                ps.setInt(1, supplierId);
                ps.setInt(2, itemId);
                ps.setDouble(3, cost);
                ps.executeUpdate();
            }

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public ResultSet getActiveSuppliers() {
        try {
            Connection conn = ScreenManager.getConnection();
            return conn.createStatement().executeQuery("SELECT supplier_id, name FROM suppliers WHERE visible = 1");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ResultSet getActiveStockItems() {
        try {
            Connection conn = ScreenManager.getConnection();
            return conn.createStatement().executeQuery("SELECT item_id, item_name, unit_of_measure FROM stock_items WHERE visible = 1");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addNewSupplier(String name, String contactPerson, String contactInfo) {
        try (Connection conn = ScreenManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO suppliers (name, contact_person, contact_info) VALUES (?, ?, ?)")) {
            ps.setString(1, name);
            ps.setString(2, contactPerson);
            ps.setString(3, contactInfo);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void enteringTransaction3() {
        try {
            s.executeUpdate(Query.disposingExpiredItems());
            s.executeUpdate(Query.setExpiredToZero());
            s.executeUpdate(Query.recordingDisposedItemsInStockMovement());
            s.executeUpdate(Query.updateInventoryAfterDisposing());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public ResultSet displayAllDisposedItems() {
        try {
            ResultSet rs = s.executeQuery(Query.displayAllDisposedItems());
            return rs;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public ResultSet displayRecentlyDisposedItems() {
        try {
            ResultSet rs = s.executeQuery(Query.displayRecentlyDisposedItems());
            return rs;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public void exitingTransaction3() {
        try {
            s.executeUpdate(Query.updateNewlyDisposedToPreviouslyDisposed());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public ResultSet getAllDishes() {
        try {
            return s.executeQuery(Query.getAllDishes());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public ResultSet getDishIngredients() {
        try {
            return s.executeQuery(Query.getDishIngredients());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public ResultSet getAllLocations() {
        try {
            return s.executeQuery(Query.getAllLocations());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public ResultSet viewDishRecords() {
        try {
            return s.executeQuery(Query.dishConsumptionHistory());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public ResultSet report1(int year, int month) {
        try {
            ResultSet rs = s.executeQuery(Query.preferredSuppliersReport(year, month));
            return rs;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public ResultSet report2(int year, int month) {
        try {
            ResultSet rs = s.executeQuery(Query.storageDistributionReport(year, month));
            return rs;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public ResultSet report3() {
        try {
            ResultSet rs = s.executeQuery(Query.seasonalStockReport());
            return rs;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public ResultSet report4(int year, int month) {
        try {
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
