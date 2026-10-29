import java.sql.*;
import java.security.*;
import java.security.spec.KeySpec;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
final class AuthService {
 private final Connection db;
 AuthService(Connection db) { this.db=db; }
 static byte[] hash(char[] password, byte[] salt) throws Exception {
  PBEKeySpec spec=new PBEKeySpec(password,salt,210000,256);
  try {return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();} finally {spec.clearPassword();}
 }
 String login(String username,char[] password) throws Exception {
  try(PreparedStatement p=db.prepareStatement("SELECT salt, password_hash, role FROM test_accounts WHERE username=?")) {
   p.setString(1,username); try(ResultSet r=p.executeQuery()) {
    if(!r.next()) return null;
    return MessageDigest.isEqual(r.getBytes(2),hash(password,r.getBytes(1))) ? r.getString(3) : null;
   }
  } finally {Arrays.fill(password,'\0');}
 }
 static void seed(Connection db,String username,String role,char[] password) throws Exception {
  byte[] salt=new byte[16];new SecureRandom().nextBytes(salt);byte[] hash=hash(password,salt);Arrays.fill(password,'\0');
  try(PreparedStatement p=db.prepareStatement("INSERT INTO test_accounts(username,salt,password_hash,role) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE salt=VALUES(salt),password_hash=VALUES(password_hash),role=VALUES(role)")) {p.setString(1,username);p.setBytes(2,salt);p.setBytes(3,hash);p.setString(4,role);p.executeUpdate();}
 }
}
