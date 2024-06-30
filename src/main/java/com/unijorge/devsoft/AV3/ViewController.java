package com.unijorge.devsoft.AV3;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@EnableCaching
@EnableAsync
public class ViewController {
    @RequestMapping(value="/map")
    public String map() {
        return "forward:map.html";
    }
}
