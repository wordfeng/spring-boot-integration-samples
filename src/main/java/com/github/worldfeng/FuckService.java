package com.github.worldfeng;

import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service("fuckService")
public class FuckService {
    @SneakyThrows
    public String fuck(String name) {
        System.out.println("fuck：" + Thread.currentThread().getName());
//        Thread.sleep(1000);
//        if ("error".equals(name)) {
//            throw new RuntimeException("fuck!");
//        }
        return "fuck: " + name;
    }
}
