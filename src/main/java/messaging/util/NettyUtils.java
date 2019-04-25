package messaging.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import messaging.common.TcpOptions;

/**
 * 
 * 
 * @author winflex
 */
public class NettyUtils {

	private static final Logger logger = LoggerFactory.getLogger(NettyUtils.class);

	public static final ChannelFutureListener LOGGING_LISTENER = (future) -> {
		if (!future.isSuccess()) {
			logger.error("Write message failed", future.cause());
		}
	};

	public static ChannelFuture writeAndFlush(Channel ch, Object msg) {
		// TODO handle write failure more gracefully
		return ch.writeAndFlush(msg).addListener(LOGGING_LISTENER);
	}

	public static void fillTcpOptions(Bootstrap b, TcpOptions tcp, boolean epoll) {
		if (tcp == null) {
			return;
		}
		if (epoll) {
			if (tcp.isKeepAliveSet()) {
				b.option(EpollChannelOption.SO_KEEPALIVE, tcp.isKeepAlive());
			}
			if (tcp.isBacklogSet()) {
				b.option(EpollChannelOption.SO_BACKLOG, tcp.getBacklog());
			}
			if (tcp.isLingerSet()) {
				b.option(EpollChannelOption.SO_LINGER, tcp.getLinger());
			}
			if (tcp.isNoDelaySet()) {
				b.option(EpollChannelOption.TCP_NODELAY, tcp.isNoDelay());
			}
			if (tcp.isRecieveBufferSet()) {
				b.option(EpollChannelOption.SO_RCVBUF, tcp.getRecieveBuffer());
			}
			if (tcp.isReuseAddressSet()) {
				b.option(EpollChannelOption.SO_REUSEADDR, tcp.isReuseAddress());
			}
			if (tcp.isSendBufferSet()) {
				b.option(EpollChannelOption.SO_SNDBUF, tcp.getSendBuffer());
			}
			if (tcp.isTimeoutSet()) {
				b.option(EpollChannelOption.SO_TIMEOUT, tcp.getTimeout());
			}
		} else {
			if (tcp.isKeepAliveSet()) {
				b.option(ChannelOption.SO_KEEPALIVE, tcp.isKeepAlive());
			}
			if (tcp.isBacklogSet()) {
				b.option(ChannelOption.SO_BACKLOG, tcp.getBacklog());
			}
			if (tcp.isLingerSet()) {
				b.option(ChannelOption.SO_LINGER, tcp.getLinger());
			}
			if (tcp.isNoDelaySet()) {
				b.option(ChannelOption.TCP_NODELAY, tcp.isNoDelay());
			}
			if (tcp.isRecieveBufferSet()) {
				b.option(ChannelOption.SO_RCVBUF, tcp.getRecieveBuffer());
			}
			if (tcp.isReuseAddressSet()) {
				b.option(ChannelOption.SO_REUSEADDR, tcp.isReuseAddress());
			}
			if (tcp.isSendBufferSet()) {
				b.option(ChannelOption.SO_SNDBUF, tcp.getSendBuffer());
			}
			if (tcp.isTimeoutSet()) {
				b.option(ChannelOption.SO_TIMEOUT, tcp.getTimeout());
			}
		}
	}

	public static void fillTcpOptions(ServerBootstrap b, TcpOptions tcp, boolean epoll) {
		if (tcp == null) {
			return;
		}
		if (epoll) {
			if (tcp.isKeepAliveSet()) {
				b.childOption(EpollChannelOption.SO_KEEPALIVE, tcp.isKeepAlive());
			}
			if (tcp.isBacklogSet()) {
				b.childOption(EpollChannelOption.SO_BACKLOG, tcp.getBacklog());
			}
			if (tcp.isLingerSet()) {
				b.childOption(EpollChannelOption.SO_LINGER, tcp.getLinger());
			}
			if (tcp.isNoDelaySet()) {
				b.childOption(EpollChannelOption.TCP_NODELAY, tcp.isNoDelay());
			}
			if (tcp.isRecieveBufferSet()) {
				b.childOption(EpollChannelOption.SO_RCVBUF, tcp.getRecieveBuffer());
			}
			if (tcp.isReuseAddressSet()) {
				b.childOption(EpollChannelOption.SO_REUSEADDR, tcp.isReuseAddress());
			}
			if (tcp.isSendBufferSet()) {
				b.childOption(EpollChannelOption.SO_SNDBUF, tcp.getSendBuffer());
			}
			if (tcp.isTimeoutSet()) {
				b.childOption(EpollChannelOption.SO_TIMEOUT, tcp.getTimeout());
			}
		} else {
			if (tcp.isKeepAliveSet()) {
				b.childOption(ChannelOption.SO_KEEPALIVE, tcp.isKeepAlive());
			}
			if (tcp.isBacklogSet()) {
				b.childOption(ChannelOption.SO_BACKLOG, tcp.getBacklog());
			}
			if (tcp.isLingerSet()) {
				b.childOption(ChannelOption.SO_LINGER, tcp.getLinger());
			}
			if (tcp.isNoDelaySet()) {
				b.childOption(ChannelOption.TCP_NODELAY, tcp.isNoDelay());
			}
			if (tcp.isRecieveBufferSet()) {
				b.childOption(ChannelOption.SO_RCVBUF, tcp.getRecieveBuffer());
			}
			if (tcp.isReuseAddressSet()) {
				b.childOption(ChannelOption.SO_REUSEADDR, tcp.isReuseAddress());
			}
			if (tcp.isSendBufferSet()) {
				b.childOption(ChannelOption.SO_SNDBUF, tcp.getSendBuffer());
			}
			if (tcp.isTimeoutSet()) {
				b.childOption(ChannelOption.SO_TIMEOUT, tcp.getTimeout());
			}
		}
	}
}
