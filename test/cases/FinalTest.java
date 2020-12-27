package cases;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import frontend.FrontEnd;

public class FinalTest {
	FrontEnd fe = null;
	@Before
	public void setUp() throws Exception {
		fe = new FrontEnd();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		String result = fe.addEvent("MTLA111119", "Seminar", 2);
		assertTrue(result.contains("success"));
		String result1 = fe.bookEvent("MTLC1111", "MTLA111119", "Seminar");
	}

}
