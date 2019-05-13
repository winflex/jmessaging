package messaging.common.codec;

import static messaging.common.codec.CodecConstants.BODY_LENGTH_OFFSET;
import static messaging.common.codec.CodecConstants.HEADER_LENGTH;
import static messaging.common.codec.CodecConstants.MAGIC;
import static messaging.common.codec.SerializerHolder.getSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import messaging.common.RpcResult;
import messaging.common.protocol.HeartbeatMessage;
import messaging.common.protocol.RpcMessage;
import messaging.common.protocol.RpcRequest;
import messaging.common.protocol.RpcResponse;

/**
 * <pre>
 * request:
 * +----------------+--------------+-------------+----------------+-----------------+------------------------+----------------------+------+
 * | magic(2 bytes) | type(1 byte) | id(8 bytes) | one way(1 bit) | reserved(7 bit) | serialize code(1 byte) | body length(4 bytes) | body |
 * +----------------+--------------+-------------+----------------+-----------------+------------------------+----------------------+------+
 * 
 * response:
 * +----------------+--------------+-------------+----------------------------------+------------------------+----------------------+------+
 * | magic(2 bytes) | type(1 byte) | id(8 bytes) |          reserved(1 byte)        | serialize code(1 byte) | body length(4 bytes) | body |
 * +----------------+--------------+-------------+----------------------------------+------------------------+----------------------+------+
 * </pre>
 * 
 * @author winflex
 * 
 */
@Slf4j
public class Decoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		int readableBytes = in.readableBytes();
		// 非整包不处理
		if (readableBytes < HEADER_LENGTH || readableBytes < (HEADER_LENGTH + in.getInt(BODY_LENGTH_OFFSET))) {
			return;
		}

		if (in.readShort() != MAGIC) {
			log.error("Recieved an unknown packet, the channel({}) will be closed", ctx.channel());
			return;
		}

		byte type = in.readByte();
		long id = in.readLong();
		byte flag = in.readByte();
		int serializerCode = in.readByte();
		int dataLength = in.readInt();

		Object data = null;
		if (dataLength > 0) {
			data = getSerializer(serializerCode).deserialize(new ByteBufInputStream(in, dataLength));
		}

		if (type == RpcMessage.TYPE_REQUEST) {
			boolean oneWay = (flag & CodecConstants.ONE_WAY_MASK) == 1;
			out.add(new RpcRequest(id, data, oneWay));
		} else if (type == RpcMessage.TYPE_RESPONSE) {
			out.add(new RpcResponse(id, (RpcResult) data));
		} else if (type == RpcMessage.TYPE_HEARTBEAT) {
			out.add(new HeartbeatMessage());
		} else {
			log.error("Recieved an unknown packet with type {}, the channel({}) will be closed", type,
					ctx.channel());
		}
	}

	final class ByteBufInputStream extends InputStream {
		private final ByteBuf buf;
		private final int size;
		private int read;

		ByteBufInputStream(ByteBuf buf, int size) {
			this.buf = buf;
			this.size = size;
		}

		@Override
		public int read() throws IOException {
			// 不能以in.readableBytes()来判断是否是eof, 以正确处理一个ByteBuf里有多个包的情况
			if (read >= size) {
				return -1;
			}
			read++;
			return buf.readByte();
		}

		@Override
		public int read(byte[] b) throws IOException {
			return read(b, 0, b.length);
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			if (read >= size) {
				return -1;
			}
			int available = size - read;
			if (len > available) {
				buf.readBytes(b, 0, available);
				return available;
			} else {
				buf.readBytes(b, 0, len);
				return len;
			}
		}

		@Override
		public int available() throws IOException {
			return size - read;
		}
	}
}
