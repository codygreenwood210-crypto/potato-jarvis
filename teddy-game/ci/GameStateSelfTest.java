import com.potatopixelartstudios.teddygame.GameState;
public final class GameStateSelfTest {
    private static int checks;
    private static void ok(boolean v,String m){checks++;if(!v)throw new AssertionError(m);}
    public static void main(String[] args){
        GameState s=new GameState();s.bossSpawned=true;s.bossHealth=GameState.PURIFY_HEALTH_THRESHOLD;s.heartland=35;s.nightmare=35;
        ok(s.isPurificationAvailable(),"purify boundary");ok(s.purifyBoss(),"purify succeeds");ok(s.bossPurified&&s.heartShards==1,"boss saved and shard granted");
        GameState q=new GameState();q.playerX=1800;q.updateStoryProgress();ok(q.questStage==GameState.QuestStage.LANTERN_BROKEN,"story does not skip lantern");q.updateStoryProgress();ok(q.questStage==GameState.QuestStage.FOREST,"forest follows lantern");
        GameState r=new GameState();r.heartland=0;r.nightmare=0;for(int i=0;i<60;i++)r.regenerateMagic(1f/60f);ok(r.heartland==3&&r.nightmare==3,"sub-frame regen accumulates");
        GameState u=new GameState();u.coins=100;ok(u.buyAxeUpgrade(),"upgrade1");ok(u.buyAxeUpgrade(),"upgrade2");ok(!u.buyAxeUpgrade(),"upgrade cap");
        GameState h=new GameState();h.health=10;ok(h.usePotion()&&h.health==65,"potion behavior");
        System.out.println("GameStateSelfTest PASS — "+checks+" assertions");
    }
}
