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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
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
import androidx.compose.ui.unit.em
import com.mun0n.spider.game.Card
import com.mun0n.spider.game.Rank
import com.mun0n.spider.game.Suit

private val CardFaceColor = Color.White
private val CardEdge = Color(0xFFB9B9B9)
private val BackRed = Color(0xFF8E1B2C)
private val BackEdge = Color(0xFF5C1018)
private val GoldLine = Color(0xFFE7C27A)
private val PipRed = Color(0xFFE10600)
private val PipBlack = Color(0xFF161616)

val HintGold = Color(0xFFFFD56A)
val SelectGold = Color(0xFFF2C14E)
val CardCorner = 6.dp

private val Tight = TextStyle(platformStyle = PlatformTextStyle(includeFontPadding = false))

private val PipPositions: Map<Int, List<Pair<Float, Float>>> = mapOf(
    2 to listOf(0.50f to 0.30f, 0.50f to 0.70f),
    3 to listOf(0.50f to 0.27f, 0.50f to 0.50f, 0.50f to 0.73f),
    4 to listOf(0.33f to 0.28f, 0.67f to 0.28f, 0.33f to 0.72f, 0.67f to 0.72f),
    5 to listOf(0.33f to 0.28f, 0.67f to 0.28f, 0.50f to 0.50f, 0.33f to 0.72f, 0.67f to 0.72f),
    6 to listOf(0.33f to 0.24f, 0.67f to 0.24f, 0.33f to 0.50f, 0.67f to 0.50f, 0.33f to 0.76f, 0.67f to 0.76f),
    7 to listOf(
        0.33f to 0.22f, 0.67f to 0.22f, 0.50f to 0.36f,
        0.33f to 0.50f, 0.67f to 0.50f, 0.33f to 0.78f, 0.67f to 0.78f,
    ),
    8 to listOf(
        0.33f to 0.20f, 0.67f to 0.20f, 0.50f to 0.35f,
        0.33f to 0.47f, 0.67f to 0.47f, 0.50f to 0.65f,
        0.33f to 0.80f, 0.67f to 0.80f,
    ),
    9 to listOf(
        0.33f to 0.18f, 0.67f to 0.18f, 0.33f to 0.36f, 0.67f to 0.36f,
        0.50f to 0.50f, 0.33f to 0.64f, 0.67f to 0.64f, 0.33f to 0.82f, 0.67f to 0.82f,
    ),
    10 to listOf(
        0.33f to 0.17f, 0.67f to 0.17f, 0.50f to 0.30f,
        0.33f to 0.39f, 0.67f to 0.39f, 0.33f to 0.61f, 0.67f to 0.61f,
        0.50f to 0.70f, 0.33f to 0.83f, 0.67f to 0.83f,
    ),
)

@Composable
fun PlayingCard(
    card: Card,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    hinted: Boolean = false,
) {
    val shape = RoundedCornerShape(CardCorner)
    val borderColor = when {
        selected -> SelectGold
        hinted -> HintGold
        card.faceUp -> CardEdge
        else -> BackEdge
    }
    Box(
        modifier
            .shadow(elevation = if (card.faceUp) 3.dp else 2.dp, shape = shape, clip = false)
            .clip(shape)
            .background(if (card.faceUp) CardFaceColor else BackRed)
            .border(width = if (selected || hinted) 2.5.dp else 1.dp, color = borderColor, shape = shape)
            .semantics {
                contentDescription = if (card.faceUp) card.description() else "carta boca abajo"
            },
    ) {
        if (card.faceUp) CardFace(card) else CardBackFace()
    }
}

