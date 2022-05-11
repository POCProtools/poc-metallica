package fr.insee.metallica.pocprotools.command.processor.payload;

public class ServiceHttpPayload extends HttpPayload {
	private String service;
	private String uri;

	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
}
