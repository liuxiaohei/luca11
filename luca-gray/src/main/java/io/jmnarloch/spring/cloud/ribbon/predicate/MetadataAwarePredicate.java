package io.jmnarloch.spring.cloud.ribbon.predicate;

import io.jmnarloch.spring.cloud.ribbon.api.RibbonFilterContext;
import io.jmnarloch.spring.cloud.ribbon.support.RibbonFilterContextHolder;
import org.springframework.cloud.zookeeper.discovery.ZookeeperServer;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * todo 待验证
 */
public class MetadataAwarePredicate extends DiscoveryEnabledPredicate {

    @Override
    protected boolean apply(ZookeeperServer server) {
        final RibbonFilterContext context = RibbonFilterContextHolder.getCurrentContext();
        final Set<Map.Entry<String, String>> attributes = Collections.unmodifiableSet(context.getAttributes().entrySet());
        final Map<String, String> metadata = server.getInstance().getPayload().getMetadata();
        return metadata.entrySet().containsAll(attributes);
    }
}
