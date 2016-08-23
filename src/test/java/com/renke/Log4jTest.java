package com.renke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log4jTest {

	final static Logger logger = LoggerFactory.getLogger(Log4jTest.class);
	
	public void printLogger(){
		long begin = System.currentTimeMillis();
		for(int i=0;i<1000000;i++){
			logger.trace("I'm trace");
			logger.debug("I'm debug");
			logger.info("I'm info");
			logger.warn("I'm warn");
			logger.error("I'm error");
		}
		logger.debug("i++: ? ms" , System.currentTimeMillis()-begin);
	}
	
	public static void main(String[] args) {
		Log4jTest log = new Log4jTest();
		log.printLogger();
	}
	
}
