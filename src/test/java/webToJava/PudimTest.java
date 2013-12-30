package webToJava;

import static webToJava.PageLoader.load;

import org.junit.Test;

public class PudimTest {

	@Test
	public void pudim(){
		final Pudim pudim = load(Pudim.class);
		System.out.println(pudim.mailTo.href);
	}
	
}
