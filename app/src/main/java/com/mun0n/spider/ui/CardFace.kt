package com.mun0n.spider.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import com.mun0n.spider.game.Suit
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mun0n.spider.game.Card
import com.mun0n.spider.game.Rank

private val CardFaceColor = Color(0xFFFFFBF4)
private val CardEdge = Color(0xFFD9CDB8)
private val BackRed = Color(0xFF8E1B2C)
private val PipRed = Color(0xFFC0182A)
private val PipBlack = Color(0xFF161616)
private val GoldLine = Color(0xFFE7C27A)
val HintGold = Color(0xFFFFD56A)
val SelectGold = Color(0xFFF2C14E)

private val Tight = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))

private val PipPositions: Map<Int, List<Pair<Float, Float>>> = mapOf(
    2 to listOf(0.50f to 0.20f, 0.50f to 0.80f),
    3 to listOf(0.50f to 0.18f, 0.50f to 0.50f, 0.50f to 0.82f),
    4 to listOf(0.30f to 0.20f, 0.70f to 0.20f, 0.30f to 0.80f, 0.70f to 0.80f),
    5 to listOf(0.30f to 0.20f, 0.70f to 0.20f, 0.50f to 0.50f, 0.30f to 0.80f, 0.70f to 0.80f),
    6 to listOf(0.30f to 0.18f, 0.70f to 0.18f, 0.30f to 0.50f, 0.70f to 0.50f, 0.30f to 0.82f, 0.70f to 0.82f),
    7 to listOf(
        0.30f to 0.16f, 0.70f to 0.16f, 0.50f to 0.33f,
        0.30f to 0.50f, 0.70f to 0.50f, 0.30f to 0.84f, 0.70f to 0.84f,
    ),
    8 to listOf(
        0.30f to 0.15f, 0.70f to 0.15f, 0.50f to 0.32f,
        0.30f to 0.46f, 0.70f to 0.46f, 0.50f to 0.68f,
        0.30f to 0.85f, 0.70f to 0.85f,
    ),
    9 to listOf(
        0.30f to 0.14f, 0.70f to 0.14f, 0.30f to 0.34f, 0.70f to 0.34f,
        0.50f to 0.50f, 0.30f to 0.66f, 0.70f to 0.66f, 0.30f to 0.86f, 0.70f to 0.86f,
    ),
    10 to listOf(
        0.30f to 0.13f, 0.70f to 0.13f, 0.50f to 0.27f,
        0.30f to 0.38f, 0.70f to 0.38f, 0.30f to 0.62f, 0.70f to 0.62f,
        0.50f to 0.73f, 0.30f to 0.87f, 0.70f to 0.87f,
    ),
)

@Composable
fun PlayingCard(
    card: Card,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    hinted: Boolean = false,
) {
    val shape = RoundedCornerShape(8.dp)
    val borderColor = when {
        selected -> SelectGold
        hinted -> HintGold
        card.faceUp -> CardEdge
        else -> Color(0xFF5C1018)
    }
    Box(
        modifier
            .shadow(elevation = if (card.faceUp) 3.dp else 2.dp, shape = shape, clip = false)
            .clip(shape)
            .background(if (card.faceUp) CardFaceColor else BackRed)
            .border(width = if (selected || hinted) 3.dp else 1.dp, color = borderColor, shape = shape)
            .semantics {
                contentDescription = if (card.faceUp) card.description() else "carta boca abajo"
            },
    ) {
        if (card.faceUp) CardFace(card) else CardBack()
    }
}

@Composable
private fun CardFace(card: Card) {
    val color = if (card.suit.red) PipRed else PipBlack
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val rankDp = if (card.rank.label.length > 1) maxWidth * 0.16f else maxWidth * 0.20f
        val mark = maxWidth * 0.13f
        val rankStyle = Tight.copy(
            color = color,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = with(density) { rankDp.toSp() },
            textAlign = TextAlign.Center,
        )
        if (card.rank == Rank.ACE) {
            SuitMark(
                card.suit,
                color,
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = maxWidth * 0.30f, vertical = maxHeight * 0.26f),
            )
        } else if (card.rank.value >= Rank.JACK.value) {
            CourtCard(
                card = card,
                suitColor = color,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = maxWidth * 0.18f, vertical = maxHeight * 0.02f),
            )
        } else {
            Pips(
                card = card,
                color = color,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = maxWidth * 0.24f, vertical = maxHeight * 0.20f),
            )
        }
        Index(card, color, rankStyle, mark, Modifier.align(Alignment.TopStart).padding(start = 4.dp, top = 2.dp))
        Index(
            card,
            color,
            rankStyle,
            mark,
            Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 4.dp, bottom = 2.dp)
                .graphicsLayer { rotationZ = 180f },
        )
    }
}

