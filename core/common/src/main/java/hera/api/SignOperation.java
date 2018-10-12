/*
 * @copyright defined in LICENSE.txt
 */

package hera.api;

import hera.annotation.ApiAudience;
import hera.annotation.ApiStability;
import hera.api.model.Signature;
import hera.api.model.Transaction;
import hera.key.AergoKey;
import hera.util.Adaptor;

@ApiAudience.Public
@ApiStability.Unstable
public interface SignOperation extends Adaptor {

  /**
   * Sign for transaction.
   *
   * @param key key to sign
   * @param transaction transaction to sign
   * @return signing result
   */
  Signature sign(AergoKey key, Transaction transaction);

  /**
   * Verify transaction.
   *
   * @param key key to verify
   * @param transaction transaction to verify
   * @return verify result
   */
  boolean verify(AergoKey key, Transaction transaction);

}
