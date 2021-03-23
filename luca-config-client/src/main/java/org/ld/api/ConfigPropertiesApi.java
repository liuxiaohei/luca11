package org.ld.api;

import org.ld.beans.ConfigProperties;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "config-server")
public interface ConfigPropertiesApi {

    @PostMapping("allConfig")
    List<ConfigProperties> getAllConfig(@RequestBody ConfigProperties configProperties);

    @PostMapping("initConfig")
    void initConfig(@RequestBody List<ConfigProperties> configProperties);
}
