package messaging.common.codec;

import static messaging.common.codec.SerializerHolder.getSerializer;

import java.io.IOException;
import java.io.OutputStream;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import messaging.common.protocol.RpcMessage;
import messaging.common.protocol.RpcRequest;

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
 */
public class Encoder extends MessageToByteEncoder<RpcMessage> {

	private final ByteBufOutputStream output = new ByteBufOutputStream();

	@Override
	protected void encode(ChannelHandlerContext ctx, RpcMessage msg, ByteBuf out) throws Exception {
		out.writeShort(CodecConstants.MAGIC);
		out.writeByte(msg.getType());
		out.writeLong(msg.getId());
		out.writeByte(getFlag(msg));
		out.writeByte(msg.getSerializerCode());

		Object data = msg.getData();
		if (data == null) {
			out.writeInt(0);
		} else {
			out.writeInt(0); // skip body length for now
			getSerializer(msg.getSerializerCode()).serialize(data, output.setBuf(out));
			int dataLength = out.readableBytes() - CodecConstants.HEADER_LENGTH;
			out.setInt(CodecConstants.BODY_LENGTH_OFFSET, dataLength); // write the real body length
		}
	}

	private byte getFlag(RpcMessage msg) {
		byte flag = 0;
		if (msg instanceof RpcRequest) {
			if (((RpcRequest) msg).isOneWay()) {
				flag |= CodecConstants.ONE_WAY_MASK;
			}
		}
		return flag;
	}

	final class ByteBufOutputStream extends OutputStream {
		private ByteBuf buf;

		ByteBufOutputStream setBuf(ByteBuf buf) {
			this.buf = buf;
			return this;
		}

		@Override
		public void write(int b) throws IOException {
			buf.writeByte(b);
		}

		@Override
		public void write(byte[] b) throws IOException {
			write(b, 0, b.length);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			buf.writeBytes(b, off, len);
		}
	}
}
