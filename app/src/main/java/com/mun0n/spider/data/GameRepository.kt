package com.mun0n.spider.data

import android.content.Context
import com.mun0n.spider.game.Difficulty
import com.mun0n.spider.game.SaveCodec
import com.mun0n.spider.game.SaveData
import java.io.File
import java.io.IOException

class GameRepository(context: Context) {
    private val file = File(context.filesDir, "spider_save.txt")

    fun load(): SaveData {
        if (!file.exists()) return SaveData(Difficulty.ONE, game = null, history = emptyList())
        return try {
            SaveCodec.decode(file.readText())
        } catch (_: RuntimeException) {
            SaveData(Difficulty.ONE, game = null, history = emptyList())
        }
    }

    fun save(data: SaveData) {
        try {
            val encoded = SaveCodec.encode(data)
            val tmp = File(file.parentFile, "${file.name}.tmp")
            tmp.writeText(encoded)
            if (file.exists() && !file.delete()) {
                file.writeText(encoded)
                tmp.delete()
                return
            }
            if (!tmp.renameTo(file)) {
                file.writeText(encoded)
                tmp.delete()
            }
        } catch (_: IOException) {
            // La partida en memoria sigue. El próximo movimiento reintenta.
        }
    }
}
