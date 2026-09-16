package com.potatopixelartstudios.teddygame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Pure game rules. Android-free by design so core combat/RPG logic can be unit tested. */
public final class GameState {
    public static final int MAX_HEALTH = 100;
    public static final int MAX_MAGIC = 100;
    public static final int BOSS_MAX_HEALTH = 360;
    public static final int PURIFY_HEALTH_THRESHOLD = 72;
    public static final int PURIFY_MAGIC_COST = 35;
    public static final int WORLD_END_X = 4400;

    public enum QuestStage { FESTIVAL, LANTERN_BROKEN, FOREST, BOSS, RETURN_HOME, COMPLETE }

    public static final class Enemy {
        public final String id;
        public float x, y;
        public int health;
        public final int maxHealth;
        public boolean alive = true;
        public float direction = 1f, patrolLeft, patrolRight;
        Enemy(String id, float x, float y, int health, float patrolRadius) {
            this.id=id; this.x=x; this.y=y; this.health=health; this.maxHealth=health;
            patrolLeft=x-patrolRadius; patrolRight=x+patrolRadius;
        }
    }

    private final List<Enemy> enemies = new ArrayList<>();
    public float playerX=220f, playerY=430f, velocityX, velocityY;
    public boolean facingRight=true, grounded=true;
    public int jumpsUsed;
    public int health=MAX_HEALTH, heartland=50, nightmare=50, xp, level=1, coins=10, weaponLevel=1, potions=1, heartShards;
    public QuestStage questStage=QuestStage.FESTIVAL;
    public boolean lanternBroken, bossSpawned, bossPurified, checkpointActivated, villageReturned;
    public int bossHealth=BOSS_MAX_HEALTH;
    public long lastPlayerHitMs, lastAttackMs, bossLastAttackMs;
    private float magicRegenAccumulator;
    public float bossX=4050f, bossY=410f;

    public GameState(){ resetEnemies(); }
    public void resetEnemies(){ enemies.clear(); enemies.add(new Enemy("slime",2300f,465f,45,120f)); enemies.add(new Enemy("goblin",2760f,450f,65,150f)); enemies.add(new Enemy("living_pumpkin",3260f,450f,80,140f)); }
    public List<Enemy> getEnemies(){ return Collections.unmodifiableList(enemies); }

    public boolean isPurificationAvailable(){ return bossSpawned&&!bossPurified&&bossHealth>0&&bossHealth<=PURIFY_HEALTH_THRESHOLD&&heartland>=PURIFY_MAGIC_COST&&nightmare>=PURIFY_MAGIC_COST; }
    public boolean purifyBoss(){ if(!isPurificationAvailable()) return false; heartland-=PURIFY_MAGIC_COST; nightmare-=PURIFY_MAGIC_COST; bossHealth=0; bossPurified=true; heartShards=Math.max(1,heartShards); addXp(120); coins+=30; questStage=QuestStage.RETURN_HOME; return true; }
    public void damageBoss(int damage){ if(!bossSpawned||bossPurified||damage<=0)return; bossHealth=Math.max(1,bossHealth-damage); gainMagic(3,4); }

    public int performPlayerAttack(float reach){
        if(reach<=0)return 0; int hits=0; float center=playerX+(facingRight?50f:-50f);
        for(Enemy enemy:enemies){
            if(!enemy.alive)continue;
            if(Math.abs(enemy.x-center)<=reach&&Math.abs(enemy.y-playerY)<95f){
                int damage=24+(weaponLevel-1)*7; enemy.health=Math.max(0,enemy.health-damage); hits++; gainMagic(2,4);
                if(enemy.health==0){ enemy.alive=false; addXp(enemy.maxHealth>=75?38:30); coins+=enemy.maxHealth>=75?8:5; gainMagic(8,8); }
            }
        }
        return hits;
    }
    public boolean useHeartland(){ if(heartland<20)return false; heartland-=20; health=Math.min(MAX_HEALTH,health+28); return true; }
    public int useNightmareBurst(){
        if(nightmare<20)return 0; nightmare-=20; int hits=0;
        for(Enemy enemy:enemies){ if(!enemy.alive)continue; if(Math.abs(enemy.x-playerX)<=175f){ enemy.health=Math.max(0,enemy.health-38); hits++; if(enemy.health==0){ enemy.alive=false; addXp(enemy.maxHealth>=75?38:30); coins+=enemy.maxHealth>=75?8:5; } } }
        if(bossSpawned&&!bossPurified&&Math.abs(bossX-playerX)<=220f){ damageBoss(32); hits++; }
        return hits;
    }
    public void gainMagic(int gold,int violet){ heartland=clamp(heartland+Math.max(0,gold),0,MAX_MAGIC); nightmare=clamp(nightmare+Math.max(0,violet),0,MAX_MAGIC); }
    public void regenerateMagic(float deltaSeconds){ if(deltaSeconds<=0f)return; magicRegenAccumulator+=deltaSeconds*3f; int step=(int)magicRegenAccumulator; if(step>0){ gainMagic(step,step); magicRegenAccumulator-=step; } }
    public void damagePlayer(int damage,long nowMs){ if(damage<=0||nowMs-lastPlayerHitMs<650)return; lastPlayerHitMs=nowMs; health=Math.max(0,health-damage); if(health==0)respawn(); }
    public void respawn(){ health=MAX_HEALTH; heartland=Math.max(35,heartland); nightmare=Math.max(35,nightmare); velocityX=velocityY=0f; playerX=questStage.ordinal()>=QuestStage.FOREST.ordinal()?(checkpointActivated?2100f:1800f):220f; playerY=430f; grounded=true; jumpsUsed=0; }
    public void addXp(int amount){ if(amount<=0)return; xp+=amount; while(xp>=xpForNextLevel()){ xp-=xpForNextLevel(); level++; health=MAX_HEALTH; gainMagic(20,20); } }
    public int xpForNextLevel(){ return 100+(level-1)*35; }
    public boolean buyAxeUpgrade(){ int cost=weaponLevel*15; if(weaponLevel>=3||coins<cost)return false; coins-=cost; weaponLevel++; return true; }
    public boolean usePotion(){ if(potions<=0||health>=MAX_HEALTH)return false; potions--; health=Math.min(MAX_HEALTH,health+55); return true; }

    public void updateStoryProgress(){
        if(!lanternBroken&&questStage==QuestStage.FESTIVAL&&playerX>=1000f){ lanternBroken=true; questStage=QuestStage.LANTERN_BROKEN; return; }
        if(questStage==QuestStage.LANTERN_BROKEN&&playerX>=1700f){ questStage=QuestStage.FOREST; checkpointActivated=true; return; }
        if(!bossSpawned&&questStage==QuestStage.FOREST&&playerX>=3650f){ bossSpawned=true; questStage=QuestStage.BOSS; return; }
        if(bossPurified&&playerX<=1720f&&questStage==QuestStage.RETURN_HOME){ villageReturned=true; questStage=QuestStage.COMPLETE; }
    }
    public static int clamp(int value,int min,int max){ return Math.max(min,Math.min(max,value)); }
}
