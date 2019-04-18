package messaging.common.codec;

/**
 * 
 * @author winflex
 */
public interface CodecConstants {
	
	int HEADER_LENGTH = 17;
	
	int BODY_LENGTH_OFFSET = 13;
	
	short MAGIC = (short) 0xebab;
	
	byte ONE_WAY_MASK = (byte) 0x80;
}
