package org.lognet.springboot.grpc.autoconfigure;

import io.grpc.ServerBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.services.HealthStatusManager;
import org.lognet.springboot.grpc.GRpcServerBuilderConfigurer;
import org.lognet.springboot.grpc.GRpcServerRunner;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Created by alexf on 25-Jan-16.
 */

@AutoConfigureOrder
@ConditionalOnBean(annotation = GRpcService.class)
@EnableConfigurationProperties(GRpcServerProperties.class)
public class GRpcAutoConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private GRpcServerProperties grpcServerProperties;

    /**
     * Support binding the netty server on a specific bind address.
     */
    @Bean(name = "grpcServerRunner")
    @ConditionalOnExpression("#{environment.getProperty('grpc.nettyBindAddress','')!=''}")
    public GRpcServerRunner grpcServerRunnerWithNettyBindAddress(GRpcServerBuilderConfigurer configurer) {
        SocketAddress addr = new InetSocketAddress(grpcServerProperties.getNettyBindAddress(), grpcServerProperties.getPort());
        ServerBuilder builder = NettyServerBuilder.forAddress(addr);
        return new GRpcServerRunner(configurer, builder);
    }

    @Bean
    @ConditionalOnMissingBean(GRpcServerRunner.class)
    @ConditionalOnProperty(value = "grpc.enabled", havingValue = "true", matchIfMissing = true)
    public GRpcServerRunner grpcServerRunner(GRpcServerBuilderConfigurer configurer) {
        return new GRpcServerRunner(configurer, ServerBuilder.forPort(grpcServerProperties.getPort()));
    }

    @Bean
    @ConditionalOnMissingBean(GRpcServerRunner.class)
    @ConditionalOnExpression("#{environment.getProperty('grpc.inProcessServerName','')!=''}")
    public GRpcServerRunner grpcInprocessServerRunner(GRpcServerBuilderConfigurer configurer){
        return new GRpcServerRunner(configurer, InProcessServerBuilder.forName(grpcServerProperties.getInProcessServerName()));
    }



    @Bean
    public HealthStatusManager healthStatusManager() {
        return new HealthStatusManager();
    }

    @Bean
    @ConditionalOnMissingBean(  GRpcServerBuilderConfigurer.class)
    public GRpcServerBuilderConfigurer serverBuilderConfigurer(){
        return new GRpcServerBuilderConfigurer();
    }
}
