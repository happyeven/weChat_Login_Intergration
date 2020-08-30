package com.zhu.controller;

import java.util.Date;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FirstController {
	
	@RequestMapping("/show")
	public String showView(Model model) {
		model.addAttribute("msg", "first work");
		model.addAttribute("key", new Date());
		return "index";
	}
}
