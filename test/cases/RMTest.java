package cases;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import frontend.FrontEnd;

public class RMTest {
	FrontEnd fe;

	@Before
	public void setUp() throws Exception {
		fe = new FrontEnd();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		assertTrue(fe.addEvent("MTLA1111", "Seminar", 1).contains("success"));
		assertTrue(fe.addEvent("MTLA2222", "Seminar", 1).contains("incorrect"));
	}

}
