/*
 * @copyright defined in LICENSE.txt
 */

package hera.client.it;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import hera.api.model.Aer;
import hera.api.model.ContractAddress;
import hera.api.model.ContractDefinition;
import hera.api.model.ContractInterface;
import hera.api.model.ContractInvocation;
import hera.api.model.ContractResult;
import hera.api.model.ContractTxHash;
import hera.api.model.ContractTxReceipt;
import hera.api.model.Event;
import hera.api.model.EventFilter;
import hera.api.model.Fee;
import hera.api.model.StreamObserver;
import hera.api.model.Subscription;
import hera.key.AergoKey;
import hera.key.Signer;
import hera.util.IoUtils;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.junit.Before;
import org.junit.Test;

public class ContractOperationIT extends AbstractIT {

  protected String payload;
  protected String redeployPayload;

  protected String executeFunction = "set";
  protected String executeKey = randomUUID().toString();
  protected int executeIntVal = randomUUID().toString().hashCode();
  protected String executeStringVal = randomUUID().toString();

  protected String queryFunction = "get";

  @Before
  public void setUp() throws Exception {
    super.setUp();
    payload = IoUtils.from(new InputStreamReader(open("payload")));
    redeployPayload = IoUtils.from(new InputStreamReader(open("redeployPayload")));
  }

  @Test
  public void shouldApplyConstructorArgsOnDeploy() throws Exception {
    // given
    final AergoKey key = createNewKey();
    final String deployKey = randomUUID().toString();
    final int deployIntVal = randomUUID().toString().hashCode();
    final String deployStringVal = randomUUID().toString();
    final ContractDefinition definition = ContractDefinition.newBuilder()
        .encodedContract(payload)
        .constructorArgs(deployKey, deployIntVal, deployStringVal)
        .build();

    // when
    final ContractTxHash deployTxHash = deploy(key, definition);

    // then
    final ContractTxReceipt deployTxReceipt = getContractTxReceipt(deployTxHash);
    assertEquals("CREATED", deployTxReceipt.getStatus());
    final ContractInterface contractInterface =
        getABI(deployTxReceipt.getContractAddress());
    final ContractResult queryResult = query(contractInterface, queryFunction, deployKey);
    final Data data = queryResult.bind(Data.class);
    assertEquals(deployIntVal, data.getIntVal());
    assertEquals(deployStringVal, data.getStringVal());
  }

  @Test
  public void shouldReDeployOnDeployedContract() throws Exception {
    // given
    final AergoKey key = createNewKey();
    final ContractDefinition definition = ContractDefinition.newBuilder()
        .encodedContract(payload)
        .build();
    // deploy
    final ContractTxHash deployTxHash = deploy(key, definition);
    final ContractTxReceipt deployTxReceipt = getContractTxReceipt(deployTxHash);
    final ContractInterface contractInterface = getABI(deployTxReceipt.getContractAddress());
    final ContractAddress deployedContract = contractInterface.getAddress();

    // when
    final String reDeployKey = randomUUID().toString();
    final int reDeployIntVal = randomUUID().toString().hashCode();
    final String reDeployStringVal = randomUUID().toString();
    final ContractDefinition newDefinition = ContractDefinition.newBuilder()
        .encodedContract(redeployPayload)
        .constructorArgs(reDeployKey, reDeployIntVal, reDeployStringVal)
        .build();
    final ContractTxHash reDeployTxHash = redeploy(key, deployedContract, newDefinition);

    // then
    final ContractTxReceipt reDeployTxReceipt = getContractTxReceipt(reDeployTxHash);
    assertEquals("RECREATED", reDeployTxReceipt.getStatus());
    final ContractInterface newContractInterface = getABI(reDeployTxReceipt.getContractAddress());
    assertEquals(deployedContract, newContractInterface.getAddress());
    final ContractResult newQueryResult = query(newContractInterface, queryFunction, reDeployKey);
    final Data newQueryData = newQueryResult.bind(Data.class);
    assertEquals(reDeployIntVal, newQueryData.getIntVal());
    assertEquals(reDeployStringVal, newQueryData.getStringVal());
  }

  @Test
  public void shouldHasAmountSameAsDeployedOne() throws Exception {
    // given
    final AergoKey key = createNewKey();
    final Aer expectedAmount = Aer.ONE;
    final ContractDefinition definition = ContractDefinition.newBuilder()
        .encodedContract(payload)
        .amount(expectedAmount)
        .build();

    // when
    final ContractTxHash deployTxHash = deploy(key, definition);

    // then
    final ContractTxReceipt deployTxReceipt = getContractTxReceipt(deployTxHash);
    assertEquals("CREATED", deployTxReceipt.getStatus());
    final ContractAddress contractAddress = deployTxReceipt.getContractAddress();
    final Aer actualAmount = getAmount(contractAddress);
    assertEquals(expectedAmount, actualAmount);
  }

