/*
 * @copyright defined in LICENSE.txt
 */

package hera.client;

import static hera.TransportConstants.CONTRACT_DEPLOY;
import static hera.TransportConstants.CONTRACT_EXECUTE;
import static hera.TransportConstants.CONTRACT_GETINTERFACE;
import static hera.TransportConstants.CONTRACT_GETRECEIPT;
import static hera.TransportConstants.CONTRACT_QUERY;
import static hera.api.tupleorerror.Functions.identify;

import hera.ContextProvider;
import hera.ContextProviderInjectable;
import hera.annotation.ApiAudience;
import hera.annotation.ApiStability;
import hera.api.ContractOperation;
import hera.api.model.Account;
import hera.api.model.ContractAddress;
import hera.api.model.ContractDefinition;
import hera.api.model.ContractInterface;
import hera.api.model.ContractInvocation;
import hera.api.model.ContractResult;
import hera.api.model.ContractTxHash;
import hera.api.model.ContractTxReceipt;
import hera.api.model.Fee;
import hera.api.tupleorerror.Function1;
import hera.api.tupleorerror.Function4;
import hera.strategy.StrategyChain;
import io.grpc.ManagedChannel;
import lombok.AccessLevel;
import lombok.Getter;

@ApiAudience.Private
@ApiStability.Unstable
public class ContractTemplate
    implements ContractOperation, ChannelInjectable, ContextProviderInjectable {

  @Getter
  protected ContractBaseTemplate contractBaseTemplate = new ContractBaseTemplate();

  protected ContextProvider contextProvider;

  @Getter(lazy = true, value = AccessLevel.PROTECTED)
  private final StrategyChain strategyChain = StrategyChain.of(contextProvider.get());

  @Override
  public void setChannel(final ManagedChannel channel) {
    getContractBaseTemplate().setChannel(channel);
  }

  @Override
  public void setContextProvider(final ContextProvider contextProvider) {
    this.contextProvider = contextProvider;
    getContractBaseTemplate().setContextProvider(contextProvider);
  }

  @Getter(lazy = true, value = AccessLevel.PROTECTED)
  private final Function1<ContractTxHash,
      FinishableFuture<ContractTxReceipt>> receiptFunction = getStrategyChain().apply(
          identify(contractBaseTemplate.getReceiptFunction(), CONTRACT_GETRECEIPT));


  @Getter(lazy = true, value = AccessLevel.PROTECTED)
  private final Function4<Account, ContractDefinition, Long, Fee,
      FinishableFuture<ContractTxHash>> deployFunction =
          getStrategyChain()
              .apply(identify(contractBaseTemplate.getDeployFunction(), CONTRACT_DEPLOY));


  @Getter(lazy = true, value = AccessLevel.PROTECTED)
  private final Function1<ContractAddress,
      FinishableFuture<ContractInterface>> contractInterfaceFunction =
          getStrategyChain().apply(identify(contractBaseTemplate.getContractInterfaceFunction(),
              CONTRACT_GETINTERFACE));

  @Getter(lazy = true, value = AccessLevel.PROTECTED)
  private final Function4<Account, ContractInvocation, Long, Fee,
      FinishableFuture<ContractTxHash>> executeFunction =
          getStrategyChain()
              .apply(identify(contractBaseTemplate.getExecuteFunction(), CONTRACT_EXECUTE));

  @Getter(lazy = true, value = AccessLevel.PROTECTED)
  private final Function1<ContractInvocation, FinishableFuture<ContractResult>> queryFunction =
      getStrategyChain().apply(identify(contractBaseTemplate.getQueryFunction(), CONTRACT_QUERY));

  @Override
  public ContractTxReceipt getReceipt(final ContractTxHash contractTxHash) {
    return getReceiptFunction().apply(contractTxHash).get();
  }

  @Override
  public ContractTxHash deploy(final Account creator, final ContractDefinition contractDefinition,
      final long nonce, final Fee fee) {
    return getDeployFunction().apply(creator, contractDefinition, nonce, fee).get();
  }

  @Override
  public ContractInterface getContractInterface(
      final ContractAddress contractAddress) {
    return getContractInterfaceFunction().apply(contractAddress).get();
  }

  @Override
  public ContractTxHash execute(final Account executor, final ContractInvocation contractInvocation,
      final long nonce, final Fee fee) {
    return getExecuteFunction().apply(executor, contractInvocation, nonce, fee).get();
  }

  @Override
  public ContractResult query(final ContractInvocation contractInvocation) {
    return getQueryFunction().apply(contractInvocation).get();
  }

}
