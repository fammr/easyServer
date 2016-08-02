package com.renke;

import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTTPExcutor implements Runnable{
	private final static Logger logger = LoggerFactory.getLogger(HTTPExcutor.class);
	private SocketChannel socketChannel;
	private AtomicInteger success;

	public HTTPExcutor(SocketChannel socketChannel,AtomicInteger success) {
		this.socketChannel = socketChannel;
		this.success = success;
	}
	
	@Override
	public void run() {
		HTTPChannelHandler http = HTTPChannelHandler.getHTTPChannelHandler();
		http.handle(socketChannel);
		logger.debug("success {}" , success.incrementAndGet());
	}

}
