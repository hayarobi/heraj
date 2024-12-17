/*
 * @copyright defined in LICENSE.txt
 */

package hera.api.transaction;

import com.google.common.io.LittleEndianDataInputStream;
import hera.annotation.ApiAudience;
import hera.annotation.ApiStability;
import hera.api.model.*;
import hera.exception.HerajException;
import hera.util.LittleEndianDataOutputStream;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Payload converter for contract deploy since hardfork V4
 */
@ApiAudience.Public
@ApiStability.Unstable
public class ContractDefinitionV4PayloadConverter implements PayloadConverter<ContractDefinitionV4> {

  public static final int LENGTH_HOLDER_OFFSET = 4;
  protected final Logger logger = getLogger(getClass());

  protected final JsonMapper mapper = new AergoJsonMapper();

  @Override
  public BytesValue convertToPayload(final ContractDefinitionV4 contractDefinition) {
    try {
      logger.debug("Convert to payload from {}", contractDefinition);
      final byte[] rawContract = contractDefinition.getRawContract().getValue();
      final ByteArrayOutputStream rawStream = new ByteArrayOutputStream();
      final LittleEndianDataOutputStream dataOut = new LittleEndianDataOutputStream(rawStream);
      try {
        int codeEndsOffset = rawContract.length + LENGTH_HOLDER_OFFSET;
        dataOut.writeInt(codeEndsOffset);
        dataOut.write(rawContract);
        if (!contractDefinition.getConstructorArgs().isEmpty()) {
          final BytesValue constructorArgs =
              mapper.marshal(contractDefinition.getConstructorArgs());
          dataOut.write(constructorArgs.getValue());
        }
      } finally {
        dataOut.close();
      }
      return BytesValue.of(rawStream.toByteArray());
    } catch (HerajException e) {
      throw e;
    } catch (Exception e) {
      throw new HerajException(e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public ContractDefinitionV4 parseToModel(final BytesValue payload) {
    try {
      logger.debug("Convert to ContractDefinitionV4 from {}", payload);
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
