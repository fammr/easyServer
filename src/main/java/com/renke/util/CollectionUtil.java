package com.renke.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class CollectionUtil {
	
	public static String collectionToString(Collection<?> coll){
		Iterator<?> it = coll.iterator();
		StringBuilder sb = new StringBuilder();
		while(it.hasNext()){
			Object obj = it.next();
			if(obj!=null){
				sb.append("\r\n").append(obj);
			}
		}
		return sb.toString().substring(2);
	}
	
	public static String mapToString(Map<String,?> map){
		StringBuilder sb = new StringBuilder();
		Iterator<String> it = map.keySet().iterator();
		while(it.hasNext()){
			String obj = it.next();
			if(obj!=null){
				sb.append("\r\n").append(obj).append(" = ").append(map.get(obj));
			}
		}
		return sb.toString().substring(sb.length()>2 ? 2 : 0);
	}
}
