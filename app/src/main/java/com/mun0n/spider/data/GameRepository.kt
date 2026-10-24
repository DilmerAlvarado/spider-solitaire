package com.mun0n.spider.data

import android.content.Context
import com.mun0n.spider.game.Difficulty
import com.mun0n.spider.game.Records
import com.mun0n.spider.game.RecordsCodec
import com.mun0n.spider.game.SaveCodec
import com.mun0n.spider.game.SaveData
import java.io.File
import java.io.IOException

class GameRepository(context: Context) {
    private val file = File(context.filesDir, "spider_save.txt")
    private val recordsFile = File(context.filesDir, "spider_records.txt")

    fun load(): SaveData {
        if (!file.exists()) return SaveData(Difficulty.ONE, game = null, history = emptyList())
        return try {
            SaveCodec.decode(file.readText())
        } catch (_: RuntimeException) {
            SaveData(Difficulty.ONE, game = null, history = emptyList())
        }
    }

    fun save(data: SaveData) {
        write(file, SaveCodec.encode(data))
    }

    fun loadRecords(): Records {
        if (!recordsFile.exists()) return Records()
        return try {
            RecordsCodec.decode(recordsFile.readText())
        } catch (_: RuntimeException) {
            Records()
        }
    }

    fun saveRecords(records: Records) {
        write(recordsFile, RecordsCodec.encode(records))
    }

    private fun write(target: File, encoded: String) {
        try {
            val tmp = File(target.parentFile, "${target.name}.tmp")
            tmp.writeText(encoded)
            if (target.exists() && !target.delete()) {
                target.writeText(encoded)
                tmp.delete()
                return
            }
            if (!tmp.renameTo(target)) {
                target.writeText(encoded)
                tmp.delete()
            }
        } catch (_: IOException) {
            // La partida en memoria sigue. El próximo movimiento reintenta.
        }
    }
}
