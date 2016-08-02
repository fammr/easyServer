package com.renke.api;
import java.nio.channels.SocketChannel;
import java.util.Map;

import com.renke.exception.HandlerException;

public interface ChannelHandler extends Handler{
	public Map<String,Object> resolveSocketChannel(SocketChannel socketChannel) throws Exception;
	public byte[] assembleData(byte[] data) throws Exception;
}