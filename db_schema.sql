/*
	Name: Comprehensive Restaurant Inventory and Stock Management System 
    Members: 
        GENDRANO, Christian D.
        HIZON, Allen Conner C.
        MARQUEZ, Jose Miguel S.
        NICOLAS, Francis Medin N.
*/

-- #################
-- # make database #
-- #################
CREATE DATABASE IF NOT EXISTS crisms_db;

-- #######################
-- # switch to crisms_db #
-- #######################
USE crisms_db;

-- #######################
-- # drop current tables #
-- #######################
DROP TABLE IF EXISTS supplier_products;
DROP TABLE IF EXISTS stock_movement;
DROP TABLE IF EXISTS purchase_line;
DROP TABLE IF EXISTS purchases;
DROP TABLE IF EXISTS inventory;
DROP TABLE IF EXISTS stock_locations;
DROP TABLE IF EXISTS stock_items;
DROP TABLE IF EXISTS suppliers;

-- #####################
-- # create the tables #
-- #####################

CREATE TABLE stock_items(
                            item_id 			INT				AUTO_INCREMENT			PRIMARY KEY,
                            item_name 			VARCHAR(30)     NOT NULL,
                            unit_of_measure 	VARCHAR(30)     NOT NULL,    -- kg, liter, pcs, etc.
                            category 			VARCHAR(30)     NOT NULL,     -- vegetable, fat, grain, etc.
                            visible				TINYINT(1)								DEFAULT 1
);

CREATE TABLE stock_locations(
                                location_id 		INT				AUTO_INCREMENT			PRIMARY KEY,
                                storage_name 		VARCHAR(30)     NOT NULL,    -- e.g., Main Kitchen, Freezer Room
                                address 			VARCHAR(100),                -- e.g., Batman, Turkey; Sesame Street; Crapstone, England
                                storage_type 		VARCHAR(15) ,                -- e.g., Sack, Barrel, Tupperware, Ziplock
                                visible				TINYINT(1)								DEFAULT 1
);

CREATE TABLE suppliers(
                          supplier_id 		INT				AUTO_INCREMENT			PRIMARY KEY,
                          name 				VARCHAR(30)     NOT NULL,
                          contact_person 		VARCHAR(30)     NOT NULL,
                          contact_info 		VARCHAR(30)     NOT NULL,
                          visible				TINYINT(1)								DEFAULT 1
);

CREATE TABLE inventory(
                          inventory_id 		INT				AUTO_INCREMENT			PRIMARY KEY,
                          running_balance 	DECIMAL(12,3)   NOT NULL				CHECK (running_balance >= 0),
                          last_restock_date 	TIMESTAMP								DEFAULT CURRENT_TIMESTAMP,
                          expiry_date 		TIMESTAMP								NULL,   -- NULL for non-perishables
                          location_id			INT			    NOT NULL,
                          item_id				INT			    NOT NULL,
                          visible				TINYINT(1)								DEFAULT 1,
                          FOREIGN KEY (location_id)  REFERENCES stock_locations (location_id)	ON DELETE CASCADE,
                          FOREIGN KEY (item_id) 	   REFERENCES stock_items (item_id)		ON DELETE CASCADE
);

CREATE TABLE purchases(
                          purchase_id 		INT				AUTO_INCREMENT			PRIMARY KEY,
                          order_date 			DATE  		    NOT NULL,
                          receive_date 		DATE,
                          total_cost 			DECIMAL(10,2)   NOT NULL,
                          supplier_id			INT			    NOT NULL,
                          visible				TINYINT(1)								DEFAULT 1,
    -- for reporting (fast month/year filters)
                          order_year          INT GENERATED ALWAYS AS (YEAR(order_date)) STORED,
    order_month         INT GENERATED ALWAYS AS (MONTH(order_date)) STORED,
    FOREIGN KEY (supplier_id)  REFERENCES suppliers (supplier_id)	ON DELETE CASCADE,
    INDEX idx_pur_year_month (order_year, order_month)
);

