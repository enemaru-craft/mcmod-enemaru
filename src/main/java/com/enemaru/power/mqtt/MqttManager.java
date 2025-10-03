package com.enemaru.power.mqtt;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public final class MqttManager {
    private static final MqttManager INSTANCE = new MqttManager();
    public static MqttManager get() { return INSTANCE; }
    private MqttManager() {}

    private volatile Mqtt3AsyncClient client;
    private static final String TOPIC_BASE = "register/power";
    private volatile String clientId;
    private volatile int qos = 1;
    private volatile boolean retain = false;

    // 接続情報
    private static String MQTT_URL = "localhost";
    private static final int PORT = 8883;
    static {
        try {
            Path cfg = FabricLoader.getInstance().getConfigDir().resolve("enemaru.json");
            System.out.println("Place config json to: "+FabricLoader.getInstance().getConfigDir());
            if (Files.exists(cfg)) {
                String json = Files.readString(cfg);
                JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                if (obj.has("mqttUrl")) {
                    MQTT_URL = obj.get("mqttUrl").getAsString();
                    System.out.println("[enemaru] MQTT_URL loaded from config: " + MQTT_URL);
                }
            } else {
                System.out.println("[enemaru] No config file found, using default MQTT_URL=" + MQTT_URL);
            }
        } catch (Exception e) {
            System.err.println("[enemaru] Failed to load config, using default MQTT_URL=" + MQTT_URL);
        }
    }

    public void initAndConnect(int sessionId) {
        // 認証情報設定
        Path cfgDir    = FabricLoader.getInstance().getConfigDir();
        Path clientP12 = cfgDir.resolve("client.p12");
        Path trustP12  = cfgDir.resolve("truststore.p12");
        Path cfg       = cfgDir.resolve("enemaru.json");

        if (!Files.exists(clientP12) || !Files.exists(trustP12)) {
            System.out.println("TLS files missing in: " + cfgDir.toAbsolutePath());
        } else {
            try {
                String p12Password = "";
                if (Files.exists(cfg)) {
                    String json = Files.readString(cfg);
                    JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                    if (obj.has("p12Password")) {
                        p12Password = obj.get("p12Password").getAsString();
                    }
                }
                // JVM 既定の KeyStore / TrustStore を設定（mTLS）
                System.setProperty("javax.net.ssl.keyStore", clientP12.toAbsolutePath().toString());
                System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
                System.setProperty("javax.net.ssl.keyStorePassword", p12Password);

                System.setProperty("javax.net.ssl.trustStore", trustP12.toAbsolutePath().toString());
                System.setProperty("javax.net.ssl.trustStoreType", "PKCS12");
                System.setProperty("javax.net.ssl.trustStorePassword", p12Password);
            } catch (Exception e) {
                System.out.println("[enemaru] TLS setup failed");
                e.printStackTrace();
            }
        }

        this.clientId = "M5-"+ sessionId +"-fire-1";
        this.qos = 1;
        this.retain = false;

        // 既存クライアントを閉じる
        try { if (client != null && client.getState().isConnected()) client.disconnect(); } catch (Exception ignored) {}

        var builder = MqttClient.builder()
                .useMqttVersion3()
                .identifier(clientId)
                .serverHost(MQTT_URL)
                .serverPort(PORT);


        builder.sslWithDefaultConfig();

        client = builder.buildAsync();

        client.connectWith()
                .keepAlive(30)
                .cleanSession(true)
                .send()
                .orTimeout(5, TimeUnit.SECONDS)
                .whenComplete((ack, ex) -> {
                    if (ex != null) {
                        ex.printStackTrace(); // まずは素で全部出す
                        System.err.println("[enemaru] connect failed cause=" +
                                (ex.getCause()!=null? ex.getCause().getClass().getName() : ex.getClass().getName()));
                    } else {
                        System.out.println(String.format(Locale.ROOT,
                                "[enemaru] MQTT connected tcp://%s:%d clientId=%s", MQTT_URL, PORT, clientId));
                    }
                });
    }


    public void publishJson(JsonObject payload) {
        Mqtt3AsyncClient c = client;
        if (c == null || !c.getState().isConnected()) {
            System.err.println("[enemaru] MQTT not connected; drop message");
            return;
        }
        String topic = TOPIC_BASE;
        byte[] bytes = payload.toString().getBytes(StandardCharsets.UTF_8);

        c.publishWith()
                .topic(topic)
                .qos(qosToMqtt(qos))
                .retain(retain)
                .payload(bytes)
                .send()
                .orTimeout(3, TimeUnit.SECONDS)
                .whenComplete((pubAck, ex) -> {
                    if (ex != null) {
                        System.err.println("[enemaru] MQTT publish failed: " + ex.getMessage());
                    }
                });
    }

    private MqttQos qosToMqtt(int q) {
        return switch (q) {
            case 0 -> MqttQos.AT_MOST_ONCE;
            case 2 -> MqttQos.EXACTLY_ONCE;
            default -> MqttQos.AT_LEAST_ONCE;
        };
    }
}
