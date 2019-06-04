/*
 * @copyright defined in LICENSE.txt
 */

package hera.transaction;

import static java.util.Collections.unmodifiableList;

import hera.api.model.Account;
import hera.api.model.AccountAddress;
import hera.api.model.Aer;
import hera.api.model.ChainIdHash;
import hera.api.model.RawTransaction;
import hera.api.model.Transaction.TxType;
import hera.api.model.internal.GovernanceRecipient;
import hera.spec.PayloadSpec.Type;
import hera.spec.resolver.PayloadResolver;
import hera.transaction.dsl.VoteTransaction;
import hera.transaction.dsl.VoteTransaction.WithChainIdHash;
import hera.transaction.dsl.VoteTransaction.WithChainIdHashAndSender;
import hera.transaction.dsl.VoteTransaction.WithChainIdHashAndSenderAndNonce;
import hera.transaction.dsl.VoteTransaction.WithChainIdHashAndSenderAndNonceAndVoteId;
import hera.transaction.dsl.VoteTransaction.WithReady;
import java.util.List;

public class VoteTransactionBuilder implements
    VoteTransaction.WithNothing,
    VoteTransaction.WithChainIdHash,
    VoteTransaction.WithChainIdHashAndSender,
    VoteTransaction.WithChainIdHashAndSenderAndNonce,
    VoteTransaction.WithChainIdHashAndSenderAndNonceAndVoteId,
    VoteTransaction.WithReady {

  protected String voteId;

  protected List<String> candidates;

  protected final PlainTransactionBuilder delegate = new PlainTransactionBuilder();

  @Override
  public WithChainIdHash chainIdHash(final ChainIdHash chainIdHash) {
    this.delegate.chainIdHash(chainIdHash);
    return this;
  }

  @Override
  public WithChainIdHashAndSender from(final String senderName) {
    this.delegate.from(senderName);
    return this;
  }

  @Override
  public WithChainIdHashAndSender from(final Account sender) {
    this.delegate.from(sender);
    return this;
  }

  @Override
  public WithChainIdHashAndSender from(final AccountAddress sender) {
    this.delegate.from(sender);
    return this;
  }

  @Override
  public WithChainIdHashAndSenderAndNonce nonce(long nonce) {
    this.delegate.nonce(nonce);
    return this;
  }

  @Override
  public WithChainIdHashAndSenderAndNonceAndVoteId voteId(String voteId) {
    this.voteId = voteId;
    return this;
  }

  @Override
  public WithReady candidates(final List<String> candidates) {
    this.candidates = unmodifiableList(candidates);
    return this;
  }

  @Override
  public RawTransaction build() {
    this.delegate.to(GovernanceRecipient.AERGO_SYSTEM);
    this.delegate.amount(Aer.EMPTY);
    this.delegate
        .payload(PayloadResolver.resolve(Type.Vote, voteId, candidates.toArray(new String[] {})));
    this.delegate.type(TxType.GOVERNANCE);
    return delegate.build();
  }

}