@Composable
private fun Index(
    card: Card,
    color: Color,
    rankStyle: TextStyle,
    mark: Dp,
    modifier: Modifier,
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(card.rank.label, style = rankStyle)
        SuitMark(
            card.suit,
            color,
            Modifier
                .padding(top = mark * 0.38f)
                .size(mark),
        )
    }
}

@Composable
private fun CourtCard(card: Card, suitColor: Color, modifier: Modifier) {
    val redSuit = suitColor == PipRed
    BoxWithConstraints(modifier) {
        val mark = maxWidth * 0.18f
        Canvas(Modifier.fillMaxSize()) {
            drawCourtHalf(card.rank, redSuit)
            rotate(degrees = 180f, pivot = center) {
                drawCourtHalf(card.rank, redSuit)
            }
            drawCircle(CardFaceColor, radius = size.minDimension * 0.075f, center = center)
        }
        SuitMark(
            card.suit,
            suitColor,
            Modifier.align(Alignment.Center).size(mark),
        )
    }
}

private val CourtInk = Color(0xFF1A120C)
private val CourtGold = Color(0xFFE6B325)
private val CourtGoldDark = Color(0xFFB8860B)
private val CourtSkin = Color(0xFFF6D7B8)
private val CourtHair = Color(0xFF1A120C)
private val CourtBeard = Color(0xFFF4F0E6)
private val CourtBlade = Color(0xFFE8EAF0)
private val CourtBlue = Color(0xFF1F4E9A)
private val CourtRed = Color(0xFFC8102E)

private fun DrawScope.drawCourtHalf(rank: Rank, redSuit: Boolean) {
    val w = size.width
    val h = size.height
    val line = w * 0.016f
    val x: (Float) -> Float = { f -> w * f }
    val y: (Float) -> Float = { f -> h * f }

    ornateRobe(rank, redSuit, line, x, y)
    if (rank != Rank.JACK) {
        ermine(x(0.12f), y(0.268f), x(0.24f), y(0.07f), line)
        ermine(x(0.64f), y(0.268f), x(0.24f), y(0.07f), line)
    }
    if (rank == Rank.QUEEN) {
        drawRoundRect(CourtHair, Offset(x(0.28f), y(0.09f)), Size(x(0.10f), y(0.18f)), CornerRadius(x(0.03f), y(0.04f)))
        drawRoundRect(CourtHair, Offset(x(0.62f), y(0.09f)), Size(x(0.10f), y(0.18f)), CornerRadius(x(0.03f), y(0.04f)))
    } else if (rank == Rank.KING) {
        drawOval(CourtBeard, Offset(x(0.30f), y(0.11f)), Size(x(0.10f), y(0.10f)))
        drawOval(CourtBeard, Offset(x(0.60f), y(0.11f)), Size(x(0.10f), y(0.10f)))
    }

    drawRoundRect(
        CourtSkin,
        topLeft = Offset(x(0.45f), y(0.185f)),
        size = Size(x(0.10f), y(0.055f)),
        cornerRadius = CornerRadius(x(0.03f), y(0.02f)),
    )
    drawOval(CourtSkin, Offset(x(0.38f), y(0.100f)), Size(x(0.24f), y(0.110f)))
    drawOval(
        color = CourtInk,
        topLeft = Offset(x(0.38f), y(0.100f)),
        size = Size(x(0.24f), y(0.110f)),
        style = Stroke(width = line),
    )

    drawCircle(CourtInk, w * 0.013f, Offset(x(0.44f), y(0.140f)))
    drawCircle(CourtInk, w * 0.013f, Offset(x(0.56f), y(0.140f)))
    val nose = Path().apply {
        moveTo(x(0.48f), y(0.138f))
        lineTo(x(0.545f), y(0.178f))
        lineTo(x(0.47f), y(0.175f))
        close()
    }
    drawPath(nose, Color(0xFFE2B08C))
    strokeInk(nose, line * 0.6f)

    when (rank) {
        Rank.KING -> {
            val beard = Path().apply {
                moveTo(x(0.40f), y(0.185f))
                lineTo(x(0.60f), y(0.185f))
                lineTo(x(0.59f), y(0.250f))
                lineTo(x(0.52f), y(0.228f))
                lineTo(x(0.50f), y(0.258f))
                lineTo(x(0.48f), y(0.228f))
                lineTo(x(0.41f), y(0.250f))
                close()
            }
            drawPath(beard, CourtBeard)
            strokeInk(beard, line * 0.8f)
            val stache = Path().apply {
                moveTo(x(0.41f), y(0.175f))
                quadraticTo(x(0.46f), y(0.158f), x(0.50f), y(0.176f))
                quadraticTo(x(0.54f), y(0.158f), x(0.59f), y(0.175f))
                quadraticTo(x(0.54f), y(0.188f), x(0.50f), y(0.180f))
                quadraticTo(x(0.46f), y(0.188f), x(0.41f), y(0.175f))
                close()
            }
            drawPath(stache, CourtBeard)
            strokeInk(stache, line * 0.7f)
            drawClassicCrown(king = true, line = line)
            drawSword(x(0.78f), line)
        }
        Rank.QUEEN -> {
            drawClassicCrown(king = false, line = line)
            drawRose(x(0.78f), y(0.09f), w * 0.26f)
        }
        else -> {
            drawJackHat(CourtBlue, line)
            drawHalberd(x(0.78f), line, CourtRed)
        }
    }
}

