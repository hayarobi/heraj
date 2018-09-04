/*
 * @copyright defined in LICENSE.txt
 */

package hera.client;

import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import hera.api.model.Abi;
import hera.api.model.AbiSet;
import hera.api.model.Account;
import hera.api.model.AccountAddress;
import hera.api.model.Authentication;
import hera.api.model.ContractTxHash;
import hera.api.model.ContractTxReceipt;
import hera.util.Base58Utils;
import hera.util.IoUtils;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Test;

public class ContractTemplateIT extends AbstractIT {

  protected static final String PASSWORD = randomUUID().toString();

  protected final AtomicInteger atomicInteger = new AtomicInteger(1);

  protected Account creator = null;

  protected AccountTemplate accountTemplate = null;

  protected TransactionTemplate transactionTemplate = null;

  protected ContractTemplate contractTemplate = null;

  @Before
  public void setUp() {
    super.setUp();
    accountTemplate = new AccountTemplate(channel);
    creator = accountTemplate.create(PASSWORD).getResult();
    transactionTemplate = new TransactionTemplate(channel);
    contractTemplate = new ContractTemplate(channel);
  }

  @Test
  public void testLuaContractDeployAndExecute() throws Throwable {
    final Boolean unlockResult =
        accountTemplate.unlock(Authentication.of(creator.getAddress(), PASSWORD)).getResult();
    assertTrue(unlockResult);

    final ContractTxHash deployTxHash = contractTemplate
        .deploy(creator.getAddress(), () -> {
          try (
              final InputStream in = open("payload");
              final Reader reader = new InputStreamReader(in)) {
            return Base58Utils.decode(IoUtils.from(reader));
          }
        })
        .getResult();
    assertNotNull(deployTxHash);
    logger.debug("Deploy hash: {}", deployTxHash);

    waitForNextBlockToGenerate();

    final ContractTxReceipt definitionReceipt =
        contractTemplate.getReceipt(deployTxHash).getResult();
    assertTrue(0 < definitionReceipt.getContractAddress().getValue().length);
    assertEquals("CREATED", definitionReceipt.getStatus());

    final AccountAddress contractAddress = definitionReceipt.getContractAddress();
    logger.debug("ContractAddress: {}", contractAddress);

    final AbiSet abiSet = contractTemplate.getAbiSet(contractAddress).getResult();
    assertNotNull(abiSet);
    logger.debug("Abi set: {}", abiSet);

    final Abi abi = abiSet.findAbiByName("helloReturn").get();
    assertNotNull(abi);
    logger.debug("Abi: {}", abi);

    final ContractTxHash executionTxHash = contractTemplate
        .execute(creator.getAddress(), contractAddress, abi, "Hello aergo").getResult();
    assertNotNull(executionTxHash);
    logger.debug("Execution hash: {}", executionTxHash);

    waitForNextBlockToGenerate();

    final ContractTxReceipt executionReceipt =
        contractTemplate.getReceipt(executionTxHash).getResult();
    assertTrue(0 < executionReceipt.getContractAddress().getValue().length);
    assertEquals("SUCCESS", executionReceipt.getStatus());
    assertTrue(0 < executionReceipt.getRet().length());

    final Boolean lockResult =
        accountTemplate.lock(Authentication.of(creator.getAddress(), PASSWORD)).getResult();
    assertTrue(lockResult);
  }
}
