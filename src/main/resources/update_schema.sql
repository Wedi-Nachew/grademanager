-- Add section column to student table
ALTER TABLE student ADD COLUMN IF NOT EXISTS section VARCHAR(10); 