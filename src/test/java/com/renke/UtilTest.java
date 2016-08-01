package com.renke;

import java.util.Arrays;

import com.renke.util.StringHex;

public class UtilTest {
	
	public static void main(String[] args) throws Exception {
		System.out.println(StringHex.decode("2B","UTF-8"));
		
		int[] b = {0,1,2,3,4,5,6,7,8,9,10};
		int[] c = Arrays.copyOfRange(b, 3, 6);
		for(int i : c)
			System.out.println(i);
		
		byte[] buf = {'2','B'};
		
		
//		byte[] buf = {StringHex.decode("2B")};
		System.out.println(new String(buf));
		
		String str = "ÄãºÃ£¬ÖÐ¹ú,hello world!";
		System.out.println(str.getBytes("UTF-8").length);
		System.out.println(str.getBytes("GBK").length);
		
	}
}
