package com.renke.api;
import java.nio.channels.SocketChannel;
import java.util.Map;

public interface ChannelHandler extends Handler{
	public Map<String,Object> resolveSocketChannel(SocketChannel socketChannel) throws Exception;
	public <DATA> void assembleData(DATA t,SocketChannel socketChannel) throws Exception;
}
