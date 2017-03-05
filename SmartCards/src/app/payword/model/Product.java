package app.payword.model;

public class Product
{

	private double price;
	private String name;
	
	public Product(String name, double price)
	{
		this.name = name;
		this.price = price;
	}

	public double getPrice()
	{
		return price;
	}
	
	@Override
	public java.lang.String toString()
	{
		return name;
	}
}
