package app.payword.model;

import java.util.ArrayList;
import java.util.List;

public class Receipt
{
	List<Product> products;
	Double cost;

	public Receipt()
	{
		this.products = new ArrayList<>();
		this.cost = 0.0;
	}
	
	public boolean coversCost(Double amount)
	{
		boolean result = (amount.equals(cost));
		return result;
	}
	
	public boolean exceedsCost(Double amount)
	{
		boolean result = (amount > cost);
		return result;
	}
	
	public boolean isEmpty()
	{
		return products.isEmpty();
	}
	
	public void clear()
	{
		this.products.clear();
		this.cost = 0.0;
	}
	
	public void addProduct(Product product)
	{
		products.add(product);
		cost += product.getPrice();
	}
	
	public Double getTotalAmount()
	{
		return cost;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for (Product product : products)
			sb.append(product);
		return sb.toString();
	}
}
