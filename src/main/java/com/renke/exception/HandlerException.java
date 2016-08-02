package com.renke.exception;

public class HandlerException extends HTTPException {
	private static final long serialVersionUID = 1L;
	public HandlerException(String error){
		super(error);
	}
	public HandlerException(Throwable e){
		super(e);
	}
}
