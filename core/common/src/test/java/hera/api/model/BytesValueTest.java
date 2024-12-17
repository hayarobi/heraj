/*
 * @copyright defined in LICENSE.txt
 */

package hera.api.model;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Test;

public class BytesValueTest {
  @Test
  public void testHashCode() {
    final byte[] byteValue1 = randomUUID().toString().getBytes();
    final byte[] byteValue2 = Arrays.copyOf(byteValue1, byteValue1.length);

    final BytesValue value1 = new BytesValue(byteValue1);
    final BytesValue value2 = new BytesValue(byteValue2);


    assertEquals(value1.hashCode(), value2.hashCode());
  }

  @Test
  public void testEquals() {
    final byte[] byteValue1 = randomUUID().toString().getBytes();
    final byte[] byteValue2 = Arrays.copyOf(byteValue1, byteValue1.length);

    final BytesValue value1 = new BytesValue(byteValue1);
    final BytesValue value2 = new BytesValue(byteValue2);

    assertEquals(value1, value2);
  }

  @Test
  public void testToString() {
    final byte[] byteValue1 = {0x00, 0x01,0x03,0x05,0x00,0x11,0x32,0x41,0x61};
    final BytesValue value1 = new BytesValue(byteValue1);

    System.out.println("value 1: " + value1.toString());

  }
}