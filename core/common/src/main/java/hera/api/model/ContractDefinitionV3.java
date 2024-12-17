package hera.api.model;

import hera.annotation.ApiAudience;
import hera.annotation.ApiStability;
import hera.api.encode.Decoder;
import hera.exception.HerajException;
import hera.util.BytesValueUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.StringReader;
import java.util.List;

import static hera.util.BytesValueUtils.trimPrefix;
import static hera.util.IoUtils.from;
import static hera.util.ValidationUtils.assertNotNull;

@ApiAudience.Public
@ApiStability.Unstable
@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class ContractDefinitionV3 extends ContractDefinition implements Payload {

  public static final byte PAYLOAD_VERSION = (byte) 0xC0;

  @ToString.Exclude
  protected final BytesValue decodedContract;

  protected final String encodedContract;

  ContractDefinitionV3(final String encodedContract, final List<Object> args,
                       final Aer amount) {
    super(ContractVersion.V3, args, amount);
    assertNotNull(encodedContract, "Encoded contract must not null");
    assertNotNull(args, "Args must not null");
    assertNotNull(amount, "Amount must not null");
    try {
      final Decoder decoder = Decoder.Base58Check;
      final byte[] raw = from(decoder.decode(new StringReader(encodedContract)));
      final BytesValue withVersion = BytesValue.of(raw);
      if (!hasPayloadVersion(withVersion)) {
        throw new HerajException("Encoded contract doesn't have a version");
      }

      this.decodedContract = trimPrefix(withVersion);
      this.encodedContract = encodedContract;
    } catch (HerajException e) {
      throw e;
    } catch (Exception e) {
      throw new HerajException(e);
    }
  }

  protected boolean hasPayloadVersion(final BytesValue bytesValue) {
    return BytesValueUtils.validatePrefix(bytesValue, PAYLOAD_VERSION);
  }

  @Override
  public BytesValue getRawContract() {
    return getDecodedContract();
  }
}
