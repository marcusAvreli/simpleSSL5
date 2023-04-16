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
import simpleSSL5.example.api.RestAPIInvoker;
import simpleSSL5.example.comm.invoker.EasemobRestAPI;
import simpleSSL5.example.comm.invoker.JerseyRestAPIInvoker;
import simpleSSL5.example.comm.utils.RestAPIUtils;

public class FactoryDAO implements TokenChangeObserver<AccessToken>{
	private static final Logger logger = LoggerFactory.getLogger(FactoryDAO.class);
	private static FactoryDAO factory;
	private ClientContext context;
	public static final String USER_CLASS = "simpleSSL5.example.api.impl.CustomApplication";

	private RestAPIInvoker jersey = new JerseyRestAPIInvoker();
	private AutoRenewingTokenProvider<MyToken> tokenProvider = null;
	private String accessToken = null;
	private FactoryDAO(ClientContext context) {
		this.context = context;
	}
	public static FactoryDAO getInstance(ClientContext context) {
		if( null == factory ) {
			
			
			factory = new FactoryDAO(context);
		}
		
		return factory;
	}
	public void startClient() {
		RestAPIUtils.getJerseyClient();
		RestAPIUtils apiUtil = new RestAPIUtils(MyToken.class);
		AccessTokenGrantRequest grant  = new AccessTokenGrantRequest("grant_type","client_id", "secret", null);
		OAuthTokenServiceDelegate oauth = new OAuthTokenServiceDelegate(grant,apiUtil,"/oauth2/generateToken");
		RetryPolicy policy = new NoRetryPolicy();
		this.tokenProvider = new AutoRenewingTokenProvider<MyToken>(oauth,policy,apiUtil);
		//this.tokenProvider.attach(this.tokenUser);
		this.tokenProvider.attach(this);
		try {
			this.tokenProvider.start();
			
			if(tokenProvider.isActive()) {
				logger.info("yes, is active");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void tokenChanged(TemporalAccessToken<AccessToken> newToken, TemporalAccessToken<AccessToken> oldToken) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
				logger.info("token_changed");
				//logger.info("token_new:"+newToken);
				//logger.info("token_old:"+oldToken);
				if(null != newToken) {
					logger.info("new_token_is_not_null");
					accessToken = newToken.token().getAccessToken();
				}else {
					logger.info("new_token_is_null");
				}
				if(null != oldToken) {
					logger.info("old_token_is_not_null");
				}else {
					logger.info("old_token_is_null");
				}
			
				ClientContext context= ClientContext.getInstance();
				context.setAccessToken(accessToken);
				
				logger.info("token:"+accessToken);
				if(null !=oldToken) {
					Duration oldTokenTtl = oldToken.ttl();
				if(null !=oldTokenTtl) {
					Duration newD = newToken.ttl();
					
					if(!newD.equals(oldTokenTtl)) {
						logger.info("=============yes===============");
						this.tokenProvider.stop(false);
						logger.info("=============after stop===============");
						try {
							this.tokenProvider.start();
							logger.info("=============restarted_successfully===============");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}else {
					logger.info("old is null");
				}
				}else {
					logger.info("old_token_is_null");
				}
				
	}
	public EasemobRestAPI newInstance(String className) {
		return (EasemobRestAPI)getClassInstance(className);
	}
	private Object getClassInstance(String className) {
		Class<?> targetClass = null;
		Object newObj = null;
		
		try {
			targetClass = Class.forName(className);
			newObj = targetClass.newInstance();
		}
		 catch (Exception e) {
			 logger.error("ssd", e);
		 }
		return newObj;
	}

}
