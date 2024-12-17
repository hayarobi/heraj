/*
 * @copyright defined in LICENSE.txt
 */

package hera.api.transaction;

import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.io.LittleEndianDataInputStream;
import hera.annotation.ApiAudience;
import hera.annotation.ApiStability;
import hera.api.model.*;
import hera.exception.HerajException;
import hera.util.Base58Utils;
import hera.util.BytesValueUtils;
import hera.util.HexUtils;
import hera.util.LittleEndianDataOutputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.slf4j.Logger;

@ApiAudience.Public
@ApiStability.Unstable
public class ContractDefinitionV3PayloadConverter implements PayloadConverter<ContractDefinitionV3> {

  public static final int LENGTH_HOLDER_OFFSET = 4;
  protected final Logger logger = getLogger(getClass());

  protected final JsonMapper mapper = new AergoJsonMapper();

  @Override
  public BytesValue convertToPayload(final ContractDefinitionV3 contractDefinition) {
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

  @Override
  public ContractDefinitionV3 parseToModel(final BytesValue payload) {
    try {
      logger.trace("parse to ContractDefinitionV3 from bytes {}", HexUtils.encode(payload.getValue()));
      LittleEndianDataInputStream dataIn = new LittleEndianDataInputStream(payload.getInputStream());
      final int codeEndsOffset = dataIn.readInt();
      int byteCodeLen = codeEndsOffset - LENGTH_HOLDER_OFFSET;
      final byte[] decodedContract = new byte[byteCodeLen];

      int readn = dataIn.read(decodedContract);
      if( readn != byteCodeLen ) {
        logger.debug("invalid payload format: code length is to short. expected {}, but actually {}",
            byteCodeLen, readn);
        throw new HerajException("invalid payload format");
      }
      final String encodedContract =
          Base58Utils.encodeWithCheck(BytesValueUtils.append(decodedContract, ContractDefinition.CONTRACT_VERSION_PREFIX));
      logger.trace("encodedContract: {}", encodedContract);
      int argsLen = payload.getValue().length - codeEndsOffset;
      ContractDefinitionBuilder builder = ContractDefinitionV3.newBuilder().encodedContract(encodedContract);
      if (argsLen > 0) {
        final byte[] argsBytes = new byte[argsLen];
        dataIn.readFully(argsBytes);
        List<Object> args = mapper.unmarshal(BytesValue.of(argsBytes), List.class);
        return (ContractDefinitionV3)builder.constructorArgs(args).build();
      } else if( argsLen == 0 ) {
        return (ContractDefinitionV3)builder.build();
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
