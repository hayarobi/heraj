/*
 * @copyright defined in LICENSE.txt
 */

package hera.api.transaction;

import hera.AbstractTestCase;
import hera.api.model.BytesValue;
import hera.api.model.ContractDefinition;
import hera.api.model.ContractDefinitionV3;
import hera.api.model.ContractDefinitionV4;
import hera.util.Base58Utils;
import org.junit.Test;

import static hera.api.transaction.ContractDefinitionV4PayloadConverter.LENGTH_HOLDER_OFFSET;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class ContractDefinitionNewPayloadConverterTest extends AbstractTestCase {
  // Compiled binary of hello.lua
  private static final String encodedHelloContract =
      "5j3524noj2Zj1XzgGd6nMc5rgpLCqMhYvGMsRxdexbsPLei9cvoxfYEyhqrjFujZBkLwV2eS9vYyaFg3" +
          "w26eM4tHGzUoNauZNp9dbVLxvck4pGpy9rGXTWmBEq3BNC6vwHvzA7nXgZJZT89m2oCmTGsyrey6zeXu" +
          "ivWtxCqKukCLHBsNpaRFyzLFsvp7QDvQB9hA22jaTzT34VuvezRqNi7m9na5PYQaBbbiXhZNYZbPkE8Y" +
          "9pMGh";

  protected final String emptyEncodedContract =
      Base58Utils.encodeWithCheck(new byte[] {ContractDefinition.CONTRACT_VERSION_PREFIX});


  protected final String contractSource = "function constructor(key, arg1, arg2)\n" +
      "  if key ~= nil then\n" +
      "    system.setItem(key, {intVal=arg1, stringVal=arg2})\n" +
      "  end\n" +
      "end\n" +
      "\n" +
      "function set(key, arg1, arg2)\n" +
      "  contract.event(\"set\", key, arg1, arg2)\n" +
      "  system.setItem(key, {intVal=arg1, stringVal=arg2})\n" +
      "end\n" +
      "\n" +
      "function set2(key, arg1, arg2)\n" +
      "  contract.event(\"set2\", key, arg1, arg2)\n" +
      "  system.setItem(key, {intVal=arg1, stringVal=arg2})\n" +
      "end\n" +
      "\n" +
      "function get(key)\n" +
      "  return system.getItem(key)\n" +
      "end\n" +
      "\n" +
      "abi.register(set, set2)\n" +
      "abi.register_view(get)";
  protected final byte[] sourceBytes = contractSource.getBytes();

  @Test
  public void testConvertEmptyV3ToPayload() {
    final PayloadConverter<ContractDefinition> converter = new ContractDefinitionNewPayloadConverter();
    final ContractDefinition definition = ContractDefinition.newBuilder()
        .encodedContract(emptyEncodedContract)
        .constructorArgs("1", "2")
        .build();
    assertTrue(definition instanceof ContractDefinitionV3);

    final BytesValue payload = converter.convertToPayload(definition);
    assertNotNull(payload);
  }

  @Test
  public void testConvertV3ToPayload() {
    final PayloadConverter<ContractDefinition> converter = new ContractDefinitionNewPayloadConverter();
    final ContractDefinition definition = ContractDefinition.newBuilder()
        .encodedContract(encodedHelloContract)
        .build();
    assertTrue(definition instanceof ContractDefinitionV3);
    final BytesValue payload = converter.convertToPayload(definition);
    assertNotNull(payload);
  }


  @Test
  public void testConvertV4ToPayload() {
    final PayloadConverter<ContractDefinition> converter = new ContractDefinitionNewPayloadConverter();
    final ContractDefinition def = ContractDefinition.newBuilder()
        .contractSource(sourceBytes)
        .build();
    assertTrue(def instanceof ContractDefinitionV4);
    final ContractDefinitionV4 definition = (ContractDefinitionV4) def;
    final BytesValue payload = converter.convertToPayload(definition);
    assertNotNull(payload);
    assertEquals(sourceBytes.length+4, payload.length());

    ContractDefinition parsed = converter.parseToModel(payload);
    assertNotNull(parsed);
    assertTrue(parsed instanceof ContractDefinitionV4);
    assertEquals(definition.getConstructorArgs(), parsed.getConstructorArgs());
    ContractDefinitionV4 parsedV4 = (ContractDefinitionV4) parsed;
    assertEquals(definition.getContractSourceBytes(), parsedV4.getContractSourceBytes());
  }


  @Test
  public void testConvertV4ToPayloadWithArgs() {
    final PayloadConverter<ContractDefinition> converter = new ContractDefinitionNewPayloadConverter();
    final ContractDefinition def = ContractDefinition.newBuilder()
        .contractSource(sourceBytes)
        .constructorArgs("1", "2")
        .build();
    assertTrue(def instanceof ContractDefinitionV4);
    final ContractDefinitionV4 definition = (ContractDefinitionV4) def;
    // arguments will be set to json array ["1","2"] (9 bytes)
    final BytesValue payload = converter.convertToPayload(definition);
    assertNotNull(payload);
    assertEquals(sourceBytes.length+ LENGTH_HOLDER_OFFSET+9, payload.length());

    ContractDefinition parsed = converter.parseToModel(payload);
    assertNotNull(parsed);
    assertTrue(parsed instanceof ContractDefinitionV4);
    assertEquals(definition.getConstructorArgs(), parsed.getConstructorArgs());
    ContractDefinitionV4 parsedV4 = (ContractDefinitionV4) parsed;
    assertEquals(definition.getContractSourceBytes(), parsedV4.getContractSourceBytes());
  }

}
