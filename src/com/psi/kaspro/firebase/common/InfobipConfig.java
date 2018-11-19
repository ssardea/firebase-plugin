package com.psi.kaspro.firebase.common;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import com.tlc.common.Logger;
import com.tlc.common.Util;

public class InfobipConfig{
  static Properties props = new Properties();
  private static String url;
  private static String host;
  private static String userid;
  private static String password;
  private static String inquireUrl;
  
  
  static{
    try{
      props.load(new FileInputStream(new File(Util.getWorkingDirectory() + "/mcash/config/sms-infobip.conf")));
      url = ((String)Util.isNull(props.getProperty("url"), "http://localhost:8080/sendsms")).toString();
      host = (String)Util.isNull(props.getProperty("host"), "localhost");
      userid = (String)Util.isNull(props.getProperty("userid"), "USERID");
      password = (String)Util.isNull(props.getProperty("password"), "PASSWORD");
      inquireUrl = ((String)Util.isNull(props.getProperty("inquireurl"), "http://localhost:8080/sendsms")).toString();
    }catch (Exception e){
      Logger.LogServer(e);
    }
  }
  
  public static String getUrl(){
    return url;
  }
  
  public static String getHost(){
    return host;
  }
  
  public static String getUserid(){
    return userid;
  }
  
  public static String getPassword(){
    return password;
  }

  public static String getInquireUrl() {
	return inquireUrl;
  }
}