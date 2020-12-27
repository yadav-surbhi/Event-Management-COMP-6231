package aspackage.beans;

import java.io.Serializable;
import java.util.Map;

/**
 * @author apoorvasharma
 *
 */
public class LogInformation implements Serializable{
	
	
	private String requestTime;
	private String requestType;
	private Map<String,String> requestParameters;
	private String requestStatus;
	private String serverResonse;
	
	
	/**
	 * @return the requestTime
	 */
	public String getRequestTime() {
		return requestTime;
	}
	
	/**
	 * @param requestTime the requestTime to set
	 */
	public void setRequestTime(String requestTime) {
		this.requestTime = requestTime;
	}
	
	/**
	 * @return the requestType
	 */
	public String getRequestType() {
		return requestType;
	}
	
	/**
	 * @param requestType the requestType to set
	 */
	public void setRequestType(String requestType) {
		this.requestType = requestType;
	}
	
	/**
	 * @return the requestParameters
	 */
	public Map<String,String> getRequestParameters() {
		return requestParameters;
	}
	
	/**
	 * @param requestParameters the requestParameters to set
	 */
	public void setRequestParameters(Map<String,String> requestParameters) {
		this.requestParameters = requestParameters;
	}
	
	/**
	 * @return the requestStatus
	 */
	public String getRequestStatus() {
		return requestStatus;
	}
	
	/**
	 * @param requestStatus the requestStatus to set
	 */
	public void setRequestStatus(String requestStatus) {
		this.requestStatus = requestStatus;
	}
	
	/**
	 * @return the serverResonse
	 */
	public String getServerResonse() {
		return serverResonse;
	}
	
	/**
	 * @param serverResonse the serverResonse to set
	 */
	public void setServerResonse(String serverResonse) {
		this.serverResonse = serverResonse;
	}

}
