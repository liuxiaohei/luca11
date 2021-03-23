package org.ld;

//import com.purgeteam.dynamic.config.starter.annotation.EnableDynamicConfigEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableScheduling
@SpringBootApplication
@RestController
@RefreshScope         // 自动刷新配置
public class ConfigClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigClientApplication.class, args);
    }

    @Value("${foo}")
    private String foo;

    @Value("${demo}")
    private String demo;

    @GetMapping(value = "/foo")
    public String hi(){
        return foo;
    }

    @GetMapping(value = "/demo")
    public String demo(){
        return demo;
    }
}
