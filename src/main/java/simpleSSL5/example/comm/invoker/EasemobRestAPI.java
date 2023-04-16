package simpleSSL5.example.comm.invoker;

import org.glassfish.jersey.client.JerseyClient;

import simpleSSL5.ClientContext;
import simpleSSL5.example.api.RestAPIInvoker;

public abstract class EasemobRestAPI implements RestAPI {
	
	private ClientContext context;
	

	private RestAPIInvoker invoker;

	public abstract String getResourceRootURI();
	
	public ClientContext getContext() {
		return context;
	}

	public void setContext(ClientContext context) {
		this.context = context;
	}

	public JerseyRestAPIInvoker getInvoker() {
		return new JerseyRestAPIInvoker();
	}

	public void setInvoker(RestAPIInvoker invoker) {
		this.invoker = invoker;
	}
}
