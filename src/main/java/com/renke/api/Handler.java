package com.renke.api;

import com.renke.exception.HandlerException;

public interface Handler {
	public <T> byte[] handle(T t) throws HandlerException;
}