private fun DrawScope.ornateRobe(
    rank: Rank,
    redSuit: Boolean,
    line: Float,
    x: (Float) -> Float,
    y: (Float) -> Float,
) {
    val robe = tunic(x, y, 0.08f, 0.92f, 0.0f, 1.0f)
    val stripes = if (redSuit) {
        listOf(CourtRed, CourtGold, CourtInk, CourtBlue, CourtGold, CourtRed, CourtBlue)
    } else {
        listOf(CourtBlue, CourtGold, CourtRed, CourtInk, CourtGold, CourtBlue, CourtRed)
    }
    clipPath(robe) {
        if (rank == Rank.JACK) {
            drawRect(CourtGold, Offset(x(0f), y(0.27f)), Size(x(0.52f), y(0.23f)))
            drawRect(CourtBlue, Offset(x(0.48f), y(0.27f)), Size(x(0.52f), y(0.23f)))
            repeat(3) { index ->
                val motif = diamondMotif(x, y, 0.14f + index * 0.12f, 0.38f, 0.045f)
                drawPath(motif, CourtRed)
                strokeInk(motif, line * 0.6f)
            }
        } else {
            stripes.forEachIndexed { index, color ->
                val left = index / stripes.size.toFloat()
                drawRect(
                    color,
                    Offset(x(left), y(0.27f)),
                    Size(x(1f / stripes.size + 0.01f), y(0.23f)),
                )
            }
            repeat(4) { index ->
                val motif = diamondMotif(x, y, 0.2f + index * 0.18f, 0.39f, 0.04f)
                drawPath(motif, if (index % 2 == 0) CourtInk else CourtGold)
                strokeInk(motif, line * 0.55f)
            }
        }
    }
    strokeInk(robe, line)
}

private fun diamondMotif(
    x: (Float) -> Float,
    y: (Float) -> Float,
    cx: Float,
    cy: Float,
    s: Float,
): Path = Path().apply {
    moveTo(x(cx), y(cy - s))
    lineTo(x(cx + s), y(cy))
    lineTo(x(cx), y(cy + s))
    lineTo(x(cx - s), y(cy))
    close()
}

private fun tunic(
    x: (Float) -> Float,
    y: (Float) -> Float,
    topL: Float,
    topR: Float,
    botL: Float,
    botR: Float,
): Path = Path().apply {
    moveTo(x(topL), y(0.29f))
    lineTo(x(topR), y(0.29f))
    lineTo(x(botR), y(0.485f))
    lineTo(x(botL), y(0.485f))
    close()
}

private fun DrawScope.ermine(left: Float, top: Float, width: Float, height: Float, line: Float) {
    drawRoundRect(CourtBeard, Offset(left, top), Size(width, height), CornerRadius(line, line))
    drawRoundRect(
        CourtInk,
        Offset(left, top),
        Size(width, height),
        CornerRadius(line, line),
        style = Stroke(width = line * 0.7f),
    )
    val spots = listOf(0.28f to 0.35f, 0.62f to 0.35f, 0.45f to 0.70f)
    spots.forEach { (sx, sy) ->
        drawCircle(CourtInk, width * 0.06f, Offset(left + width * sx, top + height * sy))
    }
}

