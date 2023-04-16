package simpleSSL5;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simpleSSL5.api.oauth2.AccessToken;
import simpleSSL5.api.oauth2.OAuth2ProtocolException;
import simpleSSL5.api.oauth2.TemporalAccessToken;
import simpleSSL5.api.oauth2.TokenChangeObserver;
import simpleSSL5.api.security.oatuh2.client.RetryPolicy;
import simpleSSL5.api.security.oatuh2.client.TokenChangeObservable;
import simpleSSL5.api.security.oatuh2.client.TokenService;
import simpleSSL5.commons.util.Observable;




public class AutoRenewingTokenProvider<T extends AccessToken>  implements TokenChangeObservable<T>{
	private static final Logger logger = LoggerFactory.getLogger(AutoRenewingTokenProvider.class);
	TokenRenewTask tokenRenewTask;
	ScheduledExecutorService schedulerExecutor;
	ScheduledFuture<?> future;
	private final TokenService tokenService;
	private boolean strictlyRefresh = false;
	private RetryPolicy retryPolicy;
	private TemporalUnit tokenExpireInTimeUnits = ChronoUnit.SECONDS;// used by
	private double delayModifier = 0.9;
	private Observable<TokenChangeObserver<T>> observable;
	private Collection listeners = (Collection) Collections.synchronizedCollection(new ArrayList<>());
	public AutoRenewingTokenProvider(final TokenService tokenService,RetryPolicy retryPolicy,Observable<TokenChangeObserver<T>> observable) {
		schedulerExecutor = Executors.newScheduledThreadPool(1);
		this.tokenService = tokenService;
		this.observable = observable;
		this.retryPolicy = retryPolicy;
	}
	class TokenRenewTask implements Runnable {
		private final AutoRenewingTokenProvider<T> svc;
		TemporalAccessToken<T> token;
		TokenRenewTask(AutoRenewingTokenProvider<T> svc, TemporalAccessToken<T> token) {
			this.svc = svc;
			this.token = token;
			if (token.token().getRefreshToken() == null && this.svc.strictlyRefresh()) {
				throw new IllegalArgumentException(
						"Cannot start refresh token timer without a valid token with refresh_token value when set to strictly refresh");
			}
		}
		@Override
		public void run() {
			logger.info("run_called");
			TemporalAccessToken<T> newToken = null;
			int retries = 0;
			RetryPolicy retryPolicy = this.svc.getRetryPolicy();
			
			while (newToken == null && retries > -1 && retries < retryPolicy.maxRetries()) {
				retries++;
				try {
					newToken = this.svc.renew(this.token);
					// Update Access Token provisioned by this provider only if
					// this is the last attempt. Intermediate nulls will not be
					// considered
					if (newToken != null || (newToken == null && retries == retryPolicy.maxRetries())) {
						TemporalAccessToken<T> previousToken = this.token;
						this.token = newToken;
						this.svc.fireTokenUpdate(this.token, previousToken);
					}
				} catch (IOException e) {
					logger.error("Token refresh task failed", e);
					if (retryPolicy.onException(e)) {
						try {
							TimeUnit.MILLISECONDS.sleep(retryPolicy.periodBetweenRetries());
						} catch (InterruptedException ie) {
						}
					} else {
						break;
					}
				}
			}
			
		}
		TemporalAccessToken<T> getToken() {
			//logger.info("getToken:"+this.token);
			return this.token;
		}

		
	}
	
	public Duration estimatedRepetitionsDelay() {
		AccessToken token = this.get();
		if (token == null)
			throw new IllegalStateException("No token to estimate for");
		if (token.getExpiresIn() < 1)
			throw new IllegalArgumentException("The token has no valid expires_in property: " + token.getExpiresIn());
		TemporalUnit ttlUnit = this.tokenRenewTask.getToken().ttlUnit();
		long delay = Math.round(token.getExpiresIn() * this.delayModifier);
		Duration delayDuraiton = Duration.of(delay, ttlUnit);
		return delayDuraiton;
	} 
	
	static ChronoUnit chronoUnit(TimeUnit unit) {
		if (unit == null)
			throw new IllegalArgumentException();
		switch (unit) {
		case NANOSECONDS:
			return ChronoUnit.NANOS;
		case MICROSECONDS:
			return ChronoUnit.MICROS;
		case MILLISECONDS:
			return ChronoUnit.MILLIS;
		case SECONDS:
			return ChronoUnit.SECONDS;
		case MINUTES:
			return ChronoUnit.MINUTES;
		case HOURS:
			return ChronoUnit.HOURS;
		case DAYS:
			return ChronoUnit.DAYS;
		default:
			throw new IllegalArgumentException("Unknown TimeUnit constant");
		}
	}
	public AutoRenewingTokenProvider<T> setRetryPolicy(RetryPolicy retryPolicy) {
		if (retryPolicy == null)
			throw new IllegalArgumentException("retryPolicy is null");
		this.retryPolicy = retryPolicy;
		return this;
	}
	public ScheduledFuture<?> start() throws IOException {
		if (this.isActive())
			throw new IllegalStateException("Already started");
		T newToken = this.getTokenService().fetch();
		if (newToken == null) {
			throw new IllegalStateException("The token fetched from this TokenService is null");
		}else {
			logger.info("new_token_is_not_null");
		}
		TemporalAccessToken<T> accessToken = new TemporalAccessToken<>(newToken, Instant.now(), this.tokenExpireInTemporalUnit());
		if(null != accessToken) {
			logger.info("TemporalAccessToken_is_not_null");
		}else {
			logger.info("TemporalAccessToken_is_null");
		}
		this.fireTokenUpdate(accessToken, null);
		this.tokenRenewTask = new TokenRenewTask(this, accessToken);
		long delayMillis = this.estimatedRepetitionsDelay().toMillis();
		//long delayMillis = 1000L;
		//logger.info("delayMillis: "+delayMillis);
		this.future = this.schedulerExecutor.scheduleWithFixedDelay(this.tokenRenewTask, 0L, delayMillis,
				TimeUnit.MILLISECONDS);
		return this.future;
	}
	public boolean isActive() {
		return this.future != null && !this.future.isDone();
	}
	
