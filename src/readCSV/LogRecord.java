package readCSV;

public class LogRecord {
	
	private String userId;
	private String URL;

	public LogRecord(String userId, String uRL) {
		this.userId = userId;
		URL = uRL;
	}
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getURL() {
		return URL;
	}
	public void setURL(String uRL) {
		URL = uRL;
	}
	
}
