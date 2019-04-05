/*
 * @copyright defined in LICENSE.txt
 */

package hera.key;

import static org.slf4j.LoggerFactory.getLogger;

import hera.annotation.ApiAudience;
import hera.annotation.ApiStability;
import hera.api.model.AccountAddress;
import hera.api.model.BytesValue;
import hera.api.model.EncryptedPrivateKey;
import hera.api.model.RawTransaction;
import hera.api.model.Signature;
import hera.api.model.Transaction;
import hera.api.model.TxHash;
import hera.exception.HerajException;
import hera.exception.UnableToGenerateKeyException;
import hera.spec.resolver.AddressResolver;
import hera.spec.resolver.EncryptedPrivateKeyResolver;
import hera.spec.resolver.SignatureResolver;
import hera.spec.resolver.TransactionHashResolver;
import hera.util.Base64Utils;
import hera.util.NumberUtils;
import hera.util.pki.ECDSAKey;
import hera.util.pki.ECDSAKeyGenerator;
import hera.util.pki.ECDSASignature;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.slf4j.Logger;

@ApiAudience.Public
@ApiStability.Unstable
@EqualsAndHashCode(exclude = {"addressResolver", "transactionHashResolver", "signatureResolver",
    "privateKeyResolver"})
public class AergoKey implements KeyPair, Signer {

  /**
   * Create a key pair with encoded encrypted private key and password.
   *
   * @param encodedEncryptedPrivateKey base58 with checksum encoded encrypted private key
   * @param password password to decrypt
   * @return key instance
   *
   * @throws UnableToGenerateKeyException on failure of creation
   */
  public static AergoKey of(final String encodedEncryptedPrivateKey, final String password) {
    return new AergoKey(encodedEncryptedPrivateKey, password);
  }

  /**
   * Create a key pair with encrypted private key and password.
   *
   * @param encryptedPrivateKey encrypted private key
   * @param password password to decrypt
   * @return key instance
   *
   * @throws UnableToGenerateKeyException on failure of creation
   */
  public static AergoKey of(final EncryptedPrivateKey encryptedPrivateKey, final String password) {
    return new AergoKey(encryptedPrivateKey, password);
  }

  protected final transient Logger logger = getLogger(getClass());

  protected final AddressResolver addressResolver = new AddressResolver();

  protected final TransactionHashResolver transactionHashResolver = new TransactionHashResolver();

  protected final SignatureResolver signatureResolver = new SignatureResolver();

  protected final EncryptedPrivateKeyResolver privateKeyResolver =
      new EncryptedPrivateKeyResolver();

  protected final ECDSAKey ecdsakey;

  @Getter
  protected final AccountAddress address;

  /**
   * AergoKey constructor.
   *
   * @param encodedEncryptedPrivateKey base58 with checksum encoded encrypted private key
   * @param password password to decrypt
   *
   * @throws UnableToGenerateKeyException on failure of creation
   */
  public AergoKey(final String encodedEncryptedPrivateKey, final String password) {
    this(new EncryptedPrivateKey(encodedEncryptedPrivateKey), password);
  }

  /**
   * AergoKey constructor.
   *
   * @param encryptedPrivateKey encrypted private key
   * @param password password to decrypt
   *
   * @throws UnableToGenerateKeyException on failure of creation
   */
  public AergoKey(final EncryptedPrivateKey encryptedPrivateKey, final String password) {
    try {
      final BytesValue decryptedBytes = privateKeyResolver.decrypt(encryptedPrivateKey, password);
      final byte[] rawPrivateKey = decryptedBytes.getValue();
      this.ecdsakey = new ECDSAKeyGenerator().create(new BigInteger(1, rawPrivateKey));
      this.address = addressResolver.deriveAddress(ecdsakey.getPublicKey());
    } catch (final Exception e) {
      throw new UnableToGenerateKeyException(e);
    }
  }

