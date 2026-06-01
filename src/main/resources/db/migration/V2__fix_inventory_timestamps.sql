ALTER TABLE supplysync.inventory
  ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;
ALTER TABLE supplysync.inventory
  ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
UPDATE supplysync.inventory
  SET created_at = last_updated_at WHERE created_at IS NULL;
UPDATE supplysync.inventory
  SET updated_at = last_updated_at WHERE updated_at IS NULL;
ALTER TABLE supplysync.inventory
  ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE supplysync.inventory
  ALTER COLUMN updated_at SET NOT NULL;
