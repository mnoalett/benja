package com.benja;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Request extends Thread {
	private double _time = 0;
	private int timeout;
	private String url;
	private int _statusCode;
	
	public Request(String url, int timeout){
        this.url = url;
        this.timeout = timeout;
    }
	
	public double getTime(){
        return _time;
    }
	
	public int getStatusCode(){
		return _statusCode;
	}
    
    public String getUrl(){
    	return this.url;
    }
    
	public void run() {
		_time = sendRequest(this.url, this.timeout);  
		//for(int i=0; i<10; i++)
		//System.out.println("test " + this.getName());
	}
	
	public double sendRequest(String link, int timeout){
		try {
			URL url = new URL(link);
			long startTime = System.currentTimeMillis();
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(timeout); //if the reply does not arrive within a specified timeout, I discard it
			connection.connect();

			int code = connection.getResponseCode();
			_statusCode = code;
			long elapsedTime = System.currentTimeMillis() - startTime;
			return elapsedTime;
		} catch(Exception ex) {
			return -1;
		}
	
    }
}
