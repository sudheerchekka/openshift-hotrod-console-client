package org.everythingjboss.jdg;


import java.util.concurrent.CountDownLatch;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.Configuration;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleHotrodClient {

	private static final Logger logger = LoggerFactory.getLogger(SimpleHotrodClient.class);

	public static void main(String[] args) {

		String jdgServerHost = System.getenv("DATAGRID_APP_HOTROD_SERVICE_HOST");
		String jdgServerPort = System.getenv("DATAGRID_APP_HOTROD_SERVICE_PORT");

		// Catch and handle null values
		jdgServerHost = (jdgServerHost == null) ? "datagrid-app-hotrod" : jdgServerHost;
		jdgServerPort = (jdgServerPort == null) ? "11333" : jdgServerPort;

		String serverEndpoint = jdgServerHost.concat(":").concat(jdgServerPort);

		Configuration configuration = new ConfigurationBuilder().addServers(serverEndpoint).build();
		RemoteCacheManager rcm = new RemoteCacheManager(configuration);
		RemoteCache<Integer, String> cache = rcm.getCache("default");

		for (Integer i = 1; i <= 100; i++) {
			cache.put(i, i.toString());
		}

		logger.info("Done putting 100 entries into the cache");

		// Since the plan is to be on OpenShift and since a service is
		// expected to be running all the time, suspend the main thread here
		CountDownLatch cdl = new CountDownLatch(1);
		try {
			cdl.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
