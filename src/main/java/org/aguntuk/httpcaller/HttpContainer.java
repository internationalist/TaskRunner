package org.aguntuk.httpcaller;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public enum HttpContainer {
	instance;
	private HttpClient client;
	
	private HttpContainer() {
		client = HttpClientBuilder.create().build();
	}

	public HttpClient getClient() {
		return client;
	}

}