  @Test
  public void testLuaContractDeployAndExecute() throws Exception {
    final AergoKey key = createNewKey();
    final ContractDefinition definition = ContractDefinition.newBuilder()
        .encodedContract(payload)
        .build();
    final ContractTxHash deployTxHash = deploy(key, definition);

    final ContractTxReceipt deployTxReceipt = getContractTxReceipt(deployTxHash);
    assertEquals("CREATED", deployTxReceipt.getStatus());

    final ContractInterface contractInterface =
        getABI(deployTxReceipt.getContractAddress());

    final ContractInvocation execution = contractInterface.newInvocationBuilder()
        .function(executeFunction)
        .args(new Object[] {executeKey, executeIntVal, executeStringVal})
        .build();
    final ContractTxHash executionTxHash = execute(key, execution);

    final ContractTxReceipt executionReceipt = getContractTxReceipt(executionTxHash);
    assertEquals("SUCCESS", executionReceipt.getStatus());

    final ContractResult queryResult = query(contractInterface, queryFunction, executeKey);
    final Data data = queryResult.bind(Data.class);
    assertEquals(executeIntVal, data.getIntVal());
    assertEquals(executeStringVal, data.getStringVal());
  }

  @Test
  public void testLuaContractDeployAndExecuteWithAmount() throws Exception {
    final AergoKey key = createNewKey();
    final Aer expectedAmount = Aer.ONE;

    final ContractDefinition definition = ContractDefinition.newBuilder()
        .encodedContract(payload).build();
    final ContractTxHash deployTxHash = deploy(key, definition);

    final ContractTxReceipt deployTxReceipt = getContractTxReceipt(deployTxHash);
    assertEquals("CREATED", deployTxReceipt.getStatus());

    final ContractAddress contractAddress = deployTxReceipt.getContractAddress();
    final ContractInterface contractInterface =
        getABI(contractAddress);

    final ContractInvocation execution = contractInterface.newInvocationBuilder()
        .function(executeFunction)
        .args(new Object[] {executeKey, executeIntVal, executeStringVal})
        .amount(expectedAmount)
        .build();
    execute(key, execution);

    final Aer actualAmount = getAmount(contractAddress);
    assertEquals(expectedAmount, actualAmount);
  }

  @Test
  public void testLuaContractDeployAndExecuteWithEscapeString() throws Exception {
    final AergoKey key = createNewKey();
    final ContractDefinition definition = ContractDefinition.newBuilder()
        .encodedContract(payload).build();
    final ContractTxHash deployTxHash = deploy(key, definition);

    final ContractTxReceipt deployTxReceipt = getContractTxReceipt(deployTxHash);
    assertEquals("CREATED", deployTxReceipt.getStatus());

    final ContractInterface contractInterface =
        getABI(deployTxReceipt.getContractAddress());

    final String escapeString = "\b\t\f\n\r";
    final ContractInvocation execution = contractInterface.newInvocationBuilder()
        .function(executeFunction)
        .args(new Object[] {executeKey, executeIntVal, escapeString})
        .build();
    final ContractTxHash executionTxHash = execute(key, execution);

    final ContractTxReceipt executionReceipt = getContractTxReceipt(executionTxHash);
    assertEquals("SUCCESS", executionReceipt.getStatus());

    final ContractResult queryResult = query(contractInterface, queryFunction, executeKey);
    final Data data = queryResult.bind(Data.class);
    assertEquals(executeIntVal, data.getIntVal());
    assertEquals(escapeString, data.getStringVal());
  }

  @Test
  public void testLuaContractDeployWithInvalidNonce() throws Exception {
    final AergoKey key = createNewKey();
    final ContractDefinition definition = ContractDefinition.newBuilder()
        .encodedContract(payload).build();
    try {
      aergoClient.getContractOperation().deploy(key, definition, 0L, Fee.getDefaultFee());
      fail();
    } catch (Exception e) {
      // good we expected this
    }
  }

  @Test
  public void testLuaContractExecuteWithInvalidNonce() throws Exception {
    final AergoKey key = createNewKey();
    final ContractDefinition definition = ContractDefinition.newBuilder()
        .encodedContract(payload).build();
    final ContractTxHash deployTxHash = deploy(key, definition);

    final ContractTxReceipt deployTxReceipt = getContractTxReceipt(deployTxHash);
    assertEquals("CREATED", deployTxReceipt.getStatus());

    final ContractInterface contractInterface =
        getABI(deployTxReceipt.getContractAddress());

    final ContractInvocation execution = contractInterface.newInvocationBuilder()
        .function(executeFunction)
        .args(new Object[] {executeKey, executeIntVal, executeStringVal})
        .build();
    try {
      aergoClient.getContractOperation().execute(key, execution, 0L, Fee.getDefaultFee());
      fail();
    } catch (Exception e) {
      // good we expected this
    }
  }

