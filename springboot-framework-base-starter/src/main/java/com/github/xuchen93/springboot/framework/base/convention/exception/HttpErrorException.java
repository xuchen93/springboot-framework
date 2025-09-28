package com.github.xuchen93.springboot.framework.base.convention.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class HttpErrorException extends RuntimeException {
	private int statusCode;
	private String body;
}
