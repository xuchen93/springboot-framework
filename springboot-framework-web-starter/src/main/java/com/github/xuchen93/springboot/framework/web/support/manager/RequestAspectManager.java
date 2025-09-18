package com.github.xuchen93.springboot.framework.web.support.manager;

import com.github.xuchen93.springboot.framework.web.support.model.RequestAspectKey;

public class RequestAspectManager {
	public static final ThreadLocal<RequestAspectKey> REQUEST_ASPECT_KEY = new ThreadLocal<>();

	public static RequestAspectKey consumeRequestAspectKey() {
		RequestAspectKey aspectKey = REQUEST_ASPECT_KEY.get();
		clear();
		return aspectKey;
	}

	public static void setRequestAspectKey(RequestAspectKey requestAspectKey) {
		RequestAspectManager.REQUEST_ASPECT_KEY.set(requestAspectKey);
	}

	public static RequestAspectKey setRequestAspectKey(long startTime, int salt, String clientIp, String uniqueKey) {
		RequestAspectKey requestAspectKey = new RequestAspectKey(startTime, salt, clientIp, uniqueKey);
		REQUEST_ASPECT_KEY.set(requestAspectKey);
		return requestAspectKey;
	}

	public static void clear() {
		REQUEST_ASPECT_KEY.remove();
	}
}
