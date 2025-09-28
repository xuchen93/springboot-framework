package com.github.xuchen93.springboot.framework.web.controller0;

import com.github.xuchen93.springboot.framework.base.convention.result.R;
import com.github.xuchen93.springboot.framework.base.support.manager.MethodStatsManager;
import com.github.xuchen93.springboot.framework.base.support.model.MethodStats;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/method/stats")
public class MethodStatsController {

	@GetMapping("peekAll")
	public R<List<MethodStats>> peekAll() {
		Collection<MethodStats> stats = MethodStatsManager.peekAll();
		return R.success(stats.stream().sorted().toList());
	}

	@GetMapping("peekByType")
	public R<List<MethodStats>> peekByType(@RequestParam Integer type) {
		Collection<MethodStats> stats = MethodStatsManager.peekByType(type);
		return R.success(stats.stream().sorted().toList());
	}
}
