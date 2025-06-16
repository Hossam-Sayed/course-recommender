-- Drop tables in reverse order of dependency to ensure clean recreation
DROP TABLE IF EXISTS course_author;
DROP TABLE IF EXISTS assessment;
DROP TABLE IF EXISTS rating;
DROP TABLE IF EXISTS course;
DROP TABLE IF EXISTS author;

-- Enable UUID generation if not already enabled (for PostgreSQL)
-- This line might only be needed once per database.
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Create the Author Table
CREATE TABLE author (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    birthdate DATE
);

-- 2. Create the Course Table
CREATE TABLE course (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    credit INT NOT NULL
);

-- 3. Create the Assessment Table (One-to-One with Course)
-- The course_id is unique because it's a one-to-one relationship
CREATE TABLE assessment (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    content TEXT NOT NULL,
    course_id UUID UNIQUE NOT NULL, -- UNIQUE constraint for 1-to-1 relationship
    CONSTRAINT fk_course_assessment FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE
);

-- 4. Create the Rating Table (One-to-Many with Course)
CREATE TABLE rating (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    number INT NOT NULL CHECK (number >= 1 AND number <= 5), -- Assuming rating is 1-5
    course_id UUID NOT NULL,
    CONSTRAINT fk_course_rating FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE
);

-- 5. Create the Course_Author Join Table (Many-to-Many)
CREATE TABLE course_author (
    course_id UUID NOT NULL,
    author_id UUID NOT NULL,
    PRIMARY KEY (course_id, author_id), -- Composite Primary Key
    CONSTRAINT fk_course_join FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE,
    CONSTRAINT fk_author_join FOREIGN KEY (author_id) REFERENCES author(id) ON DELETE CASCADE
);
