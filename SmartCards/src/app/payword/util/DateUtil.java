package app.payword.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class DateUtil
{
	private DateUtil(){ initialize(); }
	
	private static class InstanceHolder
	{
		private static final DateUtil instance = new DateUtil();
	}
	
	public static DateUtil getInstance()
	{
		return InstanceHolder.instance;
	}
	
	private void initialize()
	{
		
	}
	
	public String generateDate()
	{
		// FIXME : This should be somewhere in time 
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		return sdf.format(ts);
	}
}
