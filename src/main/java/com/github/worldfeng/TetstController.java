package com.github.worldfeng;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessageChannel;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping
@RestController
public class TetstController {

    @Autowired
    FuckService fuckService;
    @Autowired
    MessageChannel inputChannel;

    @RequestMapping("/test")
    public String test(@RequestParam String name) {
        System.out.println("controller："+Thread.currentThread().getName());
        inputChannel.send(MessageBuilder.withPayload(name).build());
        return fuckService.fuck(name);
    }
}
