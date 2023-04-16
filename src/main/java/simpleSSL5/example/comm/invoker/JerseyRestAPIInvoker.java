package simpleSSL5.example.comm.invoker;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.NameValuePair;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyWebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simpleSSL5.example.api.RestAPIInvoker;
import simpleSSL5.example.comm.constant.HTTPMethod;
import simpleSSL5.example.comm.utils.RestAPIUtils;
import simpleSSL5.example.comm.wrapper.BodyWrapper;
import simpleSSL5.example.comm.wrapper.HeaderWrapper;
import simpleSSL5.example.comm.wrapper.QueryWrapper;
import simpleSSL5.example.comm.wrapper.ResponseWrapper;

public class JerseyRestAPIInvoker implements RestAPIInvoker {

    private static final Logger logger = LoggerFactory.getLogger(JerseyRestAPIInvoker.class);

	@Override
	public ResponseWrapper sendRequest(String method, String url, HeaderWrapper header, BodyWrapper body,
			QueryWrapper query) {
		// TODO Auto-generated method stub
		  JerseyClient client = RestAPIUtils.getJerseyClient();
		  logger.info("sendRequest_called:"+url);
		  JerseyWebTarget target = client.target("https://192.168.243.133:8443/iiq/oauth2/myOAuthAPI/test2");
		  Invocation.Builder inBuilder = target.request();
		  Response response = null;
		  buildHeader(inBuilder, header);
		  if (HTTPMethod.METHOD_GET.equals(method)) {
	            response = inBuilder.get(Response.class);
	            logger.info("response:"+response.getStatus());
	            String responseContent = response.readEntity(String.class);
	            logger.info("response:"+responseContent);
		  }
		return null;
	}
    private void buildHeader(Invocation.Builder inBuilder, HeaderWrapper header) {
        if (null != header && !header.getHeaders().isEmpty()) {
            for (NameValuePair nameValuePair : header.getHeaders()) {
                inBuilder.header(nameValuePair.getName(), nameValuePair.getValue());
            }
        }
    }
}
