package messaging.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import messaging.common.protocol.HeartbeatMessage;
import messaging.util.NettyUtils;

/**
 * 
 * @author winflex
 */
public class EventHandler extends ChannelInboundHandlerAdapter {
	
	private static final Logger logger = LoggerFactory.getLogger(EventHandler.class);
	
	// heartbeat will always be the same
	private static final HeartbeatMessage HEARTBEAT = new HeartbeatMessage();
	
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			logger.debug("Idle event({}) triggered on channel({})",((IdleStateEvent) evt).state(), ctx.channel());
			NettyUtils.writeAndFlush(ctx.channel(), HEARTBEAT);
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error(cause.getMessage(), cause);
	}
	
}