private fun DrawScope.drawClassicCrown(king: Boolean, line: Float) {
    val w = size.width
    val h = size.height
    val bandTop = if (king) h * 0.072f else h * 0.092f
    val bandH = h * 0.030f
    val cap = Path().apply {
        moveTo(w * 0.32f, bandTop)
        quadraticTo(w * 0.50f, h * 0.006f, w * 0.68f, bandTop)
        close()
    }
    drawPath(cap, CourtRed)
    strokeInk(cap, line * 0.7f)
    drawRoundRect(
        CourtGold,
        Offset(w * 0.28f, bandTop),
        Size(w * 0.44f, bandH),
        CornerRadius(w * 0.012f, w * 0.012f),
    )
    if (king) {
        listOf(0.36f to 0.02f, 0.64f to 0.02f).forEach { (cx, tip) ->
            val peak = Path().apply {
                moveTo(w * (cx - 0.06f), bandTop)
                lineTo(w * cx, h * tip)
                lineTo(w * (cx + 0.06f), bandTop)
                close()
            }
            drawPath(peak, CourtGold)
            strokeInk(peak, line * 0.8f)
        }
        drawRect(CourtGold, Offset(w * 0.475f, h * 0.008f), Size(w * 0.05f, bandTop - h * 0.008f))
        drawRect(CourtGold, Offset(w * 0.44f, h * 0.028f), Size(w * 0.12f, h * 0.018f))
        strokeInk(
            Path().apply {
                moveTo(w * 0.475f, h * 0.008f)
                lineTo(w * 0.525f, h * 0.008f)
                lineTo(w * 0.525f, bandTop)
                lineTo(w * 0.475f, bandTop)
                close()
            },
            line * 0.7f,
        )
    } else {
        listOf(0.38f, 0.50f, 0.62f).forEach { cx ->
            val arch = Path().apply {
                moveTo(w * (cx - 0.07f), bandTop)
                quadraticTo(w * cx, bandTop - h * 0.055f, w * (cx + 0.07f), bandTop)
                close()
            }
            drawPath(arch, CourtGold)
            strokeInk(arch, line * 0.8f)
        }
    }
    drawRoundRect(
        CourtInk,
        Offset(w * 0.28f, bandTop),
        Size(w * 0.44f, bandH),
        CornerRadius(w * 0.012f, w * 0.012f),
        style = Stroke(width = line * 0.8f),
    )
    drawCircle(PipRed, w * 0.012f, Offset(w * 0.38f, bandTop + bandH / 2f))
    drawCircle(Color(0xFF1B3F86), w * 0.012f, Offset(w * 0.50f, bandTop + bandH / 2f))
    drawCircle(PipRed, w * 0.012f, Offset(w * 0.62f, bandTop + bandH / 2f))
}

private fun DrawScope.drawSword(x: Float, line: Float) {
    val h = size.height
    val w = size.width
    val blade = Path().apply {
        moveTo(x - w * 0.030f, h * 0.30f)
        lineTo(x - w * 0.010f, h * 0.008f)
        lineTo(x + w * 0.010f, h * 0.008f)
        lineTo(x + w * 0.030f, h * 0.30f)
        close()
    }
    drawPath(blade, CourtBlade)
    strokeInk(blade, line * 0.8f)
    drawRoundRect(CourtGold, Offset(x - w * 0.07f, h * 0.285f), Size(w * 0.14f, h * 0.022f), CornerRadius(line, line))
    drawRoundRect(CourtGoldDark, Offset(x - w * 0.02f, h * 0.30f), Size(w * 0.04f, h * 0.06f), CornerRadius(line, line))
    drawCircle(CourtGold, w * 0.026f, Offset(x, h * 0.37f))
    drawCircle(CourtSkin, w * 0.032f, Offset(x, h * 0.325f))
}

