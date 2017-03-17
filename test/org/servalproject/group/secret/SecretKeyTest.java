import org.servalproject.group.secret.SecretKey;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import org.junit.Test;


public class SecretKeyTest {
  @Test
  public void initializeStringTest() {
    SecretKey key = new SecretKey("secret");
    assertEquals("secret", key.getKey());
  }
  public void initializeBigIntegerTest() {
    SecretKey key = new SecretKey(new BigInteger("123456789"));
    assertEquals(new BigInteger("123456789"), key.getKeyBigInteger());
  }
}
