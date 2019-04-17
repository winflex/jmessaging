package messaging.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import messaging.common.RpcResult;
import messaging.common.protocol.RpcMessage;
import messaging.common.protocol.RpcResponse;

/**
 * 
 * @author winflex
 */
public class ResponseHandler extends SimpleChannelInboundHandler<RpcMessage> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) throws Exception {
		if (msg instanceof RpcResponse) {
			RpcResponse resp = (RpcResponse) msg;
			RpcResult result = (RpcResult) msg.getData();
			if (result.isSuccess()) {
				ResponseFuture.doneWithResult(resp.getId(), result.getResult());
			} else {
				ResponseFuture.doneWithException(resp.getId(), result.getCause());
			}
		}
	}
}
