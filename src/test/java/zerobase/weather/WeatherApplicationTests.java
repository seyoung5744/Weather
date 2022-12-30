package zerobase.weather;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WeatherApplicationTests {

	@Test
	void contextLoads() {

	}

	@Test
	void equalTest() {
	    assertEquals(1, 1);
	}

	@Test
	void nullTest() {
	    //given
		assertNull(null);
	    //when
	    //then
	}

	@Test
	void trueTest() {
	    //given
		assertTrue(1==1);
	    //when
	    //then
	}
}
