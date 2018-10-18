/*
 * @copyright defined in LICENSE.txt
 */

package hera.client;

import static org.junit.Assert.assertNotNull;

import hera.AbstractTestCase;
import hera.Context;
import java.io.IOException;
import org.junit.Test;

public class AergoEitherClientTest extends AbstractTestCase {

  protected Context context = AergoClientBuilder.getDefaultContext();

  @Test
  public void testGetAccountEitherOperation() throws IOException {
    try (final AergoEitherClient client = new AergoEitherClient(context)) {
      assertNotNull(client.getAccountEitherOperation());
    }
  }

  @Test
  public void testGetBlockEitherOperation() throws IOException {
    try (final AergoEitherClient client = new AergoEitherClient(context)) {
      assertNotNull(client.getBlockEitherOperation());
    }
  }

  @Test
  public void testGetBlockchainEitherOperation() throws IOException {
    try (final AergoEitherClient client = new AergoEitherClient(context)) {
      assertNotNull(client.getBlockchainEitherOperation());
    }
  }

  @Test
  public void testGetTransactionEitherOperation() throws IOException {
    try (final AergoEitherClient client = new AergoEitherClient(context)) {
      assertNotNull(client.getTransactionEitherOperation());
    }
  }

  @Test
  public void testGetContractEitherOperation() throws IOException {
    try (final AergoEitherClient client = new AergoEitherClient(context)) {
      assertNotNull(client.getContractEitherOperation());
    }
  }

}
