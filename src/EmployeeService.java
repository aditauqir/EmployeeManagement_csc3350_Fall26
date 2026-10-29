import java.sql.*;
import java.math.BigDecimal;
final class EmployeeService {
    private final Connection db;
    private final String role;
    private void requireHR() { if (!"HR".equals(role)) throw new SecurityException("Access denied: HR only."); }
    EmployeeService(Connection db, String role) { this.db = db; this.role=role; }
    String find(int id) throws SQLException {
        requireHR();
        try (PreparedStatement p = db.prepareStatement("SELECT Fname,Lname,email FROM employees WHERE empID=?")) {
            p.setInt(1,id); try (ResultSet r=p.executeQuery()) { return r.next() ? id+" | "+r.getString(1)+" "+r.getString(2)+" | "+r.getString(3) : null; }
        }
    }
    boolean update(int id, String email, String phone) throws SQLException {
        requireHR();
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || phone.isBlank()) throw new IllegalArgumentException("Invalid email or phone.");
        db.setAutoCommit(false);
        try (PreparedStatement a=db.prepareStatement("UPDATE employees SET email=? WHERE empID=?"); PreparedStatement b=db.prepareStatement("UPDATE demographics SET phone=? WHERE empID=?")) {
            a.setString(1,email); a.setInt(2,id); if(a.executeUpdate()!=1) {db.rollback(); return false;}
            b.setString(1,phone); b.setInt(2,id); if(b.executeUpdate()!=1) {db.rollback(); throw new SQLException("Demographics record missing.");}
            db.commit(); return true;
        } catch(SQLException e) {db.rollback(); throw e;} finally {db.setAutoCommit(true);}
    }
    boolean delete(int id) throws SQLException {
        requireHR();
        try (PreparedStatement p=db.prepareStatement("DELETE FROM employees WHERE empID=?")) { p.setInt(1,id); return p.executeUpdate()==1; }
    }
    int raise(BigDecimal threshold, BigDecimal percent) throws SQLException {
        requireHR();
        if(threshold.signum()<=0 || percent.signum()<=0 || percent.compareTo(new BigDecimal("100"))>0) throw new IllegalArgumentException("Threshold must be positive and percentage must be greater than 0 and at most 100.");
        try(PreparedStatement p=db.prepareStatement("UPDATE employees SET Salary=ROUND(Salary*(1+?/100),2) WHERE Salary < ?")) {p.setBigDecimal(1,percent);p.setBigDecimal(2,threshold); return p.executeUpdate();}
    }
}
