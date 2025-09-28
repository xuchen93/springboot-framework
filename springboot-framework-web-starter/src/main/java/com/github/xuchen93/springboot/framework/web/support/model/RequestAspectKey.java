package com.github.xuchen93.springboot.framework.web.support.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestAspectKey {
	private long startTime;
	private int salt;
	private String clientIp;
	private String uniqueKey;

	public String generateBaseLog() {
		return "[" + startTime + "-" + salt + "-" + clientIp + "]";
	}
}
