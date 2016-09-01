package org.everythingjboss.jdg;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.transport.tcp.TcpTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleHotrodClient {

    private static final Logger logger = LoggerFactory.getLogger(SimpleHotrodClient.class);
    private static final int CLUSTER_SIZE = 3;
    
    public static void main(String[] args) {

        String jdgServerHost = System.getenv("DATAGRID_APP_HOTROD_SERVICE_HOST");
        String jdgServerPort = System.getenv("DATAGRID_APP_HOTROD_SERVICE_PORT");
        String cacheNames = System.getenv("CACHE_NAMES");

        // Catch and handle null values
        jdgServerHost = (jdgServerHost == null) ? "127.0.0.1" : jdgServerHost;
        jdgServerPort = (jdgServerPort == null) ? "11222" : jdgServerPort;
        cacheNames = (cacheNames == null) ? "default" : cacheNames.split(",")[0];

        String serverEndpoint = jdgServerHost.concat(":").concat(jdgServerPort);

        Configuration configuration = new ConfigurationBuilder().addServers(serverEndpoint).build();
        RemoteCacheManager rcm = new RemoteCacheManager(configuration);
        RemoteCache<Integer, String> cache = rcm.getCache(cacheNames);
        TcpTransportFactory transportFactory = null;
        
        try {
            Field transportFactoryField = RemoteCacheManager.class.getDeclaredField("transportFactory");
            transportFactoryField.setAccessible(true);
            transportFactory = (TcpTransportFactory) transportFactoryField.get(rcm);
            
            while(transportFactory.getServers().size() < CLUSTER_SIZE) {
                logger.info("Still waiting for the cluster to be of size "+CLUSTER_SIZE);
                Thread.sleep(2000);
                cache.stats();
            }
            
            for(Integer i=1;i <= 100; i++) {
                cache.put(i,i.toString());
            }
            
            logger.info("Done putting 100 entries into the cache");
            
            // Since the plan is to be on OpenShift and since a service is 
            // expected to be running all the time, suspend the main thread here
            CountDownLatch cdl = new CountDownLatch(1);
            cdl.await();
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rcm.stop();
        }
        
    }

}
