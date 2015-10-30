

public class Time {

	public static void sleep(final int millis) {
		try {
			Thread.sleep(millis);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
