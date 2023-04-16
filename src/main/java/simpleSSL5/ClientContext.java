package simpleSSL5;



import java.io.IOException;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simpleSSL5.api.oauth2.AccessToken;
import simpleSSL5.api.oauth2.AccessTokenGrantRequest;
import simpleSSL5.api.oauth2.TemporalAccessToken;
import simpleSSL5.api.oauth2.TokenChangeObserver;
import simpleSSL5.api.security.oatuh2.client.NoRetryPolicy;
import simpleSSL5.api.security.oatuh2.client.RetryPolicy;

public class ClientContext {

	private static final Logger logger = LoggerFactory.getLogger(ClientContext.class);
	private Boolean initialized = Boolean.FALSE;

	private static ClientContext context;
	public static final String JERSEY_API = "jersey";
	private ClientManager kbm = null;
	private FactoryDAO factoryDAO;
	private AutoRenewingTokenProvider<MyToken> tokenProvider = null;
	private String accessToken = null;
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public static ClientContext getInstance() {
		if( null == context ) {
			context = new ClientContext();
		}
		
		return context;
	}
	public ClientContext init()  {
	
		return context;
	}
	public Boolean isInitialized() {
		return initialized;
	}
	
	
	public FactoryDAO getAPIFactory() {
		
		
		if( null == this.factoryDAO ) {
			this.factoryDAO = FactoryDAO.getInstance(context);
		}
		
		return this.factoryDAO;
	}
	public String getAuthToken() {
		if( null == accessToken ) {
			logger.error("invalid_token");
			throw new RuntimeException("invalid_token");
		}
		
		return accessToken;
	}
	public void setAuthToken(String token) {
		this.accessToken = token;
	}
}
