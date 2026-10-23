import AppKit

let outDir = "/Users/mun0n/Spider/app/src/main/res/drawable-nodpi"

func load(_ path: String) -> NSBitmapImageRep {
    let img = NSImage(contentsOfFile: path)!
    var rect = NSRect(origin: .zero, size: img.size)
    let cg = img.cgImage(forProposedRect: &rect, context: nil, hints: nil)!
    return NSBitmapImageRep(cgImage: cg)
}

func sample(_ rep: NSBitmapImageRep, _ x: Int, _ y: Int) -> (Int, Int, Int) {
    let c = rep.colorAt(x: x, y: y) ?? .white
    return (
        Int((c.redComponent * 255).rounded()),
        Int((c.greenComponent * 255).rounded()),
        Int((c.blueComponent * 255).rounded())
    )
}

enum Mode { case keep, redToBlack, blackToRed }

func isCostumeRed(_ r: Int, _ g: Int, _ b: Int) -> Bool {
    r > 120 && g < 90 && b < 110 && r > g + 50 && r > b + 35
}

func isCostumeBlack(_ r: Int, _ g: Int, _ b: Int) -> Bool {
    r < 48 && g < 48 && b < 60 && abs(r - g) < 16 && b < r + 22
}

func mapped(_ p: (Int, Int, Int), _ mode: Mode) -> (UInt8, UInt8, UInt8) {
    var (r, g, b) = p
    switch mode {
    case .keep:
        break
    case .redToBlack:
        if isCostumeRed(r, g, b) {
            r = 16
            g = 16
            b = 20
        }
    case .blackToRed:
        if isCostumeBlack(r, g, b) {
            r = 196
            g = 18
            b = 28
        }
    }
    return (UInt8(r), UInt8(g), UInt8(b))
}

func paint(_ src: NSBitmapImageRep, _ mode: Mode, crop: NSRect? = nil, scaleTo width: Int? = nil) -> NSBitmapImageRep {
    let bounds = crop ?? NSRect(x: 0, y: 0, width: src.pixelsWide, height: src.pixelsHigh)
    let cw = Int(bounds.width)
    let ch = Int(bounds.height)
    let targetW = width ?? cw
    let targetH = max(1, Int((Double(ch) * Double(targetW) / Double(cw)).rounded()))
    let dst = NSBitmapImageRep(
        bitmapDataPlanes: nil,
        pixelsWide: targetW,
        pixelsHigh: targetH,
        bitsPerSample: 8,
        samplesPerPixel: 4,
        hasAlpha: true,
        isPlanar: false,
        colorSpaceName: .deviceRGB,
        bytesPerRow: targetW * 4,
        bitsPerPixel: 32
    )!
    let ox = Int(bounds.origin.x)
    let oy = Int(bounds.origin.y)
    guard let raw = dst.bitmapData else { fatalError("no buffer") }
    for y in 0..<targetH {
        let sy = oy + min(ch - 1, Int(Double(y) * Double(ch) / Double(targetH)))
        for x in 0..<targetW {
            let sx = ox + min(cw - 1, Int(Double(x) * Double(cw) / Double(targetW)))
            let (r, g, b) = mapped(sample(src, sx, sy), mode)
            let i = (y * targetW + x) * 4
            raw[i] = r
            raw[i + 1] = g
            raw[i + 2] = b
            raw[i + 3] = 255
        }
    }
    return dst
}

func eraseCornerIndices(_ rep: NSBitmapImageRep) {
    guard let raw = rep.bitmapData else { return }
    let w = rep.pixelsWide
    let h = rep.pixelsHigh
    let row = rep.bytesPerRow
    let bpp = max(3, rep.bitsPerPixel / 8)
    func paper(_ x: Int, _ y: Int) -> Bool {
        let i = y * row + x * bpp
        return raw[i] > 230 && raw[i + 1] > 230 && raw[i + 2] > 230
    }
    func white(_ x: Int, _ y: Int) {
        let i = y * row + x * bpp
        raw[i] = 255
        raw[i + 1] = 255
        raw[i + 2] = 255
        if bpp > 3 { raw[i + 3] = 255 }
    }
    func border(_ x: Int, _ y: Int) -> Bool {
        let m = max(3, min(w, h) / 30)
        if x < m || y < m || x >= w - m || y >= h - m { return !paper(x, y) }
        return false
    }
    var seen = [Bool](repeating: false, count: w * h)
    var stack: [(Int, Int)] = []
    for seed in [(w / 2, h / 2), (w / 2, h / 3), (w / 2, 2 * h / 3), (w / 3, h / 2), (2 * w / 3, h / 2)] {
        if !paper(seed.0, seed.1) && !border(seed.0, seed.1) { stack.append(seed) }
    }
    while let point = stack.popLast() {
        let x = point.0
        let y = point.1
        if x < 0 || y < 0 || x >= w || y >= h { continue }
        let i = y * w + x
        if seen[i] { continue }
        seen[i] = true
        if paper(x, y) || border(x, y) { continue }
        stack.append((x + 1, y))
        stack.append((x - 1, y))
        stack.append((x, y + 1))
        stack.append((x, y - 1))
    }
    let rw = Int(Double(w) * 0.42)
    let rh = Int(Double(h) * 0.46)
    var cleared = 0
    for region in [(0, 0, rw, rh), (w - rw, h - rh, w, h)] {
        for y in region.1..<region.3 {
            for x in region.0..<region.2 {
                if paper(x, y) || border(x, y) { continue }
                if seen[y * w + x] { continue }
                white(x, y)
                cleared += 1
            }
        }
    }
    print("cleared", cleared)
    wipeSolidPips(rep, raw: raw, row: row, bpp: bpp)
}

