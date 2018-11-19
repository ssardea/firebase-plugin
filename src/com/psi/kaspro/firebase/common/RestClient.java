package com.psi.kaspro.firebase.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.CharEncoding;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.psi.http.rest.common.CircularLinkedList;
import com.psi.http.rest.common.HttpDelete;
import com.psi.http.rest.common.Logger;
import com.psi.http.rest.common.RestSettings;
import com.psi.http.rest.common.TLSSocketFactory;


@SuppressWarnings("deprecation")
public class RestClient {
	
	public static final String PROP_CONTENT_TYPE="Content-Type";
	public static final String PROP_AUTH  = "Authorization";
	public static final String PROP_HOST = "Host";
	
	private RestSettings cfg;
	private Hashtable<String,String> headers=new Hashtable<String, String>();
	private CircularLinkedList<HttpClient> httpClients=new CircularLinkedList<HttpClient>();
	public RestClient(RestSettings cfg) throws Exception {
		if(cfg!=null){
			this.cfg = cfg;
			initHttpClient();
		}	
	}
	
	public void addHeader(String key, String value){
		if(headers.containsKey(key))
			this.headers.replace(key, value);
		else
			this.headers.put(key, value);
	}
	
	public void removeHeader(String key) {
		this.headers.remove(key);
	}
	
	 private void initHttpClient() throws Exception {
		 		 		       
	        URI uri = new URI(cfg.getUrl());	  
	        
	        if(uri.getScheme().toLowerCase().equals("https")){
	        	for(int x = 0;x<=this.cfg.getConnections();x++){
	        		this.httpClients.add( initHttpsClient(uri.getPort(),cfg.getConnections(),
							cfg.getWorkers(),cfg.getTrustFile(),cfg.getTrustPass(),cfg.getKeyFile(),cfg.getKeyPass()));	
	        	}
	        	
	        }else{
	        	
	        	PoolingClientConnectionManager cm = new PoolingClientConnectionManager( );
		        cm.setMaxTotal(cfg.getConnections());
		        cm.setDefaultMaxPerRoute(cfg.getWorkers());
		        for(int x=0;x<=this.cfg.getConnections();x++){		        	
		        	DefaultHttpClient c =  new DefaultHttpClient(cm);
		        	HttpParams params = c.getParams();
			        HttpConnectionParams.setConnectionTimeout(params, cfg.getTimeout());
			        HttpConnectionParams.setSoTimeout(params, cfg.getTimeout());
		        	this.httpClients.add(c);
		        }
	        }   
	    }
	
