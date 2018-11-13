/*
 * @copyright defined in LICENSE.txt
 */

package hera.client.it;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import hera.api.model.Account;
import hera.api.model.AccountState;
import hera.api.model.Transaction;
import hera.api.model.TxHash;
import hera.exception.CommitException;
import org.junit.Test;

public class TransactionOperationIT extends AbstractIT {

  @Test
  public void testCommitBySigningLocally() throws Exception {
    final Account account = createClientAccount();
    rechargeCoin(account, 100L);

    waitForNextBlockToGenerate();

    final Transaction transaction = buildTransaction(account);
    signTransaction(account, transaction);

    final TxHash txHash = aergoClient.getTransactionOperation().commit(transaction);
    logger.info("TxHash: {}", txHash);

    final Transaction queried = aergoClient.getTransactionOperation().getTransaction(txHash);
    logger.info("Queired transaction: {}", queried);

    assertEquals(transaction, queried);
    assertTrue(!queried.isConfirmed());
  }

  @Test
  public void testCommitAndWaitToConfirmBySigningLocally() throws Exception {
    final Account account = createClientAccount();
    rechargeCoin(account, 100L);

    waitForNextBlockToGenerate();

    final Transaction transaction = buildTransaction(account);
    signTransaction(account, transaction);

    final TxHash txHash = aergoClient.getTransactionOperation().commit(transaction);
    logger.info("TxHash: {}", txHash);

    waitForNextBlockToGenerate();

    final Transaction queried = aergoClient.getTransactionOperation().getTransaction(txHash);
    logger.info("Queired transaction: {}", queried);

    assertTrue(queried.isConfirmed());
  }

  @Test
  public void testCommitBySigningRemotely() throws Exception {
    final String password = randomUUID().toString();
    final Account account = createServerAccount(password);
    rechargeCoin(account, 100L);

    waitForNextBlockToGenerate();

    final Transaction transaction = buildTransaction(account);

    assertTrue(unlockAccount(account, password));
    signTransaction(account, transaction);
    assertTrue(lockAccount(account, password));

    final TxHash txHash = aergoClient.getTransactionOperation().commit(transaction);
    logger.info("TxHash: {}", txHash);

    final Transaction queried = aergoClient.getTransactionOperation().getTransaction(txHash);
    logger.info("Queired transaction: {}", queried);

    assertEquals(transaction, queried);
    assertTrue(!queried.isConfirmed());
  }

  @Test
  public void testCommitAndWaitToConfirmBySigningRemotely() throws Exception {
    final String password = randomUUID().toString();
    final Account account = createServerAccount(password);
    rechargeCoin(account, 100L);
    waitForNextBlockToGenerate();

    final Transaction transaction = buildTransaction(account);

    assertTrue(unlockAccount(account, password));
    signTransaction(account, transaction);
    assertTrue(lockAccount(account, password));

    final TxHash txHash = aergoClient.getTransactionOperation().commit(transaction);
    logger.info("TxHash: {}", txHash);

    waitForNextBlockToGenerate();

    final Transaction queried = aergoClient.getTransactionOperation().getTransaction(txHash);
    logger.info("Queired transaction: {}", queried);

    assertTrue(queried.isConfirmed());
  }

  @Test
  public void testSendTx() throws Exception {
    final String password = randomUUID().toString();
    final Account account = createServerAccount(password);
    rechargeCoin(account, 100L);

    waitForNextBlockToGenerate();

    final Account recipient = createClientAccount();
    final AccountState preState = aergoClient.getAccountOperation().getState(recipient);
    final long balance = preState.getBalance();

    assertTrue(unlockAccount(account, password));
    aergoClient.getTransactionOperation().send(account, recipient, amount);
    assertTrue(lockAccount(account, password));

    waitForNextBlockToGenerate();

    final AccountState postState = aergoClient.getAccountOperation().getState(recipient);
    assertEquals(balance + amount, postState.getBalance());
  }

  @Test
  public void shouldNotConfirmedForLowNonce() throws Exception {
    final Account account = createClientAccount();
    rechargeCoin(account, 100L);

    waitForNextBlockToGenerate();

    final Transaction transaction = buildTransaction(account);
    transaction.setNonce(0L);
    signTransaction(account, transaction);

    try {
      aergoClient.getTransactionOperation().commit(transaction);
      fail();
    } catch (CommitException e) {
      assertEquals(CommitException.CommitStatus.NONCE_TOO_LOW, e.getCommitStatus());
    }
  }
}