CREATE TABLE purchase_line(
                              purchase_line_id 	INT				AUTO_INCREMENT			PRIMARY KEY,
                              quantity 			DECIMAL(12,3)   NOT NULL				CHECK (quantity >= 0),
                              unit_cost 			DECIMAL(10,2)   NOT NULL				CHECK (unit_cost >= 0),
                              purchase_id			INT			    NOT NULL,
                              item_id				INT			    NOT NULL,
                              inventory_id		INT,
                              visible				TINYINT(1)								DEFAULT 1,
                              FOREIGN KEY (purchase_id)  REFERENCES purchases (purchase_id)	ON DELETE CASCADE,
                              FOREIGN KEY (item_id)      REFERENCES stock_items (item_id)		ON DELETE CASCADE,
                              FOREIGN KEY (inventory_id) REFERENCES inventory (inventory_id)	ON DELETE CASCADE
);

CREATE TABLE supplier_products(
                                  amount 				INT 		    NOT NULL				CHECK (amount >= 0),
                                  unit_cost 			DECIMAL(10,2)   NOT NULL				CHECK (unit_cost >= 0),
                                  supplier_id			INT			    NOT NULL,
                                  item_id				INT			    NOT NULL,
                                  visible				TINYINT(1)								DEFAULT 1,
                                  FOREIGN KEY (supplier_id)  REFERENCES suppliers (supplier_id)	ON DELETE CASCADE,
                                  FOREIGN KEY (item_id)      REFERENCES stock_items (item_id)		ON DELETE CASCADE,
                                  PRIMARY KEY(supplier_id, item_id)
);

CREATE TABLE stock_movement(
                               movement_id 		INT				AUTO_INCREMENT			PRIMARY KEY,
                               quantity 			INT 		    NOT NULL				CHECK (quantity >= 0),
                               moved_at 			TIMESTAMP 	    NOT NULL				DEFAULT CURRENT_TIMESTAMP,
                               transaction_type 	ENUM('RESTOCK','DISPOSAL','CONSUMPTION',
                             'TRANSFER_IN','TRANSFER_OUT')     NOT NULL,
                               item_id             INT             NOT NULL,
                               location_id         INT             NOT NULL,
                               inventory_id		INT				NOT NULL,
                               visible				TINYINT(1)								DEFAULT 1,
                               FOREIGN KEY (item_id)      REFERENCES stock_items (item_id)		ON DELETE CASCADE,
                               FOREIGN KEY (location_id)  REFERENCES stock_locations (location_id)	ON DELETE CASCADE,
                               FOREIGN KEY (inventory_id) REFERENCES inventory (inventory_id)	ON DELETE CASCADE
);


-- STOCK ITEMS
INSERT INTO stock_items (item_name, unit_of_measure, category)
VALUES
    ('tomato',      'kg',    'vegetable'),
    ('cooking oil', 'liter', 'fat'),
    ('rice',        'kg',    'grain'),
    ('beef',        'kg',    'protein'),
    ('salt',        'kg',    'condiment'),
    ('garlic',      'kg',    'vegetable'),
    ('curry',       'kg',    'condiment'),
    ('fish',        'kg',    'protein'),
    ('soy sauce',   'liter', 'condiment'),
    ('butter',      'kg',    'dairy'),
    ('onion',        'kg',   'vegetable'),
    ('potato',       'kg',   'vegetable'),
    ('chicken',      'kg',   'protein'),
    ('pork',         'kg',   'protein'),
    ('vinegar',      'liter','condiment'),
    ('brown sugar',  'kg',   'condiment'),
    ('milk',         'liter','dairy'),
    ('cheese',         'kg',  'dairy'),
    ('ginger',       'kg',   'vegetable'),
    ('mayonnaise',   'liter','condiment');

