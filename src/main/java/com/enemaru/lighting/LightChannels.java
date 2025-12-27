package com.enemaru.lighting;

/**
 * 照明システムのチャンネル定数定義。
 * 各ライト種類にデフォルトチャンネルを割り当てます。
 */
public class LightChannels {
    // ライト種類ごとのデフォルトチャンネル
    public static final int STREET_LIGHT = 0;      // 街灯
    public static final int LANTERN = 1;            // ランタン
    public static final int SEA_LANTERN = 2;        // シーランタンランプ
    public static final int END_ROD = 3;            // エンドロッドランプ
    public static final int GLOWSTONE = 4;          // グロウストーンランプ
    public static final int STATION_END_ROD = 5;    // 駅エンドロッド

    // ユーザー自由設定用（6-15）
    public static final int USER_DEFINED_START = 6;
    public static final int USER_DEFINED_END = 15;

    /**
     * チャンネル番号からライト種類名を取得（デバッグ・UI用）
     */
    public static String getChannelName(int channel) {
        return switch (channel) {
            case STREET_LIGHT -> "街灯";
            case LANTERN -> "ランタン";
            case SEA_LANTERN -> "シーランタン";
            case END_ROD -> "エンドロッド";
            case GLOWSTONE -> "グロウストーン";
            case STATION_END_ROD -> "駅エンドロッド";
            default -> "カスタム" + channel;
        };
    }

    /**
     * チャンネル番号が有効範囲かチェック
     */
    public static boolean isValidChannel(int channel) {
        return channel >= 0 && channel <= 15;
    }

    private LightChannels() {
        // ユーティリティクラス: インスタンス化不可
    }
}

