package com.potato.jarvis.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.potato.jarvis.core.ChatMessage

class JarvisDb(context: Context) : SQLiteOpenHelper(context, "potato_local.db", null, 3) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE messages(id INTEGER PRIMARY KEY AUTOINCREMENT, role TEXT NOT NULL, content TEXT NOT NULL, created_at INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE settings(key TEXT PRIMARY KEY, value TEXT NOT NULL)")
        db.execSQL("CREATE INDEX idx_messages_created ON messages(created_at)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) db.execSQL("CREATE TABLE IF NOT EXISTS settings(key TEXT PRIMARY KEY, value TEXT NOT NULL)")
        if (oldVersion < 3) db.execSQL("CREATE INDEX IF NOT EXISTS idx_messages_created ON messages(created_at)")
    }

    fun save(message: ChatMessage) {
        writableDatabase.execSQL(
            "INSERT INTO messages(role,content,created_at) VALUES(?,?,?)",
            arrayOf(message.role, message.content, message.createdAt),
        )
    }

    fun load(limit: Int = 500): List<ChatMessage> = readableDatabase.rawQuery(
        "SELECT role,content,created_at FROM messages ORDER BY id DESC LIMIT ?",
        arrayOf(limit.coerceIn(1, 2_000).toString()),
    ).use { cursor ->
        buildList {
            while (cursor.moveToNext()) add(ChatMessage(cursor.getString(0), cursor.getString(1), cursor.getLong(2)))
        }.asReversed()
    }

    fun putSetting(key: String, value: String) {
        writableDatabase.execSQL(
            "INSERT INTO settings(key,value) VALUES(?,?) ON CONFLICT(key) DO UPDATE SET value=excluded.value",
            arrayOf(key, value),
        )
    }

    fun getSetting(key: String, fallback: String = ""): String = readableDatabase.rawQuery(
        "SELECT value FROM settings WHERE key=?",
        arrayOf(key),
    ).use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else fallback }

    fun deleteSetting(key: String) {
        writableDatabase.delete("settings", "key=?", arrayOf(key))
    }

    fun clearMessages() {
        writableDatabase.delete("messages", null, null)
    }

    fun replaceMessages(items: List<ChatMessage>) {
        writableDatabase.delete("messages", null, null)
        items.forEach { save(it) }
    }

}
