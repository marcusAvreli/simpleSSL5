package simpleSSL5.example.comm.helper;

import simpleSSL5.ClientContext;
import simpleSSL5.example.comm.wrapper.HeaderWrapper;

public class HeaderHelper {
	private static ClientContext context = ClientContext.getInstance();
	
	public static HeaderWrapper getDefaultHeader() {
		return HeaderWrapper.newInstance().addJsonContentHeader();
	}
	
	public static HeaderWrapper getDefaultHeaderWithToken() {
		return getDefaultHeader().addAuthorization(context.getAuthToken());
	}

}
