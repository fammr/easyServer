package com.renke;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.renke.api.ChannelHandler;
public class HTTPExecutor implements Runnable{
	private final static Logger logger = LoggerFactory.getLogger(HTTPExecutor.class);
	private SocketChannel socketChannel;
	private AtomicInteger success;

	public HTTPExecutor(SocketChannel socketChannel,AtomicInteger success) {
		this.socketChannel = socketChannel;
		this.success = success;
	}
	
	@Override
	public void run() {
		ChannelHandler http = HTTPChannelHandler.getHTTPChannelHandler();
		String encoding = Controller.encoding;
		byte[] data = null;
		byte[] response = null;
		//解析，解析完毕处理，处理完成封装
		try {
			//解析数据流，产生request
			Map<String,Object> request = http.resolveSocketChannel(socketChannel);
			//处理request，返回字节数组
			data = http.handle(request);
			//包装数组
			response = http.assembleData(data);
		} catch (Exception e) {
			try {
				response = e.getMessage().getBytes(encoding);
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
				response = ("encoding Exception : " + encoding).getBytes();
			}
			e.printStackTrace();
		}
		
		//返回数据
		try {
			socketChannel.write(ByteBuffer.wrap(response));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				socketChannel.shutdownOutput();
				socketChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
				logger.debug("error {}" , success.decrementAndGet());
			}
		}
		logger.debug("success {}" , success.incrementAndGet());
	}
}
