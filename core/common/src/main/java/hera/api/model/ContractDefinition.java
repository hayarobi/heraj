/*
 * @copyright defined in LICENSE.txt
 */

package hera.api.model;

import hera.annotation.ApiAudience;
import hera.annotation.ApiStability;
import hera.exception.HerajException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

import static hera.util.ValidationUtils.assertNotNull;
import static java.util.Collections.unmodifiableList;

/**
 * Base Class for deploying smart contract
 */
@ApiAudience.Public
@ApiStability.Unstable
@Getter
@ToString
@EqualsAndHashCode
public abstract class ContractDefinition implements Payload {

  public static final byte CONTRACT_VERSION_PREFIX = (byte) 0xC0;

  public static ContractDefinitionBuilder newBuilder() {
    return new ContractDefinitionBuilder();
  }

  /**
   * Version indicates the hard fork version that this object will be used.
   */
  protected final ContractVersion version;
  protected final List<Object> constructorArgs;

  protected final Aer amount;

  protected ContractDefinition(ContractVersion version, final List<Object> args,
                               final Aer amount) {
    assertNotNull(args, "Args must not null");
    assertNotNull(amount, "Amount must not null");
    this.version = version;
    try {
      this.constructorArgs = unmodifiableList(args);
      this.amount = amount;
    } catch (HerajException e) {
      throw e;
    } catch (Exception e) {
      throw new HerajException(e);
    }
  }

  public abstract BytesValue getRawContract();

  public enum ContractVersion {
    V3,
    V4,
    UNDEFINED;
  }
}
