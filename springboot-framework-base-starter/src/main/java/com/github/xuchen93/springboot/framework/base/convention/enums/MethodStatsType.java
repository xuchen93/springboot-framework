package com.github.xuchen93.springboot.framework.base.convention.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
public enum MethodStatsType {
	LOG_ANNOTATION(1),
	REQUEST(2),
	;

	@Getter
	private final int type;

	public static List<Integer> getAllTypes() {
		return List.of(LOG_ANNOTATION.getType(), REQUEST.getType());
	}


	public static String getNameByType(int type) {
		for (MethodStatsType e : MethodStatsType.values()) {
			if (e.getType() == type) {
				return e.name();
			}
		}
		return null;
	}
}
