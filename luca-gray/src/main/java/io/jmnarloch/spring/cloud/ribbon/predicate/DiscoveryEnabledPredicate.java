package io.jmnarloch.spring.cloud.ribbon.predicate;

import com.netflix.loadbalancer.AbstractServerPredicate;
import com.netflix.loadbalancer.PredicateKey;
import org.springframework.cloud.zookeeper.discovery.ZookeeperServer;

public abstract class DiscoveryEnabledPredicate extends AbstractServerPredicate {

    @Override
    public boolean apply(PredicateKey input) {
        return input != null
                && input.getServer() instanceof ZookeeperServer
                && apply((ZookeeperServer) input.getServer());
    }

    protected abstract boolean apply(ZookeeperServer server);
}
