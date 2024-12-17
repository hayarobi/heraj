package hera.api.model;

import hera.util.ValidationUtils;

import java.util.List;

import static hera.util.ValidationUtils.assertNotNull;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public class ContractDefinitionBuilder {

  private ContractDefinition.ContractVersion version = ContractDefinition.ContractVersion.UNDEFINED;
  protected String encodedContract;
  protected byte[] contractSource;

  protected List<Object> constructorArgs = emptyList();

  protected Aer amount = Aer.EMPTY;

  public ContractDefinitionBuilder encodedContract(final String encodedContract) {
    ValidationUtils.assertTrue(version == ContractDefinition.ContractVersion.UNDEFINED
    || version == ContractDefinition.ContractVersion.V3 );
    this.encodedContract = encodedContract;
    version = ContractDefinition.ContractVersion.V3;
    return this;
  }

  public ContractDefinitionBuilder contractSource(final byte[] contractSource) {
    ValidationUtils.assertTrue(version == ContractDefinition.ContractVersion.UNDEFINED
        || version == ContractDefinition.ContractVersion.V4 );
    this.contractSource = contractSource;
    version = ContractDefinition.ContractVersion.V4;
    return this;
  }

  public ContractDefinitionBuilder amount(final Aer amount) {
    this.amount = amount;
    return this;
  }

  public ContractDefinitionBuilder constructorArgs(final List<Object> args) {
    if (null != args) {
      this.constructorArgs = args;
    }
    return this;
  }

  public ContractDefinitionBuilder constructorArgs(final Object... args) {
    if (null != args) {
      this.constructorArgs = asList(args);
    }
    return this;
  }

  public ContractDefinition build() {
    switch (version) {
      case V3:
        assertNotNull(encodedContract, "Encoded contract must not null");
        return new ContractDefinitionV3(encodedContract, constructorArgs, amount);
      case V4:
        assertNotNull(contractSource, "Contract source must not null");
        return new ContractDefinitionV4(contractSource, constructorArgs, amount);
      default:
        throw new IllegalStateException("No contract was");
    }
  }
}
