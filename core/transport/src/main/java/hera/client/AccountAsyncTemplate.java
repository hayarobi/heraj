/*
 * @copyright defined in LICENSE.txt
 */

package hera.client;

import static hera.TransportConstants.ACCOUNT_GETSTATE_ASYNC;
import static hera.TransportConstants.ACCOUNT_SIGN_ASYNC;
import static hera.TransportConstants.ACCOUNT_VERIFY_ASYNC;
import static hera.api.tupleorerror.Functions.identify;

import hera.ContextProvider;
import hera.ContextProviderInjectable;
import hera.annotation.ApiAudience;
import hera.annotation.ApiStability;
import hera.api.AccountAsyncOperation;
import hera.api.model.Account;
import hera.api.model.AccountAddress;
import hera.api.model.AccountState;
import hera.api.model.Signature;
import hera.api.model.Transaction;
import hera.api.tupleorerror.Function1;
import hera.api.tupleorerror.Function2;
import hera.api.tupleorerror.ResultOrErrorFuture;
import hera.strategy.StrategyChain;
import io.grpc.ManagedChannel;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@ApiAudience.Private
@ApiStability.Unstable
public class AccountAsyncTemplate
    implements AccountAsyncOperation, ChannelInjectable, ContextProviderInjectable {

  @Getter
  protected AccountBaseTemplate accountBaseTemplate = new AccountBaseTemplate();

  @Setter
  protected ContextProvider contextProvider;
  @Getter(lazy = true, value = AccessLevel.PROTECTED)
  private final StrategyChain strategyChain = StrategyChain.of(contextProvider.get());

  @Override
  public void setChannel(final ManagedChannel channel) {
    getAccountBaseTemplate().setChannel(channel);
  }

  @Getter(lazy = true, value = AccessLevel.PROTECTED)
  private final Function1<AccountAddress,
      ResultOrErrorFuture<AccountState>> stateFunction =
          getStrategyChain()
              .apply(identify(getAccountBaseTemplate().getStateFunction(), ACCOUNT_GETSTATE_ASYNC));
  @Getter(lazy = true, value = AccessLevel.PROTECTED)
  private final Function2<Account, Transaction, ResultOrErrorFuture<Signature>> signFunction =
      getStrategyChain()
          .apply(identify(getAccountBaseTemplate().getSignFunction(), ACCOUNT_SIGN_ASYNC));

  @Getter(lazy = true, value = AccessLevel.PROTECTED)
  private final Function2<Account, Transaction, ResultOrErrorFuture<Boolean>> verifyFunction =
      getStrategyChain()
          .apply(identify(getAccountBaseTemplate().getVerifyFunction(), ACCOUNT_VERIFY_ASYNC));

  @Override
  public ResultOrErrorFuture<AccountState> getState(final AccountAddress address) {
    return getStateFunction().apply(address);
  }

  @Override
  public ResultOrErrorFuture<Signature> sign(final Account account, final Transaction transaction) {
    return getSignFunction().apply(account, transaction);
  }

  @Override
  public ResultOrErrorFuture<Boolean> verify(final Account account, final Transaction transaction) {
    return getVerifyFunction().apply(account, transaction);
  }

}
