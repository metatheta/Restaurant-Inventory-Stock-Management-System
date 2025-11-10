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
	item_id 			int,
	item_name 			varchar(30) NOT NULL,
    unit_of_measure 	varchar(30) NOT NULL,
    category 			varchar(30) NOT NULL,
    PRIMARY KEY(item_id)
    
);

CREATE TABLE stock_locations(
	location_id 		int,
    storage_name 		varchar(30)   NOT NULL,
    address 			varchar(15),
    storage_type 		varchar(15),
    PRIMARY KEY(location_id)
);

CREATE TABLE inventory(
	inventory_id 		int,
    running_balance 	int 		  NOT NULL,
    last_restock_date 	timestamp,
    expiry_date 		timestamp,
    location_id			int			  NOT NULL,
    item_id				int			  NOT NULL,
    PRIMARY KEY(inventory_id),
    FOREIGN KEY (location_id)  REFERENCES stock_locations (location_id),
    FOREIGN KEY (item_id) 	   REFERENCES stock_items (item_id)
);

CREATE TABLE suppliers(
	supplier_id 		int,
    name 				varchar(30)   NOT NULL,
    contact_person 		varchar(30)   NOT NULL,
    contact_info 		varchar(30)   NOT NULL,
    PRIMARY KEY(supplier_id)
);

CREATE TABLE purchases(
	purchase_id 		int,
    order_date 			date  		  NOT NULL,
    receive_date 		date,
    total_cost 			decimal(10,2) NOT NULL,
    supplier_id			int			  NOT NULL,
    PRIMARY KEY(purchase_id),
    FOREIGN KEY (supplier_id)  REFERENCES suppliers (supplier_id)
);

CREATE TABLE purchase_line(
	purchase_line_id 	int,
    quantity 			int 		  NOT NULL,
    unit_cost 			decimal(10,2) NOT NULL,
    purchase_id			int			  NOT NULL,
    item_id				int			  NOT NULL,
    inventory_id		int,
    PRIMARY KEY(purchase_line_id),
    FOREIGN KEY (purchase_id)  REFERENCES purchases (purchase_id),
    FOREIGN KEY (item_id)      REFERENCES stock_items (item_id),
    FOREIGN KEY (inventory_id) REFERENCES inventory (inventory_id)
);

CREATE TABLE supplier_products(
	amount 				int 		  NOT NULL,
    unit_cost 			decimal(10,2) NOT NULL,
    supplier_id			int			  NOT NULL,
    item_id				int			  NOT NULL,
    FOREIGN KEY (supplier_id)  REFERENCES suppliers (supplier_id),
    FOREIGN KEY (item_id)      REFERENCES stock_items (item_id),
    PRIMARY KEY(supplier_id, item_id)
);

CREATE TABLE stock_movement(
	movement_id 		int,
    quantity 			int 		  NOT NULL,
    moved_at 			timestamp 	  NOT NULL,
    transaction_type 	varchar(15)   NOT NULL,
    inventory_id		int			  NOT NULL,
    PRIMARY KEY(movement_id),
    FOREIGN KEY (inventory_id) REFERENCES inventory (inventory_id)
);
