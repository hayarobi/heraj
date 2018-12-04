/*
 * @copyright defined in LICENSE.txt
 */

package hera.client;

import static hera.TransportConstants.KEYSTORE_CREATE;
import static hera.TransportConstants.KEYSTORE_EXPORTKEY;
import static hera.TransportConstants.KEYSTORE_IMPORTKEY;
import static hera.TransportConstants.KEYSTORE_LIST;
import static hera.TransportConstants.KEYSTORE_LOCK;
import static hera.TransportConstants.KEYSTORE_UNLOCK;
import static hera.api.tupleorerror.Functions.identify;

import hera.ContextProvider;
import hera.ContextProviderInjectable;
import hera.annotation.ApiAudience;
import hera.annotation.ApiStability;
import hera.api.KeyStoreOperation;
import hera.api.model.Account;
import hera.api.model.AccountAddress;
import hera.api.model.Authentication;
import hera.api.model.EncryptedPrivateKey;
import hera.api.tupleorerror.Function0;
import hera.api.tupleorerror.Function1;
import hera.api.tupleorerror.Function3;
import hera.api.tupleorerror.ResultOrErrorFuture;
import hera.strategy.StrategyChain;
import io.grpc.ManagedChannel;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@ApiAudience.Private
@ApiStability.Unstable
public class KeyStoreTemplate
    implements KeyStoreOperation, ChannelInjectable, ContextProviderInjectable {

  @Getter
  protected KeyStoreBaseTemplate keyStoreBaseTemplate = new KeyStoreBaseTemplate();

  @Setter
  protected ContextProvider contextProvider;

  @Getter(lazy = true, value = AccessLevel.PROTECTED)
  private final StrategyChain strategyChain = StrategyChain.of(contextProvider.get());

  @Override
  public void setChannel(final ManagedChannel channel) {
    getKeyStoreBaseTemplate().setChannel(channel);;
  }

  @Getter(lazy = true, value = AccessLevel.PROTECTED)
  private final Function0<ResultOrErrorFuture<List<AccountAddress>>> listFunction =
      getStrategyChain()
          .apply(identify(getKeyStoreBaseTemplate().getListFunction(), KEYSTORE_LIST));

  @Getter(lazy = true, value = AccessLevel.PROTECTED)
  private final Function1<String, ResultOrErrorFuture<Account>> createFunction =
      getStrategyChain()
          .apply(identify(getKeyStoreBaseTemplate().getCreateFunction(), KEYSTORE_CREATE));
  @Getter(lazy = true, value = AccessLevel.PROTECTED)
  private final Function1<Authentication, ResultOrErrorFuture<Boolean>> unlockFunction =
      getStrategyChain()
          .apply(identify(getKeyStoreBaseTemplate().getUnlockFunction(), KEYSTORE_UNLOCK));
  @Getter(lazy = true, value = AccessLevel.PROTECTED)
  private final Function1<Authentication, ResultOrErrorFuture<Boolean>> lockFunction =
      getStrategyChain()
          .apply(identify(getKeyStoreBaseTemplate().getLockFunction(), KEYSTORE_LOCK));

  @Getter(lazy = true, value = AccessLevel.PROTECTED)
  private final Function3<EncryptedPrivateKey, String, String,
      ResultOrErrorFuture<Account>> importKeyFunction = getStrategyChain().apply(
          identify(getKeyStoreBaseTemplate().getImportKeyFunction(), KEYSTORE_IMPORTKEY));

  @Getter(lazy = true, value = AccessLevel.PROTECTED)
  private final Function1<Authentication,
      ResultOrErrorFuture<EncryptedPrivateKey>> exportKeyFunction = getStrategyChain().apply(
          identify(getKeyStoreBaseTemplate().getExportKeyFunction(), KEYSTORE_EXPORTKEY));

  @Override
  public List<AccountAddress> list() {
    return getListFunction().apply().get().getResult();
  }

  @Override
  public Account create(final String password) {
    return getCreateFunction().apply(password).get().getResult();
  }

  @Override
  public boolean lock(final Authentication authentication) {
    return getLockFunction().apply(authentication).get().getResult();
  }

  @Override
  public boolean unlock(final Authentication authentication) {
    return getUnlockFunction().apply(authentication).get().getResult();
  }

  @Override
  public Account importKey(final EncryptedPrivateKey encryptedKey,
      final String oldPassword, final String newPassword) {
    return getImportKeyFunction().apply(encryptedKey, oldPassword, newPassword).get().getResult();
  }

  @Override
  public EncryptedPrivateKey exportKey(final Authentication authentication) {
    return getExportKeyFunction().apply(authentication).get().getResult();
  }

}
