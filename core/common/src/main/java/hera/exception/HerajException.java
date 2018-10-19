/*
 * @copyright defined in LICENSE.txt
 */

package hera.exception;

public class HerajException extends RuntimeException implements Cloneable {

  private static final long serialVersionUID = 1429103468497275409L;

  protected HerajException() {
    super();
  }

  public HerajException(String message) {
    super(message);
  }

  public HerajException(Throwable cause) {
    super(cause);
  }

  public HerajException(String message, Throwable cause) {
    super(message, cause);
  }

  @Override
  public HerajException clone() {
    return new HerajException(getMessage(), getCause());
  }

}
