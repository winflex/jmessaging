package messaging.common.codec;

import org.junit.Test;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import messaging.common.protocol.RpcRequest;

/**
 * 
 * @author winflex
 */
public class CodecTest {
	
	@Test
	public void testCodec() throws IOException, Exception {
		EmbeddedChannel channel = new EmbeddedChannel();
		channel.pipeline().addLast(new Encoder());
		channel.pipeline().addLast(new Decoder());
		
		Object data = "xxx";
		RpcRequest req = new RpcRequest(1, data, false);
		ByteArrayOutputStream baos = new ByteArrayOutputStream(16);
		SerializerHolder.getSerializer(req.getSerializerCode()).serialize(data, baos);
		byte[] dataBytes = baos.toByteArray();
		
		channel.writeOutbound(req);
		ByteBuf buf = channel.readOutbound();
		assertEquals(CodecConstants.HEADER_LENGTH + dataBytes.length, buf.readableBytes());
		
		buf.writeByte(1); // 模拟粘包
		channel.writeInbound(buf);
		RpcRequest decoded = channel.readInbound();
		
		assertEquals(1, buf.readableBytes());
		assertEquals(req.getData(), decoded.getData());
		assertEquals(req.getId(), decoded.getId());
		assertEquals(req.getSerializerCode(), decoded.getSerializerCode());
		assertEquals(req.getType(), decoded.getType());
		assertEquals(req.isOneWay(), decoded.isOneWay());
	}
	
}
