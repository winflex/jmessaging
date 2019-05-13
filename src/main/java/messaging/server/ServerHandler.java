package messaging.server;

import static messaging.common.protocol.RpcMessage.TYPE_RESPONSE;

import java.util.concurrent.Executor;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import messaging.common.RpcException;
import messaging.common.RpcResult;
import messaging.common.protocol.HeartbeatMessage;
import messaging.common.protocol.RpcMessage;
import messaging.common.protocol.RpcRequest;
import messaging.util.NettyUtils;
import messaging.util.concurrent.SynchronousExecutor;

/**
 * 
 * @author winflex
 */
@Slf4j
public class ServerHandler extends SimpleChannelInboundHandler<RpcMessage> {
	private final RpcServer rpcServer;

	public ServerHandler(RpcServer rpcServer) {
		this.rpcServer = rpcServer;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) throws Exception {
		if (msg instanceof RpcRequest) {
			handleRequest(ctx, (RpcRequest) msg);
		} else if (msg instanceof HeartbeatMessage) {
			handleHeartbeat(ctx, (HeartbeatMessage) msg);
		}
	}

	private void handleHeartbeat(ChannelHandlerContext ctx, HeartbeatMessage msg) {
		log.debug("Revieved heartbeat on channel({})", ctx.channel());
	}

	private void handleRequest(ChannelHandlerContext ctx, RpcRequest msg) {
		log.debug("Recieved request({}) on channel({})", msg, ctx.channel());
		Object request = msg.getData();
		IRequestHandler<Object> handler = rpcServer.getHandler(request.getClass().getName());
		if (handler == null) {
			RpcException error = new RpcException("no handler registered for " + request.getClass().getName());
			if (!msg.isOneWay()) {
				respondWithException(ctx.channel(), msg.getId(), error);
			}
		} else {
			Executor executor = getExecutor(handler);
			executor.execute(() -> {
				try {
					Context context = new Context(msg, ctx.channel(), executor);
					handler.handleRequest(context, request);
				} catch (RuntimeException e) {
					log.error(e.getMessage(), e);
					if (!msg.isOneWay()) {
						respondWithException(ctx.channel(), msg.getId(), e);
					}
				}
			});
		}
	}

	/**
	 * 优先级： {@link IRequestHandler#getExecutor()} > {@link RpcServer#getExecutor()}
	 * > {@link SynchronousExecutor#INSTANCE}
	 */
	private Executor getExecutor(IRequestHandler<Object> handler) {
		Executor executor = handler.getExecutor();
		if (executor == null) {
			executor = rpcServer.getExecutor();
		}
		if (executor == null) {
			executor = SynchronousExecutor.INSTANCE;
		}
		return executor;
	}

	private void respondWithException(Channel ch, long requestId, Throwable cause) {
		RpcMessage response = new RpcMessage();
		response.setType(TYPE_RESPONSE);
		response.setId(requestId);
		response.setData(RpcResult.newFailureResult(cause));
		NettyUtils.writeAndFlush(ch, response);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			// idle timeout
			log.warn("Channel has passed idle timeout, channel = {}", ctx.channel());
			ctx.close();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error(cause.getMessage(), cause);
	}
}
