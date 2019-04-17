package messaging.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class NettyUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(NettyUtils.class);
	
	public static final ChannelFutureListener LOGGING_LISTENER = new ChannelFutureListener() {
		
		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if (!future.isSuccess()) {
				logger.error("Write message failed", future.cause());
			}
		}
	};
	
	public static ChannelFuture writeAndFlush(Channel ch, Object msg) {
		// TODO check for ch.isWritable();
		return ch.writeAndFlush(msg).addListener(LOGGING_LISTENER);
	}
	
}
