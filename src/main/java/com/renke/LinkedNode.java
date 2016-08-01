package com.renke;

import com.renke.api.Node;
public class LinkedNode implements Node{
	private static final long serialVersionUID = 1L;
	private int id;
	private LinkedNode prev;
	private LinkedNode next;
	private Object data;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public LinkedNode getPrev() {
		return prev;
	}
	public void setPrev(LinkedNode prev) {
		this.prev = prev;
	}
	public LinkedNode getNext() {
		return next;
	}
	public void setNext(LinkedNode next) {
		this.next = next;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	
	
	
	@Override
	public void setOrder(char order) {
		
	}
	@Override
	public char getOrder() {
		return 0;
	}
	@Override
	public void setLevel(int level) {
		
	}
	@Override
	public int getLevel() {
		return 0;
	}
	@Override
	public void setStatus(int status) {
		
	}
	@Override
	public int getStatus() {
		return 0;
	}
	
}
