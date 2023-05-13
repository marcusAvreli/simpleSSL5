package simpleSSL5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import simpleSSL5.example.api.impl.CustomApplication;


/**
 * Hello world!
 *
 */
public class App 
{
	
	//https://github.com/august-zou/emchat-server-java/blob/c956525e6eb3ff5f59a29f2d00c027b6efb7aad0/src/main/java/com/easemob/server/example/Main.java
	private static final Logger logger = LoggerFactory.getLogger(App.class);
    public static void main( String[] args )
    {
     //ClientContext context =  ClientContext.getInstance();
    	FactoryDAO factory = ClientContext.getInstance().init().getAPIFactory();
    	factory.startClient();
    	CustomApplication user = (CustomApplication) factory.newInstance(FactoryDAO.USER_CLASS);
    	user.sendMessage();
    //  context.initClient();
      
    }
}
