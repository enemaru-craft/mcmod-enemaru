package com.enemaru.lighting;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.HashMap;
import java.util.Map;

/**
 * チャンク内のライトの点灯/調光を決定論的に計算する。
 *
 * 点灯アルゴリズム：
 * 1. N = ライト総数
 * 2. K = round(percent/10000 * N) = 点灯させる数
 * 3. 各ライトにrankを割り当て（決定論的ハッシュ）
 * 4. rankの小さい順にK個をON、残りをOFF
 * 5. ONのライトには調光レベルを段階的に割り当て（密度表現）
 */
public class ChunkLightingPlanner {

    /**
     * チャンク内のライトの目標レベルを計算。
     *
     * @param positions チャンク内のライト座標（posLong）
     * @param percent 点灯割合 (0-10000)
     * @param patternSeed チャンネルのパターンシード
     * @param worldSeed ワールドシード
     * @param channel チャンネル番号
     * @return posLong -> targetLevel のマップ
     */
    public static Map<Long, Integer> planChunkLighting(
            LongSet positions,
            int percent,
            long patternSeed,
            long worldSeed,
            int channel
    ) {
        Map<Long, Integer> result = new HashMap<>();

        int N = positions.size();
        if (N == 0) return result;

        // K = 点灯させる数
        int K = Math.round(percent / 10000.0f * N);
        if (K < 0) K = 0;
        if (K > N) K = N;

        // 全ライトにrankを割り当てソート
        LongList sortedPositions = new LongArrayList(positions);
        sortedPositions.sort((a, b) -> {
            long rankA = computeRank(a, worldSeed, patternSeed, channel);
            long rankB = computeRank(b, worldSeed, patternSeed, channel);
            return Long.compare(rankA, rankB);
        });

        // K個を点灯、残りを消灯
        // 点灯するライトには密度に応じて調光レベルを段階的に割り当て
        for (int i = 0; i < N; i++) {
            long posLong = sortedPositions.getLong(i);

            if (i < K) {
                // 点灯グループ
                // 密度に応じて4段階 (15, 10, 5, 0) で調光
                // 例: 100%なら全て15、50%なら半分が15で半分が10、25%なら段階的に
                int level = calculateLightLevel(i, K, percent);
                result.put(posLong, level);
            } else {
                // 消灯グループ
                result.put(posLong, 0);
            }
        }

        return result;
    }

    /**
     * 点灯グループ内での調光レベルを計算（密度表現）。
     * percent が高いほど明るいライトが多くなる。
     *
     * @param index 点灯グループ内でのインデックス (0-based)
     * @param totalOn 点灯する総数
     * @param percent 点灯割合 (0-10000)
     * @return 光度レベル (0, 5, 10, 15)
     */
    private static int calculateLightLevel(int index, int totalOn, int percent) {
        if (totalOn == 0) return 0;

        // 点灯グループ内での位置 (0.0-1.0)
        float position = (float) index / totalOn;

        // percentが高いほど明るいライトの割合が増える
        // 4段階の閾値を動的に計算
        float percentNormalized = percent / 10000.0f;

        // パーセントに応じて各レベルの割合を調整
        // 100%の場合: 全て15
        // 50%の場合: 上位50%が15、下位50%が10-5に分散
        // 25%の場合: 上位25%が15、残りは段階的に暗く

        float threshold15 = 0.6f * percentNormalized;  // 最も明るいグループ
        float threshold10 = threshold15 + 0.25f * percentNormalized;
        float threshold5 = threshold10 + 0.15f * percentNormalized;

        if (position < threshold15) {
            return 15;
        } else if (position < threshold10) {
            return 10;
        } else if (position < threshold5) {
            return 5;
        } else {
            // 残りは最低輝度でほのかに点灯
            return 5;
        }
    }

    /**
     * 決定論的なランク計算（mix64ハッシュ）。
     * posLong, worldSeed, patternSeed, channel から一意のrankを生成。
     */
    private static long computeRank(long posLong, long worldSeed, long patternSeed, int channel) {
        long hash = posLong;
        hash ^= worldSeed;
        hash ^= patternSeed;
        hash ^= (long) channel << 32;

        // MurmurHash3 finalizer (mix64)
        hash ^= hash >>> 33;
        hash *= 0xff51afd7ed558ccdL;
        hash ^= hash >>> 33;
        hash *= 0xc4ceb9fe1a85ec53L;
        hash ^= hash >>> 33;

        return hash;
    }
}

