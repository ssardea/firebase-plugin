package com.psi.kaspro.firebase;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.psi.http.rest.common.RestSettings;
import com.psi.kaspro.firebase.common.Endpoint;
import com.psi.kaspro.firebase.common.RestClient;
import com.psi.kaspro.firebase.common.RestResponse;
import com.tlc.common.Logger;
import com.tlc.common.Util;

public class FirebaseMessage {
	static RestSettings settings = new RestSettings();
	static RestClient client = null;
	
	private String topic;
	private String title;
	private String message;
	
	static {
		settings.setUrl(Endpoint.getBaseUrl() + Endpoint.getFcmEndpoint());
		settings.setTimeout(60000);
		settings.setConnections(1);
		settings.setWorkers(100);
		settings.setTrustFile(null);
		settings.setTrustPass(null);
		settings.setKeyFile(null);
		settings.setKeyPass(null);
		settings.setHost("fcm.googleapis.com");
		
		try {
			client = new RestClient(settings);
			client.addHeader("Content-Type", "application/json; UTF-8");
		} catch (Exception e) {
			Logger.LogServer(e);
		}
	}
	
	public boolean send() {
		JSONObject notificationMessage = buildNotificationMessage();
		try {
			if (sendMessage(notificationMessage)) return true; return false;
		}catch(Exception e) {
			Logger.LogServer(e);
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	private  JSONObject buildNotificationMessage() {
		JSONObject jNotification = new JSONObject();
		jNotification.put("title", title);
		jNotification.put("body", message);
		
		JSONObject jMessage = new JSONObject();
		jMessage.put("notification", jNotification);
		jMessage.put("topic", this.topic);
		
		JSONObject jFcm = new JSONObject();
		jFcm.put(Endpoint.getMessageKey(), jMessage);
		return jFcm;
	}
		
	private boolean sendMessage(JSONObject fcmMessage) throws Exception {
		System.out.println(fcmMessage.toJSONString());
		client.addHeader("Authorization ", "Bearer " + getAccessToken());
		RestResponse res = client.post(fcmMessage.toJSONString());
		
		if(res.getStatus() == HttpStatus.OK) {
			System.out.println("Message sent to Firebase for delivery, response:");
			System.out.println(res.getData().toString());
			return true;
		}else {
			System.out.println("Unable to send message to Firebase:");
			System.out.println(res.getData().toString());
		    return false;
		}
		
	}
	
	private static String getAccessToken() throws IOException {
	    GoogleCredential googleCredential = GoogleCredential
	        .fromStream(new FileInputStream(new File(Util.getWorkingDirectory() + "/mcash/config/service-account.json")))
	        .createScoped(Endpoint.getScopes());
	    googleCredential.refreshToken();
	    return googleCredential.getAccessToken();
	 }
	
	
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
}
