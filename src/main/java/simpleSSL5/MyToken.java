package simpleSSL5;

import java.util.Collection;


import simpleSSL5.api.oauth2.AccessToken;


public class MyToken extends AccessToken {
	

	public MyToken() {
		
	}
	

	public MyToken(final String accessToken,final long expiresIn, final String refreshToken, 				 final Collection<String> scope,				 String geolocation) {
		super(accessToken, "Bearer", expiresIn, refreshToken, scope);
	
	}
	
}