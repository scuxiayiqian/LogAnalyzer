package readCSV;

class ResourceInfo {
	private String url;
	private int count;
	
	public ResourceInfo(String url, int count) {
		this.url = url;
		this.count = count;
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	
	public void countIncrement() {
		this.count++;
	}
}