	public void stop(boolean graceful) {
		// silently ignore if executor not started
		if (this.future != null) {
			if (graceful)
				this.schedulerExecutor.shutdown();
			else
				//// Kill the currently running task
				//this.schedulerExecutor.shutdownNow();
				this.future.cancel(true);
			this.future = null;
		}
	}
	public ScheduledFuture<?> resume(T token, Instant fetchMoment, boolean refetchIfExpired) {
		if (this.isActive())
			throw new IllegalStateException("Already started");
		if (token == null)
			throw new IllegalArgumentException("Cannot resume with token null");
		// fetching from a remote service will inevitably pose some delay so we
		// defensively choose to count the fetch time from the very start.
		TemporalAccessToken<T> _token = new TemporalAccessToken<>(token, fetchMoment);
		if (_token.isExpired() && !refetchIfExpired)
			throw new IllegalStateException("Cannot resume an expired token");
		this.fireTokenUpdate(_token, null);

		this.tokenRenewTask = new TokenRenewTask(this, _token);
		long delayMillis = this.estimatedRepetitionsDelay().toMillis();
		this.future = this.schedulerExecutor.scheduleWithFixedDelay(this.tokenRenewTask,
				_token.ttlLeft(ChronoUnit.MILLIS), delayMillis, TimeUnit.MILLISECONDS);
		return this.future;
	}

	public void suspend(final boolean graceful) {
		this.stop(graceful);
	}
	public AutoRenewingTokenProvider tokenExpireInTemporalUnit(TemporalUnit temporalUnit) {
		this.tokenExpireInTimeUnits = temporalUnit;
		return this;
	}

	
	public T get() {
		if (this.tokenRenewTask == null || this.tokenRenewTask.getToken() == null)
			return null;
		return this.tokenRenewTask.getToken().token();
	}
	
	
	
	

	
	public AutoRenewingTokenProvider<T> strictlyRefresh(boolean strictlyRefresh) {
		this.strictlyRefresh = strictlyRefresh;
		return this;
	}

	boolean strictlyRefresh() {
		return this.strictlyRefresh;
	}
	RetryPolicy getRetryPolicy() {
		return this.retryPolicy;
	}
	double delayModifier() {
		return this.delayModifier;
	}
	
	
	protected TemporalAccessToken<T> renew(TemporalAccessToken<T> token) throws OAuth2ProtocolException, IOException {
		T newToken = null;
		String refreshToken = token.token().getRefreshToken();
		// automatically fallback to fetch new token if refreshToken is null,
		// unless instructed otherwise
		if (refreshToken == null && !strictlyRefresh())
			newToken = this.getTokenService().fetch();
		if (newToken == null)
			newToken = this.getTokenService().refresh(refreshToken);
		if (newToken == null)
			return null;
		TemporalAccessToken<T> temporalToken = new TemporalAccessToken<>(newToken, Instant.now(), this.tokenExpireInTemporalUnit());
		return temporalToken;
	}
	
	TemporalUnit tokenExpireInTemporalUnit() {
		return this.tokenExpireInTimeUnits;
	}
	TokenService getTokenService() {
		return this.tokenService;
	}

	public AutoRenewingTokenProvider<T> schedule(double delayModifier) {
		if (!(delayModifier > 0) || delayModifier > 1)
			throw new IllegalArgumentException("delayModifier must be value between (0-1]");
		this.delayModifier = delayModifier;
		return this;
	}

	
	void fireTokenUpdate(final TemporalAccessToken<T> token, final TemporalAccessToken<T> previous) {
		// Notify the list of registered listeners
		logger.info("fire_Token_Update");
		if (this.observable != null) {
			logger.info("observable_is_not_null");
			try {
				this.observable.notify((listener) -> listener.tokenChanged(token, previous));
			} catch (Throwable t) {
				logger.error("Change listener error", t);
			}
		}else {
			logger.info("observable_is_null");
		}
	}

	

	@Override
	public TokenChangeObservable attach(TokenChangeObserver tokenChangeObserver) {
		logger.info("token_change_observable_attached");
		this.observable.attach(tokenChangeObserver);
		return this;
	}


}
