/*
	Name: Comprehensive Restaurant Inventory and Stock Management System 
    Members: GENDRANO, Christian D.
			HIZON, Allen Conner C.
            MARQUEZ, Jose Miguel S.
            NICOLAS, Francis Medin N.
*/

/*
	#################
    # make database #
    #################
*/
CREATE DATABASE IF NOT EXISTS crisms_db;

/*
	#######################
    # switch to crisms_db #
    #######################
*/
USE crisms_db;

/*
	#######################
    # drop current tables #
    #######################
*/
DROP TABLE IF EXISTS supplier_products;
DROP TABLE IF EXISTS stock_movement;
DROP TABLE IF EXISTS purchase_line;
DROP TABLE IF EXISTS purchases;
DROP TABLE IF EXISTS inventory;
DROP TABLE IF EXISTS stock_items;
DROP TABLE IF EXISTS stock_locations;
DROP TABLE IF EXISTS suppliers;

/*
	#####################
    # create the tables #
	#####################
*/
CREATE TABLE stock_items(
	item_id 			int				AUTO_INCREMENT			PRIMARY KEY,
	item_name 			varchar(30)     NOT NULL,
    unit_of_measure 	varchar(30)     NOT NULL,
    category 			varchar(30)     NOT NULL
);

CREATE TABLE stock_locations(
	location_id 		int				AUTO_INCREMENT			PRIMARY KEY,
    storage_name 		varchar(30)     NOT NULL,
    address 			varchar(15),
    storage_type 		varchar(15)
);

CREATE TABLE suppliers(
	supplier_id 		int				AUTO_INCREMENT			PRIMARY KEY,
    name 				varchar(30)     NOT NULL,
    contact_person 		varchar(30)     NOT NULL,
    contact_info 		varchar(30)     NOT NULL
);

CREATE TABLE inventory(
	inventory_id 		int				AUTO_INCREMENT			PRIMARY KEY,
    running_balance 	int 		    NOT NULL				CHECK (running_balance >= 0),
    last_restock_date 	timestamp								DEFAULT CURRENT_TIMESTAMP,
    expiry_date 		timestamp								DEFAULT CURRENT_TIMESTAMP,
    location_id			int			    NOT NULL,
    item_id				int			    NOT NULL,
    FOREIGN KEY (location_id)  REFERENCES stock_locations (location_id)			ON DELETE CASCADE,
    FOREIGN KEY (item_id) 	   REFERENCES stock_items (item_id)					ON DELETE CASCADE
);

CREATE TABLE purchases(
	purchase_id 		int				AUTO_INCREMENT			PRIMARY KEY,
    order_date 			date  		    NOT NULL,
    receive_date 		date,
    total_cost 			decimal(10,2)   NOT NULL,
    supplier_id			int			    NOT NULL,
    FOREIGN KEY (supplier_id)  REFERENCES suppliers (supplier_id)				ON DELETE CASCADE
);

CREATE TABLE purchase_line(
	purchase_line_id 	int				AUTO_INCREMENT			PRIMARY KEY,
    quantity 			int 		    NOT NULL				CHECK (quantity >= 0),
    unit_cost 			decimal(10,2)   NOT NULL				CHECK (unit_cost >= 0),
    purchase_id			int			    NOT NULL,
    item_id				int			    NOT NULL,
    inventory_id		int,
    FOREIGN KEY (purchase_id)  REFERENCES purchases (purchase_id)				ON DELETE CASCADE,
    FOREIGN KEY (item_id)      REFERENCES stock_items (item_id)					ON DELETE CASCADE,
    FOREIGN KEY (inventory_id) REFERENCES inventory (inventory_id)				ON DELETE CASCADE
);

CREATE TABLE supplier_products(
	amount 				int 		    NOT NULL				CHECK (amount >= 0),
    unit_cost 			decimal(10,2)   NOT NULL				CHECK (unit_cost >= 0),
    supplier_id			int			    NOT NULL,
    item_id				int			    NOT NULL,
    FOREIGN KEY (supplier_id)  REFERENCES suppliers (supplier_id)				ON DELETE CASCADE,
    FOREIGN KEY (item_id)      REFERENCES stock_items (item_id)					ON DELETE CASCADE,
    PRIMARY KEY(supplier_id, item_id)
);

CREATE TABLE stock_movement(
	movement_id 		int				AUTO_INCREMENT			PRIMARY KEY,
    quantity 			int 		    NOT NULL				CHECK (quantity >= 0),
    moved_at 			timestamp 	    NOT NULL				DEFAULT CURRENT_TIMESTAMP,
    transaction_type 	varchar(15)     NOT NULL,
    inventory_id		int			    NOT NULL,
    FOREIGN KEY (inventory_id) REFERENCES inventory (inventory_id)				ON DELETE CASCADE
);
