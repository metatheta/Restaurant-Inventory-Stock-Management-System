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
DROP TABLE IF EXISTS dishes;
DROP TABLE IF EXISTS supplier_products;
DROP TABLE IF EXISTS inventory;
DROP TABLE IF EXISTS stock_locations;
DROP TABLE IF EXISTS stock_items;
DROP TABLE IF EXISTS suppliers;
DROP TABLE IF EXISTS item_restocks;
DROP TABLE IF EXISTS dish_consumption;
DROP TABLE IF EXISTS product_registry_history;

-- #####################
-- # create the tables #
-- #####################

CREATE TABLE stock_items(
                            item_id             INT                AUTO_INCREMENT            PRIMARY KEY,
                            item_name             VARCHAR(30)     NOT NULL,
                            unit_of_measure     VARCHAR(30)     NOT NULL,    -- kg, liter, pcs, etc.
                            category             VARCHAR(30)     NOT NULL,     -- vegetable, fat, grain, etc.
                            visible                TINYINT(1)                                DEFAULT 1
);

CREATE TABLE stock_locations(
                                location_id         INT                AUTO_INCREMENT            PRIMARY KEY,
                                storage_name         VARCHAR(30)     NOT NULL,    -- e.g., Main Kitchen, Freezer Room
                                address             VARCHAR(100),                -- e.g., Batman, Turkey; Sesame Street; Crapstone, England
                                storage_type         VARCHAR(15) ,                -- e.g., Sack, Barrel, Tupperware, Ziplock
                                visible                TINYINT(1)                                DEFAULT 1
);

CREATE TABLE suppliers(
                          supplier_id         INT                AUTO_INCREMENT            PRIMARY KEY,
                          name                 VARCHAR(30)     NOT NULL,
                          contact_person     VARCHAR(30)     NOT NULL,
                          contact_info         VARCHAR(30)     NOT NULL,
                          visible            TINYINT(1)                                DEFAULT 1
);

CREATE TABLE inventory(
                          inventory_id         INT                AUTO_INCREMENT            PRIMARY KEY,
                          running_balance     DECIMAL(12,3)   NOT NULL                CHECK (running_balance >= 0),
                          last_restock_date TIMESTAMP                                DEFAULT CURRENT_TIMESTAMP,
                          expiry_date         TIMESTAMP                                NULL,   -- NULL for non-perishables
                          location_id        INT                NOT NULL,
                          item_id            INT                NOT NULL,
                          visible            TINYINT(1)                                DEFAULT 1,
                          FOREIGN KEY (location_id)  REFERENCES stock_locations (location_id)    ON DELETE CASCADE,
                          FOREIGN KEY (item_id)        REFERENCES stock_items (item_id)        ON DELETE CASCADE
);

CREATE TABLE supplier_products(
                                  amount                 int                NOT NULL                CHECK (amount >= 0),
                                  unit_cost             DECIMAL(10,2)   NOT NULL                CHECK (unit_cost >= 0),
                                  supplier_id            INT                NOT NULL,
                                  item_id                INT                NOT NULL,
                                  visible                TINYINT(1)                                DEFAULT 1,
                                  FOREIGN KEY (supplier_id)  REFERENCES suppliers (supplier_id)    ON DELETE CASCADE,
                                  FOREIGN KEY (item_id)      REFERENCES stock_items (item_id)        ON DELETE CASCADE,
                                  PRIMARY KEY(supplier_id, item_id)
);

CREATE TABLE dishes (
                        dish_id   INT AUTO_INCREMENT PRIMARY KEY,
                        dish_name VARCHAR(50) NOT NULL,
                        visible   TINYINT(1) DEFAULT 1
);

CREATE TABLE dish_requirements (
                                   dish_id   INT NOT NULL,
                                   item_id   INT NOT NULL,
                                   quantity  DECIMAL(12,3) NOT NULL CHECK (quantity > 0),
                                   visible   TINYINT(1) DEFAULT 1,
                                   PRIMARY KEY (dish_id, item_id),
                                   FOREIGN KEY (dish_id)  REFERENCES dishes(dish_id)      ON DELETE CASCADE,
                                   FOREIGN KEY (item_id)  REFERENCES stock_items(item_id) ON DELETE CASCADE
);

