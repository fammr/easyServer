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
		//������������ϴ���������ɷ�װ
		try {
			//����������������request
			Map<String,Object> request = http.resolveSocketChannel(socketChannel);
			//����request�������ֽ�����
			data = http.handle(request);
			//��װ����
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
		
		//��������
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
