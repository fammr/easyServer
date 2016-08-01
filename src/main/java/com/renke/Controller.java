package com.renke;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.renke.constant.ServerConf;


public class Controller {
	public static Properties properties = new Properties();
	final static Logger logger = LoggerFactory.getLogger(Controller.class);
	//编码方式
	public static String encoding;
	//接收请求数
	static AtomicInteger ai = new AtomicInteger(0);
	//成功请求数
	static AtomicInteger sucess = new AtomicInteger(0);
	private static void init(){
		try {
			properties.load(ServerConf.FILE_NAME);
			encoding = properties.getProperty(ServerConf.S_L_ENCODING,ServerConf.DEFAULT_ENCODING);
			logger.debug("--------------load {} config over-------------- \r\n properties:{}"
					,"server",properties.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void reload(){
		properties.clear();
		init();
	}
	
	public static void startSelectorServer(int port,ExecutorService es)throws IOException, InterruptedException{
		Selector sel = Selector.open();
		ServerSocketChannel ssc = ServerSocketChannel.open();
		//阻塞操作交给selector了。
		ssc.configureBlocking(false);
		ssc.bind(new InetSocketAddress(port));
		ssc.register(sel, SelectionKey.OP_ACCEPT);
		while (true) {
			logger.info("Selector:I'm  wating {} ",ai.get());
			logger.info("Selector:I'm  done {} ",sucess.get());
			int selected = sel.select();
			if(selected>0){
				ai.incrementAndGet();
				Iterator<SelectionKey> it = sel.selectedKeys().iterator();
				while(it.hasNext()){
					SelectionKey key = it.next();
					ServerSocketChannel serverChannel = (ServerSocketChannel)key.channel();
					SocketChannel socketChannel = serverChannel.accept();
					Runnable r = new HTTPExcutor(socketChannel,sucess);
					es.execute(r);
					it.remove();
				}
			}
		}
	}
	
	public static void main(String[] args) {
		//读取配置
		reload();
		//线程池配置[最大链接数，初始链接数，增加数]
		int minimumPoolSize = Integer.parseInt(properties.getProperty(ServerConf.T_P_MINSIZE));
		int maximumPoolSize = Integer.parseInt(properties.getProperty(ServerConf.T_P_MAXSIZE));
		long keepAliveTime = Long.parseLong(properties.getProperty(ServerConf.T_P_ALIVETIME));
		ExecutorService es = new ThreadPoolExecutor(minimumPoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
		//启动监听程序主线程
		int port = Integer.parseInt(properties.getProperty(ServerConf.S_L_PORT));
		try {
			startSelectorServer(port,es);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
