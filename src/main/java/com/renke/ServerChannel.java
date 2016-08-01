package com.renke;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerChannel {
	final static Logger logger = LoggerFactory.getLogger(ServerChannel.class);
	public static String response(String msg,String encode) throws UnsupportedEncodingException{
		StringBuilder sb = new StringBuilder();
		sb.append("HTTP/1.1 200 OK\r\n")
		.append("Date: Sat, 31 Dec 2005 23:59:59 GMT\r\n")
		.append("Content-Type: text/html;charset=").append(encode).append("\r\n")
		.append("Content-Length:").append(msg.getBytes(encode).length * 2).append("\r\n")
		.append("\r\n").append(msg);
		return sb.toString();
	}
	
	/**
	 * 
	 * 
	 * @author renke.zuo@foxmail.com
	 * @time 2016-07-27 09:28:55
	 * @param ssc
	 * @param msg
	 * @param encode
	 * @throws IOException
	 */
	public static void doSth(ServerSocketChannel ssc,String msg,String encode) throws IOException{
		SocketChannel sc = ssc.accept();
		logger.info("localAddress:{}",sc.getLocalAddress());
		logger.info("remoteAddress:{}",sc.getRemoteAddress());
		ByteBuffer bb = ByteBuffer.allocate(2048);
		int len = 0;
		while((len = sc.read(bb)) > 0){
			//读取到byteBuffer中，会将position设置为len+1
			//所以，如果想取到数据需要重置ByteBuffer中的几个变量
			//将limit设置为position，将position设置为0，将mark设置为-1
			//现在读取数组就是，从0开始，读取limit个数据
			bb.flip();
			byte[] buf = bb.array();
			String result = new String(buf,0,len);
//			logger.info("request:\r\n{}",result);
			if(result.endsWith("\r\n")){
				sc.shutdownInput();
				sc.write(ByteBuffer.wrap(response(msg,encode).getBytes(encode)));
				sc.write(ByteBuffer.wrap(msg.getBytes(encode)));
//				sc.shutdownOutput();
				sc.close();
				break;
			}
		}
		logger.info("---over---");
		if(sc.isOpen()) sc.close();
	}
	
	
	public static void startSimpleServer()throws IOException, InterruptedException{
		String msg = "hello world ! 你好，世界！I'm Simple";
		String encode = "UTF-8";
		ServerSocketChannel ssc = ServerSocketChannel.open();
		//如果此处不阻塞，while(true)会消耗CPU。
//		ssc.configureBlocking(false);
		ssc.bind(new InetSocketAddress(8017));
		while (true) {
			logger.info("Simple:I'm  wating ? ");
			doSth(ssc, msg, encode);
		}
	}
	
	/***
	 * 重复，循环
	 * accept/remove
	 * @author renke.zuo@foxmail.com
	 * @version V1.0
	 * @time 2016-07-21 17:19:45
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void startSelectorServer()throws IOException, InterruptedException{
		String msg = "hello world ! 你好，世界！I'm Selector";
		String encode = "UTF-8";
		Selector sel = Selector.open();
		for(int i=0;i<3;i++){
			ServerSocketChannel ssc = ServerSocketChannel.open();
			//阻塞操作交个selector了。
			ssc.configureBlocking(false);
			ssc.bind(new InetSocketAddress(8018+i));
			ssc.register(sel, SelectionKey.OP_ACCEPT);
		}
		//先搞明白selector是干嘛的再用吧
		//到目前为止，SocketChannel和ServerSocketChannel之间到底是什么关系
		//ServerSocketChannel控制通道SocketChannel
		//通道中有车Buffer
		//车中有数据Byte[]
		while (true) {
			logger.info("Selector:I'm  wating ? ");
			int selected = sel.select();
			if(selected>0){
				Iterator<SelectionKey> it = sel.selectedKeys().iterator();
				while(it.hasNext()){
					SelectionKey key = it.next();
					ServerSocketChannel serverChannel = (ServerSocketChannel)key.channel();
					doSth(serverChannel,msg,encode);
//					logger.info("I don't accept");
//					SocketChannel sc = serverChannel.accept();
//					logger.info("I  accept");
//					ByteBuffer bf = ByteBuffer.allocate(2048);
//					int len  = sc.read(bf);
//					logger.info("client say : {}",new String (bf.array(),0,len));
//					key.isReadable();
//					logger.info("I'm {}",key);
					//这行代码去掉以后，会导致sel.select不阻塞
					//现在碰到两个问题
					//1、如果没有96-102行代码，会导致无限循环
					//2、如果it.remove()这行代码删除掉，也会导致无线循环
					//首先，我猜测是因为read的原因。注释掉read，看看是否会循环
					//猜测错误，那么我向上注释，到sc=serverChannel.accept();感觉就是它了
					//OK开始无限循环了。
					//好了，取消上面的注释，注释掉it.remove()，看看执行两次会发生什么。
					//1、无限循环，selected = 1，一直循环，it.remove无效。如果不调用accept方法，他会一直执行。
					//2、也是无限循环，selected = 0，不调用remove方法，selected是0，调用remove方法，反而使它不为0了。[这是为什么啊？]
					//selector 中维护着一个selectedKeys
					//第一次进来，查找selectedKeys中不存在的就绪任务，找到了，返回1
					//第二次进来，查找selectedKeys中不存在的就绪任务，没找到，因为没有释放，返回0，为什么不等待呢？
					it.remove();
				}
			}
		}
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
//		Thread t1 = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					ServerChannel.startSelectorServer();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		});
//		Thread t2 = new Thread(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					ServerChannel.startSimpleServer();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		});
//		t1.start();
//		t2.start();
	}
}
