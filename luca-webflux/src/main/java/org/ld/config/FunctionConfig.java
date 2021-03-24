package org.ld.config;

import lombok.Data;
import org.ld.beans.ConditionRule;
import org.ld.beans.FunctionType;
import org.ld.utils.YamlConfigFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@Configuration
@PropertySource(value = "classpath:functionconfig.yml",factory = YamlConfigFactory.class)
@ConfigurationProperties(prefix = "obj")
@Data
public class FunctionConfig {
    List<FunctionType> functiontypes;

    List<ConditionRule> conditionRules;
}
