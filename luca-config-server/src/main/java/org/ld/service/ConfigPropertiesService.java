package org.ld.service;

import org.ld.mapper.ConfigPropertiesMapper;
import org.ld.pojo.ConfigProperties;
import org.ld.pojo.example.ConfigPropertiesExample;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("configPropertiesService")
public class ConfigPropertiesService {
    @Resource
    private ConfigPropertiesMapper configPropertiesMapper;

    public List<ConfigProperties> getConfigProperties() {
        ConfigPropertiesExample example = new ConfigPropertiesExample();
        example.createCriteria();
        return configPropertiesMapper.selectByExample(example);
    }
}