CREATE TABLE disposed_items(
                               disposed_id         INT                AUTO_INCREMENT            PRIMARY KEY,
                               quantity_disposed     DECIMAL(12,3)   NOT NULL                CHECK (quantity_disposed >= 0),
                               disposed_date         TIMESTAMP                                DEFAULT CURRENT_TIMESTAMP,
                               inventory_id             INT             NOT NULL,
                               visible                TINYINT(1)                                DEFAULT 1,
                               should_update_inventory    TINYINT(1)                                DEFAULT 1,
                               FOREIGN KEY (inventory_id)  REFERENCES inventory (inventory_id)    ON DELETE CASCADE
);

CREATE TABLE item_restocks (
    restock_id INT AUTO_INCREMENT PRIMARY KEY,
    item_id INT NOT NULL,
    inventory_id INT NOT NULL,
    item_name VARCHAR(50),
    supplier_name VARCHAR(50),
    cost_per_unit DECIMAL(10, 2),
    quantity INT,
    total_cost DECIMAL(10, 2),
    storage_location VARCHAR(50),
    address VARCHAR(100),
    restocked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    visible TINYINT(1) DEFAULT 1
);

CREATE TABLE dish_consumption(
    consumption_id INT AUTO_INCREMENT PRIMARY KEY,
    dish_id        INT           NOT NULL,
    servings       INT           NOT NULL CHECK (servings > 0),
    consumed_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    location_id    INT           NOT NULL,
    visible        TINYINT(1)    DEFAULT 1,
    FOREIGN KEY (dish_id)     REFERENCES dishes(dish_id)              ON DELETE CASCADE,
    FOREIGN KEY (location_id) REFERENCES stock_locations(location_id) ON DELETE CASCADE
);

CREATE TABLE product_registry_history (
    prh_id             INT             AUTO_INCREMENT     PRIMARY KEY,
    supplier_id     INT             NOT NULL,
    item_id         INT             NOT NULL,
    updated_amount     INT             CHECK (updated_amount >= 0),
    updated_cost     DECIMAL(10,2)     CHECK (updated_cost >= 0),
    category         ENUM('change', 'deletion', 'addition'),
    changed_at         TIMESTAMP         NOT NULL         DEFAULT CURRENT_TIMESTAMP,
    visible            TINYINT(1) DEFAULT 1,
    FOREIGN KEY (item_id) REFERENCES stock_items(item_id) ON DELETE CASCADE,
    FOREIGN KEY (supplier_id) REFERENCES suppliers (supplier_id) ON DELETE CASCADE
);


-- STOCK ITEMS
INSERT INTO stock_items (item_name, unit_of_measure, category)
VALUES
    ('tomato',      'kg',    'vegetable'),
    ('cooking oil', 'liter', 'fat'),
    ('rice',        'kg',    'grain'),
    ('beef',        'kg',    'protein'),
    ('salt',        'kg',    'condiment'),
    ('garlic',      'kg',    'vegetable'),
    ('curry',       'kg',    'condiment'),
    ('fish',        'kg',    'protein'),
    ('soy sauce',   'liter', 'condiment'),
    ('butter',      'kg',    'dairy'),
    ('onion',        'kg',   'vegetable'),
    ('potato',       'kg',   'vegetable'),
    ('chicken',      'kg',   'protein'),
    ('pork',         'kg',   'protein'),
    ('eggs',          'kg',      'protein'),
    ('cheese',         'kg',    'dairy');

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

