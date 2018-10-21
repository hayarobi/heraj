/*
 * @copyright defined in LICENSE.txt
 */

package hera.api.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public abstract class AbstractAccount implements Account {

  @Getter
  protected long nonce = 0;

  @Getter
  protected long balance = 0;

  @Override
  public void bindState(final AccountState state) {
    setNonce(state.getNonce());
    setBalance(state.getBalance());
  }

  @Override
  public void setNonce(final long nonce) {
    this.nonce = nonce < 0 ? 0 : nonce;
  }

  @Override
  public long nextNonce() {
    return this.nonce + 1;
  }

  @Override
  public void incrementNonce() {
    ++this.nonce;
  }

  @Override
  public void setBalance(final long balance) {
    this.balance = balance < 0 ? 0 : balance;
  }

}
