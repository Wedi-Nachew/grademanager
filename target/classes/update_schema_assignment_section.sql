-- Add section column to teacher_class_subject table
ALTER TABLE teacher_class_subject ADD COLUMN IF NOT EXISTS section VARCHAR(10); 