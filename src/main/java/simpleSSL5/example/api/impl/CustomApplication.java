package simpleSSL5.example.api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simpleSSL5.App;
import simpleSSL5.example.comm.constant.HTTPMethod;
import simpleSSL5.example.comm.helper.HeaderHelper;
import simpleSSL5.example.comm.invoker.EasemobRestAPI;
import simpleSSL5.example.comm.wrapper.HeaderWrapper;

public class CustomApplication extends EasemobRestAPI  {
	private static final Logger logger = LoggerFactory.getLogger(App.class);
    private static final String ROOT_URI = "/customApplication";

	@Override
	public String getResourceRootURI() {
		// TODO Auto-generated method stub
		return ROOT_URI;
	}

	 public void sendMessage() {
		 HeaderWrapper header = HeaderHelper.getDefaultHeaderWithToken();
		 logger.info("sendMessage called:"+header.getHeaders());
		 getInvoker().sendRequest(HTTPMethod.METHOD_GET, getResourceRootURI(), header, null, null);
	 }
}
