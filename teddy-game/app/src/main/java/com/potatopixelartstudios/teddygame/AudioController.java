package com.potatopixelartstudios.teddygame;

import android.media.AudioManager;
import android.media.ToneGenerator;

/** Lightweight offline sound layer. Production music/SFX from the asset pack can replace these cues later. */
final class AudioController {
    enum Cue { ATTACK, JUMP, HIT, HURT, HEAL, MAGIC, BOSS, COIN, ITEM, QUEST, CHECKPOINT }

    private final ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_MUSIC, 55);
    private boolean released;

    AudioController(android.content.Context ignored) { }

    void play(Cue cue) {
        if (released) return;
        int toneId;
        int duration;
        switch (cue) {
            case ATTACK: toneId = ToneGenerator.TONE_PROP_BEEP; duration = 55; break;
            case JUMP: toneId = ToneGenerator.TONE_PROP_ACK; duration = 60; break;
            case HIT: toneId = ToneGenerator.TONE_PROP_NACK; duration = 70; break;
            case HURT: toneId = ToneGenerator.TONE_SUP_ERROR; duration = 90; break;
            case HEAL: toneId = ToneGenerator.TONE_PROP_PROMPT; duration = 100; break;
            case MAGIC: toneId = ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD; duration = 90; break;
            case BOSS: toneId = ToneGenerator.TONE_CDMA_HIGH_L; duration = 180; break;
            case COIN: toneId = ToneGenerator.TONE_CDMA_CONFIRM; duration = 60; break;
            case ITEM: toneId = ToneGenerator.TONE_CDMA_PIP; duration = 70; break;
            case QUEST: toneId = ToneGenerator.TONE_CDMA_NETWORK_BUSY_ONE_SHOT; duration = 130; break;
            case CHECKPOINT: toneId = ToneGenerator.TONE_CDMA_ABBR_ALERT; duration = 100; break;
            default: toneId = ToneGenerator.TONE_PROP_BEEP; duration = 60;
        }
        tone.startTone(toneId, duration);
    }

    void release() {
        if (!released) {
            released = true;
            tone.release();
        }
    }
}