func wipeSolidPips(_ rep: NSBitmapImageRep, raw: UnsafeMutablePointer<UInt8>, row: Int, bpp: Int) {
    let w = rep.pixelsWide
    let h = rep.pixelsHigh
    func at(_ x: Int, _ y: Int) -> (Int, Int, Int) {
        let i = y * row + x * bpp
        return (Int(raw[i]), Int(raw[i + 1]), Int(raw[i + 2]))
    }
    func filled(_ x: Int, _ y: Int) -> Bool {
        if x < 0 || y < 0 || x >= w || y >= h { return false }
        let (r, g, b) = at(x, y)
        let red = r > 150 && g < 110 && b < 130 && r > g + 40
        let black = r < 42 && g < 42 && b < 58
        return red || black
    }
    func white(_ x: Int, _ y: Int) {
        let i = y * row + x * bpp
        raw[i] = 255
        raw[i + 1] = 255
        raw[i + 2] = 255
        if bpp > 3 { raw[i + 3] = 255 }
    }
    let zones = [
        (0, 0, Int(Double(w) * 0.40), Int(Double(h) * 0.40)),
        (Int(Double(w) * 0.60), Int(Double(h) * 0.60), w, h),
    ]
    for zone in zones {
        var visited = Set<Int>()
        var best: [(Int, Int)] = []
        for y in zone.1..<zone.3 {
            for x in zone.0..<zone.2 where filled(x, y) {
                let key = y * w + x
                if visited.contains(key) { continue }
                var stack = [(x, y)]
                var pixels: [(Int, Int)] = []
                var minX = w, minY = h, maxX = 0, maxY = 0
                var escaped = false
                while let p = stack.popLast() {
                    let k = p.1 * w + p.0
                    if visited.contains(k) || !filled(p.0, p.1) { continue }
                    visited.insert(k)
                    if p.0 < zone.0 || p.1 < zone.1 || p.0 >= zone.2 || p.1 >= zone.3 {
                        escaped = true
                        continue
                    }
                    pixels.append(p)
                    if p.0 < minX { minX = p.0 }
                    if p.1 < minY { minY = p.1 }
                    if p.0 > maxX { maxX = p.0 }
                    if p.1 > maxY { maxY = p.1 }
                    stack.append((p.0 + 1, p.1))
                    stack.append((p.0 - 1, p.1))
                    stack.append((p.0, p.1 + 1))
                    stack.append((p.0, p.1 - 1))
                }
                let wide = maxX - minX
                let tall = maxY - minY
                if escaped || pixels.count < 400 || pixels.count > 22000 || wide > w / 3 || tall > h / 3 { continue }
                if pixels.count > best.count { best = pixels }
            }
        }
        for p in best { white(p.0, p.1) }
        for p in best {
            for dy in -4...4 {
                for dx in -4...4 {
                    let xx = p.0 + dx
                    let yy = p.1 + dy
                    if xx < 0 || yy < 0 || xx >= w || yy >= h { continue }
                    let (r, g, b) = at(xx, yy)
                    let paper = r > 225 && g > 225 && b > 225
                    let yellow = r > 170 && g > 130 && b < 120
                    if paper || yellow { continue }
                    white(xx, yy)
                }
            }
        }
        if !best.isEmpty { print("pip", best.count) }
    }
}

func save(_ rep: NSBitmapImageRep, _ name: String) {
    eraseCornerIndices(rep)
    let data = rep.representation(using: .png, properties: [:])!
    try! data.write(to: URL(fileURLWithPath: outDir).appendingPathComponent(name))
    print("wrote", name, rep.pixelsWide, rep.pixelsHigh)
}

let king = load("/Users/mun0n/.cursor/projects/Users-mun0n-Spider/assets/image-96591095-ad25-4365-a55a-b1b6533bddc1.png")
let jack = load("/Users/mun0n/.cursor/projects/Users-mun0n-Spider/assets/image-2a34f1b7-06c1-42f0-9d2a-9ce7ed7fd3c8.png")
let queen = load("/Users/mun0n/.cursor/projects/Users-mun0n-Spider/assets/image-bc0e9dbc-4108-4dfb-bdf6-cc86628c92f4.jpg")
let queenCrop = NSRect(x: 86, y: 80, width: 538, height: 860)

save(paint(king, .keep), "court_king_red.png")
save(paint(king, .redToBlack), "court_king_black.png")
save(paint(queen, .keep, crop: queenCrop), "court_queen_red.png")
save(paint(queen, .redToBlack, crop: queenCrop), "court_queen_black.png")
save(paint(jack, .keep), "court_jack_black.png")
save(paint(jack, .blackToRed), "court_jack_red.png")
