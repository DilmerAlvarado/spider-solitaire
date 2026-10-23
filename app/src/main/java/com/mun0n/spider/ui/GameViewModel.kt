package com.mun0n.spider.ui

import android.app.Application
import androidx.annotation.StringRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mun0n.spider.R
import com.mun0n.spider.data.GameRepository
import com.mun0n.spider.game.DealBlock
import com.mun0n.spider.game.Difficulty
import com.mun0n.spider.game.GameSession
import com.mun0n.spider.game.GameState
import com.mun0n.spider.game.Hint
import com.mun0n.spider.game.RemovedRun
import com.mun0n.spider.game.SaveData
import com.mun0n.spider.game.Slide
import com.mun0n.spider.game.SpiderRules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

data class Visual(
    val id: Long,
    val before: List<List<com.mun0n.spider.game.Card>>,
    val mid: List<List<com.mun0n.spider.game.Card>>,
    val dealtIds: List<Int>,
    val slide: Slide?,
    val removed: List<RemovedRun>,
)

sealed interface Dialog {
    data object NewGame : Dialog
    data class Won(val score: Int) : Dialog
}

data class UiState(
    val loading: Boolean = true,
    val game: GameState? = null,
    val canUndo: Boolean = false,
    val hint: Hint? = null,
    val hintPlays: List<Hint.MoveCards> = emptyList(),
    val hintSerial: Int = 0,
    val dialog: Dialog? = null,
    val banner: String? = null,
    val visual: Visual? = null,
    val inputLocked: Boolean = false,
    val lastDifficulty: Difficulty = Difficulty.ONE,
)

class GameViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = GameRepository(app)
    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    private var session: GameSession? = null
    private var nextVisualId = 1L
    private var ticket = 0
    private var bannerTicket = 0
    private var auto = false
    private val saveMutex = Mutex()
    private var latestSave: SaveData? = null

    init {
        viewModelScope.launch {
            val save = withContext(Dispatchers.IO) { repo.load() }
            session = save.game?.let { GameSession(it, save.history) }
            _ui.value = UiState(
                loading = false,
                game = save.game,
                canUndo = session?.canUndo == true,
                dialog = if (save.game == null) Dialog.NewGame else null,
                lastDifficulty = save.lastDifficulty,
            )
            if (save.game != null) scheduleAutoIfReady()
        }
    }

    fun onDrop(fromCol: Int, fromIndex: Int, toCol: Int) {
        performMove(fromCol, fromIndex, toCol, fromUser = true, animate = true)
    }

    fun onDragDrop(fromCol: Int, fromIndex: Int, toCol: Int) {
        performMove(fromCol, fromIndex, toCol, fromUser = true, animate = false)
    }

    fun onDeal() {
        if (_ui.value.inputLocked) return
        val current = session ?: return
        when (SpiderRules.dealBlock(current.state)) {
            DealBlock.EmptyColumn -> {
                banner(R.string.empty_column_block)
                return
            }
            DealBlock.NoCards -> {
                banner(R.string.no_cards)
                return
            }
            null -> Unit
        }
        ticket++
        auto = false
        val before = current.state.columns
        val result = current.deal() ?: return
        val visual = Visual(
            id = nextVisualId++,
            before = before,
            mid = result.midColumns,
            dealtIds = result.dealtCards.map { it.id },
            slide = null,
            removed = result.removed,
        )
        publish(visual = visual, locked = true, hint = null, banner = null)
        persist()
    }

    fun onUndo() {
        if (_ui.value.inputLocked) return
        val current = session ?: return
        if (!current.undo()) return
        ticket++
        auto = false
        publish(visual = null, locked = false, dialog = null, hint = null, banner = null)
        persist()
    }

    fun onHint() {
        if (_ui.value.inputLocked) return
        val current = session ?: return
        val tour = SpiderRules.hintTour(current.state)
        if (tour.isEmpty()) {
            dismissHint()
            banner(R.string.no_moves)
            return
        }
        showHint(tour.first(), tour.filterIsInstance<Hint.MoveCards>())
    }

    fun dismissHint() {
        if (_ui.value.hint == null && _ui.value.hintPlays.isEmpty()) return
        showHint(null, emptyList())
    }

    fun onIllegalMove() {
        if (_ui.value.inputLocked) return
        banner(R.string.illegal_move)
    }

    fun onRequestNewGame() {
        if (_ui.value.inputLocked) return
        _ui.value = _ui.value.copy(dialog = Dialog.NewGame, hint = null)
    }

    fun onDismissDialog() {
        val game = _ui.value.game ?: return
        if (game.won) return
        _ui.value = _ui.value.copy(dialog = null)
    }

    fun onNewGame(difficulty: Difficulty) {
        ticket++
        auto = false
        session = GameSession(SpiderRules.newGame(difficulty))
        publish(
            visual = null,
            locked = false,
            dialog = null,
            hint = null,
            banner = null,
            lastDifficulty = difficulty,
        )
        persist()
    }

    fun onVisualFinished(id: Long) {
        val visual = _ui.value.visual ?: return
        if (visual.id != id) return
        val game = session?.state ?: return
        if (game.won) {
            auto = false
            publish(visual = null, locked = false, dialog = Dialog.Won(game.score), hint = null)
            persist()
            return
        }
        if (auto || SpiderRules.readyToAutoFinish(game)) {
            val move = SpiderRules.nextInSuitMove(game)
            if (move != null) {
                auto = true
                if (performMove(move.first, move.second, move.third, fromUser = false, animate = true)) {
                    return
                }
            }
        }
        auto = false
        publish(
            visual = null,
            locked = false,
            hint = _ui.value.hint,
            hintPlays = _ui.value.hintPlays,
        )
    }

    private fun showHint(hint: Hint?, plays: List<Hint.MoveCards>) {
        _ui.value = _ui.value.copy(
            hint = hint,
            hintPlays = plays,
            hintSerial = _ui.value.hintSerial + 1,
            banner = null,
        )
    }

    private fun performMove(
        fromCol: Int,
        fromIndex: Int,
        toCol: Int,
        fromUser: Boolean,
        animate: Boolean,
    ): Boolean {
        if (fromUser && _ui.value.inputLocked) return false
        val current = session ?: return false
        val source = current.state.columns.getOrNull(fromCol) ?: return false
        if (fromIndex !in source.indices) return false
        if (!SpiderRules.canMove(current.state, fromCol, fromIndex, toCol)) {
            if (fromUser) banner(R.string.illegal_move)
            return false
        }
        if (fromUser) {
            ticket++
            auto = false
        }
        val before = current.state.columns
        val moving = source.drop(fromIndex)
        val result = current.move(fromCol, fromIndex, toCol) ?: return false
        val slide = Slide(fromCol, toCol, moving)
        val needsVisual = result.removed.isNotEmpty() || animate
        val visual = if (needsVisual) {
            Visual(
                id = nextVisualId++,
                before = before,
                mid = result.midColumns,
                dealtIds = emptyList(),
                slide = if (animate) slide else null,
                removed = result.removed,
            )
        } else {
            null
        }
        publish(visual = visual, locked = visual != null, hint = null, banner = null)
        persist()
        if (visual == null) {
            if (result.state.won) {
                publish(visual = null, locked = false, dialog = Dialog.Won(result.state.score))
            } else {
                scheduleAutoIfReady()
            }
        }
        return true
    }

    private fun scheduleAutoIfReady() {
        val game = session?.state ?: return
        if (!SpiderRules.readyToAutoFinish(game)) return
        val myTicket = ticket
        viewModelScope.launch {
            delay(320)
            if (myTicket != ticket || _ui.value.visual != null) return@launch
            val move = SpiderRules.nextInSuitMove(session?.state ?: return@launch) ?: return@launch
            auto = true
            performMove(move.first, move.second, move.third, fromUser = false, animate = true)
        }
    }

    private fun banner(@StringRes id: Int) {
        val text = getApplication<Application>().getString(id)
        val myTicket = ++bannerTicket
        _ui.value = _ui.value.copy(banner = text)
        viewModelScope.launch {
            delay(2200)
            if (bannerTicket == myTicket) {
                _ui.value = _ui.value.copy(banner = null)
            }
        }
    }

    private fun publish(
        visual: Visual?,
        locked: Boolean,
        dialog: Dialog? = _ui.value.dialog,
        hint: Hint? = null,
        hintPlays: List<Hint.MoveCards> = emptyList(),
        banner: String? = _ui.value.banner,
        lastDifficulty: Difficulty = session?.state?.difficulty ?: _ui.value.lastDifficulty,
    ) {
        val current = session
        val serial = if (hint != _ui.value.hint || hintPlays != _ui.value.hintPlays) {
            _ui.value.hintSerial + 1
        } else {
            _ui.value.hintSerial
        }
        _ui.value = UiState(
            loading = false,
            game = current?.state,
            canUndo = current?.canUndo == true,
            hint = hint,
            hintPlays = hintPlays,
            hintSerial = serial,
            dialog = dialog,
            banner = banner,
            visual = visual,
            inputLocked = locked,
            lastDifficulty = lastDifficulty,
        )
    }

    private fun persist() {
        val current = session
        val difficulty = current?.state?.difficulty ?: _ui.value.lastDifficulty
        latestSave = SaveData(
            lastDifficulty = difficulty,
            game = current?.state,
            history = current?.history().orEmpty(),
        )
        viewModelScope.launch(Dispatchers.IO) {
            saveMutex.withLock {
                val data = latestSave ?: return@withLock
                repo.save(data)
            }
        }
    }
}
