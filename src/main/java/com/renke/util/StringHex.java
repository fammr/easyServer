package com.renke.util;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

public class StringHex {
	private static String hexString = "0123456789ABCDEF";
	public final static int TYPE_HEX = 16;
	public final static int TYPE_OCTAL = 8;
	public final static int TYPE_BINARY = 2;
	/***
	 * 将字符串编码为16进制数
	 * 取每个字符，
	 * @author renke.zuo@foxmail.com
	 * @version V1.0
	 * @time 2016-07-08 13:48:27
	 * @param str
	 * @return
	 */
	public static String encode(String str) {
		// 根据默认编码获取字节数组
		byte[] bytes = str.getBytes();
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		// 将字节数组中每个字节拆解成2位16进制整数
		for (int i = 0; i < bytes.length; i++) {
			//11110000  >> 4   1111  高四位做与运算，将结果移动至第四位
			//00001111    低四位做与运算
			//一个字节拆成了两个16进制数
			//charAt(x)起码比toHexString(x);快4倍。
			sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
			sb.append(hexString.charAt(bytes[i] & 0x0f));
		}
		return sb.toString();
	}
	
	/**
	 * 将16进制字符串，解析为字符串
	 * @author renke.zuo@foxmail.com
	 * @version V1.0
	 * @time 2016-07-08 13:57:54
	 * @param bytes
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	public static String decode(String bytes,String encoding) throws UnsupportedEncodingException {
		bytes = bytes.toUpperCase();
		ByteArrayOutputStream baos = new ByteArrayOutputStream(
				bytes.length() / 2);
		// 将每2位16进制整数组装成一个字节
		for (int i = 0; i < bytes.length(); i += 2){
			baos.write(
				(hexString.indexOf(bytes.charAt(i)) << 4 | hexString.indexOf(bytes.charAt(i + 1)))
					);
		}
		return new String(baos.toByteArray(),encoding);
	}
	
	public static byte decode(byte[] bytes) {
		return (byte)(hexString.indexOf(bytes[0]) << 4 | hexString.indexOf(bytes[1]));
	}
	
	/**
	 * 将字符串转换为十进制
	 * @author renke.zuo@foxmail.com
	 * @version V1.0
	 * @time 2016-07-08 15:06:03
	 * @param hexString
	 * @param type[字符串代表的类型(二进制、八进制、十六进制)]
	 * @return
	 */
	public static int toInt(String hexString,int type){
		return Integer.parseInt(hexString,type);
	}
	
	/**
	 * 将value转换为对应类型的字符串
	 * @author renke.zuo@foxmail.com
	 * @version V1.0
	 * @time 2016-07-08 15:09:15
	 * @param value
	 * @param type
	 * @return
	 */
	public static String toTypeString(int value,int type){
		if(type == TYPE_BINARY){
			return Integer.toBinaryString(value);
		}else if(type == TYPE_HEX){
			return Integer.toHexString(value);
		}else if(type == TYPE_OCTAL){
			return Integer.toOctalString(value);
		}else{
			return Integer.toString(value);
		}
	}
}