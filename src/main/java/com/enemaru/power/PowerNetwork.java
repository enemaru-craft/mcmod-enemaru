package com.enemaru.power;

import com.enemaru.blockentity.StreetLightBlockEntity;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class PowerNetwork extends PersistentState {
    private static final String KEY = "enemaru_power_network";
    private double fetchedEnergy = 0.0;
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    private int count = 1;

    /** ユーザー操作で点灯／消灯を切り替えるフラグ */
    private boolean streetlightsEnabled = false;
    /** 登録制リスト：現在読み込まれている街灯の BlockEntity */
    private final List<StreetLightBlockEntity> streetLights = new ArrayList<>();

    private PowerNetwork() {
        super();
    }

    public static PowerNetwork get(ServerWorld world) {
        return world.getPersistentStateManager()
                .getOrCreate(
                        new Type<>(
                                PowerNetwork::new,
                                PowerNetwork::createFromNbt,
                                null
                        ),
                        KEY
                );
    }

    private static PowerNetwork createFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        PowerNetwork powerNetwork = new PowerNetwork();
        powerNetwork.fetchedEnergy = nbt.getDouble("fetchedEnergy");
        return powerNetwork;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putDouble("fetchedEnergy", fetchedEnergy);
        return nbt;
    }


    public void tick(ServerWorld world) {
        // 5秒ごと（100ティック）にだけ動かす
        if (world.getTime() % 100 != 0) return;
        if(count==10){
            count = 1; // リセット
        }
//        fetchSensorDataAsync()
//                .thenAccept(json -> {
//
//                    fetchedEnergy = json.get(count).getAsJsonObject().getAsJsonObject("address").getAsJsonObject("geo").get("lat").getAsDouble();
//
//                    // サーバースレッドに戻して全プレイヤーへメッセージ送信 & 保存フラグ
//                    world.getServer().execute(() -> {
//                        for (ServerPlayerEntity player : world.getPlayers()) {
//                            player.sendMessage(
//                                    Text.literal(
//                                            String.format("緯度: %.1f",
//                                                    fetchedEnergy)
//                                    ),
//                                    false
//                            );
//                        }
//                        markDirty();
//                    });
//                })
//                .exceptionally(ex -> {
//                    world.getServer().execute(() -> {
//                        for (ServerPlayerEntity player : world.getPlayers()) {
//                            player.sendMessage(Text.literal("エネルギーデータの取得に失敗しました"), false);
//                        }
//                    });
//                    ex.printStackTrace();
//                    return null;
//                });
//        count++;
    }


    /** 非同期で HTTP GET → JSON パース */
    private CompletableFuture<JsonArray> fetchSensorDataAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create("https://jsonplaceholder.typicode.com/users"))
                        .GET()
                        .build();
                HttpResponse<String> res = HTTP_CLIENT.send(
                        req, HttpResponse.BodyHandlers.ofString()
                );
                return JsonParser.parseString(res.body()).getAsJsonArray();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }


    /** 街灯 BlockEntity を登録 */
    public void registerStreetLight(StreetLightBlockEntity te) {
        if (!streetLights.contains(te)) {
            streetLights.add(te);
        }
    }

    /** 街灯 BlockEntity を登録解除 */
    public void unregisterStreetLight(StreetLightBlockEntity te) {
        streetLights.remove(te);
    }

    /** 許可フラグを取得 */
    public boolean isStreetlightsEnabled() {
        return streetlightsEnabled;
    }

    /** ユーザー操作で呼ばれるメソッド */
    public void setStreetlightsEnabled(boolean enable) {
        this.streetlightsEnabled = enable;
        applyStreetLightState();  // 一斉オン／オフ
        markDirty();
    }

    /** すべての登録ライトに current フラグを反映 */
    private void applyStreetLightState() {
        for (var light : streetLights) {
            light.updatePowered(streetlightsEnabled);
        }
    }

}
