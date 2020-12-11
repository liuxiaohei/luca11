package org.ld.controller;

import org.ld.pojo.ConfigProperties;
import org.ld.service.ConfigPropertiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("configPropertiesController")
public class ConfigPropertiesController {
    @Autowired
    private ConfigPropertiesService configPropertiesService;

    @GetMapping("/configProperties")
    public List<ConfigProperties> getConfigProperties() {
        return configPropertiesService.getConfigProperties();
    }
}