package com.renke;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Controller {
	final static Logger logger = LoggerFactory.getLogger(Controller.class);
	static Properties properties = new Properties();
	private static void init(){
		try {
			properties.load("init.conf");
			properties.load(properties.get("config.path") , true);
			logger.debug("--------------load {} config over--------------","server");
			logger.debug(properties.toString());
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
	
	public static void main(String[] args) {
		reload();
		//线程池配置，多host配置[单机，多机]，默认编码格式，cluster，master
		//启动主线程读取master配置
		
		
		
	}
	
}
