package simpleSSL5.api.oauth2;


@FunctionalInterface
public interface TokenChangeObserver<T extends AccessToken> {

	/**
	 * Fired by {@link TokenChangeObservable} when a token value has changed,
	 * either because it was fetched for the first time, or because it was
	 * refreshed.
	 * 
	 * @param newToken
	 * @param oldToken
	 */
	public void tokenChanged(TemporalAccessToken<T> newToken, TemporalAccessToken<T> oldToken);

}