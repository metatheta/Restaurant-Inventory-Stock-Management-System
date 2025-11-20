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
DROP TABLE IF EXISTS disposed_items;
DROP TABLE IF EXISTS dish_requirements;
DROP TABLE IF EXISTS supplier_products;
DROP TABLE IF EXISTS item_restocks;
DROP TABLE IF EXISTS inventory;
DROP TABLE IF EXISTS dish_consumption;
DROP TABLE IF EXISTS stock_locations;
DROP TABLE IF EXISTS product_registry_history;
DROP TABLE IF EXISTS suppliers;
DROP TABLE IF EXISTS stock_items;
DROP TABLE IF EXISTS dishes;

-- #####################
-- # create the tables #
-- #####################

CREATE TABLE stock_items(
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    item_name VARCHAR(30) NOT NULL,
    unit_of_measure VARCHAR(30) NOT NULL, -- kg, liter, pcs, etc.
    category VARCHAR(30) NOT NULL, -- vegetable, fat, grain, etc.
    visible TINYINT(1) DEFAULT 1
);

CREATE TABLE stock_locations(
    location_id INT AUTO_INCREMENT PRIMARY KEY,
    storage_name VARCHAR(30) NOT NULL, -- e.g., Main Kitchen, Freezer Room
    address VARCHAR(100), -- e.g., Batman, Turkey; Sesame Street; Crapstone, England
    storage_type VARCHAR(15), -- e.g., Sack, Barrel, Tupperware, Ziplock
    visible TINYINT(1) DEFAULT 1
);

CREATE TABLE suppliers(
    supplier_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    contact_person VARCHAR(30) NOT NULL,
    contact_info VARCHAR(30) NOT NULL,
    visible TINYINT(1) DEFAULT 1
);

