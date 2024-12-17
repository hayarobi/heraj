/*
 * @copyright defined in LICENSE.txt
 */

package hera.api.transaction;

import static hera.api.transaction.ContractDefinitionV4PayloadConverter.LENGTH_HOLDER_OFFSET;
import static org.junit.Assert.*;

import hera.AbstractTestCase;
import hera.api.model.BytesValue;
import hera.api.model.ContractDefinition;
import hera.api.model.ContractDefinitionV3;
import hera.api.model.ContractDefinitionV4;
import hera.util.Base58Utils;
import org.junit.Test;

public class ContractDefinitionV3PayloadConverterTest extends AbstractTestCase {
  // Compiled binary of hello.lua
  private static final String encodedHelloContract =
      "5j3524noj2Zj1XzgGd6nMc5rgpLCqMhYvGMsRxdexbsPLei9cvoxfYEyhqrjFujZBkLwV2eS9vYyaFg3" +
      "w26eM4tHGzUoNauZNp9dbVLxvck4pGpy9rGXTWmBEq3BNC6vwHvzA7nXgZJZT89m2oCmTGsyrey6zeXu" +
      "ivWtxCqKukCLHBsNpaRFyzLFsvp7QDvQB9hA22jaTzT34VuvezRqNi7m9na5PYQaBbbiXhZNYZbPkE8Y" +
      "9pMGh";

  protected final String emptyEncodedContract =
      Base58Utils.encodeWithCheck(new byte[] {ContractDefinition.CONTRACT_VERSION_PREFIX});

  @Test
  public void testConvertEmptyToPayload() {
    final PayloadConverter<ContractDefinitionV3> converter = new ContractDefinitionV3PayloadConverter();
    final ContractDefinition definition = ContractDefinition.newBuilder()
        .encodedContract(emptyEncodedContract)
        .constructorArgs("1", "2")
        .build();
    assertTrue(definition instanceof ContractDefinitionV3);
    final ContractDefinitionV3 definitionV3 = (ContractDefinitionV3) definition;

    final BytesValue payload = converter.convertToPayload(definitionV3);
    assertNotNull(payload);
    int contractLength = definition.getRawContract().length();
    assertEquals(contractLength + LENGTH_HOLDER_OFFSET+9, payload.length());

    ContractDefinitionV3 parsed = converter.parseToModel(payload);
    assertNotNull(parsed);
    assertEquals(definitionV3.getEncodedContract(), parsed.getEncodedContract());
    assertEquals(definition.getConstructorArgs(), parsed.getConstructorArgs());
  }

  @Test
  public void testConvertToPayload() {
    final PayloadConverter<ContractDefinitionV3> converter = new ContractDefinitionV3PayloadConverter();
    final ContractDefinition definition = ContractDefinition.newBuilder()
        .encodedContract(encodedHelloContract)
        .build();
    assertTrue(definition instanceof ContractDefinitionV3);
    final ContractDefinitionV3 definitionV3 = (ContractDefinitionV3) definition;

    final BytesValue payload = converter.convertToPayload(definitionV3);
    assertNotNull(payload);

    int contractLength = definition.getRawContract().length();
    assertEquals(contractLength + LENGTH_HOLDER_OFFSET, payload.length());

    ContractDefinitionV3 parsed = converter.parseToModel(payload);
    assertNotNull(parsed);
    assertEquals(definitionV3.getEncodedContract(), parsed.getEncodedContract());
    assertEquals(definition.getConstructorArgs(), parsed.getConstructorArgs());
  }


  @Test
  public void testConvertWithParamToPayload() {
    final PayloadConverter<ContractDefinitionV3> converter = new ContractDefinitionV3PayloadConverter();
    final ContractDefinition definition = ContractDefinition.newBuilder()
        .encodedContract(encodedHelloContract)
        .constructorArgs("1", "2")
        .build();
    assertTrue(definition instanceof ContractDefinitionV3);
    final ContractDefinitionV3 definitionV3 = (ContractDefinitionV3) definition;

    final BytesValue payload = converter.convertToPayload(definitionV3);
    assertNotNull(payload);
    // arguments will be set to json array ["1","2"] (9 bytes)
    int contractLength = definition.getRawContract().length();
    assertEquals(contractLength + LENGTH_HOLDER_OFFSET+9, payload.length());

    ContractDefinitionV3 parsed = converter.parseToModel(payload);
    assertNotNull(parsed);
    assertEquals(definitionV3.getEncodedContract(), parsed.getEncodedContract());
    assertEquals(definition.getConstructorArgs(), parsed.getConstructorArgs());
  }

}
