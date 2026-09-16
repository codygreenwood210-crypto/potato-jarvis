package com.potatopixelartstudios.teddygame;

import org.junit.Test;
import static org.junit.Assert.*;

public class GameStateTest {
    @Test public void healthNeverDropsBelowZeroAndRespawns() {
        GameState s = new GameState();
        s.questStage = GameState.QuestStage.FOREST;
        s.damagePlayer(200, 1000);
        assertEquals(GameState.MAX_HEALTH, s.health);
        assertTrue(s.playerX >= 1800f);
    }

    @Test public void levelUpCarriesOverflowXp() {
        GameState s = new GameState();
        s.addXp(130);
        assertEquals(2, s.level);
        assertEquals(30, s.xp);
    }

    @Test public void purificationNeedsBothPowersAndLowBossHealth() {
        GameState s = new GameState();
        s.bossSpawned = true;
        s.bossHealth = GameState.PURIFY_HEALTH_THRESHOLD;
        s.heartland = 34;
        s.nightmare = 100;
        assertFalse(s.isPurificationAvailable());
        s.heartland = 35;
        assertTrue(s.isPurificationAvailable());
    }

    @Test public void ordinaryDamageCannotKillPumpkinKing() {
        GameState s = new GameState();
        s.bossSpawned = true;
        s.damageBoss(9999);
        assertEquals(1, s.bossHealth);
        assertFalse(s.bossPurified);
    }

    @Test public void purificationGrantsShardAdvancesQuestAndAppliesLevelReward() {
        GameState s = new GameState();
        s.bossSpawned = true;
        s.bossHealth = 50;
        s.heartland = 70;
        s.nightmare = 70;
        assertTrue(s.purifyBoss());
        assertTrue(s.bossPurified);
        assertEquals(1, s.heartShards);
        assertEquals(GameState.QuestStage.RETURN_HOME, s.questStage);
        assertEquals(2, s.level);
        assertEquals(20, s.xp);
        assertEquals(55, s.heartland);
        assertEquals(55, s.nightmare);
    }

    @Test public void axeUpgradeRespectsCoinsAndCap() {
        GameState s = new GameState();
        s.coins = 100;
        assertTrue(s.buyAxeUpgrade());
        assertEquals(2, s.weaponLevel);
        assertTrue(s.buyAxeUpgrade());
        assertEquals(3, s.weaponLevel);
        assertFalse(s.buyAxeUpgrade());
    }

    @Test public void potionDoesNotWasteAtFullHealth() {
        GameState s = new GameState();
        assertFalse(s.usePotion());
        assertEquals(1, s.potions);
        s.health = 25;
        assertTrue(s.usePotion());
        assertEquals(80, s.health);
        assertEquals(0, s.potions);
    }

    @Test public void magicRegenerationAccumulatesSubFrameTime() {
        GameState s = new GameState();
        s.heartland = 0;
        s.nightmare = 0;
        for (int i = 0; i < 60; i++) s.regenerateMagic(1f / 60f);
        assertEquals(3, s.heartland);
        assertEquals(3, s.nightmare);
    }

    @Test public void lanternDisasterCannotBeSkippedAndForestRequiresRescue() {
        GameState s = new GameState();
        s.playerX = 1800f;
        s.updateStoryProgress();
        assertTrue(s.lanternBroken);
        assertEquals(GameState.QuestStage.LANTERN_BROKEN, s.questStage);
        s.updateStoryProgress();
        assertEquals(GameState.QuestStage.LANTERN_BROKEN, s.questStage);
        assertTrue(s.rescueVillager());
        s.updateStoryProgress();
        assertEquals(GameState.QuestStage.FOREST, s.questStage);
    }

    @Test public void dodgeHasCooldownAndInvulnerability() {
        GameState s = new GameState();
        assertTrue(s.tryDodge(1000));
        assertFalse(s.tryDodge(1200));
        int hp = s.health;
        s.damagePlayer(50, 1100);
        assertEquals(hp, s.health);
        s.damagePlayer(20, 1400);
        assertEquals(hp - 20, s.health);
        assertTrue(s.tryDodge(1700));
    }

    @Test public void mummyHealerCostsCoinsOnlyWhenHealing() {
        GameState s = new GameState();
        int initialCoins = s.coins;
        assertFalse(s.visitMummyHealer());
        assertEquals(initialCoins, s.coins);
        s.health = 20;
        assertTrue(s.visitMummyHealer());
        assertEquals(GameState.MAX_HEALTH, s.health);
        assertEquals(initialCoins - 5, s.coins);
    }

    @Test public void secretRewardIsOneTime() {
        GameState s = new GameState();
        int coins = s.coins;
        int potions = s.potions;
        assertTrue(s.discoverSecret());
        assertEquals(coins + 12, s.coins);
        assertEquals(potions + 1, s.potions);
        assertFalse(s.discoverSecret());
        assertEquals(coins + 12, s.coins);
    }

    @Test public void returningShardChangesHubBeforeCompletingSlice() {
        GameState s = new GameState();
        s.bossPurified = true;
        s.questStage = GameState.QuestStage.RETURN_HOME;
        s.playerX = 1600f;
        s.updateStoryProgress();
        assertTrue(s.villageReturned);
        assertEquals(GameState.QuestStage.RETURN_HOME, s.questStage);
        s.playerX = 250f;
        s.updateStoryProgress();
        assertEquals(GameState.QuestStage.COMPLETE, s.questStage);
    }
}
