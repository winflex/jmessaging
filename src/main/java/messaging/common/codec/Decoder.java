package messaging.common.codec;

import static messaging.common.codec.CodecConstants.BODY_LENGTH_OFFSET;
import static messaging.common.codec.CodecConstants.HEADER_LENGTH;
import static messaging.common.codec.CodecConstants.MAGIC;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import messaging.common.RpcResult;
import messaging.common.protocol.RpcMessage;
import messaging.common.protocol.RpcRequest;
import messaging.common.protocol.RpcResponse;
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
 * @author winflex
 * 
 */
public class Decoder extends ByteToMessageDecoder {

	private static final Logger logger = LoggerFactory.getLogger(Decoder.class);

	private final Map<Integer, ISerializer> serializers = new HashMap<>();
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		int readableBytes = in.readableBytes();
		// 非整包不处理
		if (readableBytes < HEADER_LENGTH || readableBytes < (HEADER_LENGTH + in.getInt(BODY_LENGTH_OFFSET))) {
			return;
		}

		if (in.readShort() != MAGIC) {
			logger.error("Recieved an unknown packet, the channel({}) will be closed", ctx.channel());
			return;
		}

		byte type = in.readByte();
		long id = in.readLong();
		byte flag = in.readByte();
		int serializerCode = in.readByte();
		int dataLength = in.readInt();
		byte[] dataBytes = null;
		if (dataLength > 0) {
			in.readBytes(dataBytes = new byte[dataLength]);
		}

		Object data = dataBytes;
		if (dataBytes != null && dataBytes.length > 0) {
			data = getSerializer(serializerCode).deserialize(dataBytes);
		}
		
		if (type == RpcMessage.TYPE_REQUEST) {
			boolean oneWay = (flag & CodecConstants.ONE_WAY_MASK) == 1;
			out.add(new RpcRequest(id, data, oneWay));
		} else if (type == RpcMessage.TYPE_RESPONSE) {
			out.add(new RpcResponse(id, (RpcResult) data));
		} else {
			logger.error("Recieved an unknown packet with type {}, the channel({}) will be closed", type, ctx.channel());
		}
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
