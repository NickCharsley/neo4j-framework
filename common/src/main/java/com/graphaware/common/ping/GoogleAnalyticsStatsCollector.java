package com.graphaware.common.ping;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * {@link StatsCollector} that collects stats using Google Analytics.
 */
public class GoogleAnalyticsStatsCollector implements StatsCollector {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleAnalyticsStatsCollector.class);
    private static final UUID uuid = UUID.randomUUID();
    private static final String TID = "UA-53671654-1";
    private static final ExecutorService executor = Executors.newFixedThreadPool(3);

    private static final GoogleAnalyticsStatsCollector INSTANCE = new GoogleAnalyticsStatsCollector();

    public static GoogleAnalyticsStatsCollector getInstance() {
        return INSTANCE;
    }

    private GoogleAnalyticsStatsCollector() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                executor.shutdownNow();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void frameworkStart(String edition) {
        post(constructBody("framework-" + edition));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runtimeStart() {
        post(constructBody("com.graphaware.runtime.GraphAwareRuntime"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void moduleStart(String moduleClassName) {
        post(constructBody(moduleClassName));
    }

    private void post(final String body) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    //wait to make sure it's not a unit test or something like that
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    //do nothing
                }

                HttpPost httpPost = new HttpPost("http://www.google-analytics.com/collect");
                httpPost.setEntity(new StringEntity(body, ContentType.TEXT_PLAIN));

                try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                    httpClient.execute(httpPost);
                } catch (IOException e1) {
                    LOG.warn("Unable to collect stats", e1);
                }
            }
        });
    }

    private String constructBody(String appId) {
        return "v=1&tid=" + TID + "&cid=" + uuid + "&t=event&ea=start&ec=" + appId + "&el=" + VERSION;
    }
}