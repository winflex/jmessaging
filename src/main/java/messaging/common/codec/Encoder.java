package messaging.common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import messaging.common.protocol.RpcMessage;
import messaging.common.serialize.ISerializer;

/**
 * TODO: 协议层面支持序列化方式
 * +----------------+--------------+-------------+----------------+-----------------+------------------------+----------------------+------+
 * | magic(2 bytes) | type(1 byte) | id(8 bytes) | one way(1 bit) | reserved(7 bit) | serialize code(1 byte) | body length(4 bytes) | body |
 * +----------------+--------------+-------------+----------------+-----------------+------------------------+----------------------+------+
 * 
 * @author winflex
 */
public class Encoder extends MessageToByteEncoder<RpcMessage> {

	private final ISerializer serializer;
	
	public Encoder(ISerializer serializer) throws Exception {
		this.serializer = serializer;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, RpcMessage msg, ByteBuf out) throws Exception {
		out.writeShort(CodecConstants.MAGIC);
		out.writeByte(msg.getType());
		out.writeLong(msg.getId());
		
		byte flag = 0;
		if (msg.isOneWay()) {
			flag |= CodecConstants.ONE_WAY_MASK;
		}
		out.writeByte(flag);
		
		Object data = msg.getData();
		if (data == null) {
			out.writeInt(0);
		} else {
			byte[] bytes = serializer.serialize(data);
			out.writeInt(bytes.length);
			out.writeBytes(bytes);
		}
	}
}
