package simpleSSL5.example.api;

import simpleSSL5.example.comm.wrapper.BodyWrapper;
import simpleSSL5.example.comm.wrapper.HeaderWrapper;
import simpleSSL5.example.comm.wrapper.QueryWrapper;
import simpleSSL5.example.comm.wrapper.ResponseWrapper;

public interface RestAPIInvoker {
	ResponseWrapper sendRequest(String method, String url, HeaderWrapper header, BodyWrapper body, QueryWrapper query);

}