-- STOCK LOCATIONS
INSERT INTO stock_locations (storage_name, address, storage_type)
VALUES
    ('Lower Shelves', 'Taft Ave., Manila', 'Jug'),
    ('Walk-in Freezer', 'Taft Ave., Manila', 'Box'),
    ('Main Kitchen', 'Taft Ave., Manila', 'Tupperware'),
    ('Main Kitchen', '7 Pine Lane, Davao', 'Barrel'),
    ('Walk-in Freezer', '7 Pine Lane, Davao', 'Ziplock'),
    ('Refridgerator', '7 Pine Lane, Davao', 'Tupperware'),
    ('Pantry', '7 Pine Lane, Davao', 'Shelf'),
    ('Walk-in Freezer', '101 Mango St., Manila', 'Sack'),
    ('Upper Shelves', '101 Mango St., Manila', 'Ziplock'),
    ('Pantry', '101 Mango St., Manila', 'Plastic Container'),
    ('Upper Shelves', '12 Sampaguita Ave., QC', 'Box'),
    ('Refridgerator', '12 Sampaguita Ave., QC', 'Tupperware'),
    ('Main Kitchen', '12 Sampaguita Ave., QC', 'Sack'),
    ('Lower Shelves', '3 Narra Drive, Cebu', 'Jug'),
    ('Main Kitchen', '3 Narra Drive, Cebu', 'Sack'),
    ('Refridgerator', '3 Narra Drive, Cebu', 'Ziplock'),
    ('Walk-in Freezer', '3 Narra Drive, Cebu', 'Box'),
    ('Main Kitchen', '4 Matino St., QC', 'Sack'),
    ('Lower Shelves', '4 Matino St., QC', 'Jug'),
    ('Pantry', '4 Matino St., QC', 'Plastic Container');

-- INVENTORY (per item per location)
INSERT INTO inventory (running_balance, item_id, location_id)
VALUES
    (50, 1, 3), -- tomato in main kitchen
    (30, 2, 3), -- cooking oil in main kitchen
    (100, 3, 7), -- rice in pantry
    (20, 4, 2),  -- beef in freezer
    (70, 5, 4), -- salt in barrel
    (12, 6, 6), -- garlic in refridgereator
    (30, 7, 9), -- curry in upper shelves
    (68, 8, 5), -- fish in freezer
    (120, 9, 1), -- soy sauce in lower shelves
    (43, 10,16), -- butter in refridgerator
    (46, 11, 12), -- onion in refridgerator
    (167, 12, 4), -- potato in main kitchen
    (54, 13, 8), -- chicken in freezer
    (88, 14, 17), -- pork in freezer
    (12, 15, 7), -- vinegar in lower shelves
    (9, 16, 10), -- sugar in pantry
    (149, 17, 12), -- milk in refridgerator
    (105, 18, 11), -- cheese in upper shelves
    (12, 19, 13), -- ginger in main kitchen
    (42, 20, 14), -- mayonaise in lower shelves
    (197, 1, 18), -- tomatoes in main kitchen
    (24, 9, 19), -- soy sauce in lower shelves
    (192, 5, 20), -- salt in plastic container
    (180, 6, 13), -- garlic in main kitchen
    (171, 14, 2), -- pork in freezer
    (39, 3, 4), -- rice in main kitchen
    (7, 13, 2), -- chicken in freezer
    (81, 17, 1), -- milk in main kitchen
    (36, 4, 5), -- beef in freezer
    (177, 15, 1), -- vinegar in main kitchen
    (162, 16, 3); -- sugar in main kitchen

-- SUPPLIERS
INSERT INTO suppliers (name, contact_person, contact_info)
VALUES
    ('FreshHarvest Produce Co.', 'Ana Dizon', '0917-123-4567'),
    ('Golden Fields Dairy', 'Marco Reyes', '0998-456-1234'),
    ('Pacific Seas Fisheries', 'Liza Santos', '0928-344-5566'),
    ('GreenLeaf Organic Farms', 'Julian Mercado', '0917-555-3322'),
    ('Sunrise Grains Trading', 'Paolo Dominguez', '0935-888-2219'),
    ('Manila Spice Merchants', 'Rica Villanueva', '0917-666-7788'),
    ('Silver River Poultry', 'Noel Chua', '0995-123-4888'),
    ('Royal Bake Ingredients Co.', 'Steph Uy', '0918-111-2223'),
    ('Island Fresh Beverages', 'Gabriel Tan', '0947-555-6621'),
    ('Sierra Madre Vegetables', 'Celine Ramos', '0922-333-4455'),
    ('Prime Butchers Depot', 'Arvin Lim', '0918-777-6655'),
    ('Northwind Meat Supply', 'Hannah Bautista', '0935-999-1234'),
    ('Ocean Crest Seafood', 'Ralph Go', '0927-888-9911'),
    ('Golden Sun Oil Mills', 'Donna Fajardo', '0917-444-3322'),
    ('Farm2Table Distribution', 'Kenji Robles', '0947-666-5542'),
    ('PorkHaus Premium Meats', 'Patricia Ong', '0919-123-4588'),
    ('Mabuhay Baking Supply Co.', 'Ramon Villareal', '0926-111-9331'),
    ('Asian Harvest Rice Mills', 'Maria Celestino', '0922-333-7712'),
    ('FreshWave Cold Storage', 'Jonas Alonzo', '0995-777-4411'),
    ('Tropical Fruits Distributors', 'Melanie Cruz', '0918-555-6622');


