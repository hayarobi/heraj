/*
 * @copyright defined in LICENSE.txt
 */

package hera.api.transaction;

import com.google.common.io.LittleEndianDataInputStream;
import hera.annotation.ApiAudience;
import hera.annotation.ApiStability;
import hera.api.model.*;
import hera.exception.HerajException;
import org.slf4j.Logger;

import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

@ApiAudience.Public
@ApiStability.Unstable
public class ContractDefinitionNewPayloadConverter implements PayloadConverter<ContractDefinition> {

  public static final int LENGTH_HOLDER_OFFSET = 4;
  protected final Logger logger = getLogger(getClass());

  protected final JsonMapper mapper = new AergoJsonMapper();
  private final ContractDefinitionV3PayloadConverter v3PayloadConverter = new ContractDefinitionV3PayloadConverter();
  private final ContractDefinitionV4PayloadConverter v4PayloadConverter = new ContractDefinitionV4PayloadConverter();

  // FIXME avoid unchecked type casting
  @SuppressWarnings("unsafe")
  @Override
  public BytesValue convertToPayload(final ContractDefinition contractDefinition) {
    switch (contractDefinition.getVersion()) {
      case V3:
        return v3PayloadConverter.convertToPayload((ContractDefinitionV3) contractDefinition);
      case V4:
        return v4PayloadConverter.convertToPayload((ContractDefinitionV4) contractDefinition);
      default:
        throw new UnsupportedOperationException();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public ContractDefinition parseToModel(final BytesValue payload) {
    try {
      logger.debug("Convert to ContractDefinition from {}", payload);
      LittleEndianDataInputStream dataIn = new LittleEndianDataInputStream(payload.getInputStream());
      final int codeEndsOffset = dataIn.readInt();
      int sourceLen = codeEndsOffset - LENGTH_HOLDER_OFFSET;
      final byte[] source = new byte[sourceLen];
      dataIn.read(source);
      int argsLen = payload.getValue().length - codeEndsOffset;
      ContractDefinitionBuilder builder = ContractDefinitionV4.newBuilder().contractSource(source);
      if (argsLen > 0) {
        final byte[] argsBytes = new byte[argsLen];
        dataIn.readFully(argsBytes);
        List<Object> args = mapper.unmarshal(BytesValue.of(argsBytes), List.class);
        return (ContractDefinitionV4)builder.constructorArgs(args).build();
      } else if( argsLen == 0 ) {
        return (ContractDefinitionV4)builder.build();
      } else { // argsLen < 0
        throw new HerajException("invalid payload format");
      }
    } catch (Exception e) {
      if( e instanceof HerajException) {
        throw (HerajException)e;
      } else {
        throw new HerajException(e);
      }
    }
  }

}
