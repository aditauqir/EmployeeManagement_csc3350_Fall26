import java.sql.*;
public class SetupAccounts {
 public static void main(String[] args) throws Exception {
  try(Connection db=Database.connect(); Statement s=db.createStatement()) {
   s.execute("CREATE TABLE IF NOT EXISTS test_accounts(username VARCHAR(50) PRIMARY KEY, salt VARBINARY(16) NOT NULL, password_hash VARBINARY(32) NOT NULL, role ENUM('HR','EMPLOYEE') NOT NULL)");
   AuthService.seed(db,"test_hr","HR","hr-test-only".toCharArray());
   AuthService.seed(db,"test_employee","EMPLOYEE","employee-test-only".toCharArray());
   System.out.println("Test accounts created.");
  }
 }
}
