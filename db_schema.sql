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
('butter',      'kg',    'fat');

-- STOCK LOCATIONS
INSERT INTO stock_locations (storage_name, address, storage_type)
VALUES
('Main Kitchen', 'Batman, Turkey', 'Sack'),
('Main Kitchen', 'Sesame Street', 'Barrel'),
('Freezer Room', 'Sesame Street', 'Sack'),
('Cabinet', 'Batman, Turkey', 'Ziplock'),
('Lower Shelves', 'Crapstone, England', 'Tupperware'),
('Upper Shelves', 'Sesame Street', 'Sack'),
('Lower Shelves', 'Batman, Turkey', 'Tupperware'),
('Freezer Room', 'Crapstone, England', 'Ziplock'),
('Lower Shelves', 'Crapstone, England', 'Jug'),
('Refridgerator', 'Batman, Turkey', 'Tupperware'),
('Main Kitchen', 'Batman, Turkey', 'Sack');

-- SUPPLIERS
INSERT INTO suppliers (name, contact_person, contact_info)
VALUES
('FreshHarvest Co.',       'Ana Santos',        '09171234567'),
('Metro Food Supply',      'Carlos Tan',        '09179876543'),
('AgriPro Distributor',    'Liza Cruz',         '09172345678'),
('From The Backyard Inc.', 'Thomas Tiam-Lee',   '09563461648'),
('Orchard Place',          'Austin Fernandez',  '0945825243'),
('Vineyard Valley',        'Roger Uy',          '09874853264'),
('Grow A Garden',          'Josh Nacasabog',    '09789562341'),
('Aiden Tan Kitchen',      'Uncle Roger',       '09786532154'),
('Coffee Tea Co.',         'Elmar Francisco',   '09784512326'),
('Rice Lab Enterprise',    'Gordon Ramsay',     '09789865452');

-- PURCHASES
INSERT INTO purchases (order_date, receive_date, total_cost, supplier_id)
VALUES
('2025-11-01', '2025-11-03', 5000.00, 1),
('2025-11-02', '2025-11-04', 2500.00, 2),
('2025-11-05', '2025-11-06', 3000.00, 3),
('2025-10-31', '2025-11-07', 1000.00, 4),
('2025-10-30', '2025-11-09', 2000.00, 5),
('2025-10-25', '2025-11-03', 6000.00, 6),
('2025-10-21', '2025-11-07', 8000.00, 7),
('2025-10-12', '2025-11-11', 4700.00, 8),
('2025-10-29', '2025-11-10', 9800.00, 9),
('2025-10-27', '2025-11-01', 1200.00, 10);

-- INVENTORY (per item per location)
INSERT INTO inventory (running_balance, location_id, item_id)
VALUES
(50, 1, 1),
(30, 2, 2),
(100,2, 3),
(20, 3, 4),
(70, 4, 5),
(12, 5, 6),
(30, 6, 7),
(68, 7, 8),
(120,8, 9),
(43, 9,10);

-- PURCHASE LINE (link purchases to items + inventory)
INSERT INTO purchase_line (quantity, unit_cost, purchase_id, item_id, inventory_id)
VALUES
(20,  50.00,  1,  1,  1),
(10, 100.00,  2,  2,  2),
(30,  30.00,  3,  3,  3),
(15, 200.00,  4,  4,  4),
(25,  45.00,  5,  5,  5),
(8,  120.00,  6,  6,  6),
(50,  25.00,  7,  7,  7),
(10, 210.00,  8,  8,  8),
(15,  55.00,  9,  9,  9),
(5,   95.00, 10, 10, 10);

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
(120, 110.00, 10,10);

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