-- PURCHASES
INSERT INTO purchases (order_date, receive_date, total_cost, supplier_id)
VALUES
    ('2025-11-01', '2025-11-03', 1000.00, 1),
    ('2025-11-02', '2025-11-04', 1000.00, 2),
    ('2025-11-05', '2025-11-06', 3000.00, 3),
    ('2025-10-31', '2025-11-07', 1000.00, 4),
    ('2025-10-30', '2025-11-09', 2000.00, 5),
    ('2025-10-25', '2025-11-03', 6000.00, 6),
    ('2025-10-21', '2025-11-07', 8000.00, 7),
    ('2025-10-12', '2025-11-11', 4700.00, 8),
    ('2025-10-29', '2025-11-10', 9800.00, 9),
    ('2025-10-27', '2025-11-01', 1200.00, 10);

-- PURCHASE LINE (link purchases to items + inventory)
INSERT INTO purchase_line (quantity, unit_cost, purchase_id, item_id, inventory_id)
VALUES
    (20,  50.00,  1,  1,  1),
    (10, 100.00,  2,  2,  2),
    (30,  30.00,  3,  3,  3),
    (15, 200.00,  4,  4,  4),
    (25,  48.00,  5,  5,  5),
    (8,  120.00,  6,  6,  6),
    (50,  25.00,  7,  7,  7),
    (10, 210.00,  8,  8,  8),
    (15,  55.00,  9,  9,  9),
    (5,   95.00, 10, 10, 10),
    (
;

-- SUPPLIER PRODUCTS (price lists)
INSERT INTO supplier_products (amount, unit_cost, supplier_id, item_id)
VALUES
    (100,  50.00, 1, 1),
    (200, 100.00, 2, 2),
    (500,  30.00, 3, 3),
    (50,  200.00, 4, 4),
    (80,   48.00, 5, 5),
    (300,  28.00, 6, 6),
    (400,  32.00, 7, 7),
    (100, 190.00, 8, 8),
    (150,  60.00, 9, 9),
    (120, 110.00, 10,10),
    (94, 30.00, 11, 11),
    (197, 45.00, 12, 12),
    (182, 60.00, 13, 13),
    (20, 80.00, 14, 14),
    (173, 50.00, 15, 15),
    (105, 20.00, 16, 16),
    (14, 80.00, 17, 17),
    (200, 110.00, 18, 18),
    (176, 40.00, 19, 19),
    (122, 35.00, 20, 20),
    (100,  50.00, 4, 1),
    (200, 100.00, 4, 2),
    (500,  30.00, 4, 3),
    (50,  200.00, 4, 4),
    (80,   48.00, 4, 5);

-- STOCK MOVEMENT (ledger: restock / consumption / disposal)
INSERT INTO stock_movement (quantity, transaction_type, item_id, location_id, inventory_id)
VALUES
    (20, 'RESTOCK',      1, 1, 1),   -- tomato restock in Main Kitchen
    (5,  'CONSUMPTION',  2, 2, 2),   -- cooking oil used
    (10, 'RESTOCK',      3, 2, 3),   -- rice restock
    (3,  'CONSUMPTION',  5, 4, 5),   -- salt used
    (50, 'RESTOCK',      6, 5, 6),   -- garlic restock
    (20, 'CONSUMPTION',  7, 6, 7),   -- curry used
    (15, 'RESTOCK',      4, 3, 4),   -- beef restock
    (10, 'CONSUMPTION',  8, 7, 8),   -- fish used
    (30, 'CONSUMPTION',  9, 8, 9),   -- soy sauce used
    (2,  'DISPOSAL',    10, 9,10);   -- butter disposed (spoilage)
