package org.ld.gray;

import com.netflix.loadbalancer.*;
import lombok.Getter;
import org.springframework.cloud.zookeeper.discovery.ZookeeperServer;

public class MetadataAwareRule extends PredicateBasedRule {

    @Getter
    private final CompositePredicate predicate;

    public MetadataAwareRule() {
        this.predicate = CompositePredicate.withPredicates(
                new AbstractServerPredicate() {
                    @Override
                    public boolean apply(PredicateKey input) {
                        // 用当前请求的metadata 和 所有服务的metadata 进行比较 匹配则返回true 否则返回false
                        return input != null
                                && input.getServer() instanceof ZookeeperServer
                                && ((ZookeeperServer) input.getServer()).getInstance()
                                .getPayload()
                                .getMetadata().entrySet()
                                .containsAll(
                                        RibbonFilterContext.getAttributes().entrySet()
                                );
                    }
                },
                new AvailabilityPredicate(this, null)).build();
    }
}
