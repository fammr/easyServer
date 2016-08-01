package com.renke.api;
/**
 * id,data,order,level,status
 * @author renke.zuo@foxmail.com
 */

import java.io.Serializable;

public interface Node extends Serializable{
	public void setId(int id);
	public int getId();
	public void setData(Object data);
	public Object getData();
	public void setOrder(char order);
	public char getOrder();
	public void setLevel(int level);
	public int getLevel();
	public void setStatus(int status);
	public int getStatus();
}
