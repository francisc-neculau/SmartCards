package app.payword.account;

import org.apache.log4j.Logger;

public class Account 
{
	private String accountNumber	= null;
	private double amount			= 0.0;
	private static Logger logger 	= Logger.getLogger(Account.class);
	
	
	public Account(String accountNumber, double amount)
	{
		this.accountNumber 	= accountNumber;
		this.amount			= amount;
	}

	
	
	public static void transfer(Account from, Account to, double amount)
	{
		if(!from.isValid())
		{
			logger.error("The account from which the transaction is made is not valid!", new Exception("The FROM account is not valid, please check its account number!"));
			return;
		}
		
		if(!to.isValid())
		{
			logger.error("The account to which the transaction is made is not valid!", new Exception("The TO account is not valid, please check its account number!"));
			return;
		}
		
		try 
		{
			long timeStart 	= System.currentTimeMillis();
			logger.info("Beggining the transaction...");
			from.subtractAmount(amount);
			to.addAmount(amount);
			long timeEnd	= System.currentTimeMillis();
			
			logger.info("Transaction ended! Time elapsed: " + (timeEnd - timeStart) + " ms");
		} 
		catch (Exception e) 
		{
			logger.error("The transaction could not be made!",e);
		}
		
		
	}
	
	private boolean isValid()
	{
		if( accountNumber == null )
			return false;
		
		return true;
	}
	
	private void subtractAmount(double amount) throws Exception
	{
		if( amount > this.amount )
		{
			logger.error("Cannot subtract this value: " + amount + "; The amount is too big");
			throw new Exception("Invalid subtraction");
		}
	}
	
	private void addAmount(double amount)
	{
		this.amount += amount;
	}
}
