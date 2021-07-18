package id.co.hanoman.tapcash.model;



public	class ErrorResponse {

		Object error;
		String code;
		String faultcode;
		String faultmessage;
		
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		public Object getError() {
			return error;
		}
		public void setError(Object error) {
			this.error = error;
		}
		
		
		public String getFaultCode() {
			return faultcode;
		}
		public void setFaultCode(String faultcode) {
			this.faultcode = faultcode;
		}
		public String getFaultMessage() {
			return faultmessage;
		}
		public void setFaultMessage(String faultmessage) {
			this.faultmessage = faultmessage;
		}
		
	}

