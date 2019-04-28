package messaging.server;

import static messaging.common.protocol.RpcMessage.TYPE_RESPONSE;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
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
public class ServerHandler extends SimpleChannelInboundHandler<RpcMessage> {
	private static final Logger logger = LoggerFactory.getLogger(ServerHandler.class);

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
		logger.debug("Revieved heartbeat on channel({})", ctx.channel());
	}

	private void handleRequest(ChannelHandlerContext ctx, RpcRequest msg) {
		logger.debug("Recieved request({}) on channel({})", msg, ctx.channel());
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
					logger.error(e.getMessage(), e);
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
			logger.warn("Channel has passed idle timeout, channel = {}", ctx.channel());
			ctx.close();
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error(cause.getMessage(), cause);
	}
}
