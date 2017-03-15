package app.payword.util;

public class PaywordConfiguration
{
//	private PaywordConfiguration(){ initialize(); }
//	
//	private static class InstanceHolder
//	{
//		private static final PaywordConfiguration instance = new PaywordConfiguration();
//	}
//	
//	public static PaywordConfiguration getInstance()
//	{
//		return InstanceHolder.instance;
//	}
//	
//	private void initialize()
//	{
//		
//	}
	
	/*
	 * User
	 */
	public static final String  USER_IP_ADDRESS      = "127.0.0.1";
	public static final int     USER_PORT_NUMBER     = 6001;
	public static final Integer USER_IDENTITY_NUMBER = 100;
	public static final String  USER_LOGGER_NAME     = "User";
	
	/*
	 * Broker
	 */
	public static final String  BROKER_IP_ADDRESS      = "127.0.0.2";
	public static final int     BROKER_PORT_NUMBER     = 6002;
	public static final Integer BROKER_IDENTITY_NUMBER = 200;
	public static final String  BROKER_LOGGER_NAME     = "Broker";

	/*
	 * Vendor
	 */
	public static final String  VENDOR_IP_ADDRESS      = "127.0.0.3";
	public static final int     VENDOR_PORT_NUMBER     = 6003;
	public static final Integer VENDOR_IDENTITY_NUMBER = 300;
	public static final String  VENDOR_LOGGER_NAME     = "Vendor";

}
