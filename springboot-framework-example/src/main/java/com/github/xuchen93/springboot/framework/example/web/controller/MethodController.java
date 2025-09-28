package com.github.xuchen93.springboot.framework.example.web.controller;

import cn.hutool.core.thread.ThreadUtil;
import com.github.xuchen93.springboot.framework.base.common.CommonUtil;
import com.github.xuchen93.springboot.framework.base.convention.result.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MethodController {

	@RequestMapping("all")
	public R<String> all() {
		ThreadUtil.sleep(100);
		return R.success(CommonUtil.getCurrentMethodName());
	}

	@GetMapping("get")
	public R<String> get() {
		ThreadUtil.sleep(300);
		return R.success(CommonUtil.getCurrentMethodName());
	}


	@GetMapping("get/{id}")
	public R<String> getId(String id) {
		ThreadUtil.sleep(400);
		return R.success(CommonUtil.getCurrentMethodName());
	}

	@PostMapping("post")
	public R<String> post() {
		ThreadUtil.sleep(500);
		return R.success(CommonUtil.getCurrentMethodName());
	}


	@PostMapping("postBody")
	public R<String> postBody(@RequestBody Map<String, Object> map) {
		ThreadUtil.sleep(800);
		return R.success(CommonUtil.getCurrentMethodName());
	}
}