	@SuppressWarnings("unused")
	private static DefaultHttpClient initHttpsClient(int port,int connections,int workers,String trustFile,String trustKey,String keyFile,String keyKey) {
		 
		 java.lang.System.setProperty("https.protocols", "TLSv1.2");
		 java.lang.System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
		 java.lang.System.setProperty("sun.security.ssl.allowLegacyHelloMessages", "true");
		
		 port = port==-1?443:port;
			
		 
			KeyStore trustStore;
			KeyStore keyStore;
			try {
				
				TrustManagerFactory tmfactory=null;
				TrustManager tm = null;
				if(trustFile!=null){
					FileInputStream instream = new FileInputStream(new File(trustFile));
					trustStore = KeyStore.getInstance("JKS");
					trustStore.load(instream,trustKey.toCharArray());				
					tmfactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
					tmfactory.init(trustStore);
				}else{
						tm = new X509TrustManager() {
						@Override
						public void checkClientTrusted(X509Certificate[] arg0, String arg1)
								throws CertificateException {
						}
						@Override
						public void checkServerTrusted(X509Certificate[] arg0, String arg1)
								throws CertificateException {
						}
						@Override
						public X509Certificate[] getAcceptedIssuers() {
							return null;
						}
			        };
				}
				
				KeyManagerFactory keyFactory=null;
				if(keyFile!=null){
					FileInputStream knstream = new FileInputStream(new File(keyFile));
					keyStore = KeyStore.getInstance("PKCS12");
					keyStore.load(knstream,keyKey.toCharArray());
					keyFactory = KeyManagerFactory.getInstance("SunX509");
					keyFactory.init(keyStore, keyKey.toCharArray());
				}
				
				SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
				sslContext.init(null, null, null);
				
				String[] protocols = sslContext.getSocketFactory().getSupportedCipherSuites();
				System.out.println("Enabled Protocols: ");
		        for (int i = 0; i < protocols.length; i++) {
		            System.out.println(protocols[i] + ", ");
		        }
		        String[] supportedProtocols = sslContext.getSocketFactory().getDefaultCipherSuites();
		        System.out.println("Supported Ciphers: ");
		        for (int i = 0; i < protocols.length; i++) {
		            System.out.println(supportedProtocols[i] + ", ");
		        }
		        
				HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
		     	TLSSocketFactory sf = new TLSSocketFactory(sslContext,TLSSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		        SchemeRegistry schemeRegistry = new SchemeRegistry();
		        schemeRegistry.register(new Scheme("https", port,sf));
		        PoolingClientConnectionManager cm = new PoolingClientConnectionManager( schemeRegistry);
		        
		        cm.setMaxTotal(connections);
		        cm.setDefaultMaxPerRoute(workers);		        
		        return new DefaultHttpClient(cm);
		    } catch (Exception ex) {
		    	PoolingClientConnectionManager cm = new PoolingClientConnectionManager( );
		        cm.setMaxTotal(connections);
		        cm.setDefaultMaxPerRoute(workers);		        
		        return new DefaultHttpClient(cm);
		    }
		}
		 
	public RestSettings getConfig(){
		return this.cfg;
	}
	public RestResponse post(List<NameValuePair> req) throws Exception{
		return this.post(this.headers,"",req);
	}
	
	public RestResponse post(String req) throws Exception{
		return this.post("",req);
	}
	
	public RestResponse post(Hashtable<String,String> hdr,String urlparams,List<NameValuePair> req) throws Exception
	  {
		
		RestResponse ret = new RestResponse(404, "");
		Logger.LogServer(this.cfg.getUrl()+urlparams);
	    HttpPost post = new HttpPost(this.cfg.getUrl()+urlparams);
	    DefaultHttpClient c = (DefaultHttpClient)this.httpClients.nextValue();
	    HttpParams params = c.getParams();
	    HttpConnectionParams.setConnectionTimeout(params, this.cfg.getTimeout());

	    HttpResponse res = null;
	    for (String s : hdr.keySet()) {
	      post.setHeader(s, (String)hdr.get(s));
	    }

	    post.setParams(params);
	    post.setEntity(new UrlEncodedFormEntity(req,CharEncoding.UTF_8));
	    
	    StringBuilder outp = new StringBuilder();
	    res = c.execute(post);

	    BufferedReader rdr = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
	    String tmp = null;
	    do {
	      tmp = rdr.readLine();
	      if (tmp != null) outp.append(tmp); 
	    }
	    while (tmp != null);
	    ret = new RestResponse(res.getStatusLine().getStatusCode(), outp.toString());
	    for(Header h: res.getAllHeaders()){
	    	ret.addHeader(h.getName(), h.getValue());
	    }
	    return ret;
	  }

	public RestResponse get(String pathParams) throws Exception {
	    HttpGet get = new HttpGet(this.cfg.getUrl() +pathParams);
	    DefaultHttpClient c = (DefaultHttpClient)this.httpClients.nextValue();
	    HttpParams params = c.getParams();
	    HttpConnectionParams.setConnectionTimeout(params, this.cfg.getTimeout());
	    HttpConnectionParams.setSoTimeout(params, this.cfg.getTimeout());
	    RestResponse ret = new RestResponse(404, "");
	    HttpResponse res = null;

	    get.setHeader("Host", this.cfg.getHost());
	    for (String s : this.headers.keySet()) {
	      get.setHeader(s, (String)this.headers.get(s));
	    }

	    StringBuilder outp = new StringBuilder();
	    res = c.execute(get);
	  
	    int status = res.getStatusLine().getStatusCode();
	    BufferedReader rdr = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
	    String tmp = null;
	    do {
	      tmp = rdr.readLine();
	      if (tmp != null) outp.append(tmp); 
	    }
	    while (tmp != null);

	    ret = new RestResponse(status, outp.toString());
	    for(Header h: res.getAllHeaders()){
	    	ret.addHeader(h.getName(), h.getValue());
	    }
	    return ret;
	  }
	
	
	public RestResponse post(HashMap<String,String> hdr,String urlparams,String req) throws Exception{
		HttpPost post = new HttpPost(cfg.getUrl()+urlparams);
		DefaultHttpClient c = (DefaultHttpClient) this.httpClients.nextValue();
		HttpParams params = c.getParams();
        HttpConnectionParams.setConnectionTimeout(params, cfg.getTimeout());
        HttpConnectionParams.setSoTimeout(params, cfg.getTimeout());
        RestResponse ret = new RestResponse(404, "");
		HttpResponse res = null;
		
		post.setHeader(PROP_HOST,cfg.getHost());
		for(String s : this.headers.keySet()){
			post.setHeader(s,this.headers.get(s));
		}
		
		if(hdr!=null){
			for(String s : hdr.keySet()){
				post.setHeader(s,hdr.get(s));
			}
		}
		
		post.setEntity(new StringEntity(req.toString()));
	
		StringBuilder outp = new StringBuilder();
		res = c.execute(post);
		BufferedReader rdr = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
	    String tmp=null;
	    do{
	    	tmp = rdr.readLine();
	    	if(tmp!=null) outp.append(tmp);
	    }while(tmp!=null);
	    
	    ret = new RestResponse(res.getStatusLine().getStatusCode(), outp.toString());
	    for(Header h: res.getAllHeaders()){
	    	ret.addHeader(h.getName(), h.getValue());
	    }
	    return ret;
	
	}
	public RestResponse post(String urlparams, String req) throws Exception{
		return post(null, urlparams,req);
	}

	public RestResponse put(String urlparam,String req) throws Exception{
		HttpPut put = new HttpPut(cfg.getUrl()+urlparam);
		DefaultHttpClient c = (DefaultHttpClient) this.httpClients.nextValue();
		HttpParams params = c.getParams();
        HttpConnectionParams.setConnectionTimeout(params, cfg.getTimeout());
        HttpConnectionParams.setSoTimeout(params, cfg.getTimeout());
        RestResponse ret = new RestResponse(404, "");
		HttpResponse res = null;
		put.setHeader(PROP_CONTENT_TYPE,RestSettings.PROP_CONTENT_TYPE);
		put.setHeader(PROP_HOST,cfg.getHost());
		for(String s : this.headers.keySet()){
			put.setHeader(s,this.headers.get(s));
		}
		put.setEntity(new StringEntity(req.toString()));
	
		StringBuilder outp = new StringBuilder();
		res = c.execute(put);
		BufferedReader rdr = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
	    String tmp=null;
	    do{
	    	tmp = rdr.readLine();
	    	if(tmp!=null) outp.append(tmp);
	    }while(tmp!=null);
	    
	    ret = new RestResponse(res.getStatusLine().getStatusCode(), outp.toString());
	    for(Header h: res.getAllHeaders()){
	    	ret.addHeader(h.getName(), h.getValue());
	    	System.out.println(h.getName() + ": " +   h.getValue());
	    }
	    return ret;
	}
	
	public RestResponse put(String req) throws Exception{
		HttpPut put = new HttpPut(cfg.getUrl());
		DefaultHttpClient c = (DefaultHttpClient) this.httpClients.nextValue();
		HttpParams params = c.getParams();
        HttpConnectionParams.setConnectionTimeout(params, cfg.getTimeout());
        HttpConnectionParams.setSoTimeout(params, cfg.getTimeout());
        RestResponse ret = new RestResponse(404, "");
		HttpResponse res = null;
		put.setHeader(PROP_CONTENT_TYPE,RestSettings.PROP_CONTENT_TYPE);
		put.setHeader(PROP_HOST,cfg.getHost());
		for(String s : this.headers.keySet()){
			put.setHeader(s,this.headers.get(s));
		}
		put.setEntity(new StringEntity(req.toString()));
	
		StringBuilder outp = new StringBuilder();
		res = c.execute(put);
		
		BufferedReader rdr = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
	    String tmp=null;
	    do{
	    	tmp = rdr.readLine();
	    	if(tmp!=null) outp.append(tmp);
	    }while(tmp!=null);
	    
	    ret = new RestResponse(res.getStatusLine().getStatusCode(), outp.toString());
	    for(Header h: res.getAllHeaders()){
	    	ret.addHeader(h.getName(), h.getValue());
	    }
	    return ret;
	}
	

	public RestResponse delete(String urlparam,String req) throws Exception{
		HttpPut put = new HttpPut(cfg.getUrl()+urlparam);
		DefaultHttpClient c = (DefaultHttpClient) this.httpClients.nextValue();
		HttpParams params = c.getParams();
        HttpConnectionParams.setConnectionTimeout(params, cfg.getTimeout());
        HttpConnectionParams.setSoTimeout(params, cfg.getTimeout());
        RestResponse ret = new RestResponse(404, "");
		HttpResponse res = null;
		put.setHeader(PROP_CONTENT_TYPE,RestSettings.PROP_CONTENT_TYPE);
		put.setHeader(PROP_HOST,cfg.getHost());
		for(String s : this.headers.keySet()){
			put.setHeader(s,this.headers.get(s));
		}
		put.setEntity(new StringEntity(req.toString()));
	
		StringBuilder outp = new StringBuilder();
		res = c.execute(put);
		BufferedReader rdr = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
	    String tmp=null;
	    do{
	    	tmp = rdr.readLine();
	    	if(tmp!=null) outp.append(tmp);
	    }while(tmp!=null);
	    
	    ret = new RestResponse(res.getStatusLine().getStatusCode(), outp.toString());
	    for(Header h: res.getAllHeaders()){
	    	ret.addHeader(h.getName(), h.getValue());
	    }
	    return ret;
	}
	public RestResponse delete(List<NameValuePair> req) throws Exception
	  {
		RestResponse ret = new RestResponse(404, "");
	    HttpDelete delete = new HttpDelete(this.cfg.getUrl());
	    DefaultHttpClient c = (DefaultHttpClient)this.httpClients.nextValue();
	    HttpParams params = c.getParams();
	    HttpConnectionParams.setConnectionTimeout(params, this.cfg.getTimeout());
	    HttpConnectionParams.setSoTimeout(params, this.cfg.getTimeout());

	    HttpResponse res = null;

	    delete.setHeader("Host", this.cfg.getHost());
	    for (String s : this.headers.keySet()) {
	      delete.setHeader(s, (String)this.headers.get(s));
	    }
	    for(NameValuePair p: req){
	    	params.setParameter(p.getName(), p.getValue());
	    }
	    delete.setParams(params);
	    
	 
	    StringBuilder outp = new StringBuilder();
	    res = c.execute(delete);

	    BufferedReader rdr = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
	    String tmp = null;
	    do {
	      tmp = rdr.readLine();
	      if (tmp != null) outp.append(tmp); 
	    }
	    while (tmp != null);
	    rdr.close();
	    ret = new RestResponse(res.getStatusLine().getStatusCode(), outp.toString());
	    for(Header h: res.getAllHeaders()){
	    	ret.addHeader(h.getName(), h.getValue());
	    }
	    return ret;
	  }
	
	
	
	public class TrustAllX509TrustManager implements X509TrustManager {
	    public X509Certificate[] getAcceptedIssuers() {
	        return new X509Certificate[0];
	    }

	    public void checkClientTrusted(java.security.cert.X509Certificate[] certs,
	            String authType) {
	    }

	    public void checkServerTrusted(java.security.cert.X509Certificate[] certs,
	            String authType) {
	    }

	}
	
	/** Create a trust manager that does not validate certificate chains **/
	static TrustManager[] trustAllCerts = new TrustManager[]{ new X509TrustManager() {
		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {
		}
	
		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1)
				throws CertificateException {		
		}
	
		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}			
	}};
}
