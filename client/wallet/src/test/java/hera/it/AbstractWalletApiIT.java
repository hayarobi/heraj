/*
 * @copyright defined in LICENSE.txt
 */

package hera.it;

import static java.util.UUID.randomUUID;

import hera.api.model.Authentication;
import hera.key.AergoKey;
import hera.keystore.AergoKeyStore;
import hera.keystore.InMemoryKeyStore;
import hera.keystore.JavaKeyStore;
import hera.keystore.KeyStore;
import hera.model.KeyAlias;
import hera.wallet.WalletApi;
import hera.wallet.WalletApiFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public abstract class AbstractWalletApiIT extends AbstractIT {

  protected static final String keyStoreRoot =
      System.getProperty("java.io.tmpdir") + "/" + randomUUID().toString();

  @Parameters
  public static Collection<Object[]> data() {
    final List<Object[]> args = new ArrayList<>();

    // in memory
    final KeyStore inMemoryKeyStore = new InMemoryKeyStore();
    args.add(new Object[] {inMemoryKeyStore});

    // java kesytore
    final KeyStore javaKeyStore = new JavaKeyStore("PKCS12");
    args.add(new Object[] {javaKeyStore});

    // aergo kesytore
    final KeyStore aergoKeyStore = new AergoKeyStore(keyStoreRoot);
    args.add(new Object[] {javaKeyStore});

    return args;
  }

  @Parameter(0)
  public KeyStore keyStore;

  protected WalletApi walletApi;

  protected Authentication authentication;

  @Before
  public void setUp() throws Exception {
    super.setUp();

    new File(keyStoreRoot).mkdirs();

    final KeyAlias alias = new KeyAlias(randomUUID().toString().replace("-", ""));
    authentication = Authentication.of(alias, randomName().toString());

    final AergoKey newKey = createNewKey();
    keyStore.save(authentication, newKey);

    walletApi = new WalletApiFactory().create(keyStore);
    walletApi.bind(aergoClient);
  }

}
