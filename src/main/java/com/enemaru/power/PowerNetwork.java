package com.enemaru.power;

import com.enemaru.blockentity.StreetLightBlockEntity;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
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
    private int generatedEnergy = 0;
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

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
        return powerNetwork;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        return nbt;
    }


    public void tick(ServerWorld world) {
        // 3秒ごとに動かす
        if (world.getTime() % 60 != 0) return;
        fetchWorldStateAsync()
                .thenAccept(json -> {
                    Gson gson = new Gson();
                    WorldState states = gson.fromJson(json, WorldState.class);
                    updateState(states);

                    // デバッグ用
                    world.getServer().execute(() -> {
                        for (ServerPlayerEntity player : world.getPlayers()) {
                            player.sendMessage(
                                    Text.literal(
                                            String.format("fetched")
                                    ),
                                    false
                            );
                        }
                    });

                    markDirty();
                })
                .exceptionally(ex -> {
                    world.getServer().execute(() -> {
                        for (ServerPlayerEntity player : world.getPlayers()) {
                            player.sendMessage(Text.literal("エネルギーデータの取得に失敗しました"), false);
                        }
                    });
                    ex.printStackTrace();
                    return null;
                });
    }

    public void syncWorldState(WorldStateUpdate update) {
        Gson gson = new Gson();
        String statePayload = gson.toJson(update);
        postWorldStateAsync(statePayload)
                .thenAccept(response -> {
                    //レスポンスを受け取った後の処理
                    Gson gson1 = new Gson();
                    WorldState states = gson1.fromJson(response, WorldState.class);
                    updateState(states);
                    System.out.println("State updated successfully: " + response);
                })
                .exceptionally(ex -> {
                    System.out.println("State update failed");
                    ex.printStackTrace();
                    return null;
                });
    }

    /** 非同期で HTTP GET → JSON パース */
    private CompletableFuture<JsonObject> fetchWorldStateAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:3000/energy"))
                        .GET()
                        .build();
                HttpResponse<String> res = HTTP_CLIENT.send(
                        req, HttpResponse.BodyHandlers.ofString()
                );
                return JsonParser.parseString(res.body()).getAsJsonObject();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private CompletableFuture<JsonObject> postWorldStateAsync(String json) {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:3000/state"))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        System.out.println("送信JSON: " + json);
        return HTTP_CLIENT.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    int code = res.statusCode();
                    if (code < 200 || code >= 300) {
                        throw new RuntimeException("HTTP " + code + " : " + res.body());
                    }
                    return JsonParser.parseString(res.body()).getAsJsonObject();
                });
    }

    /** ワールドの状態を更新 */
    private void updateState(WorldState states) {
        // ここでワールドのギミックのオンオフを更新
        setStreetlightsEnabled(states.state.isLightEnabled);
        this.generatedEnergy = (int)states.variables.totalPower;
        // デバッグ用
        System.out.println(states.variables.totalPower);
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

    public int getGeneratedEnergy() {
        return generatedEnergy;
    }

}
