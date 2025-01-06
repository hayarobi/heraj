/*
 * @copyright defined in LICENSE.txt
 */

package hera.keystore;

import hera.annotation.ApiAudience;
import hera.annotation.ApiStability;
import hera.util.ValidationUtils;

@ApiAudience.Public
@ApiStability.Unstable
public class KeyStores {

  /**
   * Create a new in-memory keystore.
   *
   * @return an in-memory keystore
   */
  public static KeyStore newInMemoryKeyStore() {
    return new InMemoryKeyStore();
  }

  /**
   * Create a new CompositeMemory keystore.
   * CompositeMemoryKeyStore is a utility keystore that can load the key stored in the original keystore,
   * and can save key in temporarily in memory.
   * It doesn't modify the original keystore specified as baseKeyStore.
   *
   * @return instance of CompositeMemoryKeyStore
   */
  public static KeyStore newCompositeMemoryKeyStore(KeyStore baseKeyStore) {
    ValidationUtils.assertNotNull(baseKeyStore, "Base keystore must not null");
    return new CompositeMemoryKeyStore(baseKeyStore);
  }

  /**
   * Create aergo keystore with root directory {@code keyStoreDir}.
   *
   * @param root a keystore root directory
   * @return an aergo keystore
   */
  public static KeyStore newAergoKeyStore(final String root) {
    return new AergoKeyStore(root);
  }

  /**
   * Create a keystore which uses {@link java.security.KeyStore}.
   *
   * @param delegate a java keystore
   * @return a java keystore
   */
  public static KeyStore newJavaKeyStore(final java.security.KeyStore delegate) {
    return new JavaKeyStore(delegate);
  }

  private KeyStores() {

  }

}
