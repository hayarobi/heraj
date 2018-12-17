/*
 * @copyright defined in LICENSE.txt
 */

package hera.wallet;

import hera.api.model.Account;
import hera.api.model.Authentication;
import hera.api.model.EncryptedPrivateKey;
import hera.key.AergoKey;

public interface KeyStore {

  /**
   * Store an {@code AergoKey} to the storage.
   *
   * @param key an aergo key to store
   * @param password a password to encrypt the aergo key
   */
  void save(AergoKey key, String password);

  /**
   * Export an private key encrypted.
   *
   * @param authentication an authentication to used in exporting key
   * @return an encrypted private key.
   */
  EncryptedPrivateKey export(Authentication authentication);

  /**
   * Unlock and return unlocked account. null if failure.
   *
   * @param authentication an authentication which is used in unlocking account
   * @return an unlocked account. null if failure
   */
  Account unlock(Authentication authentication);

  /**
   * Lock an account corresponding to {@code authentication}.
   *
   * @param authentication an authentication which is used in locking account
   * @return true if success. false if failure
   */
  boolean lock(Authentication authentication);

}
