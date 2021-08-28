package com.weiheng.secondkill.controller;

import com.weiheng.secondkill.enums.StatusCode;
import com.weiheng.secondkill.response.BaseResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/demo")
public class DemoController {

    @RequestMapping("/hello")
    public String hello(Model model) {
        model.addAttribute("h1", "SecondKill Hello!");
        return "hello";
    }

    @RequestMapping(value = "/resp", method = RequestMethod.GET)
    @ResponseBody
    public BaseResponse<String> response(String name) {
        BaseResponse<String> response = new BaseResponse<String>(StatusCode.SUCCESS);
        response.setData("Hello World");
        return response;
    }
}
