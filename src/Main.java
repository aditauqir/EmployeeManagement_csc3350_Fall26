import java.sql.*;
import java.math.BigDecimal;
import java.util.Scanner;
public class Main {
 public static void main(String[] args) {
  try(Connection db=Database.connect(); Scanner in=new Scanner(System.in)) {
   System.out.println("Employee management prototype - test data only.");
   System.out.print("Username: "); String username=in.nextLine().trim();
   System.out.print("Password: "); java.io.Console console=System.console(); char[] password=console != null ? console.readPassword() : in.nextLine().toCharArray();
   String role=new AuthService(db).login(username,password);
   if(role==null) {System.out.println("Invalid credentials."); return;}
   System.out.println("Signed in as " + role + ".");
   EmployeeService service=new EmployeeService(db,role);
   while(true) {
    System.out.println("1 Update employee | 2 Delete employee | 3 Increase salary | 0 Exit");
    System.out.print("Choice: "); if(!in.hasNextLine()) break; String choice=in.nextLine().trim();
    if(choice.equals("0")) break;
    try {
     if(choice.equals("1")) {
      if (!"HR".equals(role)) {System.out.println("Access denied: HR only."); continue;}
      System.out.print("Employee ID: "); int id=Integer.parseInt(in.nextLine().trim()); String found=service.find(id);
      if(found==null){System.out.println("No matching employee.");continue;} System.out.println(found);
      System.out.print("New email: ");String email=in.nextLine().trim();System.out.print("New phone: ");String phone=in.nextLine().trim();
      if(service.update(id,email,phone)) System.out.println("Employee updated successfully.");
     } else if(choice.equals("2")) {
      if (!"HR".equals(role)) {System.out.println("Access denied: HR only."); continue;}
      System.out.print("Employee ID: "); int id=Integer.parseInt(in.nextLine().trim()); String found=service.find(id);
      if(found==null){System.out.println("No matching employee.");continue;} System.out.println(found);
      System.out.print("Confirm delete (yes/no): "); if(!in.nextLine().trim().equalsIgnoreCase("yes")) {System.out.println("Deletion cancelled.");continue;}
      System.out.println(service.delete(id)?"Employee deleted.":"No matching employee.");
     } else if(choice.equals("3")) {
      if (!"HR".equals(role)) {System.out.println("Access denied: HR only."); continue;}
      System.out.print("Salary threshold: "); BigDecimal threshold=new BigDecimal(in.nextLine().trim());
      System.out.print("Increase percentage: "); BigDecimal percent=new BigDecimal(in.nextLine().trim());
      System.out.println("Employees updated: "+service.raise(threshold,percent));
     } else System.out.println("Invalid choice.");
    } catch(IllegalArgumentException e) {System.out.println("Validation error: "+e.getMessage());}
      catch(SQLException e) {System.out.println("Database operation failed. No change was completed.");}
   }
  } catch(Exception e) {System.err.println("Could not start the prototype: "+e.getMessage()); System.exit(1);}
 }
}
