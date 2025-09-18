package com.github.xuchen93.springboot.framework.example.web.controller;

import com.github.xuchen93.springboot.framework.base.convention.exception.BusinessException;
import com.github.xuchen93.springboot.framework.base.convention.result.R;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("exception")
public class ExceptionController {


	@GetMapping("businessException")
	@SneakyThrows
	public R<Void> businessException() {
		throw new BusinessException("business exception");
	}


	@GetMapping("businessExceptionWait")
	@SneakyThrows
	public R<Void> businessExceptionWait() {
		Thread.sleep(1000);
		throw new BusinessException("business exception wait");
	}


	@GetMapping("unknownException")
	@SneakyThrows
	public R<Void> unknownException() {
		throw new RuntimeException("unknown exception");
	}
}
