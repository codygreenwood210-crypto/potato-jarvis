package com.potatopixelartstudios.teddygame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

/**
 * Canvas-driven Android vertical slice: Moonstitch hub, platforming, combat,
 * persistent RPG progression and a multi-pattern Pumpkin King boss.
 */
public final class GameView extends View {
    private static final float VW = 1280f;
    private static final float VH = 720f;
    private static final float GROUND = 540f;
    private static final float PLAYER_H = 96f;
    private static final float GRAVITY = 1550f;
    private static final float SPEED = 330f;
    private static final float JUMP = -610f;

    // World-space side-route platforms in the Whispering Forest.
    private static final float[][] PLATFORMS = {
            {2140f, 395f, 240f, 24f},
            {2500f, 330f, 210f, 24f},
            {2850f, 280f, 210f, 24f},
            {3160f, 225f, 250f, 24f}
    };

    private enum Screen { TITLE, INTRO, PLAYING, COMPLETE }
    private enum Control { LEFT, RIGHT, JUMP, DODGE, ATTACK, GOLD, VIOLET, BALANCE, TALK, POTION }

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pixel = new Paint();
    private final GameState s = new GameState();
    private final SaveManager save;
    private final AudioController audio;
    private final Map<Integer, Control> held = new HashMap<>();
    private final Map<Integer, Bitmap> images = new HashMap<>();

    private Screen screen = Screen.TITLE;
    private boolean paused, released, lanternShown, forestShown, returnShown, bossRoared;
    private long lastNs, attackUntil, magicUntil, storyUntil, hurtUntil, dodgeVisualUntil;
    private String story = "";
    private float cam;

    // Boss pattern state: every attack has a visible telegraph before it resolves.
    private int bossPattern = -1;
    private long bossTelegraphUntil, bossProjectileUntil;
    private float bossTargetX, bossProjectileX, bossProjectileVx;

    GameView(Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setContentDescription("Teddy Game playable area");
        pixel.setAntiAlias(false);
        save = new SaveManager(context);
        audio = new AudioController(context);
        loadImages();
        requestFocus();
    }

    private void loadImages() {
        int[] ids = {
                R.drawable.bg_village, R.drawable.bg_forest,
                R.drawable.hero_idle, R.drawable.hero_run_1, R.drawable.hero_run_2,
                R.drawable.hero_jump, R.drawable.hero_fall, R.drawable.hero_attack,
                R.drawable.hero_golden, R.drawable.hero_nightmare, R.drawable.hero_heal,
                R.drawable.npc_witch_guide, R.drawable.npc_ghost_child,
                R.drawable.npc_skeleton_blacksmith, R.drawable.npc_vampire_merchant,
                R.drawable.enemy_slime, R.drawable.enemy_goblin, R.drawable.enemy_living_pumpkin,
                R.drawable.boss_pumpkin_king, R.drawable.item_heart_shard,
                R.drawable.prop_quest_board, R.drawable.interactable_checkpoint,
                R.drawable.prop_magic_lamp, R.drawable.prop_pumpkin,
                R.drawable.tile_grass, R.drawable.tile_bridge
        };
        for (int id : ids) {
            Bitmap b = BitmapFactory.decodeResource(getResources(), id);
            if (b != null) images.put(id, b);
        }
    }

    void resumeGame() {
        paused = false;
        lastNs = System.nanoTime();
        postInvalidateOnAnimation();
    }

    void pauseAndSave() {
        if (screen == Screen.PLAYING || screen == Screen.COMPLETE) save.save(s);
        paused = true;
        held.clear();
    }

    void release() {
        if (released) return;
        released = true;
        audio.release();
        for (Bitmap b : images.values()) if (!b.isRecycled()) b.recycle();
        images.clear();
    }

    @Override
    protected void onDraw(Canvas out) {
        super.onDraw(out);
        if (released) return;
        long nowNs = System.nanoTime();
        long now = nowNs / 1_000_000L;
        float dt = lastNs == 0 ? 0f : Math.min(.033f, (nowNs - lastNs) / 1_000_000_000f);
        lastNs = nowNs;
        if (screen == Screen.PLAYING && !paused) update(dt, now);

        float sc = Math.min(getWidth() / VW, getHeight() / VH);
        float ox = (getWidth() - VW * sc) / 2f;
        float oy = (getHeight() - VH * sc) / 2f;
        out.save();
        out.translate(ox, oy);
        out.scale(sc, sc);
        drawVirtual(out, now);
        out.restore();
        postInvalidateOnAnimation();
    }

    private void update(float dt, long now) {
        boolean left = pressed(Control.LEFT), right = pressed(Control.RIGHT);
        boolean dodging = now < dodgeVisualUntil;
        if (!dodging) {
            float target = (left ^ right) ? (left ? -SPEED : SPEED) : 0f;
            s.velocityX += (target - s.velocityX) * Math.min(1f, dt * 12f);
            if (target != 0) s.facingRight = target > 0;
        }

        s.playerX = clampf(s.playerX + s.velocityX * dt, 20, GameState.WORLD_END_X - 100);
        // The shattered-rift tutorial gate makes the rescue objective real gameplay.
        if (s.lanternBroken && !s.rescuedVillager && s.playerX > 1660f) {
            s.playerX = 1660f;
            s.velocityX = Math.min(0f, s.velocityX);
        }

        float previousY = s.playerY;
        s.velocityY += GRAVITY * dt;
        s.playerY += s.velocityY * dt;
        resolveVerticalCollision(previousY);

        updateEnemies(dt, now);
        updateBoss(dt, now);
        s.updateStoryProgress();
        s.regenerateMagic(dt);
        updateSecret(now);
        storyEvents(now);
        cam = clampf(s.playerX - 430, 0, GameState.WORLD_END_X - VW);
        if (s.questStage == GameState.QuestStage.COMPLETE) {
            save.save(s);
            screen = Screen.COMPLETE;
        }
    }

