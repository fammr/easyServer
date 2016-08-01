package com.renke;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ChannelClient {
	private final static Logger logger = LoggerFactory.getLogger(ChannelClient.class);
	class SocketThread implements Runnable{
		private int port = 0;
		public SocketThread(int port){
			this.port = port;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				SocketChannel sc = SocketChannel.open();
				logger.info("thread");
				sc.connect(new InetSocketAddress(port));
				String msg = "I say : I'm not a bad guy ";
//				使用socket处理
				OutputStream os = sc.socket().getOutputStream();
				os.write(msg.getBytes());
				sc.socket().shutdownOutput();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	static class BufferThread implements Runnable{
		
		private int port = 0;
		public BufferThread(int port){
			this.port = port;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				int k = 10;
				while(true){
					SocketChannel sc;
					sc = SocketChannel.open();
					sc.connect(new InetSocketAddress(port));
					String msg = "beginabcdefghijklmnopqrstuvwxyz "+"abcdefghijklmnopqrstuvwxyz ";
					for(int i=0;i<50;i++){
						msg += "abcdefghijklmnopqrstuvwxyz "+"abcdefghijklmnopqrstuvwxyz ";
					}
					ByteBuffer buf = ByteBuffer.wrap(msg.getBytes());
					sc.write(buf);
					sc.shutdownOutput();
					buf.flip();
					int len = sc.read(buf);
					logger.info(new String(buf.array(),0,len,"UTF-8"));
					sc.close();
					if(k--<=0) break;
//					if(k>0) break;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	static class RefThread implements Runnable{
		ObjectTest obj = null;
		public RefThread(ObjectTest obj){
			this.obj = obj;
		}
		
		@Override
		public void run() {
			logger.info("obj is {}",obj.getObj());
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			logger.info("obj is {}",obj.getObj());
		}
	}
	
	static class ChangeRefThread implements Runnable{
		ObjectTest obj = null;
		public ChangeRefThread(ObjectTest obj){
			this.obj = obj;
		}
		
		@Override
		public void run() {
			logger.info("obj is {}",obj.getObj());
			obj.setObj(new Object());
			logger.info("obj is {}",obj.getObj());
		}
	}
	
	public static void main(String[] args) throws IOException {
//		Thread t = new Thread(new BufferThread(8080));
//		t.start();
//		for(int i=0;i<300;i++){
//			Thread t = new Thread(new BufferThread(8080));
//			t.start();
//			logger.info("thread---"+i);
//		}
		ObjectTest obj = new ObjectTest();
		obj.setObj(new Object());

		logger.info("obj is {}",obj.getObj());
		
		Thread t = new Thread(new RefThread(obj));
		Thread t2 = new Thread(new ChangeRefThread(obj));
		t.start();
		t2.start();
		
	}
}
