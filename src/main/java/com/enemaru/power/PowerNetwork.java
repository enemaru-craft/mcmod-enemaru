package com.enemaru.power;

import com.enemaru.blockentity.EndRodLampBlockEntity;
import com.enemaru.blockentity.GlowstoneLampBlockEntity;
import com.enemaru.blockentity.SeaLanternLampBlockEntity;
import com.enemaru.blockentity.StreetLightBlockEntity;
import com.enemaru.talkingclouds.commands.TalkCloudCommand;
import com.enemaru.commands.TrainCommand;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
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

    private boolean debug = false;
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

    /** 登録制リスト：現在読み込まれている街灯・シーランタンの BlockEntity */
    private final List<StreetLightBlockEntity> streetLights = new ArrayList<>();
    private final List<SeaLanternLampBlockEntity> seaLanterns = new ArrayList<>();
    private final List<GlowstoneLampBlockEntity> glowstoneLamps = new ArrayList<>();
    private final List<EndRodLampBlockEntity> endRodLamps = new ArrayList<>();

    private List<String> lastTexts = new ArrayList<>();

    private int trainTickCounter = 0;

    // 電車スポーン地点　（３両目最後尾車両の中央の座標となる）
    private List<Vec3d> spawnCoords = new ArrayList<>(List.of(
            new Vec3d(17, -60, 155)
//            new Vec3d(20, 64, 20),
//            new Vec3d(-15, 70, 5)
    ));
    // 座標に対応する電車の向き
    private List<Float> spawnYaws = new ArrayList<>(List.of(
            90f
//            45f,
//            180f
    ));

    private final int SPAWN_DURATION_TICKS = 20 * 60 * 5;

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
        if (world.isClient) return;
        if (!world.getRegistryKey().equals(ServerWorld.OVERWORLD)) return;
        // 電車をスポーンさせるか決定
        if(isTrainEnabled) {
            trainTickCounter++;
            if(trainTickCounter >= SPAWN_DURATION_TICKS) {
                trainTickCounter = 0;
                MinecraftServer server = world.getServer();
                ServerCommandSource source = server.getCommandSource();
                for (int i = 0; i < spawnCoords.size(); i++) {
                    Vec3d base = spawnCoords.get(i);
                    float yaw = spawnYaws.get(i);
                    TrainCommand.summonTrain(base, yaw, server, source);
                }
            }
        }

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
                    if (debug) {
                        world.getServer().execute(() -> {
                            for (ServerPlayerEntity player : world.getPlayers()) {
                                player.sendMessage(
                                        Text.literal("fetched:" + json),
                                        false
                                );
                            }
                        });
                    }

                    markDirty();
                })
                .exceptionally(ex -> {
                    if (debug) {
                        world.getServer().execute(() -> {
                            for (ServerPlayerEntity player : world.getPlayers()) {
                                player.sendMessage(Text.literal("エネルギーデータの取得に失敗しました"), false);
                            }
                        });
                        ex.printStackTrace();
                    }
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
                    Gson gson = new Gson();
                    WorldState states = gson.fromJson(response, WorldState.class);
                    updateState(states, world);
                    if(debug) {
                        System.out.println("State updated successfully: " + response);
                    }
                })
                .exceptionally(ex -> {
                    if(debug) {
                        System.out.println("State update failed");
                        ex.printStackTrace();
                    }
                    return null;
                });
    }

    private CompletableFuture<JsonObject> postAsync(String json, String endpoint) {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + endpoint))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        if (debug) {
            System.out.println("送信JSON: " + json);
        }
        return HTTP_CLIENT.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    int code = res.statusCode();
                    if (code < 200 || code >= 300) {
                        throw new RuntimeException("HTTP " + code + " : " + res.body());
                    }
                    return JsonParser.parseString(res.body()).getAsJsonObject();
                });
    }

    private void updateState(WorldState states, ServerWorld world) {
        setStreetlightsEnabled(states.state.isLightEnabled);
        this.isTrainEnabled = states.state.isTrainEnabled;
        this.isFactoryEnabled = states.state.isFactoryEnabled;
        this.isBlackout = states.state.isBlackout;

        this.generatedEnergy = (int) states.variables.totalPower;
        this.surplusEnergy = (int) states.variables.surplusPower;

        MinecraftServer server = world.getServer();
        ServerCommandSource source = server.getCommandSource();
        if(states.state.isTrainEnabled){
            TrainCommand.runTrain(server, source);
        }else{
            TrainCommand.stopTrain(server, source);
        }

        // 村人にテキストを分配
        if(states.texts.equals(this.lastTexts)) return;

        var villagers = world.getEntitiesByType(EntityType.VILLAGER, v -> true);
        var numTexts = states.texts.size();
        int counter = 0;

        for (Entity entity : villagers) {
            TalkCloudCommand.sendBubble(entity, Text.of(""), false, true);
            if(counter < numTexts) {
                TalkCloudCommand.sendBubble(entity, Text.of(states.texts.get(counter)), true,false);
            }
            counter++;
        }

        this.lastTexts = states.texts;
    }

    /** 街灯 BlockEntity を登録 */
    public void registerStreetLight(StreetLightBlockEntity te) {
        if (!streetLights.contains(te)) streetLights.add(te);
    }

    /** 街灯 BlockEntity を登録解除 */
    public void unregisterStreetLight(StreetLightBlockEntity te) {
        streetLights.remove(te);
    }

    /** シーランタン BlockEntity を登録 */
    public void registerSeaLantern(SeaLanternLampBlockEntity te) {
        if (!seaLanterns.contains(te)) seaLanterns.add(te);
    }

    /** シーランタン BlockEntity を登録解除 */
    public void unregisterSeaLantern(SeaLanternLampBlockEntity te) {
        seaLanterns.remove(te);
    }

    /** Glowstone BlockEntity を登録 */
    public void registerGlowstone(GlowstoneLampBlockEntity te) {
        if (!glowstoneLamps.contains(te)) glowstoneLamps.add(te);
    }

    /** Glowstone BlockEntity を登録解除 */
    public void unregisterGlowstone(GlowstoneLampBlockEntity te) {
        glowstoneLamps.remove(te);
    }

    /** エンドロッドランプ BlockEntity を登録 */
    public void registerEndRodLamp(EndRodLampBlockEntity te) {
        if (!endRodLamps.contains(te)) endRodLamps.add(te);
    }

    /** エンドロッドランプ BlockEntity を登録解除 */
    public void unregisterEndRodLamp(EndRodLampBlockEntity te) {
        endRodLamps.remove(te);
    }



    /** 許可フラグを取得 */
    public boolean getStreetlightsEnabled() { return isStreetlightsEnabled; }
    public boolean getTrainEnabled() { return isTrainEnabled; }
    public boolean getFactoryEnabled() { return isFactoryEnabled; }
    public boolean getBlackout() { return isBlackout; }

    /** ユーザー操作で呼ばれるメソッド */
    public void setStreetlightsEnabled(boolean enable) {
        this.isStreetlightsEnabled = enable;
        applyStreetLightState();
        markDirty();
    }

    /** すべての登録ライトに current フラグを反映 */
    private void applyStreetLightState() {
        for (var light : streetLights) {
            light.updatePowered(isStreetlightsEnabled);
        }
        for (var lantern : seaLanterns) {
            lantern.updatePowered(isStreetlightsEnabled);
        }
        for (var glow : glowstoneLamps) {
            glow.updatePowered(isStreetlightsEnabled);
        }
        for (var endRod : endRodLamps) {
            endRod.updatePowered(isStreetlightsEnabled);
        }
    }



    public int getGeneratedEnergy() { return generatedEnergy; }
    public int getSurplusEnergy() { return surplusEnergy; }

    public int getSessionId() { return sessionId; }
    public void setSessionId(int id) { this.sessionId = id; }

    public void setDebug(boolean debug) { this.debug = debug; }
}