    private void resolveVerticalCollision(float previousY) {
        boolean landed = false;
        if (s.velocityY >= 0 && s.playerX >= 1950f) {
            float previousBottom = previousY + PLAYER_H;
            float newBottom = s.playerY + PLAYER_H;
            for (float[] platform : PLATFORMS) {
                float left = platform[0], top = platform[1], right = left + platform[2];
                if (s.playerX + 34f >= left && s.playerX - 34f <= right
                        && previousBottom <= top + 8f && newBottom >= top) {
                    s.playerY = top - PLAYER_H;
                    s.velocityY = 0f;
                    s.grounded = true;
                    s.jumpsUsed = 0;
                    landed = true;
                    break;
                }
            }
        }
        if (!landed && s.playerY >= GROUND - PLAYER_H) {
            s.playerY = GROUND - PLAYER_H;
            s.velocityY = 0f;
            s.grounded = true;
            s.jumpsUsed = 0;
            landed = true;
        }
        if (!landed) s.grounded = false;
    }

    private void updateEnemies(float dt, long now) {
        for (GameState.Enemy e : s.getEnemies()) {
            if (!e.alive) continue;
            if (Math.abs(e.x - s.playerX) < 320) {
                e.direction = e.x < s.playerX ? 1 : -1;
                e.x += e.direction * 70 * dt;
                if (Math.abs(e.x - s.playerX) < 62) {
                    int before = s.health;
                    s.damagePlayer(e.maxHealth >= 75 ? 15 : 10, now);
                    if (s.health < before) {
                        hurtUntil = now + 180;
                        audio.play(AudioController.Cue.HURT);
                    }
                }
            } else {
                e.x += e.direction * 36 * dt;
                if (e.x <= e.patrolLeft || e.x >= e.patrolRight) e.direction *= -1;
            }
        }
    }

    private void updateBoss(float dt, long now) {
        if (!s.bossSpawned || s.bossPurified) return;
        if (!bossRoared) {
            bossRoared = true;
            s.bossLastAttackMs = now;
            audio.play(AudioController.Cue.BOSS);
            card("NIGHTMARE-SURGED PUMPKIN KING\n\"Leave. Can't control it... RUN!\"", 2600, now);
        }

        if (bossProjectileUntil > now) {
            bossProjectileX += bossProjectileVx * dt;
            if (Math.abs(bossProjectileX - s.playerX) < 52f && s.playerY > 330f) {
                int before = s.health;
                s.damagePlayer(16, now);
                bossProjectileUntil = 0;
                if (s.health < before) {
                    hurtUntil = now + 220;
                    audio.play(AudioController.Cue.HURT);
                }
            }
        }

        if (bossTelegraphUntil > 0) {
            if (now >= bossTelegraphUntil) {
                resolveBossAttack(now);
                bossTelegraphUntil = 0;
                s.bossLastAttackMs = now;
            }
            return;
        }

        float dx = s.playerX - s.bossX;
        if (Math.abs(dx) > 125) s.bossX += Math.signum(dx) * 68 * dt;
        if (now - s.bossLastAttackMs >= 1500) {
            bossPattern = (bossPattern + 1) % 3;
            bossTargetX = s.playerX;
            bossTelegraphUntil = now + 680;
            audio.play(AudioController.Cue.BOSS);
        }
    }

    private void resolveBossAttack(long now) {
        if (bossPattern == 0) {
            float delta = clampf(bossTargetX - s.bossX, -330f, 330f);
            s.bossX = clampf(s.bossX + delta, 3670f, 4270f);
            bossHitIfNear(135f, 24, now);
        } else if (bossPattern == 1) {
            if (Math.abs(s.playerX - s.bossX) < 290f && s.playerY > 360f) bossDamagePlayer(22, now);
        } else {
            bossProjectileX = s.bossX;
            bossProjectileVx = bossTargetX < s.bossX ? -430f : 430f;
            bossProjectileUntil = now + 1750;
        }
    }

    private void bossHitIfNear(float reach, int damage, long now) {
        if (Math.abs(s.playerX - s.bossX) < reach) bossDamagePlayer(damage, now);
    }

    private void bossDamagePlayer(int damage, long now) {
        int before = s.health;
        s.damagePlayer(damage, now);
        if (s.health < before) {
            hurtUntil = now + 240;
            audio.play(AudioController.Cue.HURT);
        }
    }

    private void updateSecret(long now) {
        if (!s.secretFound && s.questStage.ordinal() >= GameState.QuestStage.FOREST.ordinal()
                && Math.abs(s.playerX - 3280f) < 85f && s.playerY < 290f) {
            if (s.discoverSecret()) {
                card("SECRET: MOONSTITCH CACHE\nExploration pays off: +12 coins, +1 potion, +25 XP.", 2800, now);
                audio.play(AudioController.Cue.ITEM);
                save.save(s);
            }
        }
    }

    private void storyEvents(long now) {
        if (s.lanternBroken && !lanternShown) {
            lanternShown = true;
            card("THE NIGHT THE LIGHT BROKE\nThe Heart Lantern shatters. Golden and violet shards tear across the sky.\nA voice whispers: \"At last.\"", 3900, now);
            audio.play(AudioController.Cue.MAGIC);
            save.save(s);
        }
        if (s.questStage == GameState.QuestStage.FOREST && !forestShown) {
            forestShown = true;
            card("WHISPERING FOREST\nThese creatures are overwhelmed, not evil. Find the first Heart Shard.\nLook upward: the forest rewards platforming and exploration.", 3600, now);
            audio.play(AudioController.Cue.CHECKPOINT);
            save.save(s);
        }
        if (s.bossPurified && !returnShown) {
            returnShown = true;
            card("BALANCE\nGold gives chaos direction. Violet gives it force.\nThe Pumpkin King is free — bring the Heart Shard home.", 4200, now);
            audio.play(AudioController.Cue.QUEST);
            save.save(s);
        }
    }

