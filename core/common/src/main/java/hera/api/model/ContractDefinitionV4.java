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

import static hera.util.BytesValueUtils.trimPrefix;
import static hera.util.IoUtils.from;
import static hera.util.ValidationUtils.assertNotNull;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

@ApiAudience.Public
@ApiStability.Unstable
@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class ContractDefinitionV4 extends ContractDefinition implements Payload {

  @ToString.Exclude
  protected final BytesValue contractSourceBytes;

  protected ContractDefinitionV4(final byte[] contractSource, final List<Object> args,
                       final Aer amount) {
    super(ContractVersion.V4, args, amount);
    assertNotNull(contractSource, "Contract source must not null");
    try {
      contractSourceBytes = BytesValue.of(contractSource);
    } catch (HerajException e) {
      throw e;
    } catch (Exception e) {
      throw new HerajException(e);
    }
  }

  @Override
  public BytesValue getRawContract() {
    return getContractSourceBytes();
  }
}
