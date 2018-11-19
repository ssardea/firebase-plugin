package com.psi.kaspro.firebase.common;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;

import com.tlc.common.Logger;
import com.tlc.common.Util;

public class Endpoint {
	static Properties props = new Properties();
	private static String projectID;
	private static String baseUrl;
	private static String messagingScope;
	private static ArrayList<String> scopes = new ArrayList<String>();
	private static String fcmEndpoint;		
	private static final String MESSAGE_KEY = "message";
	
	static{
	    try{
	      props.load(new FileInputStream(new File(Util.getWorkingDirectory() + "/mcash/config/firebase.conf")));
	      baseUrl = ((String)Util.isNull(props.getProperty("baseurl"), "https://fcm.googleapis.com")).toString();
	      messagingScope = ((String)Util.isNull(props.getProperty("messagingscope"), "https://www.googleapis.com/auth/firebase.messaging")).toString();
	      scopes.add(messagingScope);
	      
	      projectID = (String)Util.isNull(props.getProperty("projectid"), "fcmapp-5363b");
	      fcmEndpoint = "/v1/projects/" + projectID + "/messages:send";
	    }catch (Exception e){
	      Logger.LogServer(e);
	    }
	  }

	public static String getProjectID() {
		return projectID;
	}

	public void setProjectID(String projectID) {
		Endpoint.projectID = projectID;
	}

	public static String getBaseUrl() {
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl) {
		Endpoint.baseUrl = baseUrl;
	}

	public static String getMessagingScope() {
		return messagingScope;
	}

	public void setMessagingScope(String messagingScope) {
		Endpoint.messagingScope = messagingScope;
	}

	public static ArrayList<String> getScopes() {
		return scopes;
	}

	public void setScopes(ArrayList<String> scopes) {
		Endpoint.scopes = scopes;
	}

	public static String getFcmEndpoint() {
		return fcmEndpoint;
	}

	public void setFcmEndpoint(String fcmEndpoint) {
		Endpoint.fcmEndpoint = fcmEndpoint;
	}

	public static String getMessageKey() {
		return MESSAGE_KEY;
	}
}