  @Test
  public void testLuaContractEvent() {
    final AergoKey key = createNewKey();
    final ContractDefinition definition = ContractDefinition.newBuilder()
        .encodedContract(payload)
        .build();
    final ContractTxHash deployTxHash = deploy(key, definition);

    final ContractTxReceipt deployTxReceipt = getContractTxReceipt(deployTxHash);
    assertEquals("CREATED", deployTxReceipt.getStatus());

    final ContractAddress contractAddress = deployTxReceipt.getContractAddress();
    final ContractInterface contractInterface = getABI(contractAddress);

    final EventFilter eventFilter = EventFilter.newBuilder(contractAddress)
        .recentBlockCount(100)
        .build();

    final CountDownLatch latch = new CountDownLatch(2);
    final Subscription<Event> subscription =
        aergoClient.getContractOperation().subscribeEvent(eventFilter,
            new StreamObserver<Event>() {

              @Override
              public void onNext(Event value) {
                latch.countDown();
              }

              @Override
              public void onError(Throwable t) {
                // do nothing
              }

              @Override
              public void onCompleted() {
                // do nothing
              }
            });

    final ContractInvocation execution = contractInterface.newInvocationBuilder()
        .function(executeFunction)
        .args(new Object[] {executeKey, executeIntVal, executeStringVal})
        .build();

    execute(key, execution);
    execute(key, execution);

    subscription.unsubscribe();

    execute(key, execution);

    assertTrue(subscription.isUnsubscribed());
    assertEquals(0L, latch.getCount());
  }

  @Test
  public void testLuaContractEventWithEventNameFilter() {
    final AergoKey key = createNewKey();
    final ContractDefinition definition = ContractDefinition.newBuilder()
        .encodedContract(payload)
        .build();
    final ContractTxHash deployTxHash = deploy(key, definition);

    final ContractTxReceipt deployTxReceipt = getContractTxReceipt(deployTxHash);
    assertEquals("CREATED", deployTxReceipt.getStatus());

    final ContractAddress contractAddress = deployTxReceipt.getContractAddress();
    final ContractInterface contractInterface = getABI(contractAddress);

    final EventFilter eventFilter = EventFilter.newBuilder(contractAddress)
        .eventName("set")
        .build();

    final AtomicInteger count = new AtomicInteger(2);
    final Subscription<Event> subscription =
        aergoClient.getContractOperation().subscribeEvent(eventFilter,
            new StreamObserver<Event>() {

              @Override
              public void onNext(Event value) {
                count.decrementAndGet();
              }

              @Override
              public void onError(Throwable t) {
                // do nothing
              }

              @Override
              public void onCompleted() {
                // do nothing
              }
            });

    final ContractInvocation targetExec = contractInterface.newInvocationBuilder()
        .function(executeFunction)
        .args(new Object[] {executeKey, executeIntVal, executeStringVal})
        .build();

    final ContractInvocation otherExec = contractInterface.newInvocationBuilder()
        .function("set2")
        .args(new Object[] {randomUUID().toString(), randomUUID().toString().hashCode(),
            randomUUID().toString()})
        .build();

    execute(key, targetExec);
    execute(key, targetExec);

    execute(key, otherExec);
    execute(key, otherExec);
    execute(key, otherExec);

    subscription.unsubscribe();

    execute(key, targetExec);

    assertTrue(subscription.isUnsubscribed());
    assertEquals(0, count.get());
  }

  @Test
  public void testLuaContractEventWithArgFilter() {
    final AergoKey key = createNewKey();
    final ContractDefinition definition = ContractDefinition.newBuilder()
        .encodedContract(payload)
        .build();
    final ContractTxHash deployTxHash = deploy(key, definition);

    final ContractTxReceipt deployTxReceipt = getContractTxReceipt(deployTxHash);
    assertEquals("CREATED", deployTxReceipt.getStatus());

    final ContractAddress contractAddress = deployTxReceipt.getContractAddress();
    final ContractInterface contractInterface = getABI(contractAddress);

    final EventFilter eventFilter = EventFilter.newBuilder(contractAddress)
        .args(asList(new Object[] {executeKey}))
        .build();

    final AtomicInteger count = new AtomicInteger(2);
    final Subscription<Event> subscription =
        aergoClient.getContractOperation().subscribeEvent(eventFilter,
            new StreamObserver<Event>() {

              @Override
              public void onNext(Event value) {
                count.decrementAndGet();
              }

              @Override
              public void onError(Throwable t) {
                // do nothing
              }

              @Override
              public void onCompleted() {
                // do nothing
              }
            });

    final ContractInvocation targetExec = contractInterface.newInvocationBuilder()
        .function("set2")
        .args(new Object[] {executeKey, executeIntVal, executeStringVal})
        .build();

    final ContractInvocation otherExec = contractInterface.newInvocationBuilder()
        .function("set2")
        .args(new Object[] {randomUUID().toString(), executeIntVal, executeStringVal})
        .build();

    execute(key, targetExec);
    execute(key, targetExec);

    execute(key, otherExec);
    execute(key, otherExec);
    execute(key, otherExec);

    subscription.unsubscribe();

    execute(key, targetExec);

    assertTrue(subscription.isUnsubscribed());
    assertEquals(0, count.get());
  }

