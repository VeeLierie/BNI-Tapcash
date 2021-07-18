package id.co.hanoman.tapcash.model;

import javax.validation.constraints.NotNull;


public class Response {
	
    Object request;

	@NotNull
	Object response ;

	public Object getRequest() {
		return request;
	}

	public void setRequest(Object request) {
		this.request = request;
	}

	public Object getResponse() {
		return response;
	}

	public void setResponse(Object response) {
		this.response = response;
	}

}
