package com.edaoren.dubbo.devtool;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.common.extension.SPI;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.router.AbstractRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * 直连路由
 *
 * @author EDaoren
 */
public class DirectRouter extends AbstractRouter {

    private static final Logger log = LoggerFactory.getLogger(DirectRouter.class);
    private boolean active = false;
    private String host;
    private String HOSTS_NAME_KEY = "HOSTS_NAME_KEY";


    @Override
    public <T> List<Invoker<T>> route(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        if (!this.active) {
            return invokers;
        } else if (CollectionUtils.isEmpty(invokers)) {
            log.error("Service Provider list is empty, It need to start service provider or check the register center！");
            return invokers;
        } else {
            String hosts = (String)invocation.getAttachments().get(this.HOSTS_NAME_KEY);
            if (hosts == null && this.host != null) {
                hosts = this.host;
                invocation.getAttachments().put(this.HOSTS_NAME_KEY, hosts);
            }

            if (hosts == null) {
                hosts = this.host == null ? url.getHost() : this.getHost();
            }

            List<Invoker<T>> result = this.getDirectHostService(hosts.trim(), invokers);
            if (result.isEmpty()) {
                return invokers;
            } else {
                log.debug("Found the service [{}] on host [{}]", url.getServiceInterface(), this.host);
                return result;
            }
        }
    }


    public <T> List<Invoker<T>> getDirectHostService(String hosts, List<Invoker<T>> invokers) {
        List<Invoker<T>> result = new ArrayList<>(1);
        if (hosts != null && hosts.length() != 0) {
            String[] hostList = hosts.split(";");
            String[] temp = hostList;
            int length = hostList.length;

            for(int i = 0; i < length; ++i) {
                String one = temp[i];
                Iterator iterator = invokers.iterator();

                while(iterator.hasNext()) {
                    Invoker<T> invoker = (Invoker)iterator.next();
                    if (invoker.getUrl().getHost().equals(one)) {
                        result.add(invoker);
                        return result;
                    }
                }
            }
            return Collections.EMPTY_LIST;
        } else {
            return result;
        }
    }


    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHOSTS_NAME_KEY() {
        return HOSTS_NAME_KEY;
    }

    public void setHOSTS_NAME_KEY(String HOSTS_NAME_KEY) {
        this.HOSTS_NAME_KEY = HOSTS_NAME_KEY;
    }
}