INSERT INTO supplier_products (amount, unit_cost, supplier_id, item_id)
VALUES
    (100, 50.00, 1, 1), -- tomato
    (200, 30.00, 2, 2), -- oil
    (200, 30.00, 3, 3), -- rice
    (50, 200.00, 4, 4), -- beef
    (80, 48.00, 5, 5), -- salt
    (90, 28.00, 6, 6), -- garlic
    (60, 32.00, 7, 7), -- curry
    (30, 180.00, 8, 8), -- fish
    (120, 110.00, 9, 9),-- soy sauce
    (15, 250.00, 10, 10), -- butter
    (45, 20.00, 1, 11),  -- onion
    (60, 30, 1, 12), -- potato
    (150, 150, 4, 13), -- chicken
    (100, 175, 4, 14), -- pork
    (150, 30, 3, 15), -- eggs
    (500, 100, 10, 16); -- cheese

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
    (50, '2025-11-01 08:00:00', '2025-11-10', 3, 1),   -- tomato in Main Kitchen
    (30, '2025-11-02 09:00:00', '2025-11-11', 1, 2),   -- cooking oil in Lower Shelves
    (100, '2025-11-03 10:00:00', '2025-11-13', 7, 3),  -- rice in Pantry
    (20, '2025-11-04 11:00:00', '2025-11-14', 2, 4),   -- beef in Walk-in Freezer
    (15, '2025-11-05 12:00:00', '2025-11-15', 4, 5),   -- salt in Main Kitchen
    (40, '2025-11-06 13:00:00', '2025-11-16', 6, 6),   -- garlic in Refrigerator
    (25, '2025-11-07 14:00:00', '2025-12-30', 9, 7),   -- curry in Upper Shelves
    (10, '2025-11-08 15:00:00', '2025-11-28', 5, 8),   -- fish in Walk-in Freezer
    (30, '2025-11-09 16:00:00', '2025-11-25', 1, 9),   -- soy sauce in Lower Shelves
    (12, '2025-11-10 17:00:00', '2025-11-30', 6, 10), -- butter in Refrigerator
    (18, '2025-11-11 08:00:00', '2025-11-19', 10, 11), -- onion in Refrigerator
    (40, '2025-11-12 09:00:00', '2025-11-18', 7, 12),  -- potato in Pantry
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
    (1, 4, 0.500),   -- Beef Curry: 0.5 kg beef
    (1, 7, 0.100),   -- Beef Curry: 0.1 kg curry
    (1, 5, 0.010),   -- Beef Curry: 0.01 kg salt
    (2, 3, 0.200),   -- Garlic Rice: 0.2 kg rice
    (2, 6, 0.050),   -- Garlic Rice: 0.05 kg garlic
    (2, 5, 0.005),   -- Garlic Rice: 0.005 kg salt
    (3, 1, 0.250),   -- Ragu Sauce: 0.25 kg tomato
    (3, 4, 0.500),   -- Ragu Sauce: 0.5 kg beef
    (3, 11, 0.150),  -- Ragu Sauce: 0.15 kg onion
    (4, 13, 0.250),  -- Garlic Butter Chicken: 0.5 kg chicken
    (4, 6, 0.150),   -- Garlic Butter Chicken: 0.15 kg garlic
    (4, 10, 0.250),  -- Garlic Butter Chicken: 0.25 kg butter
    (5, 12, 0.250),  -- Fries: 0.25 kg potato
    (5, 2, 0.500),   -- Fries: 0.5 oil
    (5, 5, 0.100),   -- Fries: 0.1 salt
    (6 , 14, 0.500), -- Katsudon: 0.5 kg pork
    (6 , 15, 0.150), -- Katsudon: 0.15 kg eggs
    (6 , 9, 0.150),  -- Katsudon: 0.15 liter soy sauce
    (7, 12, 0.500),  -- Hashbrown: 0.5 kg potato
    (7, 2, 0.150),   -- Hashbrown: 0.15 liter oil
    (7, 15, 0.150),  -- Hashbrown: 0.15 kg eggs
    (8, 1, 0.250),   -- Butter Chicken: 0.25 kg tomato
    (8, 7, 0.100),   -- Butter Chicken: 0.1 kg butter
    (8, 13, 0.500),  -- Butter Chicken: 0.5 kg chicken
    (9, 8, 0.500), -- Braised Fish: 0.5 kg fish
    (9, 9, 0.150), -- Braised Fish: 0.15 liter soy sauce
    (9, 3, 0.250), -- Braised Fish: 0.25 kg rice
    (10, 16, 0.250), -- Omelet: 0.25 kg cheese
    (10, 15, 0.500), -- Omelet: 0.5 kg egg
    (10, 2, 0.150); -- Omelet: 0.15 liter oil

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