-- Section 3.0: Records Management (inventory table)
-- Viewing of inventory record and the locations with the inventory item in stock
SET @desired_id := 2;
SELECT
	i.item_id,
    si.item_name,
    i.inventory_id,
    sl.location_id,
    sl.storage_name,
    sl.address,
    sl.storage_type,
    i.running_balance,
    si.unit_of_measure,
    i.expiry_date
FROM inventory i
INNER JOIN stock_locations sl ON i.location_id = sl.location_id
INNER JOIN stock_items si ON i.item_id = si.item_id
WHERE i.item_id = @desired_id;
    
    
-- Section 4.0: Transactions
-- Purchasing a new Stock Item will involve the following operations:

SET @new_item_name = "beef";
SET @new_supplier_name = "KFC";
SET @new_quantity = 3;

-- A. Creation of new stock item record for the item

-- Note: Only add if stock item doesn't exist yet (?)
-- Query that checks if stock item exists already:
SET @stock_item_exists = EXISTS (SELECT 1 FROM stock_items WHERE item_name = @new_item_name);

-- Other note: I think it's more convenient to do the IF statement in Java
--             (doesn't look like there's an IF control structure in MySQL)
    
	-- If item doesn't exist:
	SET @new_unit_of_measure = "kg"; -- prompt user for these
	SET @new_category = "protein";
	INSERT INTO stock_items (item_name, unit_of_measure, category)
	VALUES (@new_item_name, @new_unit_of_measure, @new_category);
    
    -- Regardless:
    SET @new_item_id = (SELECT item_id FROM stock_items WHERE item_name = @new_item_name);

-- B. Retrieving the supplier record for the item provider, or registering a new supplier

-- Check if the supplier exists already
SET @supplier_exists = EXISTS (SELECT 1 FROM suppliers WHERE name = @new_supplier_name);

	-- If it doesn't:
	SET @new_contact_person = "Colonel Sanders";
	SET @new_contact_info = "09123456789"; -- prompt user for these
	INSERT INTO suppliers (name, contact_person, contact_info)
	VALUES (@new_supplier_name, @new_contact_person, @new_contact_info);
    
	-- Regardless:
	SET @new_supplier_id = (SELECT supplier_id FROM suppliers WHERE name = @new_supplier_name);

-- Check if the supplier/item combination combination exists already
SET @supplier_item_exists = EXISTS (SELECT 1 FROM supplier_products WHERE
	supplier_id = @new_supplier_id AND item_id = @new_item_id);

	-- If it doesn't:
    SET @new_amount = 5; -- prompt user for these
    SET @new_unit_cost = 300;
    INSERT INTO supplier_products (supplier_id, item_id, amount, unit_cost)
    VALUES (@new_supplier_id, @new_item_id, @new_amount, @new_unit_cost);
    
    -- If it does:
    SET @new_amount = (SELECT amount FROM supplier_products WHERE
		supplier_id = @new_supplier_id AND item_id = @new_item_id);
	SET @new_unit_cost = (SELECT unit_cost FROM supplier_products WHERE
		supplier_id = @new_supplier_id AND item_id = @new_item_id);

-- C. Recording the purchase with the supplier and cost details

SET @new_total_cost = 1000; -- replace this with actual calculations for the total cost?
-- note: idk how this would work when purchasing multiple items at the same time
-- maybe just remove total_cost from purchases since it can probably be derived?

SET @new_order_date = NOW();
INSERT INTO purchases (supplier_id, order_date, total_cost)
VALUES (@new_supplier_id, @new_order_date, @new_total_cost);
-- getting the purchase id from the newly added record
SET @new_purchase_id = (SELECT purchase_id FROM purchases WHERE order_date = @new_order_date);

-- repeat the code below for each item in the purchase? (if multiple items can be purchased)
-- (accepting different values for all the variables)
INSERT INTO purchase_line (purchase_id, item_id, quantity)
VALUES (@new_purchase_id, @new_item_id, @new_quantity); -- note: assuming unit_cost is removed from here

-- D. Recording a new inventory record for the purchased item

-- When the purchase arrives
SET @new_purchase_id = 3; -- prompt the user for this
-- (maybe like a dropdown menu that checks which purchases have not been received yet, and the
-- user can select one of them?)

UPDATE purchases
SET receive_date = NOW()
WHERE purchase_id = @new_purchase_id;
-- alternatively, if we want to make it easier, we can just prompt the user for both
-- order date and receive date and then skip this step

-- Repeat the code below for each record in purchase_line linked to that purchase_id

-- @new_purchase_line_id, @new_item_id, @new_inventory_id, and @new_quantity are just the values
-- from the current record (iteration)

SET @new_location_id = 4; -- prompt the user for this (maybe also a dropdown menu showing
-- which location the user wants to add this to?)
-- what if the user wants to add to a location that doesn't exist yet? idk
SET @new_expiry_date = "2026-01-01"; -- prompt the user for this
	-- (note: if item does not expire, set this to NULL)

-- note: maybe just add to an existing record if the item/location combination already exists
--       instead of always creating a new record?
INSERT INTO inventory (item_id, running_balance, last_restock_date, expiry_date, location_id)
VALUES (@new_item_id, @new_quantity, NOW(), @new_expiry_date, @new_location_id);

-- getting the inventory id from the newly created record
-- note: this assumes the max inventory_id is always the latest (due to auto increment)
-- there might be a better way to do this
SET @new_inventory_id = (SELECT MAX(inventory_id) FROM inventory);

-- finally, updating the inventory part of purchase_line
UPDATE purchase_line
SET inventory_id = @new_inventory_id
WHERE purchase_line_id = @new_purchase_line_id;


-- Section 5.0: Reports to be Generated
-- Storage Type Distribution Report
-- (Total number and frequency of items stored) per stock location, for a given Year and Month
SET @desired_year = 2025;
SET @desired_month = 11;
SELECT
	l.location_id,
    l.storage_name,
    l.address,
	it.item_name,
	SUM(m.quantity) AS total,
    it.unit_of_measure,
    COUNT(m.quantity) AS frequency
FROM stock_locations l
INNER JOIN inventory i ON l.location_id = i.location_id
INNER JOIN stock_items it ON i.item_id = it.item_id
INNER JOIN stock_movement m ON i.inventory_id = m.movement_id
WHERE
	m.transaction_type = "RESTOCK" AND
	YEAR(m.moved_at) = @desired_year AND
	MONTH(m.moved_at) = @desired_month
GROUP BY l.location_id, it.item_id;