# Employee Management System - CSc 3350 Fall 2026

This is Team 16's Java console prototype for Deliverable 2. It uses MySQL and fictional test data to check employee updates, deletion, salary increases, login, and HR role restrictions. This is not the completed employee management system: reports, full employee data management, and production security are still needed.

## What it does

- HR can change an employee's email and phone, search for an employee by ID and confirm or cancel deletion, and increase salaries below a threshold.
- Test accounts have HR or employee roles. Employee accounts cannot use those three HR operations.
- The prototype stores salted PBKDF2 password hashes for the test accounts.

## Requirements

- Java JDK (the prototype was tested with JDK 26)
- MySQL 8.4 running locally
- MySQL Connector/J 9.4.0

## Set up the isolated test database

The commands below are for macOS with Homebrew MySQL at `/opt/homebrew/opt/mysql@8.4`. The test server uses its own data directory, listens only on localhost port 13350, and has an empty root password. Use it only with fictional test data. Do not use this setup for a real company database.

Create and start the test server in one Terminal window:

```sh
mkdir -p /private/tmp/csc3350_mysql_test
/opt/homebrew/opt/mysql@8.4/bin/mysqld --no-defaults --initialize-insecure --basedir=/opt/homebrew/opt/mysql@8.4 --datadir=/private/tmp/csc3350_mysql_test
/opt/homebrew/opt/mysql@8.4/bin/mysqld --no-defaults --basedir=/opt/homebrew/opt/mysql@8.4 --datadir=/private/tmp/csc3350_mysql_test --socket=/private/tmp/csc3350_mysql.sock --pid-file=/private/tmp/csc3350_mysql.pid --bind-address=127.0.0.1 --port=13350 --log-error=/private/tmp/csc3350_mysql.log
```

Run initialization only once on an empty data directory. If the test server is already running, skip these commands. Temporary files can be removed by the operating system.

In a second Terminal window, from this repository folder, create the fixture and download the JDBC driver:

```sh
/opt/homebrew/opt/mysql@8.4/bin/mysql --no-defaults --socket=/private/tmp/csc3350_mysql.sock -u root < setup.sql
mkdir -p lib build
curl -fL -o lib/mysql-connector-j.jar https://repo.maven.apache.org/maven2/com/mysql/mysql-connector-j/9.4.0/mysql-connector-j-9.4.0.jar
javac -cp lib/mysql-connector-j.jar -d build src/*.java
java -cp 'build:lib/mysql-connector-j.jar' SetupAccounts
```

The fixture script creates `csc3350_test` and must be run only on a fresh test instance. SetupAccounts creates the fictional accounts `test_hr` / `hr-test-only` and `test_employee` / `employee-test-only`. These are public test credentials, not real user accounts.

## Run the app

```sh
java -cp 'build:lib/mysql-connector-j.jar' Main
```

Sign in with one of the test accounts. When you are done, stop the isolated server:

```sh
/opt/homebrew/opt/mysql@8.4/bin/mysqladmin --no-defaults --socket=/private/tmp/csc3350_mysql.sock -u root shutdown
```

## Run the checks

```sh
./run_checks.sh
```

The script resets and changes the fictional test records. It requires the isolated server, fixture, compiled classes, and test accounts above. It prints the app output and database results for the test cases. It must not be run against real data.

The prototype uses a test-only MySQL root connection and local test passwords. It does not implement a production account system, encrypted database connection, employee self-service, reports, or all of the features required for the final project.
