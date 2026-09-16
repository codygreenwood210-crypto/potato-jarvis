package com.potatopixelartstudios.teddygame;

import org.junit.Test;
import static org.junit.Assert.*;

public class GameStateTest {
    @Test public void healthNeverDropsBelowZeroAndRespawns(){GameState s=new GameState();s.questStage=GameState.QuestStage.FOREST;s.damagePlayer(200,1000);assertEquals(GameState.MAX_HEALTH,s.health);assertTrue(s.playerX>=1800f);}
    @Test public void levelUpCarriesOverflowXp(){GameState s=new GameState();s.addXp(130);assertEquals(2,s.level);assertEquals(30,s.xp);}
    @Test public void purificationNeedsBothPowersAndLowBossHealth(){GameState s=new GameState();s.bossSpawned=true;s.bossHealth=GameState.PURIFY_HEALTH_THRESHOLD;s.heartland=34;s.nightmare=100;assertFalse(s.isPurificationAvailable());s.heartland=35;assertTrue(s.isPurificationAvailable());}
    @Test public void ordinaryDamageCannotKillPumpkinKing(){GameState s=new GameState();s.bossSpawned=true;s.damageBoss(9999);assertEquals(1,s.bossHealth);assertFalse(s.bossPurified);}
    @Test public void purificationGrantsShardAndAdvancesQuest(){GameState s=new GameState();s.bossSpawned=true;s.bossHealth=50;s.heartland=70;s.nightmare=70;assertTrue(s.purifyBoss());assertTrue(s.bossPurified);assertEquals(1,s.heartShards);assertEquals(GameState.QuestStage.RETURN_HOME,s.questStage);assertEquals(35,s.heartland);assertEquals(35,s.nightmare);}
    @Test public void axeUpgradeRespectsCoinsAndCap(){GameState s=new GameState();s.coins=100;assertTrue(s.buyAxeUpgrade());assertEquals(2,s.weaponLevel);assertTrue(s.buyAxeUpgrade());assertEquals(3,s.weaponLevel);assertFalse(s.buyAxeUpgrade());}
    @Test public void potionDoesNotWasteAtFullHealth(){GameState s=new GameState();assertFalse(s.usePotion());assertEquals(1,s.potions);s.health=25;assertTrue(s.usePotion());assertEquals(80,s.health);assertEquals(0,s.potions);}
    @Test public void magicRegenerationAccumulatesSubFrameTime(){GameState s=new GameState();s.heartland=0;s.nightmare=0;for(int i=0;i<60;i++)s.regenerateMagic(1f/60f);assertEquals(3,s.heartland);assertEquals(3,s.nightmare);}
    @Test public void questTransitionCannotSkipLanternEvent(){GameState s=new GameState();s.playerX=1800;s.updateStoryProgress();assertTrue(s.lanternBroken);assertEquals(GameState.QuestStage.LANTERN_BROKEN,s.questStage);s.updateStoryProgress();assertEquals(GameState.QuestStage.FOREST,s.questStage);}
}
