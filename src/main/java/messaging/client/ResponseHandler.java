package messaging.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import messaging.common.RpcResult;
import messaging.common.protocol.RpcResponse;

/**
 * 
 * @author winflex
 */
public class ResponseHandler extends SimpleChannelInboundHandler<RpcResponse> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcResponse resp) throws Exception {
		RpcResult result = (RpcResult) resp.getData();
		if (result.isSuccess()) {
			ResponseFuture.doneWithResult(resp.getId(), result.getResult());
		} else {
			ResponseFuture.doneWithException(resp.getId(), result.getCause());
		}
	}
}
