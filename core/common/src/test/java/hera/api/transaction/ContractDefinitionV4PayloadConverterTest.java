package hera.api.transaction;

import hera.api.model.BytesValue;
import hera.api.model.ContractDefinition;
import hera.api.model.ContractDefinitionV4;
import org.junit.Test;

import static hera.api.transaction.ContractDefinitionV4PayloadConverter.LENGTH_HOLDER_OFFSET;
import static org.junit.Assert.*;

public class ContractDefinitionV4PayloadConverterTest {
  protected final JsonMapper mapper = new AergoJsonMapper();

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
  public void testConvertToPayload() {
    final PayloadConverter<ContractDefinitionV4> converter = new ContractDefinitionV4PayloadConverter();
    final ContractDefinition def = ContractDefinition.newBuilder()
        .contractSource(sourceBytes)
        .build();
    assertTrue(def instanceof ContractDefinitionV4);
    final ContractDefinitionV4 definition = (ContractDefinitionV4) def;
    final BytesValue payload = converter.convertToPayload(definition);
    assertNotNull(payload);
    assertEquals(sourceBytes.length+4, payload.length());

    ContractDefinitionV4 parsed = converter.parseToModel(payload);
    assertNotNull(parsed);
    assertEquals(definition.getContractSourceBytes(), parsed.getContractSourceBytes());
    assertEquals(definition.getConstructorArgs(), parsed.getConstructorArgs());
  }


  @Test
  public void testConvertToPayloadWithArgs() {
    final PayloadConverter<ContractDefinitionV4> converter = new ContractDefinitionV4PayloadConverter();
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

    ContractDefinitionV4 parsed = converter.parseToModel(payload);
    assertNotNull(parsed);
    assertEquals(definition.getContractSourceBytes(), parsed.getContractSourceBytes());
    assertEquals(definition.getConstructorArgs(), parsed.getConstructorArgs());
  }

  @Test
  public void testConvertToPayloadWithMultiTypedArgs() {
    final PayloadConverter<ContractDefinitionV4> converter = new ContractDefinitionV4PayloadConverter();
    final ContractDefinition def = ContractDefinition.newBuilder()
        .contractSource(sourceBytes)
        .constructorArgs("abcd", 333L, "what")
        .build();
    assertTrue(def instanceof ContractDefinitionV4);
    final ContractDefinitionV4 definition = (ContractDefinitionV4) def;
    // arguments will be set to json array ["abcd",333,"what"] (19 bytes)
    final BytesValue payload = converter.convertToPayload(definition);
    assertNotNull(payload);
    assertEquals(sourceBytes.length+ LENGTH_HOLDER_OFFSET+19, payload.length());

    ContractDefinitionV4 parsed = converter.parseToModel(payload);
    assertNotNull(parsed);
    assertEquals(definition.getContractSourceBytes(), parsed.getContractSourceBytes());
    assertEquals(mapper.marshal(definition.getConstructorArgs()),
        mapper.marshal(parsed.getConstructorArgs()));
  }
}