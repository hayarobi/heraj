/*
 * @copyright defined in LICENSE.txt
 */

package hera.api;

import hera.annotation.ApiAudience;
import hera.annotation.ApiStability;
import hera.api.model.Account;
import hera.api.model.AccountAddress;
import hera.api.model.AccountState;
import hera.api.model.Signature;
import hera.api.model.Transaction;
import hera.exception.AdaptException;

@ApiAudience.Public
@ApiStability.Unstable
public interface AccountOperation {

  /**
   * Get account state by address.
   *
   * @param address account address
   * @return an account state
   */
  AccountState getState(AccountAddress address);

  /**
   * Get account state by account.
   *
   * @param account account
   * @return an account state
   */
  default AccountState getState(Account account) {
    return account.adapt(AccountAddress.class).map(this::getState)
        .orElseThrow(() -> new AdaptException(account.getClass(), AccountAddress.class));
  }

  /**
   * Sign for transaction.
   *
   * @param account account to sign
   * @param transaction transaction to sign
   * @return signing result
   */
  Signature sign(Account account, Transaction transaction);

  /**
   * Verify transaction.
   *
   * @param account account to verify
   * @param transaction transaction to verify
   * @return verify result
   */
  boolean verify(Account account, Transaction transaction);

}
