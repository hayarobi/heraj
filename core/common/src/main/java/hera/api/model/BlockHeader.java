/*
 * @copyright defined in LICENSE.txt
 */

package hera.api.model;

import static hera.util.ValidationUtils.assertNotNull;

import hera.exception.HerajException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class BlockHeader {

  @Getter
  protected final BlockHash hash;

  @Getter
  protected final BlockHash previousHash;

  @Getter
  protected final long blockNumber;

  @Getter
  protected final long timestamp;

  @Getter
  protected final BlockHash rootHash;

  @Getter
  protected final TxHash txRootHash;

  @Getter
  protected final Hash receiptRootHash;

  @Getter
  protected final long confirmsCount;

  @Getter
  protected final Hash publicKey;

  @Getter
  protected final Hash sign;

  @Getter
  protected final AccountAddress coinbaseAccount;

  /**
   * BlockHeader constructor.
   *
   * @param hash a block hash
   * @param previousHash a previous block hash
   * @param blockNumber a block number
   * @param timestamp a timestamp
   * @param rootHash a block root hash
   * @param txRootHash a tx root hash
   * @param receiptRootHash a receipt root hash
   * @param confirmsCount confirmsCount
   * @param publicKey a public key
   * @param sign a sign
   * @param coinbaseAccount a coinbase account
   */
  public BlockHeader(final BlockHash hash, final BlockHash previousHash, final long blockNumber,
      final long timestamp, final BlockHash rootHash, final TxHash txRootHash,
      final Hash receiptRootHash, final long confirmsCount, final Hash publicKey, final Hash sign,
      final AccountAddress coinbaseAccount) {
    assertNotNull(hash, new HerajException("Block hash must not null"));
    this.hash = hash;
    this.previousHash = null != previousHash ? previousHash : BlockHash.of(BytesValue.EMPTY);
    this.blockNumber = blockNumber;
    this.timestamp = timestamp;
    this.rootHash = null != rootHash ? rootHash : BlockHash.of(BytesValue.EMPTY);
    this.txRootHash = null != txRootHash ? txRootHash : TxHash.of(BytesValue.EMPTY);
    this.receiptRootHash = null != receiptRootHash ? receiptRootHash : Hash.of(BytesValue.EMPTY);
    this.confirmsCount = confirmsCount;
    this.publicKey = null != publicKey ? publicKey : Hash.of(BytesValue.EMPTY);
    this.sign = null != sign ? sign : Hash.of(BytesValue.EMPTY);
    this.coinbaseAccount =
        null != coinbaseAccount ? coinbaseAccount : AccountAddress.of(BytesValue.EMPTY);
  }

}
