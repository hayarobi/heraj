/*
 * @copyright defined in LICENSE.txt
 */

package hera.spec.resolver;

import static hera.api.model.BytesValue.of;
import static hera.util.ValidationUtils.assertNotNull;
import static java.util.UUID.randomUUID;

import hera.AbstractTestCase;
import hera.api.model.AccountAddress;
import hera.api.model.BytesValue;
import hera.api.model.ContractAddress;
import hera.api.model.ContractDefinition;
import hera.api.model.ContractFunction;
import hera.api.model.ContractInvocation;
import hera.api.model.PeerId;
import hera.key.AergoKeyGenerator;
import hera.spec.AddressSpec;
import hera.spec.PayloadSpec.Type;
import hera.util.Base58Utils;
import org.junit.Test;

public class PayloadResolverTest extends AbstractTestCase {

  protected final AccountAddress accountAddress =
      new AccountAddress(of(new byte[] {AddressSpec.PREFIX}));

  protected final ContractAddress contractAddress =
      new ContractAddress(of(new byte[] {AddressSpec.PREFIX}));

  protected final PayloadResolver resolver = new PayloadResolver();

  @Test
  public void testResolveOnContractDefinition() {
    final ContractDefinition definition = ContractDefinition.newBuilder()
        .encodedContract(
            Base58Utils.encodeWithCheck(new byte[] {ContractDefinition.PAYLOAD_VERSION}))
        .constructorArgs("1", "2")
        .build();
    final BytesValue payload = resolver.resolve(Type.ContractDefinition, definition);
    assertNotNull(payload);
  }

  @Test
  public void testResolveOnContractInvocation() {
    final ContractInvocation invocation = new ContractInvocation(contractAddress,
        new ContractFunction(randomUUID().toString()), randomUUID().toString());
    final BytesValue payload = resolver.resolve(Type.ContractInvocation, invocation);
    assertNotNull(payload);
  }

  @Test
  public void testResolveOnStake() {
    final BytesValue payload = resolver.resolve(Type.Stake);
    assertNotNull(payload);
  }

  @Test
  public void testResolveOnUnstake() {
    final BytesValue payload = resolver.resolve(Type.Unstake);
    assertNotNull(payload);
  }

  @Test
  public void testResolveOnVote() {
    final PeerId peerId = new PeerId(of(randomUUID().toString().getBytes()));
    final BytesValue payload = resolver.resolve(Type.Vote, peerId);
    assertNotNull(payload);
  }

  @Test
  public void testResolveOnCreateName() {
    final String name = randomUUID().toString();
    final BytesValue payload = resolver.resolve(Type.CreateName, name);
    assertNotNull(payload);
  }

  @Test
  public void testResolveOnUpdateName() {
    final String name = randomUUID().toString();
    final AccountAddress nextOwner = new AergoKeyGenerator().create().getAddress();
    final BytesValue payload = resolver.resolve(Type.UpdateName, name, nextOwner);
    assertNotNull(payload);
  }

}
