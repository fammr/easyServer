package com.renke.exception;

public class HTTPException extends Exception {
	private static final long serialVersionUID = 1L;
	public HTTPException(String error){
		super(error);
	}
	public HTTPException(Throwable e){
		super(e);
	}
}
