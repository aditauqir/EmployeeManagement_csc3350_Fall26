CREATE DATABASE csc3350_test;
USE csc3350_test;
CREATE TABLE employees (empID INT PRIMARY KEY, Fname VARCHAR(65) NOT NULL, Lname VARCHAR(65) NOT NULL, email VARCHAR(65) NOT NULL, HireDate DATE, Salary DECIMAL(10,2) NOT NULL, SSN VARCHAR(12));
CREATE TABLE demographics (empID INT PRIMARY KEY, DOB DATE, phone VARCHAR(25), emergencyContactName VARCHAR(100), emergencyContactMobile VARCHAR(25), FOREIGN KEY(empID) REFERENCES employees(empID));
CREATE TABLE payroll (payID INT PRIMARY KEY, empID INT, pay_date DATE, earnings DECIMAL(8,2), FOREIGN KEY(empID) REFERENCES employees(empID));
INSERT INTO employees VALUES (1001,'Test','One','old@example.com','2025-01-01',49000,NULL),(1002,'Test','Two','two@example.com','2025-01-01',50000,NULL),(1003,'Test','Three','three@example.com','2025-01-01',51000,NULL);
INSERT INTO demographics (empID,phone) VALUES (1001,'404-555-0101');
INSERT INTO payroll VALUES (1,1001,'2026-10-01',1000.00);
