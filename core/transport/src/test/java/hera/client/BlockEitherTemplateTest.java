/*
 * @copyright defined in LICENSE.txt
 */

package hera.client;

import static hera.api.model.BytesValue.of;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import hera.AbstractTestCase;
import hera.api.model.Block;
import hera.api.model.BlockHash;
import hera.api.model.BlockHeader;
import hera.api.tupleorerror.ResultOrError;
import hera.api.tupleorerror.ResultOrErrorFuture;
import java.util.List;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import types.AergoRPCServiceGrpc.AergoRPCServiceBlockingStub;
import types.Blockchain;
import types.Rpc;

@SuppressWarnings("unchecked")
@PrepareForTest({AergoRPCServiceBlockingStub.class, Blockchain.Block.class,
    Rpc.BlockHeaderList.class})
public class BlockEitherTemplateTest extends AbstractTestCase {

  @Override
  public void setUp() {
    super.setUp();
  }

  @Test
  public void testGetBlockByHash() {
    ResultOrErrorFuture<Block> futureMock = mock(ResultOrErrorFuture.class);
    when(futureMock.get()).thenReturn(mock(ResultOrError.class));
    BlockAsyncTemplate asyncOperationMock = mock(BlockAsyncTemplate.class);
    when(asyncOperationMock.getBlock(any())).thenReturn(futureMock);

    final BlockEitherTemplate blockTemplate = new BlockEitherTemplate();
    blockTemplate.setContext(context);
    blockTemplate.blockAsyncOperation = asyncOperationMock;

    final ResultOrError<Block> block =
        blockTemplate.getBlock(new BlockHash(of(randomUUID().toString().getBytes())));
    assertNotNull(block);
  }

  @Test
  public void testGetBlockByHeight() {
    ResultOrErrorFuture<Block> futureMock = mock(ResultOrErrorFuture.class);
    when(futureMock.get()).thenReturn(mock(ResultOrError.class));
    BlockAsyncTemplate asyncOperationMock = mock(BlockAsyncTemplate.class);
    when(asyncOperationMock.getBlock(anyLong())).thenReturn(futureMock);

    final BlockEitherTemplate blockTemplate = new BlockEitherTemplate();
    blockTemplate.setContext(context);
    blockTemplate.blockAsyncOperation = asyncOperationMock;

    final ResultOrError<Block> block = blockTemplate.getBlock(randomUUID().hashCode());
    assertNotNull(block);
  }

  @Test
  public void testListBlockHeadersByHash() {
    ResultOrErrorFuture<List<BlockHeader>> futureMock = mock(ResultOrErrorFuture.class);
    when(futureMock.get()).thenReturn(mock(ResultOrError.class));
    BlockAsyncTemplate asyncOperationMock = mock(BlockAsyncTemplate.class);
    when(asyncOperationMock.listBlockHeaders(any(), anyInt())).thenReturn(futureMock);

    final BlockEitherTemplate blockTemplate = new BlockEitherTemplate();
    blockTemplate.setContext(context);
    blockTemplate.blockAsyncOperation = asyncOperationMock;

    final ResultOrError<List<BlockHeader>> block = blockTemplate.listBlockHeaders(
        new BlockHash(of(randomUUID().toString().getBytes())), randomUUID().hashCode());
    assertNotNull(block);
  }

  @Test
  public void testListBlockHeadersByHeight() {
    ResultOrErrorFuture<List<BlockHeader>> futureMock = mock(ResultOrErrorFuture.class);
    when(futureMock.get()).thenReturn(mock(ResultOrError.class));
    BlockAsyncTemplate asyncOperationMock = mock(BlockAsyncTemplate.class);
    when(asyncOperationMock.listBlockHeaders(anyLong(), anyInt())).thenReturn(futureMock);

    final BlockEitherTemplate blockTemplate = new BlockEitherTemplate();
    blockTemplate.setContext(context);
    blockTemplate.blockAsyncOperation = asyncOperationMock;

    final ResultOrError<List<BlockHeader>> block =
        blockTemplate.listBlockHeaders(randomUUID().hashCode(), randomUUID().hashCode());
    assertNotNull(block);
  }

}
