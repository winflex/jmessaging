package messaging.common.codec;

import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import messaging.common.protocol.RpcMessage;
import messaging.common.protocol.RpcRequest;
import messaging.common.serialize.ISerializer;
import messaging.util.ExtensionLoader;

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

	private final Map<Integer, ISerializer> serializers = new HashMap<>();

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
			byte[] bytes = getSerializer(msg.getSerializerCode()).serialize(data);
			out.writeInt(bytes.length);
			out.writeBytes(bytes);
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

	private ISerializer getSerializer(int code) throws Exception {
		ISerializer serializer = serializers.get(Integer.valueOf(code));
		if (serializer == null) {
			serializer = ExtensionLoader.getLoader(ISerializer.class).getExtension(String.valueOf(code));
			serializers.put(code, serializer);
		}
		return serializer;
	}
}
