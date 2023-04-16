package simpleSSL5.api.security.oatuh2.client;

public interface RetryPolicy {
	long periodBetweenRetries();

	long maxRetries();

	boolean onException(Throwable t);
}