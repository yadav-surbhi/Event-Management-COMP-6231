/**
 * 
 */
package extension;

import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * Global clock interface.
 *
 * @author vanduong
 */
public interface Clock {
	
	/**
	 * Gets the local time of the given component name.
	 *
	 * @param name the name
	 * @return the local time
	 */
	public int getLocalTime(String name);
	
	/**
	 * Increment local time of the given component name
	 *
	 * @param name the name
	 */
	public void incrementLocalTime(String name);
	
	/**
	 * Update local clock of the given the given component name. Local clock = max{localClock, messageClock}
	 *
	 * @param name the name
	 * @param messageClock the message clock
	 * @return the updated clock
	 */
	public Map<String, Integer> updateLocalClock(String name, Map<String, Integer> messageClock);

}
