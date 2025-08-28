package com.enemaru.power;

import com.enemaru.blockentity.StreetLightBlockEntity;
import com.enemaru.talkingclouds.commands.TalkCloudCommand;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class PowerNetwork extends PersistentState {
    private static final String KEY = "enemaru_power_network";
    private static String API_URL = "http://localhost:3000";
    static {
        try {
            Path cfg = FabricLoader.getInstance().getConfigDir().resolve("enemaru.json");
            System.out.println("Place config json to: "+FabricLoader.getInstance().getConfigDir());
            if (Files.exists(cfg)) {
                String json = Files.readString(cfg);
                JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                if (obj.has("backendBaseUrl")) {
                    API_URL = obj.get("backendBaseUrl").getAsString();
                    System.out.println("[enemaru] API_URL loaded from config: " + API_URL);
                }
            } else {
                System.out.println("[enemaru] No config file found, using default API_URL=" + API_URL);
            }
        } catch (Exception e) {
            System.err.println("[enemaru] Failed to load config, using default API_URL=" + API_URL);
        }
    }
    private int sessionId = 2021;
    private int generatedEnergy = 0;
    private int surplusEnergy = 0;
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    /** ユーザー操作で点灯／消灯を切り替えるフラグ */
    private boolean isStreetlightsEnabled = false;
    private boolean isTrainEnabled = false;
    private boolean isFactoryEnabled = false;
    private boolean isBlackout = false;
    /** 登録制リスト：現在読み込まれている街灯の BlockEntity */
    private final List<StreetLightBlockEntity> streetLights = new ArrayList<>();
    private List<String> lastTexts = new ArrayList<>();

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
        // サーバー側でのみ実行
        if (world.isClient) {
            return;
        }
        // オーバーワールドでのみ実行
        if (!world.getRegistryKey().equals(ServerWorld.OVERWORLD)) {
            return;
        }
        // 3秒ごとに動かす
        if (world.getTime() % 60 != 0) return;

        JsonObject obj = new JsonObject();
        obj.addProperty("sessionId", Integer.toString(sessionId));
        String session = obj.toString();
        postAsync(session, "/get-current-world-state")
                .thenAccept(json -> {
                    Gson gson = new Gson();
                    WorldState states = gson.fromJson(json, WorldState.class);
                    updateState(states, world);

                    // デバッグ用
                    world.getServer().execute(() -> {
                        for (ServerPlayerEntity player : world.getPlayers()) {
                            player.sendMessage(
                                    Text.literal(
                                            String.format("fetched:"+json)
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

    public void sendWorldState(String equipment, boolean enable, ServerWorld world) {
        JsonObject obj = new JsonObject();
        obj.addProperty("sessionId", Integer.toString(sessionId));
        obj.addProperty("equipment", equipment);
        String statePayload = obj.toString();
        String endpoint = enable ? "/turn-on-equipment" : "/turn-off-equipment";
        postAsync(statePayload, endpoint)
                .thenAccept(response -> {
                    //レスポンスを受け取った後の処理
                    Gson gson1 = new Gson();
                    WorldState states = gson1.fromJson(response, WorldState.class);
                    updateState(states, world);
                    System.out.println("State updated successfully: " + response);
                })
                .exceptionally(ex -> {
                    System.out.println("State update failed");
                    ex.printStackTrace();
                    return null;
                });
    }

    private CompletableFuture<JsonObject> postAsync(String json, String endpoint) {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(API_URL+endpoint))
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
    private void updateState(WorldState states, ServerWorld world) {
        // ここでワールドのギミックのオンオフを更新
        setStreetlightsEnabled(states.state.isLightEnabled);

        // TODO: 他のギミックもここで更新する
        // とりあえずフィールドのみ変更
        this.isTrainEnabled = states.state.isTrainEnabled;
        this.isFactoryEnabled = states.state.isFactoryEnabled;
        this.isBlackout = states.state.isBlackout;

        this.generatedEnergy = (int)states.variables.totalPower;
        this.surplusEnergy = (int)states.variables.surplusPower;
        // 村人の吹き出しにメッセージを分配
        if(states.texts.equals(this.lastTexts)){
            return; // 前回と同じなら更新しない
        }
        // 村人のエンティティリストを取得
        var villagers = world.getEntitiesByType(EntityType.VILLAGER,v -> true);
        var numTexts = states.texts.size();
        int counter = 0;
        System.out.println("Villager count: " + villagers.size());
        for (Entity entity : villagers) {
            TalkCloudCommand.sendBubble(entity, Text.of(""), false, true);
            if(counter < numTexts) {
                TalkCloudCommand.sendBubble(entity, Text.of(states.texts.get(counter)), true,false);
            }else{
                System.out.println("Texts count Overflow!!!!");
            }
            counter++;
        }
        this.lastTexts = states.texts;
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
    public boolean getStreetlightsEnabled() {
        return isStreetlightsEnabled;
    }

    public boolean getTrainEnabled() {
        return isTrainEnabled;
    }

    public boolean getFactoryEnabled() {
        return isFactoryEnabled;
    }

    public boolean getBlackout() {
        return isBlackout;
    }

    /** ユーザー操作で呼ばれるメソッド */
    public void setStreetlightsEnabled(boolean enable) {
        this.isStreetlightsEnabled = enable;
        applyStreetLightState();  // 一斉オン／オフ
        markDirty();
    }

    /** すべての登録ライトに current フラグを反映 */
    private void applyStreetLightState() {
        for (var light : streetLights) {
            light.updatePowered(isStreetlightsEnabled);
        }
    }

    public int getGeneratedEnergy() {
        return generatedEnergy;
    }

    public int getSurplusEnergy() {
        return surplusEnergy;
    }

}