  /**
   * AergoKey constructor.
   *
   * @param ecdsakey keypair
   */
  public AergoKey(final ECDSAKey ecdsakey) {
    this.ecdsakey = ecdsakey;
    this.address = addressResolver.deriveAddress(ecdsakey.getPublicKey());
  }

  @Override
  public PrivateKey getPrivateKey() {
    return ecdsakey.getPrivateKey();
  }

  @Override
  public PublicKey getPublicKey() {
    return ecdsakey.getPublicKey();
  }

  @Override
  public Transaction sign(final RawTransaction rawTransaction) {
    try {
      logger.debug("Sign raw transaction: {}", rawTransaction);
      final TxHash txHash = transactionHashResolver.calculateHash(rawTransaction);
      final BytesValue plainText = txHash.getBytesValue();
      final ECDSASignature ecdsaSignature = ecdsakey.sign(plainText.getInputStream());
      final Signature signature =
          signatureResolver.serialize(ecdsaSignature, ecdsakey.getParams().getN());
      logger.trace("Serialized signature: {}", signature);
      return Transaction.newBuilder(rawTransaction)
          .signature(signature)
          .build();
    } catch (final Exception e) {
      throw new HerajException(e);
    }
  }

  /**
   * Sign a plain message.
   *
   * @param message a message to sign
   * @return base64 encoded signature
   */
  public String signMessage(final String message) {
    try {
      logger.debug("Sign message: {}", message);
      final BytesValue plainText = new BytesValue(message.getBytes());
      final ECDSASignature ecdsaSignature = ecdsakey.sign(plainText.getInputStream());
      final Signature signature =
          signatureResolver.serialize(ecdsaSignature, ecdsakey.getParams().getN());
      logger.trace("Serialized signature: {}", signature);
      return Base64Utils.encode(signature.getSign().getValue());
    } catch (Exception e) {
      throw new HerajException(e);
    }
  }

  @Override
  public boolean verify(final Transaction transaction) {
    try {
      logger.debug("Verify transaction: {}", transaction);
      final TxHash txHash = transactionHashResolver.calculateHash(transaction);
      final BytesValue plainText = txHash.getBytesValue();
      final ECDSASignature parsedSignature =
          signatureResolver.parse(transaction.getSignature(), ecdsakey.getParams().getN());
      return ecdsakey.verify(plainText.getInputStream(), parsedSignature);
    } catch (final Exception e) {
      logger.info("Verification failed by exception {}", e.getLocalizedMessage());
      return false;
    }
  }

  /**
   * Verify message with base64 encoded signature.
   *
   * @param message a message to verify
   * @param base64EncodedSignature a base64 encoded signature
   * @return verification result
   */
  public boolean verifyMessage(final String message, final String base64EncodedSignature) {
    try {
      logger.debug("Verify message {} with signature", message, base64EncodedSignature);
      final BytesValue plainText = new BytesValue(message.getBytes());
      final Signature signature =
          new Signature(BytesValue.of(Base64Utils.decode(base64EncodedSignature)));
      final ECDSASignature parsedSignature =
          signatureResolver.parse(signature, ecdsakey.getParams().getN());
      return ecdsakey.verify(plainText.getInputStream(), parsedSignature);
    } catch (final Exception e) {
      logger.info("Verification failed by exception {}", e.getLocalizedMessage());
      return false;
    }
  }

  @Override
  public EncryptedPrivateKey export(final String password) {
    try {
      final BytesValue privateKeyBytes = new BytesValue(getRawPrivateKey());
      return privateKeyResolver.encrypt(privateKeyBytes, password);
    } catch (Exception e) {
      throw new HerajException(e);
    }
  }

  protected byte[] getRawPrivateKey() {
    final org.bouncycastle.jce.interfaces.ECPrivateKey ecPrivateKey =
        (org.bouncycastle.jce.interfaces.ECPrivateKey) getPrivateKey();
    final BigInteger d = ecPrivateKey.getD();
    return NumberUtils.positiveToByteArray(d);
  }

  @Override
  public String toString() {
    return String.format("Address: %s", getAddress());
  }
}
