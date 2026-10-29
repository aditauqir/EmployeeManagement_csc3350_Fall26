#!/bin/bash
set -eu
cd "$(dirname "$0")"
MYSQL=/opt/homebrew/opt/mysql@8.4/bin/mysql
SOCKET=/private/tmp/csc3350_mysql.sock
query() { "$MYSQL" --no-defaults --socket="$SOCKET" -u root --batch --skip-column-names csc3350_test -e "$1"; }
reset() { query "UPDATE employees SET email='old@example.com',Salary=49000 WHERE empID=1001; UPDATE demographics SET phone='404-555-0101' WHERE empID=1001; INSERT INTO employees VALUES (1002,'Test','Two','two@example.com','2025-01-01',50000,NULL) ON DUPLICATE KEY UPDATE Salary=50000; UPDATE employees SET Salary=51000 WHERE empID=1003;"; }
app() { { printf "test_hr\nhr-test-only\n"; cat; } | java -cp 'build:lib/mysql-connector-j.jar' Main; }
query "CREATE TABLE IF NOT EXISTS cities (cityID INT PRIMARY KEY, cityName VARCHAR(80)); CREATE TABLE IF NOT EXISTS states (stateID INT PRIMARY KEY, abbreviation CHAR(2)); CREATE TABLE IF NOT EXISTS addresses (empID INT PRIMARY KEY, street VARCHAR(100), cityID INT, stateID INT, zip VARCHAR(10), FOREIGN KEY(empID) REFERENCES employees(empID), FOREIGN KEY(cityID) REFERENCES cities(cityID), FOREIGN KEY(stateID) REFERENCES states(stateID)); INSERT IGNORE INTO cities VALUES(1,'Atlanta'); INSERT IGNORE INTO states VALUES(1,'GA'); INSERT IGNORE INTO addresses VALUES(1001,'123 Test St',1,1,'30303');"
reset
before=$(query "SELECT empID,Fname,Lname,HireDate,Salary,SSN FROM employees ORDER BY empID; SELECT empID,email FROM employees WHERE empID<>1001 ORDER BY empID; SELECT empID,DOB,emergencyContactName,emergencyContactMobile FROM demographics ORDER BY empID; SELECT * FROM addresses ORDER BY empID; SELECT * FROM cities ORDER BY cityID; SELECT * FROM states ORDER BY stateID; SELECT * FROM payroll ORDER BY payID;")
printf '\nTC1 VALID UPDATE\n'
printf '1\n1001\nnew@example.com\n404-555-0199\n0\n' | app
query "SELECT e.empID,e.email,d.phone FROM employees e JOIN demographics d ON e.empID=d.empID WHERE e.empID=1001;"
after=$(query "SELECT empID,Fname,Lname,HireDate,Salary,SSN FROM employees ORDER BY empID; SELECT empID,email FROM employees WHERE empID<>1001 ORDER BY empID; SELECT empID,DOB,emergencyContactName,emergencyContactMobile FROM demographics ORDER BY empID; SELECT * FROM addresses ORDER BY empID; SELECT * FROM cities ORDER BY cityID; SELECT * FROM states ORDER BY stateID; SELECT * FROM payroll ORDER BY payID;")
[ "$before" = "$after" ] && printf "Other checked fields unchanged: YES\n"
reset
printf '\nTC2 INVALID UPDATE\n'
printf '1\n1001\nnot-an-email\n404-555-0199\n0\n' | app
query "SELECT e.empID,e.email,d.phone FROM employees e JOIN demographics d ON e.empID=d.empID WHERE e.empID=1001;"
reset
printf '\nTC3 CONFIRM DELETE\n'
printf '2\n1002\nyes\n0\n' | app
query 'SELECT COUNT(*) AS remaining FROM employees WHERE empID=1002;'
reset
before=$(query 'SELECT * FROM employees ORDER BY empID; SELECT * FROM demographics ORDER BY empID; SELECT * FROM payroll ORDER BY payID;')
printf '\nTC4 CANCEL DELETE\n'
printf '2\n1002\nno\n0\n' | app
query 'SELECT COUNT(*) AS remaining FROM employees WHERE empID=1002;'
after=$(query 'SELECT * FROM employees ORDER BY empID; SELECT * FROM demographics ORDER BY empID; SELECT * FROM payroll ORDER BY payID;')
[ "$before" = "$after" ] && printf 'Database records unchanged: YES\n'
reset
before=$(query 'SELECT * FROM employees ORDER BY empID; SELECT * FROM demographics ORDER BY empID; SELECT * FROM payroll ORDER BY payID;')
printf '\nTC5 NO MATCH\n'
printf '2\n9999\n0\n' | app
after=$(query 'SELECT * FROM employees ORDER BY empID; SELECT * FROM demographics ORDER BY empID; SELECT * FROM payroll ORDER BY payID;')
[ "$before" = "$after" ] && printf 'Database records unchanged: YES\n'
reset
printf '\nTC6 SALARY INCREASE\n'
printf '3\n50000\n10\n0\n' | app
query 'SELECT empID,Salary FROM employees ORDER BY empID; SELECT payID,empID,pay_date,earnings FROM payroll;'
reset
printf '\nTC7 INVALID PERCENT\n'
printf '3\n50000\n-10\n0\n' | app
query 'SELECT empID,Salary FROM employees ORDER BY empID;'
reset
before=$(query 'SELECT * FROM employees ORDER BY empID; SELECT * FROM demographics ORDER BY empID; SELECT * FROM payroll ORDER BY payID;')
printf '\nTC8 EMPLOYEE ACCESS\n'
printf 'test_employee\nemployee-test-only\n1\n2\n3\n0\n' | java -cp 'build:lib/mysql-connector-j.jar' Main
query "SELECT e.empID,e.email,d.phone,e.Salary FROM employees e JOIN demographics d ON e.empID=d.empID WHERE e.empID=1001; SELECT COUNT(*) FROM employees WHERE empID=1002;"
after=$(query 'SELECT * FROM employees ORDER BY empID; SELECT * FROM demographics ORDER BY empID; SELECT * FROM payroll ORDER BY payID;')
if [ "$before" = "$after" ]; then printf 'Database records unchanged: YES\n'; else printf 'Database records unchanged: NO\n'; exit 1; fi
printf '\nINVALID LOGIN\n'
printf 'test_hr\nwrong-password\n' | java -cp 'build:lib/mysql-connector-j.jar' Main
