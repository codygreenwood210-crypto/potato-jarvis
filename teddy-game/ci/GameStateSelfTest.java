import com.potatopixelartstudios.teddygame.GameState;

public final class GameStateSelfTest {
    private static int checks;
    private static void ok(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        GameState s = new GameState();
        s.bossSpawned = true;
        s.bossHealth = GameState.PURIFY_HEALTH_THRESHOLD;
        s.heartland = 35;
        s.nightmare = 35;
        ok(s.isPurificationAvailable(), "purify boundary");
        ok(s.purifyBoss(), "purify succeeds");
        ok(s.bossPurified && s.heartShards == 1, "boss saved and shard granted");

        GameState q = new GameState();
        q.playerX = 1800;
        q.updateStoryProgress();
        ok(q.questStage == GameState.QuestStage.LANTERN_BROKEN, "story does not skip lantern");
        q.updateStoryProgress();
        ok(q.questStage == GameState.QuestStage.LANTERN_BROKEN, "forest blocked until rescue");
        ok(q.rescueVillager(), "villager rescue succeeds");
        q.updateStoryProgress();
        ok(q.questStage == GameState.QuestStage.FOREST, "forest follows rescue");

        GameState r = new GameState();
        r.heartland = 0;
        r.nightmare = 0;
        for (int i = 0; i < 60; i++) r.regenerateMagic(1f / 60f);
        ok(r.heartland == 3 && r.nightmare == 3, "sub-frame regen accumulates");

        GameState u = new GameState();
        u.coins = 100;
        ok(u.buyAxeUpgrade(), "upgrade1");
        ok(u.buyAxeUpgrade(), "upgrade2");
        ok(!u.buyAxeUpgrade(), "upgrade cap");

        GameState h = new GameState();
        h.health = 10;
        ok(h.usePotion() && h.health == 65, "potion behavior");
        h.health = 30;
        int before = h.coins;
        ok(h.visitMummyHealer() && h.health == GameState.MAX_HEALTH && h.coins == before - 5, "healer behavior");

        GameState d = new GameState();
        ok(d.tryDodge(1000), "dodge starts");
        d.damagePlayer(50, 1100);
        ok(d.health == GameState.MAX_HEALTH, "dodge invulnerability");
        ok(!d.tryDodge(1200), "dodge cooldown");
        ok(d.tryDodge(1700), "dodge cooldown expires");

        GameState secret = new GameState();
        int coins = secret.coins;
        ok(secret.discoverSecret(), "secret reward starts");
        ok(secret.coins == coins + 12 && secret.potions == 2, "secret reward granted");
        ok(!secret.discoverSecret(), "secret reward one-time");

        GameState home = new GameState();
        home.bossPurified = true;
        home.questStage = GameState.QuestStage.RETURN_HOME;
        home.playerX = 1600;
        home.updateStoryProgress();
        ok(home.villageReturned, "changed hub activates on return");
        ok(home.questStage == GameState.QuestStage.RETURN_HOME, "changed hub remains playable");
        home.playerX = 250;
        home.updateStoryProgress();
        ok(home.questStage == GameState.QuestStage.COMPLETE, "slice completes in town square");

        System.out.println("GameStateSelfTest PASS — " + checks + " assertions");
    }
}
