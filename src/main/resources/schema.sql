-- Create assessment table
CREATE TABLE IF NOT EXISTS assessment (
    assessment_id VARCHAR(50) PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    date DATE NOT NULL,
    total_marks INT NOT NULL,
    teacher_id VARCHAR(50) NOT NULL,
    assessment_type VARCHAR(20) NOT NULL,
    subject VARCHAR(100) NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    semester VARCHAR(20) NOT NULL,
    FOREIGN KEY (teacher_id) REFERENCES teacher(user_id)
);

-- Create assessment_class table for many-to-many relationship between assessments and classes
CREATE TABLE IF NOT EXISTS assessment_class (
    assessment_id VARCHAR(50),
    class_level VARCHAR(20),
    PRIMARY KEY (assessment_id, class_level),
    FOREIGN KEY (assessment_id) REFERENCES assessment(assessment_id)
);

-- Create result table
CREATE TABLE IF NOT EXISTS result (
    result_id VARCHAR(50) PRIMARY KEY,
    assessment_id VARCHAR(50) NOT NULL,
    student_id VARCHAR(50) NOT NULL,
    marks_obtained DOUBLE NOT NULL,
    FOREIGN KEY (assessment_id) REFERENCES assessment(assessment_id),
    FOREIGN KEY (student_id) REFERENCES student(student_id)
);

-- Create course table
CREATE TABLE IF NOT EXISTS course (
    course_id VARCHAR(50) PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    grade VARCHAR(10) NOT NULL,
    stream VARCHAR(20),
    language VARCHAR(20)
);

-- Insert default courses for grades 9-10
INSERT INTO course (course_id, title, grade) VALUES
('C001', 'Mathematics', '9'),
('C002', 'English', '9'),
('C003', 'Physics', '9'),
('C004', 'Chemistry', '9'),
('C005', 'Biology', '9'),
('C006', 'History', '9'),
('C007', 'Geography', '9'),
('C008', 'Mathematics', '10'),
('C009', 'English', '10'),
('C010', 'Physics', '10'),
('C011', 'Chemistry', '10'),
('C012', 'Biology', '10'),
('C013', 'History', '10'),
('C014', 'Geography', '10');

-- Insert default courses for grades 11-12 (Natural)
INSERT INTO course (course_id, title, grade, stream, language) VALUES
('C015', 'Mathematics', '11', 'Natural', NULL),
('C016', 'English', '11', 'Natural', NULL),
('C017', 'Physics', '11', 'Natural', NULL),
('C018', 'Chemistry', '11', 'Natural', NULL),
('C019', 'Biology', '11', 'Natural', NULL),
('C020', 'Mathematics', '12', 'Natural', NULL),
('C021', 'English', '12', 'Natural', NULL),
('C022', 'Physics', '12', 'Natural', NULL),
('C023', 'Chemistry', '12', 'Natural', NULL),
('C024', 'Biology', '12', 'Natural', NULL);

-- Insert default courses for grades 11-12 (Social)
INSERT INTO course (course_id, title, grade, stream, language) VALUES
('C025', 'Mathematics', '11', 'Social', NULL),
('C026', 'English', '11', 'Social', NULL),
('C027', 'History', '11', 'Social', NULL),
('C028', 'Geography', '11', 'Social', NULL),
('C029', 'Economics', '11', 'Social', NULL),
('C030', 'Mathematics', '12', 'Social', NULL),
('C031', 'English', '12', 'Social', NULL),
('C032', 'History', '12', 'Social', NULL),
('C033', 'Geography', '12', 'Social', NULL),
('C034', 'Economics', '12', 'Social', NULL);

-- Add language-specific courses for grades 11-12
INSERT INTO course (course_id, title, grade, stream, language) VALUES
('C035', 'Amharic', '11', 'Natural', 'Amharic'),
('C036', 'Tigrigna', '11', 'Natural', 'Tigrigna'),
('C037', 'Amharic', '11', 'Social', 'Amharic'),
('C038', 'Tigrigna', '11', 'Social', 'Tigrigna'),
('C039', 'Amharic', '12', 'Natural', 'Amharic'),
('C040', 'Tigrigna', '12', 'Natural', 'Tigrigna'),
('C041', 'Amharic', '12', 'Social', 'Amharic'),
('C042', 'Tigrigna', '12', 'Social', 'Tigrigna'); 