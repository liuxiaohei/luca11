package org.ld.grpc.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;


public class AnnotationGrpcServiceDiscoverer implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 获取所有带有 @GrpcService注解的 beanName
     */
    public Collection<String> findGrpcServiceBeanNames() {
        var beanNames = this.applicationContext.getBeanNamesForAnnotation(GrpcService.class);
        return Arrays.asList(beanNames);
    }

    /**
     * 获取GrpcServiceDefinition对象集合
     */
    public Collection<GrpcServiceDefinition> findGrpcServices() {
        var beanNames = findGrpcServiceBeanNames();
        List<GrpcServiceDefinition> definitions = Lists.newArrayListWithCapacity(beanNames.size());
        var globalServerInterceptorRegistry = applicationContext.getBean(GlobalServerInterceptorRegistry.class);
        var globalInterceptorList = globalServerInterceptorRegistry.getServerInterceptors();
        for (String beanName : beanNames) {
            var bindableService = this.applicationContext.getBean(beanName, BindableService.class);
            var serviceDefinition = bindableService.bindService();
            var grpcServiceAnnotation = applicationContext.findAnnotationOnBean(beanName, GrpcService.class);
            assert grpcServiceAnnotation != null;
            serviceDefinition = bindInterceptors(serviceDefinition, grpcServiceAnnotation, globalInterceptorList);
            definitions.add(new GrpcServiceDefinition(beanName, bindableService.getClass(), serviceDefinition));
        }
        return definitions;
    }

    /**
     * 创建 ServiceDefinition对象、并绑定代理实例
     */
    private ServerServiceDefinition bindInterceptors(ServerServiceDefinition serviceDefinition, GrpcService grpcServiceAnnotation, List<ServerInterceptor> globalInterceptorList) {
        Set<ServerInterceptor> interceptorSet = Sets.newHashSet();
        interceptorSet.addAll(globalInterceptorList);
        for (Class<? extends ServerInterceptor> serverInterceptorClass : grpcServiceAnnotation.interceptors()) {
            ServerInterceptor serverInterceptor;
            if (applicationContext.getBeanNamesForType(serverInterceptorClass).length > 0) {
                serverInterceptor = applicationContext.getBean(serverInterceptorClass);
            } else {
                try {
                    serverInterceptor = serverInterceptorClass.newInstance();
                } catch (Exception e) {
                    throw new BeanCreationException("Failed to create interceptor instance", e);
                }
            }
            interceptorSet.add(serverInterceptor);
        }
        return ServerInterceptors.intercept(serviceDefinition, Lists.newArrayList(interceptorSet));
    }
}
