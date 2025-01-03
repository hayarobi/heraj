/*
 * @copyright defined in LICENSE.txt
 */

package hera.transport;

import static org.slf4j.LoggerFactory.getLogger;

import hera.annotation.ApiAudience;
import hera.annotation.ApiStability;
import hera.api.function.Function1;
import hera.api.model.Transaction;
import hera.api.model.Transaction.TxType;
import org.slf4j.Logger;
import types.Blockchain;

@ApiAudience.Private
@ApiStability.Unstable
public class TransactionTypeConverterFactory {

  protected final transient Logger logger = getLogger(getClass());

  protected final Function1<Transaction.TxType, Blockchain.TxType> domainConverter =
      new Function1<Transaction.TxType, Blockchain.TxType>() {

        @Override
        public Blockchain.TxType apply(final Transaction.TxType domainTxType) {
          logger.trace("Rpc transaction type to convert: {}", domainTxType);
          switch (domainTxType) {
            case NORMAL:
              return Blockchain.TxType.NORMAL;
            case GOVERNANCE:
              return Blockchain.TxType.GOVERNANCE;
            case REDEPLOY:
              return Blockchain.TxType.REDEPLOY;
            case FEE_DELEGATION:
              return Blockchain.TxType.FEEDELEGATION;
            case TRANSFER:
              return Blockchain.TxType.TRANSFER;
            case CALL:
              return Blockchain.TxType.CALL;
            case DEPLOY:
              return Blockchain.TxType.DEPLOY;
            case MULTICALL:
              return Blockchain.TxType.MULTICALL;
            default:
              return Blockchain.TxType.UNRECOGNIZED;
          }
        }
      };

  protected final Function1<Blockchain.TxType, Transaction.TxType> rpcConverter =
      new Function1<Blockchain.TxType, Transaction.TxType>() {

        @Override
        public TxType apply(Blockchain.TxType rpcTxType) {
          logger.trace("Domain transaction type to convert: {}", rpcTxType);
          switch (rpcTxType) {
            case NORMAL:
              return Transaction.TxType.NORMAL;
            case GOVERNANCE:
              return Transaction.TxType.GOVERNANCE;
            case REDEPLOY:
              return Transaction.TxType.REDEPLOY;
            case FEEDELEGATION:
              return Transaction.TxType.FEE_DELEGATION;
            case TRANSFER:
              return Transaction.TxType.TRANSFER;
            case CALL:
              return Transaction.TxType.CALL;
            case DEPLOY:
              return Transaction.TxType.DEPLOY;
            case MULTICALL:
              return Transaction.TxType.MULTICALL;
            default:
              return Transaction.TxType.UNRECOGNIZED;
          }
        }
      };

  public ModelConverter<Transaction.TxType, Blockchain.TxType> create() {
    return new ModelConverter<>(domainConverter, rpcConverter);
  }

}