    private void card(String text, long ms, long now) {
        story = text;
        storyUntil = now + ms;
    }

    private void drawVirtual(Canvas c, long now) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.rgb(18, 12, 28));
        c.drawRect(0, 0, VW, VH, p);
        if (screen == Screen.TITLE) drawTitle(c);
        else if (screen == Screen.INTRO) drawIntro(c);
        else if (screen == Screen.PLAYING) drawWorld(c, now);
        else drawComplete(c);
    }

    private void drawTitle(Canvas c) {
        background(c, R.drawable.bg_village);
        shade(c, 190);
        txt(c, "TEDDY GAME", 640, 175, 68, Color.rgb(246, 205, 89), true);
        txt(c, "2D SIDE-SCROLLING RPG ACTION-PLATFORMER", 640, 230, 25, Color.WHITE, true);
        txt(c, "TWO SIDES. ONE HEART.", 640, 278, 30, Color.rgb(205, 110, 235), true);
        sprite(c, R.drawable.hero_idle, 564, 315, 152, 152, false);
        button(c, 440, 505, 840, 575, save.hasSave() ? "CONTINUE" : "BEGIN STORY", true);
        if (save.hasSave()) button(c, 490, 595, 790, 650, "NEW GAME", false);
        txt(c, "Potato Pixel Art Studios • Android Vertical Slice v0.2", 640, 690, 18, Color.LTGRAY, false);
    }

    private void drawIntro(Canvas c) {
        background(c, R.drawable.bg_village);
        shade(c, 205);
        txt(c, "MOONSTITCH HOLLOW", 640, 145, 42, Color.rgb(246, 205, 89), true);
        String[] lines = {
                "Between the Heartlands and Nightmare Realm stands a village where strange creatures live together.",
                "The Heart Lantern burns with one golden flame and one violet flame — neither complete alone.",
                "Teddy carries both powers in one stitched heart. Nightmare is chaos and instinct, not evil.",
                "Tonight is Moonstitch Hollow's greatest Halloween celebration.",
                "Explore, talk, jump, fight and protect the village."
        };
        float y = 215;
        for (String line : lines) {
            txt(c, line, 640, y, 23, Color.WHITE, false);
            y += 62;
        }
        button(c, 470, 535, 810, 600, "ENTER THE FESTIVAL", true);
    }

    private void drawWorld(Canvas c, long now) {
        background(c, s.playerX < 1900 ? R.drawable.bg_village : R.drawable.bg_forest);
        p.setColor(s.playerX < 1900 ? Color.rgb(66, 48, 54) : Color.rgb(48, 68, 48));
        c.drawRect(0, GROUND, VW, VH, p);
        Bitmap tile = images.get(R.drawable.tile_grass);
        if (tile != null) {
            for (int x = -64; x < VW + 64; x += 64)
                c.drawBitmap(tile, null, new RectF(x, GROUND - 4, x + 64, GROUND + 60), pixel);
        }

        drawPlatforms(c);
        drawWorldProps(c, now);
        drawNpcs(c);
        drawEnemies(c);
        drawBoss(c, now);
        drawPlayer(c, now);
        drawHud(c);
        controls(c);
        if (paused) pausePanel(c);
        if (storyUntil > now) storyCard(c);
    }

    private void drawPlatforms(Canvas c) {
        for (float[] platform : PLATFORMS) {
            float sx = platform[0] - cam;
            if (sx > VW || sx + platform[2] < 0) continue;
            p.setColor(Color.rgb(83, 61, 87));
            c.drawRoundRect(new RectF(sx, platform[1], sx + platform[2], platform[1] + platform[3]), 6, 6, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(3);
            p.setColor(Color.rgb(178, 111, 61));
            c.drawLine(sx + 8, platform[1] + 8, sx + platform[2] - 8, platform[1] + 8, p);
            p.setStyle(Paint.Style.FILL);
        }
    }

    private void drawWorldProps(Canvas c, long now) {
        worldSprite(c, R.drawable.prop_quest_board, 430, 440, 72, 88);
        worldSprite(c, R.drawable.prop_magic_lamp, 1080, 395, 96, 128);
        worldSprite(c, R.drawable.interactable_checkpoint, 1880, 430, 86, 100);
        for (float x : new float[]{2200, 2520, 3020, 3440}) worldSprite(c, R.drawable.prop_pumpkin, x, 475, 58, 58);

        if (s.lanternBroken && !s.rescuedVillager) {
            worldSprite(c, R.drawable.prop_pumpkin, 1450, 455, 70, 70);
            float x = 1450 - cam;
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(5);
            p.setColor(Color.rgb(170, 92, 190));
            c.drawRect(x - 45, 415, x + 45, 535, p);
            p.setStyle(Paint.Style.FILL);
            worldLabel(c, "TRAPPED PUMPKIN FOLK • TALK", 1450, 395, Color.rgb(250, 220, 115));
        }
        if (s.lanternBroken && !s.rescuedVillager) {
            float x = 1680 - cam;
            p.setColor(Color.argb(130, 151, 62, 190));
            c.drawRect(x - 18, 180, x + 18, GROUND, p);
            txt(c, "RESCUE FIRST", x, 165, 15, Color.WHITE, true);
        }
        if (!s.secretFound && s.playerX > 2600f) {
            worldSprite(c, R.drawable.item_heart_shard, 3280, 155, 48, 48);
            worldLabel(c, "?", 3280, 145, Color.rgb(246, 205, 89));
        }
        if (s.villageReturned && s.playerX < 1900f) {
            float x = 1080 - cam;
            p.setColor(Color.argb(95, 245, 197, 73));
            c.drawCircle(x - 15, 375, 62, p);
            p.setColor(Color.argb(95, 171, 79, 212));
            c.drawCircle(x + 20, 375, 62, p);
            worldLabel(c, "THE HOLLOW IS CHANGING", 1080, 350, Color.WHITE);
        }
    }

    private void drawNpcs(Canvas c) {
        worldSprite(c, R.drawable.npc_ghost_child, 360, 432, 92, 96);
        worldSprite(c, R.drawable.npc_skeleton_blacksmith, 610, 428, 96, 100);
        worldSprite(c, R.drawable.npc_vampire_merchant, 800, 428, 96, 100);
        worldSprite(c, R.drawable.npc_witch_guide, 930, 426, 98, 102);
        drawMummyHealer(c, 1230, 438);

        if (Math.abs(s.playerX - 360) < 115) worldLabel(c, "Ghost Child • TALK", 360, 405, Color.WHITE);
        if (Math.abs(s.playerX - 610) < 115) worldLabel(c, "Blacksmith • UPGRADE", 610, 402, Color.rgb(246, 205, 89));
        if (Math.abs(s.playerX - 800) < 115) worldLabel(c, "Vampire Merchant • SHOP", 800, 402, Color.WHITE);
        if (Math.abs(s.playerX - 930) < 120) worldLabel(c, "Witch Guide • TALK", 930, 398, Color.rgb(205, 110, 235));
        if (Math.abs(s.playerX - 1230) < 125) worldLabel(c, "Mummy Healer • 5 COINS", 1230, 405, Color.rgb(238, 217, 171));
    }

    private void drawMummyHealer(Canvas c, float wx, float y) {
        float x = wx - cam;
        if (x < -80 || x > VW + 80) return;
        p.setColor(Color.rgb(218, 205, 169));
        c.drawRoundRect(new RectF(x - 32, y, x + 32, y + 88), 18, 18, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(5);
        p.setColor(Color.rgb(111, 86, 114));
        for (int i = 0; i < 5; i++) c.drawLine(x - 28, y + 15 + i * 14, x + 28, y + 8 + i * 14, p);
        p.setStyle(Paint.Style.FILL);
        p.setColor(Color.rgb(246, 205, 89));
        c.drawCircle(x - 12, y + 25, 4, p);
        c.drawCircle(x + 12, y + 25, 4, p);
    }

    private void drawEnemies(Canvas c) {
        for (GameState.Enemy e : s.getEnemies()) {
            if (!e.alive) continue;
            int id = e.id.equals("slime") ? R.drawable.enemy_slime
                    : e.id.equals("goblin") ? R.drawable.enemy_goblin : R.drawable.enemy_living_pumpkin;
            worldSprite(c, id, e.x, e.y, 84, 84);
            bar(c, e.x - cam - 38, e.y - 24, 76, 7, e.health / (float) e.maxHealth, Color.rgb(224, 78, 92));
        }
    }

    private void drawBoss(Canvas c, long now) {
        if (!s.bossSpawned || s.bossPurified) return;
        worldSprite(c, R.drawable.boss_pumpkin_king, s.bossX, s.bossY - 40, 150, 150);
        txt(c, "OVERWHELMED PUMPKIN KING", 640, 92, 24, Color.WHITE, true);
        bar(c, 340, 108, 600, 18, s.bossHealth / (float) GameState.BOSS_MAX_HEALTH, Color.rgb(208, 73, 93));
        if (s.isPurificationAvailable()) txt(c, "THE KING IS READY — USE BALANCE", 640, 150, 25, Color.rgb(251, 219, 103), true);

        if (bossTelegraphUntil > now) {
            float bx = s.bossX - cam;
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(7);
            if (bossPattern == 0) {
                p.setColor(Color.argb(210, 236, 91, 70));
                c.drawLine(bx, 470, bossTargetX - cam, 470, p);
                txt(c, "CHARGE — DODGE!", 640, 185, 22, Color.rgb(255, 181, 145), true);
            } else if (bossPattern == 1) {
                p.setColor(Color.argb(210, 246, 205, 89));
                c.drawCircle(bx, GROUND - 6, 285, p);
                txt(c, "ROOT SLAM — JUMP!", 640, 185, 22, Color.rgb(255, 232, 148), true);
            } else {
                p.setColor(Color.argb(220, 185, 91, 224));
                c.drawLine(bx, 455, bossTargetX - cam, 455, p);
                txt(c, "NIGHTMARE SEED — MOVE!", 640, 185, 22, Color.rgb(226, 151, 250), true);
            }
            p.setStyle(Paint.Style.FILL);
        }
        if (bossProjectileUntil > now) {
            float x = bossProjectileX - cam;
            p.setColor(Color.rgb(151, 63, 190));
            c.drawCircle(x, 460, 22, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(5);
            p.setColor(Color.rgb(246, 151, 67));
            c.drawCircle(x, 460, 28, p);
            p.setStyle(Paint.Style.FILL);
        }
    }

    private void drawPlayer(Canvas c, long now) {
        int id;
        if (now < attackUntil) id = R.drawable.hero_attack;
        else if (now < magicUntil) id = s.nightmare >= s.heartland ? R.drawable.hero_nightmare : R.drawable.hero_golden;
        else if (!s.grounded) id = s.velocityY < 0 ? R.drawable.hero_jump : R.drawable.hero_fall;
        else if (Math.abs(s.velocityX) > 40) id = ((now / 150) % 2 == 0) ? R.drawable.hero_run_1 : R.drawable.hero_run_2;
        else id = R.drawable.hero_idle;

        float x = s.playerX - cam;
        if (!(now < hurtUntil && (now / 60) % 2 == 0)) sprite(c, id, x - 44, s.playerY - 10, 96, 96, !s.facingRight);
        if (now < dodgeVisualUntil) {
            p.setColor(Color.argb(75, s.facingRight ? 246 : 174, 120, 226));
            c.drawOval(new RectF(x - 72, s.playerY + 10, x + 65, s.playerY + 82), p);
        }
        if (now < magicUntil) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(4);
            p.setColor(Color.rgb(211, 94, 233));
            c.drawCircle(x + 4, s.playerY + 42, 63, p);
            p.setStyle(Paint.Style.FILL);
        }
    }

    private void drawHud(Canvas c) {
        p.setColor(Color.argb(195, 16, 10, 28));
        c.drawRoundRect(new RectF(20, 18, 500, 140), 18, 18, p);
        txtL(c, "LV " + s.level + "  AXE +" + s.weaponLevel + "  COINS " + s.coins + "  SHARDS " + s.heartShards, 40, 48, 19, Color.WHITE, true);
        bar(c, 40, 62, 280, 14, s.health / 100f, Color.rgb(211, 65, 82));
        txtL(c, "HP " + s.health, 330, 75, 17, Color.WHITE, false);
        bar(c, 40, 88, 180, 10, s.heartland / 100f, Color.rgb(241, 194, 67));
        txtL(c, "HEARTLAND " + s.heartland, 230, 98, 15, Color.rgb(250, 220, 115), true);
        bar(c, 40, 110, 180, 10, s.nightmare / 100f, Color.rgb(160, 72, 199));
        txtL(c, "NIGHTMARE " + s.nightmare, 230, 120, 15, Color.rgb(218, 130, 239), true);
        txtL(c, objective(), 650, 42, 19, Color.WHITE, true);
        txtL(c, "XP " + s.xp + "/" + s.xpForNextLevel() + (s.secretFound ? "   SECRET ✓" : ""), 650, 70, 17, Color.LTGRAY, false);
    }

    private String objective() {
        switch (s.questStage) {
            case FESTIVAL: return "QUEST: Reach the Heart Lantern";
            case LANTERN_BROKEN: return s.rescuedVillager ? "QUEST: Follow the Heart Shard trail" : "QUEST: Rescue the trapped villager";
            case FOREST: return "QUEST: Cross the Whispering Forest";
            case BOSS: return s.isPurificationAvailable() ? "QUEST: Stabilize the Pumpkin King" : "QUEST: Learn his patterns and weaken him";
            case RETURN_HOME: return "QUEST: Carry the Heart Shard through changed Moonstitch";
            default: return "QUEST COMPLETE: First Heart Shard";
        }
    }

    private void controls(Canvas c) {
        circle(c, 95, 620, 65, "◀", Control.LEFT, Color.rgb(73, 59, 88));
        circle(c, 235, 620, 65, "▶", Control.RIGHT, Color.rgb(73, 59, 88));
        circle(c, 1120, 610, 62, "JUMP", Control.JUMP, Color.rgb(73, 101, 125));
        circle(c, 975, 620, 58, "AXE", Control.ATTACK, Color.rgb(123, 75, 60));
        circle(c, 840, 620, 50, "DODGE", Control.DODGE, Color.rgb(68, 93, 113));
        circle(c, 1115, 465, 54, "GOLD", Control.GOLD, Color.rgb(173, 137, 50));
        circle(c, 995, 475, 54, "VIOLET", Control.VIOLET, Color.rgb(112, 54, 139));
        circle(c, 862, 500, 58, "BALANCE", Control.BALANCE, s.isPurificationAvailable() ? Color.rgb(182, 82, 137) : Color.rgb(65, 51, 76));
        circle(c, 700, 620, 48, "TALK", Control.TALK, Color.rgb(63, 92, 84));
        circle(c, 575, 625, 45, "POTION", Control.POTION, Color.rgb(117, 59, 94));
        button(c, 1198, 22, 1262, 72, "II", false);
    }

    private void pausePanel(Canvas c) {
        shadeRect(c, 250, 110, 1030, 610, 230);
        txt(c, "PAUSED", 640, 185, 44, Color.rgb(246, 205, 89), true);
        txt(c, "Teddy is one person. Gold and violet are both his.", 640, 238, 22, Color.WHITE, false);
        txt(c, objective(), 640, 285, 20, Color.WHITE, true);
        txt(c, "INVENTORY", 640, 335, 22, Color.rgb(205, 110, 235), true);
        txt(c, "Axe +" + s.weaponLevel + "   Potions " + s.potions + "   Heart Shards " + s.heartShards + "   Coins " + s.coins, 640, 372, 19, Color.LTGRAY, false);
        txt(c, s.secretFound ? "Relic: Moonstitch Cache Token" : "Relic slot: undiscovered", 640, 405, 17, Color.LTGRAY, false);
        button(c, 445, 445, 835, 510, "RESUME", true);
        button(c, 445, 530, 835, 590, "SAVE & TITLE", false);
    }

    private void storyCard(Canvas c) {
        shadeRect(c, 150, 160, 1130, 385, 230);
        String[] lines = story.split("\\n");
        float y = 215;
        for (int i = 0; i < lines.length; i++) {
            txt(c, lines[i], 640, y, i == 0 ? 29 : 20, i == 0 ? Color.rgb(246, 205, 89) : Color.WHITE, i == 0);
            y += i == 0 ? 52 : 36;
        }
    }

    private void drawComplete(Canvas c) {
        background(c, R.drawable.bg_village);
        shade(c, 205);
        sprite(c, R.drawable.item_heart_shard, 570, 95, 140, 140, false);
        txt(c, "FIRST HEART SHARD RESTORED", 640, 285, 42, Color.rgb(246, 205, 89), true);
        txt(c, "The Pumpkin King was saved by Heartland focus and Nightmare force working together.", 640, 340, 21, Color.WHITE, false);
        txt(c, "Moonstitch Hollow changed because of Teddy's actions — and remembers them.", 640, 385, 24, Color.rgb(210, 118, 234), true);
        txt(c, "LV " + s.level + " • Axe +" + s.weaponLevel + " • Secret " + (s.secretFound ? "found" : "still hidden"), 640, 435, 19, Color.LTGRAY, false);
        button(c, 430, 500, 850, 570, "ROAM MOONSTITCH", true);
        button(c, 480, 590, 800, 648, "TITLE", false);
    }

    private void background(Canvas c, int id) {
        Bitmap b = images.get(id);
        if (b != null) c.drawBitmap(b, new Rect(0, 0, b.getWidth(), b.getHeight()), new RectF(0, 0, VW, VH), pixel);
    }

    private void shade(Canvas c, int a) {
        p.setColor(Color.argb(a, 15, 9, 24));
        c.drawRect(0, 0, VW, VH, p);
    }

    private void shadeRect(Canvas c, float l, float t, float r, float b, int a) {
        p.setColor(Color.argb(a, 15, 9, 24));
        c.drawRoundRect(new RectF(l, t, r, b), 18, 18, p);
    }

    private void worldSprite(Canvas c, int id, float wx, float y, float w, float h) {
        float x = wx - cam;
        if (x < -w || x > VW + w) return;
        sprite(c, id, x - w / 2, y, w, h, false);
    }

    private void worldLabel(Canvas c, String label, float wx, float y, int color) {
        float x = wx - cam;
        if (x < -180 || x > VW + 180) return;
        txt(c, label, x, y, 15, color, true);
    }

    private void sprite(Canvas c, int id, float x, float y, float w, float h, boolean flip) {
        Bitmap b = images.get(id);
        if (b == null) return;
        RectF d = new RectF(x, y, x + w, y + h);
        if (flip) {
            c.save();
            c.scale(-1, 1, x + w / 2, y + h / 2);
            c.drawBitmap(b, null, d, pixel);
            c.restore();
        } else c.drawBitmap(b, null, d, pixel);
    }

    private void bar(Canvas c, float x, float y, float w, float h, float q, int color) {
        q = clampf(q, 0, 1);
        p.setColor(Color.argb(210, 28, 19, 35));
        c.drawRoundRect(new RectF(x, y, x + w, y + h), h / 2, h / 2, p);
        p.setColor(color);
        c.drawRoundRect(new RectF(x, y, x + w * q, y + h), h / 2, h / 2, p);
    }

    private void txt(Canvas c, String text, float x, float y, float size, int color, boolean bold) {
        p.setTextSize(size);
        p.setTextAlign(Paint.Align.CENTER);
        p.setColor(color);
        p.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        c.drawText(text, x, y, p);
    }

    private void txtL(Canvas c, String text, float x, float y, float size, int color, boolean bold) {
        p.setTextSize(size);
        p.setTextAlign(Paint.Align.LEFT);
        p.setColor(color);
        p.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        c.drawText(text, x, y, p);
    }

    private void button(Canvas c, float l, float t, float r, float b, String text, boolean hi) {
        p.setColor(hi ? Color.rgb(111, 50, 142) : Color.rgb(47, 37, 58));
        c.drawRoundRect(new RectF(l, t, r, b), 16, 16, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(3);
        p.setColor(hi ? Color.rgb(244, 190, 74) : Color.rgb(104, 83, 121));
        c.drawRoundRect(new RectF(l, t, r, b), 16, 16, p);
        p.setStyle(Paint.Style.FILL);
        txt(c, text, (l + r) / 2, (t + b) / 2 + 8, 22, Color.WHITE, true);
    }

    private void circle(Canvas c, float x, float y, float radius, String text, Control ctl, int color) {
        int fill = pressed(ctl)
                ? Color.rgb(Math.min(255, Color.red(color) + 38), Math.min(255, Color.green(color) + 38), Math.min(255, Color.blue(color) + 38))
                : color;
        p.setColor(Color.argb(215, Color.red(fill), Color.green(fill), Color.blue(fill)));
        c.drawCircle(x, y, radius, p);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(3);
        p.setColor(Color.argb(190, 240, 232, 248));
        c.drawCircle(x, y, radius, p);
        p.setStyle(Paint.Style.FILL);
        txt(c, text, x, y + 7, text.length() > 5 ? 13 : 19, Color.WHITE, true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float sc = Math.min(getWidth() / VW, getHeight() / VH);
        if (sc <= 0f) return false;
        float ox = (getWidth() - VW * sc) / 2f;
        float oy = (getHeight() - VH * sc) / 2f;
        int action = e.getActionMasked();
        int index = e.getActionIndex();
        if (action == MotionEvent.ACTION_CANCEL) {
            held.clear();
            return true;
        }
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            handle(e.getPointerId(index), (e.getX(index) - ox) / sc, (e.getY(index) - oy) / sc);
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            held.remove(e.getPointerId(index));
            if (action == MotionEvent.ACTION_UP) performClick();
        } else if (action == MotionEvent.ACTION_MOVE) {
            for (int i = 0; i < e.getPointerCount(); i++) {
                int id = e.getPointerId(i);
                if (!held.containsKey(id)) continue;
                Control updated = controlAt((e.getX(i) - ox) / sc, (e.getY(i) - oy) / sc);
                if (updated == Control.LEFT || updated == Control.RIGHT) held.put(id, updated);
            }
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void handle(int pointer, float x, float y) {
        long now = System.currentTimeMillis();
        if (screen == Screen.TITLE) {
            if (y >= 495 && y <= 585) {
                if (save.hasSave()) {
                    save.loadInto(s);
                    syncFlags();
                    screen = s.questStage == GameState.QuestStage.COMPLETE ? Screen.COMPLETE : Screen.PLAYING;
                } else screen = Screen.INTRO;
            } else if (save.hasSave() && y >= 585) {
                save.clear();
                newGame();
                screen = Screen.INTRO;
            }
            return;
        }
        if (screen == Screen.INTRO) {
            if (y >= 510) {
                newGame();
                screen = Screen.PLAYING;
                card("HALLOWEEN FESTIVAL\nGhost Child: \"Does it ever feel strange? Being both?\"\nTeddy: \"Does being see-through feel strange?\"", 4200, now);
            }
            return;
        }
        if (screen == Screen.COMPLETE) {
            if (y >= 485 && y <= 585) {
                screen = Screen.PLAYING;
                s.questStage = GameState.QuestStage.RETURN_HOME;
                s.villageReturned = true;
                s.playerX = 1500f;
            } else if (y >= 580) screen = Screen.TITLE;
            return;
        }
        if (paused) {
            if (y >= 430 && y <= 520) paused = false;
            else if (y >= 520 && y <= 610) {
                save.save(s);
                screen = Screen.TITLE;
                paused = false;
            }
            return;
        }
        if (x >= 1180 && y <= 90) {
            paused = true;
            save.save(s);
            return;
        }

        Control control = controlAt(x, y);
        if (control == null) return;
        if (control == Control.LEFT || control == Control.RIGHT) {
            held.put(pointer, control);
            return;
        }
        if (control == Control.JUMP) {
            jump();
            return;
        }
        if (control == Control.DODGE) {
            dodge(now);
            return;
        }
        if (control == Control.ATTACK) {
            attack(now);
            return;
        }
        if (control == Control.GOLD) {
            if (s.useHeartland()) {
                magicUntil = now + 380;
                audio.play(AudioController.Cue.HEAL);
            }
            return;
        }
        if (control == Control.VIOLET) {
            if (s.useNightmareBurst() > 0) audio.play(AudioController.Cue.HIT);
            magicUntil = now + 420;
            audio.play(AudioController.Cue.MAGIC);
            return;
        }
        if (control == Control.BALANCE) {
            if (s.purifyBoss()) {
                magicUntil = now + 850;
                bossProjectileUntil = bossTelegraphUntil = 0;
                save.save(s);
            } else card("BALANCE NEEDS BOTH POWERS\nWeaken the Pumpkin King and keep at least 35 Heartland + 35 Nightmare.", 2300, now);
            return;
        }
        if (control == Control.TALK) {
            interact(now);
            return;
        }
        if (control == Control.POTION && s.usePotion()) audio.play(AudioController.Cue.HEAL);
    }

    private Control controlAt(float x, float y) {
        if (dist(x, y, 95, 620) <= 78) return Control.LEFT;
        if (dist(x, y, 235, 620) <= 78) return Control.RIGHT;
        if (dist(x, y, 1120, 610) <= 75) return Control.JUMP;
        if (dist(x, y, 975, 620) <= 70) return Control.ATTACK;
        if (dist(x, y, 840, 620) <= 62) return Control.DODGE;
        if (dist(x, y, 1115, 465) <= 66) return Control.GOLD;
        if (dist(x, y, 995, 475) <= 66) return Control.VIOLET;
        if (dist(x, y, 862, 500) <= 70) return Control.BALANCE;
        if (dist(x, y, 700, 620) <= 60) return Control.TALK;
        if (dist(x, y, 575, 625) <= 58) return Control.POTION;
        return null;
    }

    private void jump() {
        if (s.grounded || s.jumpsUsed < 2) {
            s.velocityY = s.jumpsUsed == 0 ? JUMP : JUMP * .88f;
            s.grounded = false;
            s.jumpsUsed++;
            audio.play(s.jumpsUsed == 2 ? AudioController.Cue.MAGIC : AudioController.Cue.JUMP);
        }
    }

    private void dodge(long now) {
        if (s.tryDodge(now)) {
            dodgeVisualUntil = now + GameState.DODGE_INVULNERABLE_MS;
            audio.play(AudioController.Cue.JUMP);
        }
    }

    private void attack(long now) {
        if (now - s.lastAttackMs < 260) return;
        s.lastAttackMs = now;
        attackUntil = now + 210;
        audio.play(AudioController.Cue.ATTACK);
        int hits = s.performPlayerAttack(75);
        if (s.bossSpawned && !s.bossPurified) {
            float front = s.playerX + (s.facingRight ? 70 : -70);
            if (Math.abs(s.bossX - front) < 125) {
                s.damageBoss(20 + s.weaponLevel * 3);
                hits++;
            }
        }
        if (hits > 0) audio.play(AudioController.Cue.HIT);
    }

    private void interact(long now) {
        if (s.lanternBroken && !s.rescuedVillager && Math.abs(s.playerX - 1450f) < 150f) {
            if (s.rescueVillager()) {
                card("RESCUE COMPLETE\nThe Pumpkin villager is safe. The rift weakens and the forest path opens.\n+20 XP • +5 coins", 3000, now);
                audio.play(AudioController.Cue.QUEST);
                save.save(s);
            }
            return;
        }
        if (Math.abs(s.playerX - 360) < 135) {
            card("GHOST CHILD\n" + (s.villageReturned ? "\"I knew you'd come back.\"" : "\"Whatever happens, come back.\""), 2300, now);
            return;
        }
        if (Math.abs(s.playerX - 610) < 135) {
            int cost = s.weaponLevel * 15;
            if (s.buyAxeUpgrade()) {
                card("SKELETON BLACKSMITH\nAxe upgraded. \"Try not to break this one, Teddy.\"", 2200, now);
                audio.play(AudioController.Cue.CHECKPOINT);
                save.save(s);
            } else {
                card("SKELETON BLACKSMITH\n" + (s.weaponLevel >= 3 ? "Your axe is already at the slice's maximum tier." : "Upgrade costs " + cost + " coins. Current coins: " + s.coins + "."), 2300, now);
            }
            return;
        }
        if (Math.abs(s.playerX - 800) < 135) {
            if (s.coins >= 8) {
                s.coins -= 8;
                s.potions++;
                card("VAMPIRE MERCHANT\nPotion purchased. \"An absolute bargain. For me.\"", 2200, now);
                audio.play(AudioController.Cue.COIN);
                save.save(s);
            } else card("VAMPIRE MERCHANT\nHealth potion: 8 coins. \"No, I do not accept sunlight as currency.\"", 2200, now);
            return;
        }
        if (Math.abs(s.playerX - 930) < 140) {
            card("WITCH GUIDE\n" + (s.villageReturned
                    ? "\"You brought the shard home without rejecting either half of yourself. Remember that.\""
                    : "\"The Lantern holds two flames. Neither survives alone. Whatever happens tonight, remember that.\""), 3000, now);
            return;
        }
        if (Math.abs(s.playerX - 1230) < 145) {
            if (s.health >= GameState.MAX_HEALTH) card("MUMMY HEALER\n\"All stitched up already. Save your coins.\"", 2100, now);
            else if (s.visitMummyHealer()) {
                card("MUMMY HEALER\nBandages, moon-salt and tea. Health restored for 5 coins.", 2200, now);
                audio.play(AudioController.Cue.HEAL);
                save.save(s);
            } else card("MUMMY HEALER\nHealing costs 5 coins. Come back when the bats stop stealing your change.", 2200, now);
            return;
        }
        card("No one nearby needs Teddy's help right now.", 1500, now);
    }

    private void syncFlags() {
        lanternShown = s.lanternBroken;
        forestShown = s.questStage.ordinal() >= GameState.QuestStage.FOREST.ordinal();
        returnShown = s.bossPurified;
        bossRoared = s.bossSpawned || s.bossPurified;
    }

    private void newGame() {
        s.playerX = 220f;
        s.playerY = GROUND - PLAYER_H;
        s.velocityX = s.velocityY = 0;
        s.health = GameState.MAX_HEALTH;
        s.heartland = 50;
        s.nightmare = 50;
        s.xp = 0;
        s.level = 1;
        s.coins = 10;
        s.weaponLevel = 1;
        s.potions = 1;
        s.heartShards = 0;
        s.questStage = GameState.QuestStage.FESTIVAL;
        s.lanternBroken = false;
        s.rescuedVillager = false;
        s.secretFound = false;
        s.bossSpawned = false;
        s.bossPurified = false;
        s.bossHealth = GameState.BOSS_MAX_HEALTH;
        s.bossX = 4050f;
        s.checkpointActivated = false;
        s.villageReturned = false;
        s.lastPlayerHitMs = 0;
        s.lastAttackMs = 0;
        s.lastDodgeMs = -GameState.DODGE_COOLDOWN_MS;
        s.invulnerableUntilMs = 0;
        s.resetEnemies();
        lanternShown = forestShown = returnShown = bossRoared = false;
        bossTelegraphUntil = bossProjectileUntil = 0;
        bossPattern = -1;
        cam = 0;
    }

    private boolean pressed(Control c) {
        return held.containsValue(c);
    }

    private static float dist(float a, float b, float c, float d) {
        float x = a - c, y = b - d;
        return (float) Math.sqrt(x * x + y * y);
    }

    private static float clampf(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public boolean onKeyDown(int key, KeyEvent e) {
        if (screen != Screen.PLAYING) return super.onKeyDown(key, e);
        long now = System.currentTimeMillis();
        if (key == KeyEvent.KEYCODE_DPAD_LEFT || key == KeyEvent.KEYCODE_A) held.put(-1, Control.LEFT);
        else if (key == KeyEvent.KEYCODE_DPAD_RIGHT || key == KeyEvent.KEYCODE_D) held.put(-2, Control.RIGHT);
        else if (key == KeyEvent.KEYCODE_SPACE || key == KeyEvent.KEYCODE_BUTTON_A) jump();
        else if (key == KeyEvent.KEYCODE_BUTTON_B || key == KeyEvent.KEYCODE_K) dodge(now);
        else if (key == KeyEvent.KEYCODE_J || key == KeyEvent.KEYCODE_BUTTON_X) attack(now);
        else if (key == KeyEvent.KEYCODE_H || key == KeyEvent.KEYCODE_BUTTON_L1) s.useHeartland();
        else if (key == KeyEvent.KEYCODE_N || key == KeyEvent.KEYCODE_BUTTON_R1) s.useNightmareBurst();
        else if (key == KeyEvent.KEYCODE_B || key == KeyEvent.KEYCODE_BUTTON_R2) s.purifyBoss();
        else if (key == KeyEvent.KEYCODE_E || key == KeyEvent.KEYCODE_BUTTON_Y) interact(now);
        else if (key == KeyEvent.KEYCODE_ESCAPE || key == KeyEvent.KEYCODE_BUTTON_START) paused = !paused;
        return true;
    }

    @Override
    public boolean onKeyUp(int key, KeyEvent e) {
        if (key == KeyEvent.KEYCODE_DPAD_LEFT || key == KeyEvent.KEYCODE_A) held.remove(-1);
        if (key == KeyEvent.KEYCODE_DPAD_RIGHT || key == KeyEvent.KEYCODE_D) held.remove(-2);
        return true;
    }
}
