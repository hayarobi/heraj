/*
 * @copyright defined in LICENSE.txt
 */

package hera.client;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hera.AbstractTestCase;
import hera.Context;
import hera.ContextStorage;
import hera.EmptyContext;
import hera.Invocation;
import hera.Requester;
import hera.WriteSynchronizedContextStorage;
import hera.api.model.Aer;
import hera.api.model.BytesValue;
import hera.api.model.ChainIdHash;
import hera.api.model.RawTransaction;
import hera.api.model.Transaction;
import hera.api.model.TxHash;
import hera.key.AergoKey;
import hera.key.AergoKeyGenerator;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

public class TransactionTemplateTest extends AbstractTestCase {

  protected final ContextStorage<Context> contextStorage = new WriteSynchronizedContextStorage<>();

  protected Transaction anyTransaction;

  {
    contextStorage.put(EmptyContext.getInstance());
    final AergoKey signer = new AergoKeyGenerator().create();
    final RawTransaction rawTransaction = RawTransaction
        .newBuilder(ChainIdHash.of(BytesValue.EMPTY))
        .from(signer.getAddress())
        .to(signer.getAddress())
        .amount(Aer.ZERO)
        .nonce(1L)
        .build();
    anyTransaction = signer.sign(rawTransaction);
  }

  @Test
  public void testGetTransaction() throws Exception {
    // given
    final TransactionTemplate transactionTemplate = new TransactionTemplate(contextStorage);
    final Requester mockRequester = mock(Requester.class);
    final Transaction expected = anyTransaction;
    when(mockRequester.request(ArgumentMatchers.<Invocation<?>>any()))
        .thenReturn(expected);
    transactionTemplate.requester = mockRequester;

    // then
    final Transaction actual = transactionTemplate.getTransaction(TxHash.of(BytesValue.EMPTY));
    assertEquals(expected, actual);
  }

  @Test
  public void testCommit() throws Exception {
    // given
    final TransactionTemplate transactionTemplate = new TransactionTemplate(contextStorage);
    final Requester mockRequester = mock(Requester.class);
    final TxHash expected = TxHash.of(BytesValue.EMPTY);
    when(mockRequester.request(ArgumentMatchers.<Invocation<?>>any()))
        .thenReturn(expected);
    transactionTemplate.requester = mockRequester;

    // then
    final TxHash actual = transactionTemplate.commit(anyTransaction);
    assertEquals(expected, actual);
  }

}
