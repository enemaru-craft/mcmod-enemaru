package com.enemaru.mqtt;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.net.ssl.SSLContext;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;


public class AsyncMQTTPublisher {

    private final Gson gson = new Gson();
    private final ExecutorService executor;
    private MqttClient mqttClient;
    private final String mqttEndpoint;
    private final SSLContext sslContext;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);


    /**
     * Creates an AsyncMQTTPublisher with specified client ID.
     *
     * @param mqttEndpoint MQTT broker endpoint
     * @param sslContext   SSL context for secure connection
     * @param clientId     Custom client ID to use
     * @throws Exception if MQTT client creation fails
     */
    public AsyncMQTTPublisher(String mqttEndpoint, SSLContext sslContext, String clientId, boolean useTls) throws Exception {
        this.executor = Executors.newFixedThreadPool(3);
        this.mqttEndpoint = mqttEndpoint;
        this.sslContext = sslContext;

        // Create initial MQTT client with specified client ID
        this.mqttClient = new MqttClient(mqttEndpoint, clientId, new MemoryPersistence());

        // Connect asynchronously
        connectAsync(useTls);
    }

    /**
     * Connects to MQTT broker asynchronously.
     */
    private void connectAsync(boolean useTls) {
        // Configure MQTT connection
        MqttConnectOptions options = new MqttConnectOptions();
        if(useTls) {
            options.setSocketFactory(sslContext.getSocketFactory());
        }
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);

        CompletableFuture.runAsync(() -> {
            try {
                if (mqttClient.isConnected()) {
                    mqttClient.disconnect();
                }
                mqttClient.connect(options);
                isConnected.set(true);
                System.out.println("[MQTT] Connected with client ID: " + mqttClient.getClientId());
            } catch (Exception e) {
                System.err.println("[MQTT] Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                e.printStackTrace();
                isConnected.set(false);
            }
        }, executor);
    }

    /**
     * Reconnects with a new client ID.
     *
     * @param newClientId New client ID to use for reconnection
     * @return CompletableFuture that completes when reconnection is done
     */
    public CompletableFuture<Void> reconnectWithNewClientId(String newClientId, boolean useTls) {
        return CompletableFuture.runAsync(() -> {
            try {
                System.out.println("[MQTT] Reconnecting with new client ID: " + newClientId);

                // Disconnect current client
                if (mqttClient!=null&& mqttClient.isConnected()) {
                    mqttClient.disconnect();
                }
                if(mqttClient!=null) {
                    mqttClient.close();
                }
                isConnected.set(false);

                // Create new client with specified ID
                mqttClient = new MqttClient(mqttEndpoint, newClientId, new MemoryPersistence());

                // Connect with new client
                connectAsync(useTls);

                // Wait for connection to establish
                int retries = 50; // Wait up to 5 seconds
                while (!isConnected.get() && retries > 0) {
                    Thread.sleep(100);
                    retries--;
                }

                if (isConnected.get()) {
                    System.out.println("[MQTT] Successfully reconnected with client ID: " + newClientId);
                } else {
                    System.err.println("[MQTT] Failed to reconnect with new client ID: " + newClientId);
                }

            } catch (Exception e) {
                System.err.println("[MQTT] Reconnection failed: " + e.getMessage());
                isConnected.set(false);
                throw new RuntimeException(e);
            }
        }, executor);
    }

    /**
     * Gets the current client ID.
     *
     * @return Current MQTT client ID
     */
    public String getCurrentClientId() {
        try {
            return mqttClient.getClientId();
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * Publishes data asynchronously. Won't block the calling thread.
     *
     * @param topic   MQTT topic
     * @param payload Data to publish
     * @return CompletableFuture for chaining if needed
     */
    public CompletableFuture<Void> publishAsync(String topic, Map<String, Object> payload) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Wait for connection
                while (!isConnected.get()) {
                    Thread.sleep(100);
                }

                // Convert Map to JSON using Gson
                String json = gson.toJson(payload);

                MqttMessage msg = new MqttMessage(json.getBytes("UTF-8"));
                msg.setQos(1);

                synchronized (mqttClient) {
                    mqttClient.publish(topic, msg);
                }

                System.out.println("[MQTT] Published: " + json);

            } catch (Exception e) {
                System.err.println("[MQTT] Publish failed: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, executor);
    }

    /**
     * Simple publish without waiting for result.
     * Perfect for game data where you don't need to handle errors.
     */
    public void publish(String topic, Map<String, Object> payload) {
        publishAsync(topic, payload).exceptionally(error -> {
            // Log error but don't throw
            return null;
        });
    }

    /**
     * Check if connected to MQTT broker.
     */
    public boolean isConnected() {
        return isConnected.get();
    }

    /**
     * Shutdown the publisher.
     */
    public void shutdown() {
        try {
            if (mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
            mqttClient.close();
            executor.shutdown();
            System.out.println("[MQTT] Shutdown complete");
        } catch (Exception e) {
            System.err.println("[MQTT] Shutdown error: " + e.getMessage());
        }
    }
}