private fun DrawScope.drawRose(cx: Float, cy: Float, s: Float) {
    val stem = size.height
    drawLine(Color(0xFF2E6B32), Offset(cx, cy + s * 0.15f), Offset(cx, stem * 0.40f), strokeWidth = s * 0.08f, cap = StrokeCap.Round)
    drawOval(Color(0xFF2E6B32), Offset(cx, cy + s * 0.85f), Size(s * 0.45f, s * 0.22f))
    listOf(
        0f to -0.34f,
        0.32f to -0.08f,
        0.20f to 0.28f,
        -0.20f to 0.28f,
        -0.32f to -0.08f,
    ).forEach { (dx, dy) ->
        drawCircle(PipRed, s * 0.22f, Offset(cx + dx * s, cy + dy * s))
    }
    drawCircle(CourtGold, s * 0.12f, Offset(cx, cy))
}

private fun DrawScope.drawJackHat(cloth: Color, line: Float) {
    val w = size.width
    val h = size.height
    val cap = Path().apply {
        moveTo(w * 0.30f, h * 0.11f)
        quadraticTo(w * 0.34f, h * 0.02f, w * 0.50f, h * 0.02f)
        quadraticTo(w * 0.70f, h * 0.02f, w * 0.74f, h * 0.11f)
        close()
    }
    drawPath(cap, cloth)
    strokeInk(cap, line)
    drawRoundRect(CourtGold, Offset(w * 0.24f, h * 0.095f), Size(w * 0.52f, h * 0.032f), CornerRadius(w * 0.02f, h * 0.01f))
    drawRoundRect(
        CourtInk,
        Offset(w * 0.24f, h * 0.095f),
        Size(w * 0.52f, h * 0.032f),
        CornerRadius(w * 0.02f, h * 0.01f),
        style = Stroke(width = line * 0.7f),
    )
    val feather = Path().apply {
        moveTo(w * 0.70f, h * 0.10f)
        quadraticTo(w * 0.96f, h * 0.0f, w * 0.84f, h * 0.16f)
        quadraticTo(w * 0.90f, h * 0.04f, w * 0.70f, h * 0.10f)
        close()
    }
    drawPath(feather, CourtGold)
    strokeInk(feather, line * 0.7f)
}

private fun DrawScope.drawHalberd(x: Float, line: Float, cloth: Color) {
    val h = size.height
    val w = size.width
    drawLine(CourtGoldDark, Offset(x, h * 0.02f), Offset(x, h * 0.46f), strokeWidth = w * 0.018f, cap = StrokeCap.Round)
    val spike = Path().apply {
        moveTo(x - w * 0.012f, h * 0.06f)
        lineTo(x, h * 0.005f)
        lineTo(x + w * 0.012f, h * 0.06f)
        close()
    }
    drawPath(spike, CourtBlade)
    strokeInk(spike, line * 0.6f)
    val axe = Path().apply {
        moveTo(x, h * 0.035f)
        quadraticTo(x + w * 0.22f, h * 0.05f, x + w * 0.20f, h * 0.14f)
        quadraticTo(x + w * 0.10f, h * 0.11f, x, h * 0.155f)
        close()
    }
    drawPath(axe, CourtBlade)
    strokeInk(axe, line * 0.7f)
    drawCircle(cloth, w * 0.012f, Offset(x + w * 0.07f, h * 0.09f))
}

