package simpleSSL5;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simpleSSL5.api.http.TokenServiceHttpClient;
import simpleSSL5.api.oauth2.AccessToken;
import simpleSSL5.api.oauth2.AccessTokenGrantRequest;
import simpleSSL5.api.oauth2.OAuth2ProtocolException;
import simpleSSL5.api.security.oatuh2.client.TokenService;


public class OAuthTokenServiceDelegate implements TokenService{
	private static final Logger logger = LoggerFactory.getLogger(OAuthTokenServiceDelegate.class);
	private AccessTokenGrantRequest grant;
	private TokenServiceHttpClient client;
	//private RefreshTokenGrantRequest refreshTokenGrantRequest;
	private String pathToTokenEndpoint;
	public OAuthTokenServiceDelegate(final AccessTokenGrantRequest grant,TokenServiceHttpClient client,  String pathToTokenEndpoint) {
		this.grant = grant;
		this.client = client;
		
		this.pathToTokenEndpoint = pathToTokenEndpoint;
	}
	@Override
	public <T extends AccessToken> T fetch() {
		// TODO Auto-generated method stub
		logger.info("fetch_called");
		T token = null;
		try {
			token = client.post(pathToTokenEndpoint, grant);
		} catch (OAuth2ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return token;
	}

	@Override
	public <T extends AccessToken> T refresh(String refreshTokenString) {
		logger.info("refresh_called");
		T token  =null;
		try {
			token = client.post(pathToTokenEndpoint, grant);
		} catch (OAuth2ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return  token;
	}

}
