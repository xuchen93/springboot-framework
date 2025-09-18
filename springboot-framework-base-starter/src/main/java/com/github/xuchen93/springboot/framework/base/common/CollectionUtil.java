package com.github.xuchen93.springboot.framework.base.common;

import java.util.List;

public class CollectionUtil {
	/**
	 * 获取集合最小值的索引
	 */
	public static int minIndex(List<? extends Number> list) {
		int minIndex = 0;
		for (int i = 1; i < list.size(); i++) {
			if (list.get(i).longValue() < list.get(minIndex).longValue()) {
				minIndex = i;
			}
		}
		return minIndex;
	}
}
