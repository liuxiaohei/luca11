package org.ld.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 映射html到指定路径
 */
@RestController
public class HtmlMappingController {

    @Value("classpath:/static/index.html")
    private Resource index;

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public Resource getIndex() {
        return index;
    }

    @Value("classpath:/static/login.html")
    private Resource login;

    @GetMapping(value = "/login", produces = MediaType.TEXT_HTML_VALUE)
    public Resource getLogin() {
        return login;
    }

}
