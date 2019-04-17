package messaging.server;

import java.util.concurrent.Executor;

/**
 * 消息处理器接口定义
 * 
 * @author winflex
 */
public interface IRequestHandler<T> {
	
	/**
	 * 请求处理方法, 通过{@link Context#writeResponse(Object)}回写响应
	 */
	void handleRequest(Context ctx, T request);
	
	/**
	 * 该处理器处理的消息类型
	 */
	String interestClass();
	
	Executor getExecutor();
}