CREATE TABLE inventory(
    inventory_id INT AUTO_INCREMENT PRIMARY KEY,
    running_balance DECIMAL(12,3) NOT NULL CHECK (running_balance >= 0),
    last_restock_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expiry_date TIMESTAMP NULL, -- NULL for non-perishables
    location_id INT NOT NULL,
    item_id INT NOT NULL,
    visible TINYINT(1) DEFAULT 1,
    FOREIGN KEY (location_id) REFERENCES stock_locations (location_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES stock_items (item_id) ON DELETE CASCADE
);

CREATE TABLE supplier_products(
    unit_cost DECIMAL(10,2) NOT NULL CHECK (unit_cost >= 0),
    supplier_id INT NOT NULL,
    item_id INT NOT NULL,
    visible TINYINT(1) DEFAULT 1,
    FOREIGN KEY (supplier_id) REFERENCES suppliers (supplier_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES stock_items (item_id) ON DELETE CASCADE,
    PRIMARY KEY(supplier_id, item_id)
);

CREATE TABLE dishes (
    dish_id INT AUTO_INCREMENT PRIMARY KEY,
    dish_name VARCHAR(50) NOT NULL,
    visible TINYINT(1) DEFAULT 1
);

CREATE TABLE dish_requirements (
    dish_id INT NOT NULL,
    item_id INT NOT NULL,
    quantity DECIMAL(12,3) NOT NULL CHECK (quantity > 0),
    visible TINYINT(1) DEFAULT 1,
    PRIMARY KEY (dish_id, item_id),
    FOREIGN KEY (dish_id) REFERENCES dishes(dish_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES stock_items(item_id) ON DELETE CASCADE
);

CREATE TABLE disposed_items (
    disposed_id INT AUTO_INCREMENT PRIMARY KEY,
    quantity_disposed DECIMAL(12,3) NOT NULL CHECK (quantity_disposed >= 0),
    disposed_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    inventory_id INT NOT NULL,
    visible TINYINT(1) DEFAULT 1,
    should_update_inventory TINYINT(1) DEFAULT 1,
    FOREIGN KEY (inventory_id) REFERENCES inventory (inventory_id) ON DELETE CASCADE
);

CREATE TABLE item_restocks (
    restock_id INT AUTO_INCREMENT PRIMARY KEY,
    item_id INT NOT NULL,
    inventory_id INT NOT NULL,
    supplier_id INT NOT NULL, 
    item_name VARCHAR(50),
    supplier_name VARCHAR(50),
    cost_per_unit DECIMAL(10, 2),
    quantity INT,
    total_cost DECIMAL(10, 2),
    storage_location VARCHAR(50),
    address VARCHAR(100),
    restocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    visible TINYINT(1) DEFAULT 1,
    FOREIGN KEY (item_id) REFERENCES stock_items(item_id) ON DELETE CASCADE,
	FOREIGN KEY (inventory_id) REFERENCES inventory (inventory_id) ON DELETE CASCADE,
    FOREIGN KEY (supplier_id) REFERENCES suppliers (supplier_id) ON DELETE CASCADE
);

CREATE TABLE dish_consumption(
    consumption_id INT AUTO_INCREMENT PRIMARY KEY,
    dish_id INT NOT NULL,
    servings INT NOT NULL CHECK (servings > 0),
    consumed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    location_id INT NOT NULL,
    visible TINYINT(1) DEFAULT 1,
    FOREIGN KEY (dish_id) REFERENCES dishes(dish_id) ON DELETE CASCADE,
    FOREIGN KEY (location_id) REFERENCES stock_locations(location_id) ON DELETE CASCADE
);

CREATE TABLE product_registry_history (
    prh_id INT AUTO_INCREMENT PRIMARY KEY,
    supplier_id INT NOT NULL,
    item_id INT NOT NULL,
    updated_cost DECIMAL(10,2) CHECK (updated_cost >= 0),
    category ENUM('change', 'deletion', 'addition'),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    visible TINYINT(1) DEFAULT 1,
    FOREIGN KEY (item_id) REFERENCES stock_items(item_id) ON DELETE CASCADE,
    FOREIGN KEY (supplier_id) REFERENCES suppliers (supplier_id) ON DELETE CASCADE
);
    
-- STOCK ITEMS
INSERT INTO stock_items (item_name, unit_of_measure, category)
VALUES
    ('tomato', 'kg', 'vegetable'),
    ('cooking oil', 'liter', 'fat'),
    ('rice', 'kg', 'grain'),
    ('beef', 'kg', 'protein'),
    ('salt', 'kg', 'condiment'),
    ('garlic', 'kg', 'vegetable'),
    ('curry', 'kg', 'condiment'),
    ('fish', 'kg', 'protein'),
    ('soy sauce', 'liter', 'condiment'),
    ('butter', 'kg', 'dairy'),
    ('onion', 'kg', 'vegetable'),
    ('potato', 'kg', 'vegetable'),
    ('chicken', 'kg', 'protein'),
    ('pork', 'kg', 'protein'),
    ('eggs', 'kg', 'protein'),
    ('cheese', 'kg', 'dairy');

-- SUPPLIERS
INSERT INTO suppliers (name, contact_person, contact_info)
VALUES
    ('FreshHarvest Produce Co.', 'Ana Dizon', '0917-123-4567'),
    ('Golden Sun Oil Mills', 'Marco Reyes', '0998-456-1234'),
    ('Sunrise Grains Trading', 'Liza Santos', '0928-344-5566'),
    ('GreenLeaf Organic Butchery', 'Julian Mercado', '0917-555-3322'),
    ('Pacific Seas Inc.', 'Paolo Dominguez', '0935-888-2219'),
    ('Vampire Free Garden', 'Rica Villanueva', '0917-666-7788'),
    ('Manila Spice Merchants', 'Noel Chua', '0995-123-4888'),
    ('West Philippine Fisheries', 'Steph Uy', '0918-111-2223'),
    ('Bean Trading Co.', 'Gabriel Tan', '0947-555-6621'),
    ('Churned and Packed Depot', 'Celine Ramos', '0922-333-4455');

INSERT INTO supplier_products (unit_cost, supplier_id, item_id)
VALUES
    (50.00, 1, 1), -- tomato
    (30.00, 2, 2), -- oil
    (30.00, 3, 3), -- rice
    (200.00, 4, 4), -- beef
    (48.00, 5, 5), -- salt
    (28.00, 6, 6), -- garlic
    (32.00, 7, 7), -- curry
    (180.00, 8, 8), -- fish
    (110.00, 9, 9),-- soy sauce
    (250.00, 10, 10), -- butter
    (20.00, 1, 11),  -- onion
    (30, 1, 12), -- potato
    (50, 4, 13), -- chicken
    (175, 4, 14), -- pork
    (30, 3, 15), -- eggs
    (100, 10, 16); -- cheese

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
    ('Walk-in Freezer', '101 Mango St., Manila', 'Box'),
    ('Upper Shelves', '101 Mango St., Manila', 'Ziplock'),
    ('Pantry', '101 Mango St., Manila', 'Airlock');

-- INVENTORY
INSERT INTO inventory (running_balance, last_restock_date, expiry_date, location_id, item_id)
VALUES
    (50, '2025-11-01 08:00:00', '2025-11-10', 3, 1),   -- tomato in Main Kitchen
    (30, '2025-11-02 09:00:00', '2025-11-11', 1, 2),   -- cooking oil in Lower Shelves
    (100, '2025-11-03 10:00:00', '2025-11-13', 7, 3),  -- rice in Pantry
    (20, '2025-11-04 11:00:00', '2025-11-14', 2, 4),   -- beef in Walk-in Freezer
    (15, '2025-11-05 12:00:00', '2025-11-15', 4, 5),   -- salt in Main Kitchen
    (40, '2025-11-06 13:00:00', '2025-11-16', 6, 6),   -- garlic in Refrigerator
    (25, '2025-11-07 14:00:00', '2025-12-30', 9, 7),   -- curry in Upper Shelves
    (10, '2025-11-08 15:00:00', '2025-11-28', 5, 8),   -- fish in Walk-in Freezer
    (30, '2025-11-09 16:00:00', '2025-11-25', 1, 9),   -- soy sauce in Lower Shelves
    (12, '2025-11-10 17:00:00', '2025-11-30', 6, 10), -- butter in Refrigerator
    (18, '2025-11-11 08:00:00', '2025-11-19', 10, 11), -- onion in Refrigerator
    (40, '2025-11-12 09:00:00', '2025-11-18', 7, 12),  -- potato in Pantry
    (10, '2025-11-12 09:00:00', '2025-11-29', 8, 13), -- chicken in Freezer
    (10, '2025-11-12 09:00:00', '2025-11-29', 2, 14), -- pork in Freezer
    (20, '2025-11-13 09:00:00', '2025-12-01', 9, 15), -- eggs in Upper Shelves
    (500, '2025-11-13 09:00:00', '2026-4-4', 7, 16); -- cheese in Pantry

-- DISHES (for dish-based consumption transaction)
INSERT INTO dishes (dish_name)
VALUES
    ('Beef Curry'),
    ('Garlic Rice'),
    ('Ragu Sauce'),
    ('Fried Garlic Butter Chicken'),
    ('Fries'),
    ('Katsudon'),
    ('Hashbrown'),
    ('Butter Chicken'),
    ('Braised Fish'),
    ('Omelet');

-- DISH REQUIREMENTS (recipe: item + quantity per dish)
-- Note: item_id values correspond to the stock_items inserted above.
INSERT INTO dish_requirements (dish_id, item_id, quantity)
VALUES
    (1, 4, 0.500),   -- Beef Curry: 0.5 kg beef
    (1, 7, 0.100),   -- Beef Curry: 0.1 kg curry
    (1, 5, 0.010),   -- Beef Curry: 0.01 kg salt
    (2, 3, 0.200),   -- Garlic Rice: 0.2 kg rice
    (2, 6, 0.050),   -- Garlic Rice: 0.05 kg garlic
    (2, 5, 0.005),   -- Garlic Rice: 0.005 kg salt
    (3, 1, 0.250),   -- Ragu Sauce: 0.25 kg tomato
    (3, 4, 0.500),   -- Ragu Sauce: 0.5 kg beef
    (3, 11, 0.150),  -- Ragu Sauce: 0.15 kg onion
    (4, 13, 0.250),  -- Garlic Butter Chicken: 0.5 kg chicken
    (4, 6, 0.150),   -- Garlic Butter Chicken: 0.15 kg garlic
    (4, 10, 0.250),  -- Garlic Butter Chicken: 0.25 kg butter
    (5, 12, 0.250),  -- Fries: 0.25 kg potato
    (5, 2, 0.500),   -- Fries: 0.5 oil
    (5, 5, 0.100),   -- Fries: 0.1 salt
    (6 , 14, 0.500), -- Katsudon: 0.5 kg pork
    (6 , 15, 0.150), -- Katsudon: 0.15 kg eggs
    (6 , 9, 0.150),  -- Katsudon: 0.15 liter soy sauce
    (7, 12, 0.500),  -- Hashbrown: 0.5 kg potato
    (7, 2, 0.150),   -- Hashbrown: 0.15 liter oil
    (7, 15, 0.150),  -- Hashbrown: 0.15 kg eggs
    (8, 1, 0.250),   -- Butter Chicken: 0.25 kg tomato
    (8, 7, 0.100),   -- Butter Chicken: 0.1 kg butter
    (8, 13, 0.500),  -- Butter Chicken: 0.5 kg chicken
    (9, 8, 0.500), -- Braised Fish: 0.5 kg fish
    (9, 9, 0.150), -- Braised Fish: 0.15 liter soy sauce
    (9, 3, 0.250), -- Braised Fish: 0.25 kg rice
    (10, 16, 0.250), -- Omelet: 0.25 kg cheese
    (10, 15, 0.500), -- Omelet: 0.5 kg egg
    (10, 2, 0.150); -- Omelet: 0.15 liter oil

-- DISPOSED ITEMS TRANSACTION
INSERT INTO disposed_items (quantity_disposed, disposed_date, inventory_id)
VALUES
    (7.000, '2025-11-10', 1),
    (1.200, '2025-11-13', 1),
    (1.800, '2025-10-03', 2),
    (4.000, '2025-09-23', 2),
    (1.900, '2025-08-07', 3),
    (3.100, '2024-11-10', 3),
    (5.200, '2025-05-23', 4),
    (5.100, '2025-03-14', 4),
    (1.900, '2025-06-17', 5),
    (2.300, '2025-08-19', 5),
    (8.000, '2025-11-23', 6),
    (4.100, '2025-10-10', 6),
    (6.700, '2025-10-11', 7),
    (4.300, '2025-10-13', 7),
    (1.100, '2025-10-15', 8),
    (1.200, '2025-10-18', 8),
    (1.000, '2025-10-19', 9),
    (3.000, '2025-10-23', 9),
    (5.000, '2025-10-24', 10),
    (2.000, '2025-10-27', 10),
    (4.000, '2025-10-29', 11),
    (5.300, '2025-11-01', 11),
    (2.000, '2025-11-03', 12),
    (1.200, '2025-11-05', 12),
    (6.500, '2025-11-07', 13),
    (1.200, '2025-11-09', 13),
    (4.500, '2025-11-11', 14),
    (2.300, '2025-11-13', 14),
    (3.400, '2025-11-15', 15),
    (1.900, '2025-11-17', 15),
    (1.900, '2025-11-19', 16),
    (3.200, '2025-11-21', 16);

-- ITEM RESTOCKS TRANSACTION
INSERT INTO item_restocks
(item_id, inventory_id, supplier_id, item_name, supplier_name, cost_per_unit, quantity,
total_cost, storage_location, address, restocked_at)
VALUES
(1, 1, 1, 'tomato', 'FreshHarvest Produce Co.', 50.00, 8, 400.00, 'Main Kitchen', 'Taft Ave., Manila', '2025-10-01 17:00:00'),
(1, 1, 1, 'tomato', 'FreshHarvest Produce Co.', 50.00, 13, 650.00, 'Main Kitchen', 'Taft Ave., Manila', '2025-11-01 23:00:00'),
(11, 11, 1, 'onion', 'FreshHarvest Produce Co.', 20.00, 18, 360.00, 'Pantry', '101 Mango St., Manila', '2025-10-02 08:00:00'),
(11, 11, 1, 'onion', 'FreshHarvest Produce Co.', 20.00, 13, 260.00, 'Pantry', '101 Mango St., Manila', '2025-11-02 03:00:00'),
(12, 12, 1, 'potato', 'FreshHarvest Produce Co.', 30.00, 20, 600.00, 'Pantry', '7 Pine Lane, Davao', '2025-10-03 22:00:00'),
(12, 12, 1, 'potato', 'FreshHarvest Produce Co.', 30.00, 6, 180.00, 'Pantry', '7 Pine Lane, Davao', '2025-11-03 19:00:00'),
(2, 2, 2, 'cooking oil', 'Golden Sun Oil Mills', 30.00, 10, 300.00, 'Lower Shelves', 'Taft Ave., Manila', '2025-10-04 06:00:00'),
(2, 2, 2, 'cooking oil', 'Golden Sun Oil Mills', 30.00, 19, 570.00, 'Lower Shelves', 'Taft Ave., Manila', '2025-11-04 07:00:00'),
(3, 3, 3, 'rice', 'Sunrise Grains Trading', 30.00, 4, 120.00, 'Pantry', '7 Pine Lane, Davao', '2025-10-05 10:00:00'),
(3, 3, 3, 'rice', 'Sunrise Grains Trading', 30.00, 7, 210.00, 'Pantry', '7 Pine Lane, Davao', '2025-11-05 20:00:00'),
(15, 15, 3, 'eggs', 'Sunrise Grains Trading', 30.00, 20, 600.00, 'Upper Shelves', '101 Mango St., Manila', '2025-10-06 09:00:00'),
(15, 15, 3, 'eggs', 'Sunrise Grains Trading', 30.00, 14, 420.00, 'Upper Shelves', '101 Mango St., Manila', '2025-11-06 04:00:00'),
(4, 4, 4, 'beef', 'GreenLeaf Organic Butchery', 200.00, 15, 3000.00, 'Walk-in Freezer', 'Taft Ave., Manila', '2025-10-07 06:00:00'),
(4, 4, 4, 'beef', 'GreenLeaf Organic Butchery', 200.00, 7, 1400.00, 'Walk-in Freezer', 'Taft Ave., Manila', '2025-11-07 13:00:00'),
(13, 13, 4, 'chicken', 'GreenLeaf Organic Butchery', 50.00, 19, 950.00, 'Walk-in Freezer', '101 Mango St., Manila', '2025-10-08 00:00:00'),
(13, 13, 4, 'chicken', 'GreenLeaf Organic Butchery', 50.00, 5, 250.00, 'Walk-in Freezer', '101 Mango St., Manila', '2025-11-08 17:00:00'),
(14, 14, 4, 'pork', 'GreenLeaf Organic Butchery', 175.00, 8, 1400.00, 'Walk-in Freezer', 'Taft Ave., Manila', '2025-10-09 03:00:00'),
(14, 14, 4, 'pork', 'GreenLeaf Organic Butchery', 175.00, 7, 1225.00, 'Walk-in Freezer', 'Taft Ave., Manila', '2025-11-09 15:00:00'),
(5, 5, 5, 'salt', 'Pacific Seas Inc.', 48.00, 10, 480.00, 'Main Kitchen', '7 Pine Lane, Davao', '2025-10-10 20:00:00'),
(5, 5, 5, 'salt', 'Pacific Seas Inc.', 48.00, 13, 624.00, 'Main Kitchen', '7 Pine Lane, Davao', '2025-11-10 03:00:00'),
(6, 6, 6, 'garlic', 'Vampire Free Garden', 28.00, 4, 112.00, 'Refridgerator', '7 Pine Lane, Davao', '2025-10-11 19:00:00'),
(6, 6, 6, 'garlic', 'Vampire Free Garden', 28.00, 14, 392.00, 'Refridgerator', '7 Pine Lane, Davao', '2025-11-11 17:00:00'),
(7, 7, 7, 'curry', 'Manila Spice Merchants', 32.00, 18, 576.00, 'Upper Shelves', '101 Mango St., Manila', '2025-10-12 17:00:00'),
(7, 7, 7, 'curry', 'Manila Spice Merchants', 32.00, 16, 512.00, 'Upper Shelves', '101 Mango St., Manila', '2025-11-12 10:00:00'),
(8, 8, 8, 'fish', 'West Philippine Fisheries', 180.00, 14, 2520.00, 'Walk-in Freezer', '7 Pine Lane, Davao', '2025-10-13 03:00:00'),
(8, 8, 8, 'fish', 'West Philippine Fisheries', 180.00, 7, 1260.00, 'Walk-in Freezer', '7 Pine Lane, Davao', '2025-11-13 03:00:00'),
(9, 9, 9, 'soy sauce', 'Bean Trading Co.', 110.00, 12, 1320.00, 'Lower Shelves', 'Taft Ave., Manila', '2025-10-14 22:00:00'),
(9, 9, 9, 'soy sauce', 'Bean Trading Co.', 110.00, 6, 660.00, 'Lower Shelves', 'Taft Ave., Manila', '2025-11-14 09:00:00'),
(10, 10, 10, 'butter', 'Churned and Packed Depot', 250.00, 18, 4500.00, 'Refridgerator', '7 Pine Lane, Davao', '2025-10-15 03:00:00'),
(10, 10, 10, 'butter', 'Churned and Packed Depot', 250.00, 11, 2750.00, 'Refridgerator', '7 Pine Lane, Davao', '2025-11-15 05:00:00'),
(16, 16, 10, 'cheese', 'Churned and Packed Depot', 100.00, 13, 1300.00, 'Pantry', '7 Pine Lane, Davao', '2025-10-16 06:00:00'),
(16, 16, 10, 'cheese', 'Churned and Packed Depot', 100.00, 17, 1700.00, 'Pantry', '7 Pine Lane, Davao', '2025-11-16 15:00:00');

-- DISH CONSUMPTION TRANSACTION
INSERT INTO dish_consumption (dish_id, servings, consumed_at, location_id)
VALUES
	(1, 7, '2025-10-01 17:00:00', 2),
    (1, 3, '2025-11-01 16:00:00', 2),
    (2, 4, '2025-10-02 15:00:00', 7),
    (2, 6, '2025-11-02 14:00:00', 7),
    (3, 5, '2025-10-03 13:00:00', 3),
    (3, 8, '2025-11-03 12:00:00', 3),
    (4, 7, '2025-10-04 11:00:00', 6),
    (4, 2, '2025-11-04 10:00:00', 6),
    (5, 1, '2025-10-05 09:00:00', 1),
	(5, 4, '2025-11-05 08:00:00', 1),
    (6, 9, '2025-10-06 07:00:00', 1),
    (6, 3, '2025-11-06 20:00:00', 1),
    (7, 4, '2025-10-07 19:00:00', 1),
    (7, 5, '2025-11-07 18:00:00', 1),
    (8, 6, '2025-10-08 17:00:00', 3),
    (8, 2, '2025-11-08 16:00:00', 3),
    (9, 3, '2025-10-09 15:00:00', 7),
    (9, 8, '2025-11-09 14:00:00', 7),
    (10, 3, '2025-10-10 13:00:00', 1),
    (10, 7, '2025-11-10 12:00:00', 1);

-- PRODUCT REGISTRY HISTORY TRANSACTION
INSERT INTO product_registry_history (supplier_id, item_id, updated_cost, category, changed_at)
VALUES
	(1, 1, 40.00, 'addition', '2025-10-01 08:00:00'),
	(1, 1, 50.00, 'change', '2025-10-02 09:00:00'),
    (1, 11, 30.00, 'addition', '2025-10-03 10:00:00'),
    (1, 11, NULL, 'deletion', '2025-10-04 11:00:00'),
    (1, 11, 20.00, 'addition', '2025-10-05 12:00:00'),
    (1, 12, 40.00, 'addition', '2025-10-06 13:00:00'),
    (1, 12, 30.00, 'change', '2025-10-07 14:00:00'),
    (2, 2, 25.00, 'addition', '2025-10-08 15:00:00'),
    (2, 2, NULL, 'deletion', '2025-10-09 16:00:00'),
    (2, 2, 30.00, 'addition', '2025-10-10 17:00:00'),
    (3, 3, 35.00, 'addition', '2025-10-11 18:00:00'),
    (3, 3, 30.00, 'change', '2025-10-12 08:00:00'),
    (3, 15, 50.00, 'addition', '2025-10-13 09:00:00'),
    (3, 15, NULL, 'deletion', '2025-10-14 10:00:00'),
    (3, 15, 30.00, 'addition', '2025-10-15 11:00:00'),
    (4, 4, 220.00, 'addition', '2025-10-16 12:00:00'),
    (4, 4, 200.00, 'change', '2025-10-17 13:00:00'),
    (4, 13, 60.00, 'addition', '2025-10-18 14:00:00'),
    (4, 13, NULL, 'deletion', '2025-10-19 15:00:00'),
    (4, 13, 50.00, 'addition', '2025-10-20 16:00:00'),
    (4, 14, 155.00, 'addition', '2025-11-01 17:00:00'),
    (4, 14, 175.00, 'change', '2025-11-02 18:00:00'),
    (5, 5, 38.00, 'addition', '2025-11-03 08:00:00'),
    (5, 5, NULL, 'deletion', '2025-11-04 09:00:00'),
    (5, 5, 48.00, 'addition', '2025-11-05 10:00:00'),
    (6, 6, 35.00, 'addition', '2025-11-06 11:00:00'),
    (6, 6, 28.00, 'change', '2025-11-07 12:00:00'),
    (7, 7, 42.00, 'addition', '2025-11-08 13:00:00'),
    (7, 7, NULL, 'deletion', '2025-11-09 14:00:00'),
    (7, 7, 32.00, 'addition', '2025-11-10 15:00:00'),
    (8, 8, 160.00, 'addition', '2025-11-11 16:00:00'),
    (8, 8, 180.00, 'change', '2025-11-12 17:00:00'),
    (9, 9, 100.00, 'addition', '2025-11-13 18:00:00'),
    (9, 9, NULL, 'deletion', '2025-11-14 08:00:00'),
    (9, 9, 110.00, 'addition', '2025-11-15 09:00:00'),
    (10, 10, 275.00, 'addition', '2025-11-16 10:00:00'),
    (10, 10, 250.00, 'change', '2025-11-17 11:00:00'),
    (10, 16, 115.00, 'addition', '2025-11-18 12:00:00'),
    (10, 16, NULL, 'deletion', '2025-11-19 13:00:00'),
    (10, 16, 100.00, 'addition', '2025-11-20 14:00:00');

DELIMITER $$

CREATE TRIGGER cascade_hide_item_inventory
AFTER UPDATE ON stock_items
FOR EACH ROW
BEGIN
    IF NEW.visible = 0 AND OLD.visible = 1 THEN
        UPDATE inventory
        SET visible = 0
        WHERE item_id = NEW.item_id;
    END IF;
END$$

DELIMITER $$
CREATE TRIGGER cascade_hide_location_inventory
AFTER UPDATE ON stock_locations
FOR EACH ROW
BEGIN
    IF NEW.visible = 0 AND OLD.visible = 1 THEN
        UPDATE inventory
        SET visible = 0
        WHERE location_id = NEW.location_id;
    END IF;
END$$

DELIMITER ;

-- These two hide the rows in inventory if a location or stock_item is set to invisible because
-- inventory depends on both stock_items and stock_locations