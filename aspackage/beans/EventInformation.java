package aspackage.beans;

import java.util.Map;

/**
 * @author apoorvasharma
 *
 */
public class EventInformation {
	private String eventId;
	private String eventType;
	private int capasity;
	private String eventDate;
	private String eventTime;
	private String eventlocation;
	/**
	 * @return the eventId
	 */
	public String getEventId() {
		return eventId;
	}
	/**
	 * @param eventId the eventId to set
	 */
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}
	/**
	 * @return the eventType
	 */
	public String getEventType() {
		return eventType;
	}
	/**
	 * @param eventType the eventType to set
	 */
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	/**
	 * @return the capasity
	 */
	public int getCapasity() {
		return capasity;
	}
	/**
	 * @param bookingCapacity the capasity to set
	 */
	public void setCapasity(int bookingCapacity) {
		this.capasity = bookingCapacity;
	}
	/**
	 * @return the eventTime
	 */
	public String getEventTime() {
		return eventTime;
	}
	/**
	 * @param eventTime the eventTime to set
	 */
	public void setEventTime(String eventTime) {
		this.eventTime = eventTime;
	}
	/**
	 * @return the eventlocation
	 */
	public String getEventlocation() {
		return eventlocation;
	}
	/**
	 * @param eventlocation the eventlocation to set
	 */
	public void setEventlocation(String eventlocation) {
		this.eventlocation = eventlocation;
	}
	/**
	 * @return the eventDate
	 */
	public String getEventDate() {
		return eventDate;
	}
	/**
	 * @param eventDate the eventDate to set
	 */
	public void setEventDate(String eventDate) {
		this.eventDate = eventDate;
	}
}
