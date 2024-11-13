package com.example.testubi2.ubidots;

import com.example.testubi2.ubidots.ApiClient;
import com.example.testubi2.ubidots.ServerBridge;

import java.util.HashMap;
import java.util.Map;

class ApiObject {

	private Map<String, Object> raw;
	protected com.example.testubi2.ubidots.ApiClient api;
	protected ServerBridge bridge;
	
	ApiObject(Map<String, Object> raw, ApiClient api) {
		this.raw = new HashMap<String, Object>(raw);
		this.api = api;
		bridge = api.getServerBridge();
	}
	
	String getAttributeString(String name) {
		return (String) raw.get(name);
	}
	
	Double getAttributeDouble(String name) {
		return (Double) raw.get(name);
	}
	
	protected Object getAttribute(String name) {
		return raw.get(name);
	}
	
	public String getId() {
		return (String) raw.get("id");
	}

	public HashMap<String, Object> getRaw() { return (HashMap<String, Object>) raw; }
}