@Composable
fun CardBackFace(modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxSize().padding(5.dp)) {
        val stroke = size.minDimension * 0.018f
        drawRoundRect(
            color = GoldLine,
            cornerRadius = CornerRadius(10f, 10f),
            style = Stroke(width = stroke),
        )
        val inset = size.minDimension * 0.055f
        drawRoundRect(
            color = BackEdge,
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

@Composable
private fun CardFace(card: Card) {
    val color = if (card.suit.red) PipRed else PipBlack
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val ten = card.rank == Rank.TEN
        val rankDp = maxWidth * if (ten) 0.175f else 0.24f
        val mark = maxWidth * 0.16f
        val rankStyle = Tight.copy(
            color = color,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Black,
            fontSize = with(density) { rankDp.toSp() },
            lineHeight = with(density) { rankDp.toSp() },
            letterSpacing = if (ten) (-0.06).em else 0.em,
            textAlign = TextAlign.Center,
        )
        val indexPadH = maxWidth * 0.035f
        val indexPadV = maxHeight * 0.008f
        if (card.rank == Rank.ACE) {
            SuitMark(
                card.suit,
                color,
                Modifier.align(Alignment.Center).size(maxWidth * 0.52f),
            )
        } else if (card.rank.value >= Rank.JACK.value) {
            CourtCard(card = card, redSuit = card.suit.red, modifier = Modifier.fillMaxSize())
        } else {
            Pips(card = card, color = color, modifier = Modifier.fillMaxSize())
        }
        Index(
            card,
            color,
            rankStyle,
            mark,
            Modifier
                .align(Alignment.TopStart)
                .background(CardFaceColor)
                .padding(start = indexPadH, top = indexPadV, end = maxWidth * 0.012f, bottom = maxHeight * 0.004f),
        )
        Index(
            card,
            color,
            rankStyle,
            mark,
            Modifier
                .align(Alignment.BottomEnd)
                .background(CardFaceColor)
                .padding(end = indexPadH, bottom = indexPadV, start = maxWidth * 0.012f, top = maxHeight * 0.004f)
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
            Modifier.padding(top = mark * 0.06f).size(mark),
        )
    }
}

@Composable
private fun CourtCard(card: Card, redSuit: Boolean, modifier: Modifier) {
    Canvas(modifier) {
        drawCourtHalf(card.rank, redSuit)
        rotate(degrees = 180f, pivot = center) {
            drawCourtHalf(card.rank, redSuit)
        }
    }
}

private val CourtInk = Color(0xFF1A120C)
private val CourtGold = Color(0xFFE6B325)
private val CourtGoldDark = Color(0xFFB8860B)
private val CourtSkin = Color(0xFFF3C9A0)
private val CourtSkinShadow = Color(0xFFE2B08C)
private val CourtHair = Color(0xFF1A120C)
private val CourtBeard = Color(0xFFF7F4EC)
private val CourtBlade = Color(0xFFE7EAF2)
private val CourtBlue = Color(0xFF1D4E9F)
private val CourtRed = Color(0xFFD0122D)

private fun DrawScope.drawCourtHalf(rank: Rank, redSuit: Boolean) {
    val w = size.width
    val h = size.height
    val line = w * 0.012f
    val x: (Float) -> Float = { f -> w * f }
    val y: (Float) -> Float = { f -> h * f }

    robe(redSuit, rank, line, x, y)
    when (rank) {
        Rank.KING -> {
            drawSword(x(0.80f), line)
            drawKingHead(line, x, y)
        }
        Rank.QUEEN -> {
            drawQueenHead(line, x, y)
            drawRose(x(0.78f), y(0.05f), w * 0.22f)
        }
        else -> {
            drawJackHead(line, x, y)
            drawHalberd(x(0.80f), line)
        }
    }
}

private fun DrawScope.robe(
    redSuit: Boolean,
    rank: Rank,
    line: Float,
    x: (Float) -> Float,
    y: (Float) -> Float,
) {
    val cloth = Path().apply {
        moveTo(x(0.20f), y(0.30f))
        quadraticTo(x(0.50f), y(0.255f), x(0.80f), y(0.30f))
        lineTo(x(0.86f), y(0.50f))
        lineTo(x(0.14f), y(0.50f))
        close()
    }
    val bands = if (redSuit) {
        listOf(CourtRed, CourtGold, CourtBlue, CourtInk, CourtGold, CourtBlue, CourtRed)
    } else {
        listOf(CourtBlue, CourtGold, CourtRed, CourtInk, CourtGold, CourtRed, CourtBlue)
    }
    clipPath(cloth) {
        bands.forEachIndexed { index, color ->
            val left = 0.12f + index * (0.76f / bands.size)
            drawRect(
                color,
                Offset(x(left), y(0.25f)),
                Size(x(0.76f / bands.size + 0.02f), y(0.26f)),
            )
        }
        checkers(x(0.30f), y(0.33f), x(0.18f), y(0.16f), CourtGold, CourtInk)
        checkers(x(0.52f), y(0.33f), x(0.16f), y(0.16f), CourtFaceColorOr(redSuit), CourtGold)
        repeat(4) { index ->
            val motif = diamond(x, y, 0.24f + index * 0.16f, 0.40f, 0.035f)
            drawPath(motif, if (index % 2 == 0) CourtGold else CourtInk)
            strokeInk(motif, line * 0.45f)
        }
        drawRect(CourtGold, Offset(x(0.14f), y(0.305f)), Size(x(0.72f), y(0.028f)))
        drawRect(CourtInk, Offset(x(0.14f), y(0.328f)), Size(x(0.72f), line * 0.4f))
    }
    strokeInk(cloth, line)
    if (rank != Rank.JACK) {
        ermine(x(0.18f), y(0.268f), x(0.16f), y(0.055f), line)
        ermine(x(0.66f), y(0.268f), x(0.16f), y(0.055f), line)
    }
}

private fun CourtFaceColorOr(redSuit: Boolean): Color = if (redSuit) CourtRed else CourtBlue

private fun diamond(
    x: (Float) -> Float,
    y: (Float) -> Float,
    cx: Float,
    cy: Float,
    s: Float,
): Path = Path().apply {
    moveTo(x(cx), y(cy - s))
    lineTo(x(cx + s * 0.7f), y(cy))
    lineTo(x(cx), y(cy + s))
    lineTo(x(cx - s * 0.7f), y(cy))
    close()
}

private fun DrawScope.checkers(left: Float, top: Float, width: Float, height: Float, a: Color, b: Color) {
    val cols = 4
    val rows = 3
    val cw = width / cols
    val ch = height / rows
    for (row in 0 until rows) {
        for (col in 0 until cols) {
            drawRect(
                if ((row + col) % 2 == 0) a else b,
                Offset(left + col * cw, top + row * ch),
                Size(cw + 0.6f, ch + 0.6f),
            )
        }
    }
}

private fun DrawScope.ermine(left: Float, top: Float, width: Float, height: Float, line: Float) {
    drawRoundRect(CourtBeard, Offset(left, top), Size(width, height), CornerRadius(line, line))
    drawRoundRect(
        CourtInk,
        Offset(left, top),
        Size(width, height),
        CornerRadius(line, line),
        style = Stroke(width = line * 0.65f),
    )
    listOf(0.28f to 0.42f, 0.62f to 0.42f, 0.45f to 0.72f).forEach { (sx, sy) ->
        drawCircle(CourtInk, width * 0.055f, Offset(left + width * sx, top + height * sy))
    }
}

private fun DrawScope.drawKingHead(line: Float, x: (Float) -> Float, y: (Float) -> Float) {
    drawClassicCrown(king = true, line = line)
    drawOval(CourtBeard, Offset(x(0.33f), y(0.16f)), Size(x(0.34f), y(0.16f)))
    drawFace(x, y, bearded = true, line = line)
    val beard = Path().apply {
        moveTo(x(0.38f), y(0.23f))
        lineTo(x(0.62f), y(0.23f))
        lineTo(x(0.64f), y(0.31f))
        lineTo(x(0.54f), y(0.285f))
        lineTo(x(0.50f), y(0.33f))
        lineTo(x(0.46f), y(0.285f))
        lineTo(x(0.36f), y(0.31f))
        close()
    }
    drawPath(beard, CourtBeard)
    strokeInk(beard, line * 0.7f)
    val stache = Path().apply {
        moveTo(x(0.40f), y(0.22f))
        quadraticTo(x(0.46f), y(0.20f), x(0.50f), y(0.222f))
        quadraticTo(x(0.54f), y(0.20f), x(0.60f), y(0.22f))
        quadraticTo(x(0.54f), y(0.235f), x(0.50f), y(0.224f))
        quadraticTo(x(0.46f), y(0.235f), x(0.40f), y(0.22f))
        close()
    }
    drawPath(stache, CourtBeard)
    strokeInk(stache, line * 0.55f)
}

private fun DrawScope.drawQueenHead(line: Float, x: (Float) -> Float, y: (Float) -> Float) {
    drawOval(CourtHair, Offset(x(0.30f), y(0.10f)), Size(x(0.12f), y(0.18f)))
    drawOval(CourtHair, Offset(x(0.58f), y(0.10f)), Size(x(0.12f), y(0.18f)))
    drawClassicCrown(king = false, line = line)
    drawFace(x, y, bearded = false, line = line)
    drawCircle(PipRed, size.width * 0.012f, Offset(x(0.50f), y(0.245f)))
}

private fun DrawScope.drawJackHead(line: Float, x: (Float) -> Float, y: (Float) -> Float) {
    drawJackHat(line)
    drawFace(x, y, bearded = false, line = line)
}

private fun DrawScope.drawFace(
    x: (Float) -> Float,
    y: (Float) -> Float,
    bearded: Boolean,
    line: Float,
) {
    val head = Offset(x(0.37f), y(0.095f))
    val headSize = Size(x(0.26f), y(0.20f))
    drawOval(CourtSkin, head, headSize)
    drawOval(CourtInk, head, headSize, style = Stroke(width = line * 0.9f))
    drawCircle(CourtInk, size.width * 0.013f, Offset(x(0.44f), y(0.165f)))
    drawCircle(CourtInk, size.width * 0.013f, Offset(x(0.55f), y(0.160f)))
    drawCircle(Color.White, size.width * 0.0045f, Offset(x(0.446f), y(0.161f)))
    val nose = Path().apply {
        moveTo(x(0.50f), y(0.162f))
        lineTo(x(0.57f), y(0.21f))
        lineTo(x(0.48f), y(0.205f))
        close()
    }
    drawPath(nose, CourtSkinShadow)
    strokeInk(nose, line * 0.45f)
    if (!bearded) {
        drawLine(
            PipRed,
            Offset(x(0.47f), y(0.23f)),
            Offset(x(0.55f), y(0.228f)),
            strokeWidth = line * 0.75f,
            cap = StrokeCap.Round,
        )
    }
    drawRoundRect(
        CourtSkin,
        Offset(x(0.46f), y(0.25f)),
        Size(x(0.08f), y(0.045f)),
        CornerRadius(x(0.02f), y(0.015f)),
    )
}

private fun DrawScope.drawClassicCrown(king: Boolean, line: Float) {
    val w = size.width
    val h = size.height
    val bandTop = if (king) h * 0.078f else h * 0.092f
    val bandH = h * 0.028f
    if (king) {
        val cap = Path().apply {
            moveTo(w * 0.36f, bandTop)
            quadraticTo(w * 0.50f, h * 0.012f, w * 0.64f, bandTop)
            close()
        }
        drawPath(cap, CourtRed)
        strokeInk(cap, line * 0.6f)
        listOf(0.40f, 0.60f).forEach { cx ->
            val peak = Path().apply {
                moveTo(w * (cx - 0.055f), bandTop)
                lineTo(w * cx, h * 0.018f)
                lineTo(w * (cx + 0.055f), bandTop)
                close()
            }
            drawPath(peak, CourtGold)
            strokeInk(peak, line * 0.7f)
            drawCircle(CourtGold, w * 0.012f, Offset(w * cx, h * 0.016f))
        }
        drawRect(CourtGold, Offset(w * 0.485f, h * 0.006f), Size(w * 0.03f, bandTop))
        drawRect(CourtGold, Offset(w * 0.455f, h * 0.030f), Size(w * 0.09f, h * 0.012f))
    } else {
        listOf(0.40f, 0.50f, 0.60f).forEach { cx ->
            val arch = Path().apply {
                moveTo(w * (cx - 0.06f), bandTop)
                quadraticTo(w * cx, bandTop - h * 0.045f, w * (cx + 0.06f), bandTop)
                close()
            }
            drawPath(arch, CourtGold)
            strokeInk(arch, line * 0.7f)
            drawCircle(CourtGold, w * 0.012f, Offset(w * cx, bandTop - h * 0.038f))
        }
    }
    drawRoundRect(
        CourtGold,
        Offset(w * 0.32f, bandTop),
        Size(w * 0.36f, bandH),
        CornerRadius(w * 0.01f, w * 0.01f),
    )
    drawRoundRect(
        CourtInk,
        Offset(w * 0.32f, bandTop),
        Size(w * 0.36f, bandH),
        CornerRadius(w * 0.01f, w * 0.01f),
        style = Stroke(width = line * 0.75f),
    )
    drawCircle(PipRed, w * 0.011f, Offset(w * 0.40f, bandTop + bandH / 2f))
    drawCircle(CourtBlue, w * 0.011f, Offset(w * 0.50f, bandTop + bandH / 2f))
    drawCircle(PipRed, w * 0.011f, Offset(w * 0.60f, bandTop + bandH / 2f))
}

private fun DrawScope.drawJackHat(line: Float) {
    val w = size.width
    val h = size.height
    val cap = Path().apply {
        moveTo(w * 0.32f, h * 0.105f)
        quadraticTo(w * 0.34f, h * 0.035f, w * 0.50f, h * 0.030f)
        quadraticTo(w * 0.68f, h * 0.035f, w * 0.70f, h * 0.105f)
        close()
    }
    drawPath(cap, CourtBlue)
    strokeInk(cap, line * 0.8f)
    drawRoundRect(
        CourtGold,
        Offset(w * 0.26f, h * 0.095f),
        Size(w * 0.50f, h * 0.028f),
        CornerRadius(w * 0.012f, h * 0.01f),
    )
    drawRoundRect(
        CourtInk,
        Offset(w * 0.26f, h * 0.095f),
        Size(w * 0.50f, h * 0.028f),
        CornerRadius(w * 0.012f, h * 0.01f),
        style = Stroke(width = line * 0.6f),
    )
    val feather = Path().apply {
        moveTo(w * 0.66f, h * 0.09f)
        quadraticTo(w * 0.92f, h * 0.0f, w * 0.78f, h * 0.15f)
        quadraticTo(w * 0.86f, h * 0.04f, w * 0.66f, h * 0.09f)
        close()
    }
    drawPath(feather, CourtGold)
    strokeInk(feather, line * 0.65f)
}

private fun DrawScope.drawSword(x: Float, line: Float) {
    val h = size.height
    val w = size.width
    val blade = Path().apply {
        moveTo(x - w * 0.018f, h * 0.30f)
        lineTo(x - w * 0.006f, h * 0.012f)
        lineTo(x + w * 0.006f, h * 0.012f)
        lineTo(x + w * 0.018f, h * 0.30f)
        close()
    }
    drawPath(blade, CourtBlade)
    strokeInk(blade, line * 0.7f)
    drawLine(Color(0xFFB7BCC8), Offset(x, h * 0.02f), Offset(x, h * 0.29f), strokeWidth = line * 0.35f)
    drawRoundRect(CourtGold, Offset(x - w * 0.055f, h * 0.285f), Size(w * 0.11f, h * 0.018f), CornerRadius(line, line))
    drawRoundRect(CourtGoldDark, Offset(x - w * 0.016f, h * 0.30f), Size(w * 0.032f, h * 0.055f), CornerRadius(line, line))
    drawCircle(CourtGold, w * 0.02f, Offset(x, h * 0.365f))
}

private fun DrawScope.drawRose(cx: Float, cy: Float, s: Float) {
    drawLine(
        Color(0xFF2E6B32),
        Offset(cx, cy + s * 0.2f),
        Offset(cx, size.height * 0.42f),
        strokeWidth = s * 0.08f,
        cap = StrokeCap.Round,
    )
    drawOval(Color(0xFF2E6B32), Offset(cx - s * 0.02f, cy + s * 0.85f), Size(s * 0.42f, s * 0.2f))
    listOf(0f to -0.28f, 0.30f to -0.06f, 0.18f to 0.24f, -0.18f to 0.24f, -0.30f to -0.06f).forEach { (dx, dy) ->
        drawCircle(PipRed, s * 0.2f, Offset(cx + dx * s, cy + dy * s))
    }
    drawCircle(CourtGold, s * 0.1f, Offset(cx, cy))
}

private fun DrawScope.drawHalberd(x: Float, line: Float) {
    val h = size.height
    val w = size.width
    drawLine(CourtGoldDark, Offset(x, h * 0.015f), Offset(x, h * 0.46f), strokeWidth = w * 0.014f, cap = StrokeCap.Round)
    val spike = Path().apply {
        moveTo(x - w * 0.012f, h * 0.05f)
        lineTo(x, h * 0.004f)
        lineTo(x + w * 0.012f, h * 0.05f)
        close()
    }
    drawPath(spike, CourtBlade)
    strokeInk(spike, line * 0.55f)
    val axe = Path().apply {
        moveTo(x, h * 0.04f)
        quadraticTo(x + w * 0.20f, h * 0.055f, x + w * 0.18f, h * 0.15f)
        quadraticTo(x + w * 0.08f, h * 0.11f, x, h * 0.16f)
        close()
    }
    drawPath(axe, CourtBlade)
    strokeInk(axe, line * 0.65f)
    drawCircle(CourtRed, w * 0.012f, Offset(x + w * 0.07f, h * 0.09f))
}

private fun DrawScope.strokeInk(path: Path, width: Float) {
    drawPath(path, CourtInk, style = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

@Composable
private fun Pips(card: Card, color: Color, modifier: Modifier) {
    val spots = PipPositions[card.rank.value].orEmpty()
    Canvas(modifier) {
        val side = size.width * when {
            card.rank.value >= 9 -> 0.16f
            card.rank.value >= 7 -> 0.185f
            else -> 0.23f
        }
        spots.forEach { (x, y) ->
            val cx = size.width * x
            val cy = size.height * y
            val degrees = if (y > 0.56f) 180f else 0f
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
        Suit.DIAMONDS -> drawPath(diamondPip(px, py), color)
        Suit.SPADES -> {
            drawPath(spadeLeaf(px, py), color)
            drawPath(spadeStem(px, py), color)
        }
        Suit.CLUBS -> {
            val r = side * 0.23f
            drawCircle(color, r, Offset(px(0.50f), py(0.30f)))
            drawCircle(color, r, Offset(px(0.28f), py(0.56f)))
            drawCircle(color, r, Offset(px(0.72f), py(0.56f)))
            drawPath(clubStem(px, py), color)
        }
    }
}

private fun heart(px: (Float) -> Float, py: (Float) -> Float) = Path().apply {
    moveTo(px(0.50f), py(0.92f))
    cubicTo(px(0.10f), py(0.62f), px(0.0f), py(0.40f), px(0.04f), py(0.28f))
    cubicTo(px(0.08f), py(0.10f), px(0.28f), py(0.06f), px(0.40f), py(0.16f))
    cubicTo(px(0.46f), py(0.22f), px(0.49f), py(0.30f), px(0.50f), py(0.34f))
    cubicTo(px(0.51f), py(0.30f), px(0.54f), py(0.22f), px(0.60f), py(0.16f))
    cubicTo(px(0.72f), py(0.06f), px(0.92f), py(0.10f), px(0.96f), py(0.28f))
    cubicTo(px(1.0f), py(0.40f), px(0.90f), py(0.62f), px(0.50f), py(0.92f))
    close()
}

private fun diamondPip(px: (Float) -> Float, py: (Float) -> Float) = Path().apply {
    moveTo(px(0.50f), py(0.02f))
    lineTo(px(0.96f), py(0.50f))
    lineTo(px(0.50f), py(0.98f))
    lineTo(px(0.04f), py(0.50f))
    close()
}

private fun spadeLeaf(px: (Float) -> Float, py: (Float) -> Float) = Path().apply {
    moveTo(px(0.50f), py(0.02f))
    cubicTo(px(0.14f), py(0.26f), px(0.0f), py(0.42f), px(0.02f), py(0.58f))
    cubicTo(px(0.04f), py(0.78f), px(0.24f), py(0.84f), px(0.38f), py(0.70f))
    cubicTo(px(0.44f), py(0.64f), px(0.48f), py(0.60f), px(0.50f), py(0.54f))
    cubicTo(px(0.52f), py(0.60f), px(0.56f), py(0.64f), px(0.62f), py(0.70f))
    cubicTo(px(0.76f), py(0.84f), px(0.96f), py(0.78f), px(0.98f), py(0.58f))
    cubicTo(px(1.0f), py(0.42f), px(0.86f), py(0.26f), px(0.50f), py(0.02f))
    close()
}

private fun spadeStem(px: (Float) -> Float, py: (Float) -> Float) = Path().apply {
    moveTo(px(0.42f), py(0.60f))
    quadraticTo(px(0.38f), py(0.80f), px(0.30f), py(0.98f))
    lineTo(px(0.70f), py(0.98f))
    quadraticTo(px(0.62f), py(0.80f), px(0.58f), py(0.60f))
    close()
}

private fun clubStem(px: (Float) -> Float, py: (Float) -> Float) = Path().apply {
    moveTo(px(0.44f), py(0.62f))
    quadraticTo(px(0.40f), py(0.80f), px(0.32f), py(0.98f))
    lineTo(px(0.68f), py(0.98f))
    quadraticTo(px(0.60f), py(0.80f), px(0.56f), py(0.62f))
    close()
}
