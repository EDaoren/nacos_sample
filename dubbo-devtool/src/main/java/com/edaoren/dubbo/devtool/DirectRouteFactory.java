package com.edaoren.dubbo.devtool;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.common.extension.SPI;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.rpc.cluster.Router;
import org.apache.dubbo.rpc.cluster.RouterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 直连路由工厂
 *
 * @author EDaoren
 */
@SPI
@Activate
public class DirectRouteFactory implements RouterFactory {

    private static final Logger log = LoggerFactory.getLogger(DirectRouteFactory.class);
    DirectRouter router = new DirectRouter();

    public DirectRouteFactory() {
        boolean active = false;
        if ("true".equals(System.getProperty("direct.connection.active"))) {
            active = true;
        }
        this.router.setActive(active);
        if (active) {
            log.info("Open dubbo direct connection mode since system property 'direct.connection.active' is true");
            String host = System.getProperty("direct.connection.host");
            if (StringUtils.isBlank(host)) {
                log.info("It not found system property 'direct.connection.host', use current host");
            } else {
                log.info("Found system property 'direct.connection.host' {}", host);
                this.router.setHost(host);
            }

        } else {
            log.info("Close dubbo direct connection mode since 'direct.connection.active' is false");
        }
    }


    @Override
    public Router getRouter(URL url) {
        return this.router;
    }
}
