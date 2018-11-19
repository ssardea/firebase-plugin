package com.psi.kaspro.firebase.common;

import java.util.HashMap;

import org.springframework.http.HttpStatus;

public final class RestResponse {

	private String data;
	private HashMap<String, String> headers = new HashMap<String, String>();
	private HttpStatus status;
	
	public RestResponse(int status,String data){
		this.data = data;
		this.status = HttpStatus.valueOf(status);
	}
	protected void addHeader(String key,String value){
		this.headers.put(key, value);
	}
	
	public String getData(){
		return this.data;
	}
	
	public HttpStatus getStatus(){
		return this.status;
	}
	
	public String getHeader(String key){
		return this.headers.get(key);
	}
	
	public HashMap<String, String> allHeaders(){
		return this.headers;
	}
	
	
}
