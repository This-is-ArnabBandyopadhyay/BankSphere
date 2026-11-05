-- BankSphere Database Setup

CREATE DATABASE IF NOT EXISTS BankSphere
CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;

-- Create application user (already created separately)
-- Do not execute this statement again.
-- CREATE USER 'banksphere_app'@'localhost'
-- IDENTIFIED BY 'YOUR_PASSWORD';

-- Grant access to BankSphere database
GRANT ALL PRIVILEGES ON BankSphere.*
TO 'banksphere_app'@'localhost';