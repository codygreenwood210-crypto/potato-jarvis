package com.potatopixelartstudios.teddygame;

import android.content.Context;
import android.content.SharedPreferences;

final class SaveManager {
    private static final String PREFS = "teddy_game_save_v1";
    private final SharedPreferences prefs;

    SaveManager(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    boolean hasSave() {
        return prefs.getBoolean("has_save", false);
    }

    void save(GameState s) {
        prefs.edit()
                .putBoolean("has_save", true)
                .putFloat("player_x", s.playerX)
                .putInt("health", s.health)
                .putInt("heartland", s.heartland)
                .putInt("nightmare", s.nightmare)
                .putInt("xp", s.xp)
                .putInt("level", s.level)
                .putInt("coins", s.coins)
                .putInt("weapon_level", s.weaponLevel)
                .putInt("potions", s.potions)
                .putInt("heart_shards", s.heartShards)
                .putString("quest_stage", s.questStage.name())
                .putBoolean("lantern_broken", s.lanternBroken)
                .putBoolean("boss_purified", s.bossPurified)
                .putBoolean("checkpoint", s.checkpointActivated)
                .putBoolean("village_returned", s.villageReturned)
                .apply();
    }

    void loadInto(GameState s) {
        if (!hasSave()) return;
        s.playerX = prefs.getFloat("player_x", 220f);
        s.health = GameState.clamp(prefs.getInt("health", GameState.MAX_HEALTH), 1, GameState.MAX_HEALTH);
        s.heartland = GameState.clamp(prefs.getInt("heartland", 50), 0, GameState.MAX_MAGIC);
        s.nightmare = GameState.clamp(prefs.getInt("nightmare", 50), 0, GameState.MAX_MAGIC);
        s.xp = Math.max(0, prefs.getInt("xp", 0));
        s.level = Math.max(1, prefs.getInt("level", 1));
        s.coins = Math.max(0, prefs.getInt("coins", 10));
        s.weaponLevel = GameState.clamp(prefs.getInt("weapon_level", 1), 1, 3);
        s.potions = Math.max(0, prefs.getInt("potions", 1));
        s.heartShards = Math.max(0, prefs.getInt("heart_shards", 0));
        try {
            s.questStage = GameState.QuestStage.valueOf(prefs.getString("quest_stage", GameState.QuestStage.FESTIVAL.name()));
        } catch (IllegalArgumentException ex) {
            s.questStage = GameState.QuestStage.FESTIVAL;
        }
        s.lanternBroken = prefs.getBoolean("lantern_broken", false);
        s.bossPurified = prefs.getBoolean("boss_purified", false);
        s.bossHealth = s.bossPurified ? 0 : GameState.BOSS_MAX_HEALTH;
        s.bossSpawned = s.questStage.ordinal() >= GameState.QuestStage.BOSS.ordinal() && !s.bossPurified;
        s.checkpointActivated = prefs.getBoolean("checkpoint", false);
        s.villageReturned = prefs.getBoolean("village_returned", false);
        s.playerY = 430f;
    }

    void clear() {
        prefs.edit().clear().apply();
    }
}
