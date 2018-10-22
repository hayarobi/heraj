/*
 * @copyright defined in LICENSE.txt
 */

package hera.client;

import static com.google.common.util.concurrent.Futures.addCallback;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static hera.util.TransportUtils.copyFrom;
import static hera.util.TransportUtils.longToByteArray;
import static java.util.stream.Collectors.toList;
import static types.AergoRPCServiceGrpc.newFutureStub;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import hera.Context;
import hera.FutureChain;
import hera.annotation.ApiAudience;
import hera.annotation.ApiStability;
import hera.api.BlockAsyncOperation;
import hera.api.model.Block;
import hera.api.model.BlockHash;
import hera.api.model.BlockHeader;
import hera.api.model.BytesValue;
import hera.api.tupleorerror.ResultOrErrorFuture;
import hera.api.tupleorerror.ResultOrErrorFutureFactory;
import hera.transport.BlockConverterFactory;
import hera.transport.ModelConverter;
import io.grpc.ManagedChannel;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import types.AergoRPCServiceGrpc.AergoRPCServiceFutureStub;
import types.Blockchain;
import types.Rpc.BlockHeaderList;
import types.Rpc.ListParams;
import types.Rpc.SingleBytes;

@ApiAudience.Private
@ApiStability.Unstable
@SuppressWarnings("unchecked")
public class BlockAsyncTemplate implements BlockAsyncOperation, ChannelInjectable {

  protected final ModelConverter<Block, Blockchain.Block> blockConverter =
      new BlockConverterFactory().create();

  @Setter
  protected Context context;

  @Getter
  protected AergoRPCServiceFutureStub aergoService;

  @Override
  public void injectChannel(final ManagedChannel channel) {
    this.aergoService = newFutureStub(channel);
  }

  @Override
  public ResultOrErrorFuture<Block> getBlock(final BlockHash blockHash) {
    final ResultOrErrorFuture<Block> nextFuture = ResultOrErrorFutureFactory.supplyEmptyFuture();

    final ByteString byteString = copyFrom(blockHash.getBytesValue());
    final SingleBytes bytes = SingleBytes.newBuilder().setValue(byteString).build();
    ListenableFuture<Blockchain.Block> listenableFuture = aergoService.getBlock(bytes);
    FutureChain<Blockchain.Block, Block> callback =
        new FutureChain<>(nextFuture, blockConverter::convertToDomainModel);
    addCallback(listenableFuture, callback, directExecutor());

    return nextFuture;
  }

  @Override
  public ResultOrErrorFuture<Block> getBlock(final long height) {
    final ResultOrErrorFuture<Block> nextFuture = ResultOrErrorFutureFactory.supplyEmptyFuture();

    final ByteString byteString = copyFrom(BytesValue.of(longToByteArray(height)));
    final SingleBytes bytes = SingleBytes.newBuilder().setValue(byteString).build();
    ListenableFuture<Blockchain.Block> listenableFuture = aergoService.getBlock(bytes);
    FutureChain<Blockchain.Block, Block> callback =
        new FutureChain<>(nextFuture, blockConverter::convertToDomainModel);
    addCallback(listenableFuture, callback, directExecutor());

    return nextFuture;
  }

  @Override
  public ResultOrErrorFuture<List<BlockHeader>> listBlockHeaders(final BlockHash blockHash,
      final int size) {
    final ResultOrErrorFuture<List<BlockHeader>> nextFuture =
        ResultOrErrorFutureFactory.supplyEmptyFuture();

    final ListParams listParams =
        ListParams.newBuilder().setHash(copyFrom(blockHash.getBytesValue())).setSize(size).build();
    ListenableFuture<BlockHeaderList> listenableFuture = aergoService.listBlockHeaders(listParams);
    FutureChain<BlockHeaderList, List<BlockHeader>> callback = new FutureChain<>(nextFuture,
        blockHeaders -> blockHeaders.getBlocksList().stream()
            .map(blockConverter::convertToDomainModel).map(BlockHeader.class::cast)
            .collect(toList()));
    addCallback(listenableFuture, callback, directExecutor());

    return nextFuture;
  }

  @Override
  public ResultOrErrorFuture<List<BlockHeader>> listBlockHeaders(final long height,
      final int size) {
    final ResultOrErrorFuture<List<BlockHeader>> nextFuture =
        ResultOrErrorFutureFactory.supplyEmptyFuture();

    final ListParams listParams = ListParams.newBuilder().setHeight(height).setSize(size).build();
    ListenableFuture<BlockHeaderList> listenableFuture = aergoService.listBlockHeaders(listParams);
    FutureChain<BlockHeaderList, List<BlockHeader>> callback = new FutureChain<>(nextFuture,
        blockHeaders -> blockHeaders.getBlocksList().stream()
            .map(blockConverter::convertToDomainModel).map(BlockHeader.class::cast)
            .collect(toList()));
    addCallback(listenableFuture, callback, directExecutor());

    return nextFuture;
  }

}