private fun DrawScope.strokeInk(path: Path, width: Float) {
    drawPath(path, CourtInk, style = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

@Composable
private fun Pips(card: Card, color: Color, modifier: Modifier) {
    val spots = PipPositions[card.rank.value].orEmpty()
    Canvas(modifier) {
        val side = minOf(size.width, size.height) * 0.20f
        spots.forEach { (x, y) ->
            val cx = size.width * x
            val cy = size.height * y
            val degrees = if (y > 0.55f) 180f else 0f
            rotate(degrees, Offset(cx, cy)) {
                drawFrenchSuit(card.suit, color, cx - side / 2f, cy - side / 2f, side)
            }
        }
    }
}

@Composable
private fun SuitMark(suit: Suit, color: Color, modifier: Modifier) {
    Canvas(modifier) {
        val side = minOf(size.width, size.height)
        drawFrenchSuit(suit, color, (size.width - side) / 2f, (size.height - side) / 2f, side)
    }
}

private fun DrawScope.drawFrenchSuit(suit: Suit, color: Color, left: Float, top: Float, side: Float) {
    val px: (Float) -> Float = { f -> left + side * f }
    val py: (Float) -> Float = { f -> top + side * f }
    when (suit) {
        Suit.HEARTS -> drawPath(heart(px, py), color)
        Suit.DIAMONDS -> drawPath(diamond(px, py), color)
        Suit.SPADES -> {
            drawPath(spadeLeaf(px, py), color)
            drawPath(spadeStem(px, py), color)
        }
        Suit.CLUBS -> {
            val r = side * 0.22f
            drawCircle(color, r, Offset(px(0.50f), py(0.32f)))
            drawCircle(color, r, Offset(px(0.29f), py(0.56f)))
            drawCircle(color, r, Offset(px(0.71f), py(0.56f)))
            drawPath(clubStem(px, py), color)
        }
    }
}

private fun heart(px: (Float) -> Float, py: (Float) -> Float) = Path().apply {
    moveTo(px(0.50f), py(0.90f))
    cubicTo(px(0.50f), py(0.90f), px(0.02f), py(0.58f), px(0.02f), py(0.36f))
    cubicTo(px(0.02f), py(0.14f), px(0.20f), py(0.04f), px(0.34f), py(0.06f))
    cubicTo(px(0.44f), py(0.08f), px(0.48f), py(0.18f), px(0.50f), py(0.28f))
    cubicTo(px(0.52f), py(0.18f), px(0.56f), py(0.08f), px(0.66f), py(0.06f))
    cubicTo(px(0.80f), py(0.04f), px(0.98f), py(0.14f), px(0.98f), py(0.36f))
    cubicTo(px(0.98f), py(0.58f), px(0.50f), py(0.90f), px(0.50f), py(0.90f))
    close()
}

private fun diamond(px: (Float) -> Float, py: (Float) -> Float) = Path().apply {
    moveTo(px(0.50f), py(0.02f))
    lineTo(px(0.96f), py(0.50f))
    lineTo(px(0.50f), py(0.98f))
    lineTo(px(0.04f), py(0.50f))
    close()
}

private fun spadeLeaf(px: (Float) -> Float, py: (Float) -> Float) = Path().apply {
    moveTo(px(0.50f), py(0.02f))
    cubicTo(px(0.16f), py(0.28f), px(0.02f), py(0.42f), px(0.02f), py(0.58f))
    cubicTo(px(0.02f), py(0.78f), px(0.22f), py(0.84f), px(0.36f), py(0.72f))
    cubicTo(px(0.42f), py(0.66f), px(0.47f), py(0.62f), px(0.50f), py(0.56f))
    cubicTo(px(0.53f), py(0.62f), px(0.58f), py(0.66f), px(0.64f), py(0.72f))
    cubicTo(px(0.78f), py(0.84f), px(0.98f), py(0.78f), px(0.98f), py(0.58f))
    cubicTo(px(0.98f), py(0.42f), px(0.84f), py(0.28f), px(0.50f), py(0.02f))
    close()
}

private fun spadeStem(px: (Float) -> Float, py: (Float) -> Float) = Path().apply {
    moveTo(px(0.42f), py(0.62f))
    quadraticTo(px(0.40f), py(0.82f), px(0.32f), py(0.98f))
    lineTo(px(0.68f), py(0.98f))
    quadraticTo(px(0.60f), py(0.82f), px(0.58f), py(0.62f))
    close()
}

private fun clubStem(px: (Float) -> Float, py: (Float) -> Float) = Path().apply {
    moveTo(px(0.44f), py(0.62f))
    quadraticTo(px(0.42f), py(0.80f), px(0.34f), py(0.98f))
    lineTo(px(0.66f), py(0.98f))
    quadraticTo(px(0.58f), py(0.80f), px(0.56f), py(0.62f))
    close()
}

@Composable
private fun CardBack() {
    Canvas(Modifier.fillMaxSize().padding(5.dp)) {
        val stroke = size.minDimension * 0.018f
        drawRoundRect(
            color = GoldLine,
            cornerRadius = CornerRadius(10f, 10f),
            style = Stroke(width = stroke),
        )
        val inset = size.minDimension * 0.055f
        drawRoundRect(
            color = Color(0xFF5C1018),
            topLeft = Offset(inset, inset),
            size = Size(size.width - inset * 2, size.height - inset * 2),
            cornerRadius = CornerRadius(8f, 8f),
            style = Stroke(width = stroke * 0.8f),
        )
        val cx = size.width / 2f
        val cy = size.height / 2f
        val arm = size.minDimension * 0.16f
        drawLine(GoldLine, Offset(cx - arm, cy), Offset(cx + arm, cy), strokeWidth = stroke)
        drawLine(GoldLine, Offset(cx, cy - arm), Offset(cx, cy + arm), strokeWidth = stroke)
    }
}
