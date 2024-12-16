/*
 * @copyright defined in LICENSE.txt
 */

package hera.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;
import static java.lang.System.identityHashCode;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.SecureRandom;

public class ObjectUtils {
  private static final Logger logger = LoggerFactory.getLogger(ObjectUtils.class);

  private static final SecureRandom SEEDER = new SecureRandom();
  private static String hexServerIP = null;

  protected static int getInt(
      final byte[] bytes) {
    int i = 0;
    int j = 24;
    for (int k = 0; 0 <= j; ++k) {
      int l = bytes[k] & 0xff;
      i += l << j;
      j -= 8;
    }
    return i;
  }

  protected static String hex(
      final int value) {
    return format("%08x", value);
  }

  public static String guid() {
    return guid(null);
  }

  /**
   * Generate global unique identifier for {@code obj}.
   *
   * @param obj object to use for generation
   * @return guid generated guid
   */
  public static String guid(
      final Object obj) {
    final StringBuilder guid = new StringBuilder(32);

    // time value
    long timeNow = System.currentTimeMillis();
    int timeLow = (int) timeNow;
    guid.append(hex(timeLow));

    // server IP
    if (null == hexServerIP) {
      InetAddress localInetAddress;
      try {
        // get the inet address
        localInetAddress = InetAddress.getLocalHost();
      } catch (final UnknownHostException uhe) {
        try {
          localInetAddress = InetAddress.getByName("localhost");
        } catch (final UnknownHostException e) {
          // Failing to get address of localhost means the system is insane.
          logger.warn("failed to get localhost address ", e);
          return null;
        }
      }

      byte[] address = localInetAddress.getAddress();

      hexServerIP = hex(getInt(address));
    }

    guid.append(hexServerIP);

    // hash of object
    guid.append(hex(identityHashCode(obj)));

    // pseudo random
    int node;
    synchronized (SEEDER) {
      node = SEEDER.nextInt();
    }
    guid.append(hex(node));

    return guid.toString();

  }

  /**
   * Check if two object is equal.
   *
   * @param obj1 first object to compare
   * @param obj2 second object to compare
   * @return if equal
   */
  public static boolean equal(
      final Object obj1,
      final Object obj2) {

    if (obj1 == obj2) {
      return true;
    }
    if (obj1 == null || obj2 == null) {
      return false;
    }
    return obj1.equals(obj2);
  }

  /**
   * Compare two object.
   *
   * @param <T> object type
   * @param o1 first object to compare
   * @param o2 second object to compare
   *
   * @return result of comparison
   *
   * @see Comparable
   */
  public static <T> int compare(
      final Comparable<T> o1,
      final T o2) {
    if (o1 == o2) {
      return 0;
    }
    if (null == o1) {
      return -1;
    }
    return o1.compareTo(o2);
  }

  /**
   * Return the first object which is not null.
   * <p>
   * Return {@code null}, if @{code objs} is {@code null} Return {@code null}, if all elements
   * in @{code objs} are null
   * </p>
   *
   * @param <K> object type
   * @param objs object array to check
   *
   * @return found object
   */
  @SafeVarargs
  public static <K> K nvl(
      final K... objs) {
    if (null == objs) {
      return null;
    }
    K ret = null;
    for (final K obj : objs) {
      if (null != obj) {
        ret = obj;
        break;
      }
    }
    return ret;
  }
}
