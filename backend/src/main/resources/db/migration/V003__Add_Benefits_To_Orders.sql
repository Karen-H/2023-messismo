-- Migration script to add benefit functionality to orders
-- This script:
-- 1. Renames points_awarded column to points_earned
-- 2. Adds points_used column for tracking benefit usage
-- 3. Adds applied_benefit_id foreign key for tracking which benefit was used

-- Step 1: Add new columns
ALTER TABLE orders ADD COLUMN points_earned DOUBLE PRECISION;
ALTER TABLE orders ADD COLUMN points_used INTEGER DEFAULT 0;
ALTER TABLE orders ADD COLUMN applied_benefit_id BIGINT;

-- Step 2: Migrate existing data (copy points_awarded to points_earned)
UPDATE orders SET points_earned = points_awarded WHERE points_awarded IS NOT NULL;
UPDATE orders SET points_earned = 0 WHERE points_earned IS NULL;

-- Step 3: Set default values for new columns on existing records
UPDATE orders SET points_used = 0 WHERE points_used IS NULL;

-- Step 4: Drop the old column
ALTER TABLE orders DROP COLUMN points_awarded;

-- Step 5: Add foreign key constraint for applied_benefit_id
ALTER TABLE orders ADD CONSTRAINT fk_orders_applied_benefit 
    FOREIGN KEY (applied_benefit_id) REFERENCES benefits(id);

-- Step 6: Add NOT NULL constraint to points_earned (after data migration)
ALTER TABLE orders ALTER COLUMN points_earned SET NOT NULL;
ALTER TABLE orders ALTER COLUMN points_used SET NOT NULL;