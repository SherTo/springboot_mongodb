package net.ebh.exam.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by xh on 2017/5/22.
 */
@RestController
public class Test {
    @RequestMapping("/")
    public String justTest() {
        return "hello,test...";
    }

}
