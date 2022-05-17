package fr.insee.metallica.pocprotoolscommand.processor.payload;

public class ServiceHttpPayload extends HttpPayload {
	private String service;
	private String path;

	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
}