  @Test
  public void testLuaContractEventWithEventNameAndArgFilter() {
    final AergoKey key = createNewKey();
    final ContractDefinition definition = ContractDefinition.newBuilder()
        .encodedContract(payload)
        .build();
    final ContractTxHash deployTxHash = deploy(key, definition);

    final ContractTxReceipt deployTxReceipt = getContractTxReceipt(deployTxHash);
    assertEquals("CREATED", deployTxReceipt.getStatus());

    final ContractAddress contractAddress = deployTxReceipt.getContractAddress();
    final ContractInterface contractInterface = getABI(contractAddress);

    final EventFilter eventFilter = EventFilter.newBuilder(contractAddress)
        .eventName("set")
        .args(asList(new Object[] {executeKey}))
        .build();

    final AtomicInteger count = new AtomicInteger(2);
    final Subscription<Event> subscription =
        aergoClient.getContractOperation().subscribeEvent(eventFilter,
            new StreamObserver<Event>() {

              @Override
              public void onNext(Event value) {
                count.decrementAndGet();
              }

              @Override
              public void onError(Throwable t) {
                // do nothing
              }

              @Override
              public void onCompleted() {
                // do nothing
              }
            });

    final ContractInvocation targetExec = contractInterface.newInvocationBuilder()
        .function(executeFunction)
        .args(new Object[] {executeKey, executeIntVal, executeStringVal})
        .build();

    final ContractInvocation otherExec = contractInterface.newInvocationBuilder()
        .function(executeFunction)
        .args(new Object[] {randomUUID().toString(), executeIntVal, executeStringVal})
        .build();

    execute(key, targetExec);
    execute(key, targetExec);

    execute(key, otherExec);
    execute(key, otherExec);
    execute(key, otherExec);

    subscription.unsubscribe();

    execute(key, targetExec);

    assertTrue(subscription.isUnsubscribed());
    assertEquals(0, count.get());
  }

  @ToString
  public static class Data {

    @Getter
    @Setter
    protected int intVal;

    @Getter
    @Setter
    protected String stringVal;
  }

  protected ContractTxHash deploy(final Signer signer, final ContractDefinition definition) {
    final ContractTxHash txHash = aergoClient.getContractOperation().deploy(signer,
        definition, nonceProvider.incrementAndGetNonce(signer.getPrincipal()), Fee.getDefaultFee());
    waitForNextBlockToGenerate();
    return txHash;
  }

  protected ContractTxHash redeploy(final Signer signer, final ContractAddress contractAddress,
      final ContractDefinition definition) {
    final ContractTxHash txHash =
        aergoClient.getContractOperation().redeploy(signer, contractAddress, definition,
            nonceProvider.incrementAndGetNonce(signer.getPrincipal()), Fee.getDefaultFee());
    waitForNextBlockToGenerate();
    return txHash;
  }

  protected ContractTxReceipt getContractTxReceipt(final ContractTxHash contractTxHash) {
    final ContractTxReceipt receipt = aergoClient.getContractOperation().getReceipt(contractTxHash);
    return receipt;
  }

  protected ContractInterface getABI(final ContractAddress contractAddress) {
    final ContractInterface contractInterface =
        aergoClient.getContractOperation().getContractInterface(contractAddress);
    return contractInterface;
  }

  protected ContractTxHash execute(final Signer signer, final ContractInvocation execution) {
    final ContractTxHash txHash = aergoClient.getContractOperation().execute(signer,
        execution, nonceProvider.incrementAndGetNonce(signer.getPrincipal()), Fee.getDefaultFee());
    waitForNextBlockToGenerate();
    return txHash;
  }

  protected ContractResult query(final ContractInterface contractInterface, final String function,
      final Object... args) {
    final ContractInvocation query =
        contractInterface.newInvocationBuilder().function(function).args(args).build();
    return aergoClient.getContractOperation().query(query);
  }

}
