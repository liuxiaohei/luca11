package org.ld.gray.support;

import org.ld.gray.rule.MetadataAwareRule;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.cloud.netflix.ribbon.RibbonClientConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * 自定义灰度策略 参照↓
 * https://perkins4j2.github.io/posts/25628/
 */
@Configuration
@AutoConfigureBefore(RibbonClientConfiguration.class)
public class RibbonDiscoveryRuleAutoConfiguration {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public MetadataAwareRule metadataAwareRule() {
        return new MetadataAwareRule();
    }

}