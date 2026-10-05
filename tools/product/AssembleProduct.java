/*
 * OpenData IDE — сборщик продукта (этап 5).
 *
 * Берёт открытую сборку IntelliJ IDEA (Apache 2.0, github.com/JetBrains/intellij-community/releases),
 * оставляет только платформу и открытый grid, добавляет плагин OpenData (DB-слой) и брендинг,
 * переименовывает launcher/vmoptions, разносит каталоги настроек и собирает дистрибутив.
 *
 * Запуск (JDK 17+, без компиляции):
 *   java tools/product/AssembleProduct.java --base <распакованная idea-oss> --plugin <opendata-db-*.zip>
 *        --drivers <каталог с bundled JAR> --out <каталог> --version 0.2.0 [--zip <файл.zip>]
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.*;
import javax.imageio.ImageIO;

@SuppressWarnings("unchecked")
public class AssembleProduct {

    /** Плагины открытой сборки, которые остаются в продукте. Всё остальное (Java, Kotlin, VCS, …) удаляется. */
    static final Set<String> KEEP_PLUGINS = Set.of("grid-core-plugin", "platform-structureView-plugin");
    static final String PRODUCT = "OpenData IDE";
    static final String CODE = "OD";
    static final String SCRIPT = "opendata";
    static final String VENDOR = "OpenData";
    static final String URL = "https://github.com/gamesmod/opendata-ide";

    public static void main(String[] args) throws Exception {
        Map<String, String> o = new HashMap<>();
        for (int i = 0; i + 1 < args.length; i += 2) o.put(args[i].replaceFirst("^--", ""), args[i + 1]);
        for (String k : List.of("base", "plugin", "out", "version"))
            if (!o.containsKey(k)) { System.err.println("missing --" + k); System.exit(2); }
        Path base = Path.of(o.get("base")), out = Path.of(o.get("out"));
        String version = o.get("version");
        new AssembleProduct().assemble(base, Path.of(o.get("plugin")), o.containsKey("drivers") ? Path.of(o.get("drivers")) : null, out, version);
        if (o.containsKey("rcedit")) setExeIcon(Path.of(o.get("rcedit")), out, version);
        if (o.containsKey("zip")) zip(out, Path.of(o.get("zip")), "OpenData-IDE-" + version);
        System.out.println("Done: " + out.toAbsolutePath());
    }

    void assemble(Path base, Path pluginZip, Path drivers, Path out, String version) throws Exception {
        if (!Files.isRegularFile(base.resolve("product-info.json"))) throw new IllegalArgumentException("Не открытая сборка IntelliJ: " + base);
        deleteTree(out);
        Files.createDirectories(out);

        // 1. Копия платформы без лишних плагинов и инструментов JetBrains.
        Set<String> skipBin = Set.of("format.sh", "inspect.sh", "ltedit.sh", "format.bat", "inspect.bat", "ltedit.bat", "idea.sh", "idea.bat", "idea.svg", "idea.png", "idea.ico");
        try (Stream<Path> s = Files.walk(base)) {
            for (Path p : (Iterable<Path>) s::iterator) {
                Path rel = base.relativize(p);
                if (rel.getNameCount() >= 2 && rel.getName(0).toString().equals("plugins") && !KEEP_PLUGINS.contains(rel.getName(1).toString())) continue;
                if (rel.toString().equals("plugins" + File.separator + "plugin-classpath.txt")) continue;
                if (rel.getNameCount() == 2 && rel.getName(0).toString().equals("bin") && skipBin.contains(rel.getName(1).toString())) continue;
                Path t = out.resolve(rel.toString());
                if (Files.isDirectory(p)) Files.createDirectories(t);
                else Files.copy(p, t, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        // 2. Плагин OpenData + драйверы, поставляемые в комплекте.
        unzip(pluginZip, out.resolve("plugins"));
        Path pluginDir;
        try (Stream<Path> s = Files.list(out.resolve("plugins"))) {
            pluginDir = s.filter(p -> p.getFileName().toString().startsWith("opendata")).findFirst().orElseThrow();
        }
        if (drivers != null) {
            Path dd = Files.createDirectories(pluginDir.resolve("drivers"));
            try (Stream<Path> s = Files.list(drivers)) {
                for (Path j : (Iterable<Path>) s.filter(p -> p.toString().endsWith(".jar"))::iterator) Files.copy(j, dd.resolve(j.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            }
        }

        // 3. Брендинг: ApplicationInfo, иконки, splash — в отдельном jar, первым в classpath.
        Map<String, Object> info = (Map<String, Object>) new Json(Files.readString(base.resolve("product-info.json"))).parse();
        String platformBuild = String.valueOf(info.get("buildNumber"));
        String platformVersion = String.valueOf(info.get("version"));
        String selector = "OpenData" + platformVersion.replaceAll("^(\\d+\\.\\d+).*", "$1");
        writeBrandingJar(out.resolve("lib/opendata-branding.jar"), version, platformBuild);
        Files.write(out.resolve("bin/" + SCRIPT + ".svg"), iconSvg(256).getBytes(StandardCharsets.UTF_8));
        ImageIO.write(iconPng(256), "png", out.resolve("bin/" + SCRIPT + ".png").toFile());
        Files.write(out.resolve("bin/" + SCRIPT + ".ico"), iconIco());
        Files.writeString(out.resolve("build.txt"), CODE + "-" + platformBuild);

        // 4. product-info.json: имя, код продукта, каталоги настроек, launcher, состав плагинов.
        info.put("name", PRODUCT);
        info.put("version", version);
        info.put("productCode", CODE);
        info.put("envVarBaseName", "OPENDATA");
        info.put("dataDirectoryName", selector);
        info.put("svgIconPath", "bin/" + SCRIPT + ".svg");
        info.put("productVendor", VENDOR);
        List<Object> launches = (List<Object>) info.get("launch");
        for (Object lo : launches) {
            Map<String, Object> l = (Map<String, Object>) lo;
            l.put("launcherPath", renameIn(out, (String) l.get("launcherPath")));
            l.put("vmOptionsFilePath", renameIn(out, (String) l.get("vmOptionsFilePath")));
            l.put("startupWmClass", "opendata-ide");
            List<Object> cp = new ArrayList<>((List<Object>) l.get("bootClassPathJarNames"));
            cp.remove("opendata-branding.jar");
            cp.add(0, "opendata-branding.jar");
            l.put("bootClassPathJarNames", cp);
            List<Object> jvm = new ArrayList<>();
            for (Object a : (List<Object>) l.get("additionalJvmArguments")) {
                String s = (String) a;
                if (s.startsWith("-Didea.vendor.name=")) s = "-Didea.vendor.name=" + VENDOR;
                if (s.startsWith("-Didea.paths.selector=")) s = "-Didea.paths.selector=" + selector;
                jvm.add(s);
            }
            // ApplicationInfo открытой сборки вшит в байт-код; штатный ключ платформы заставляет читать его из ресурсов,
            // т.е. из opendata-branding.jar (первый в classpath).
            jvm.add("-Dintellij.platform.load.app.info.from.resources=true");
            l.put("additionalJvmArguments", jvm);
            l.remove("customCommands");
        }
        List<Object> layout = new ArrayList<>();
        for (Object e : (List<Object>) info.get("layout")) {
            Map<String, Object> m = (Map<String, Object>) e;
            List<Object> cp = (List<Object>) m.getOrDefault("classPath", List.of());
            boolean removed = cp.stream().anyMatch(c -> { String s = String.valueOf(c); return s.startsWith("plugins/") && !KEEP_PLUGINS.contains(s.split("/")[1]); });
            if (!removed) layout.add(m);
        }
        String pluginName = pluginDir.getFileName().toString();
        List<Object> pluginCp = new ArrayList<>();
        try (Stream<Path> s = Files.list(pluginDir.resolve("lib"))) { s.sorted().forEach(j -> pluginCp.add("plugins/" + pluginName + "/lib/" + j.getFileName())); }
        layout.add(new LinkedHashMap<>(Map.of("name", "io.opendata.db", "kind", "plugin", "classPath", pluginCp)));
        info.put("layout", layout);
        info.put("bundledPlugins", List.of("com.intellij.platform.structureView", "intellij.grid.core.plugin", "io.opendata.db"));
        info.put("fileExtensions", List.of("*.sql", "*.ddl", "*.dml", "*.csv", "*.tsv"));
        Files.writeString(out.resolve("product-info.json"), Json.write(info, 0));

        // Прочие исполняемые файлы с именем idea*.exe (Windows) — тоже под именем продукта.
        try (Stream<Path> s = Files.list(out.resolve("bin"))) {
            for (Path p : (Iterable<Path>) s.filter(p -> p.getFileName().toString().matches("idea.*\\.exe"))::iterator)
                Files.move(p, p.resolveSibling(p.getFileName().toString().replaceFirst("^idea", SCRIPT)), StandardCopyOption.REPLACE_EXISTING);
        }

        // 5. VM options и свойства: без проверки обновлений JetBrains и без рекламы/подсказок.
        for (Object lo : launches) {
            Path vm = out.resolve((String) ((Map<String, Object>) lo).get("vmOptionsFilePath"));
            if (Files.isRegularFile(vm) && !Files.readString(vm).contains("ide.no.platform.update")) {
                Files.writeString(vm, "-Dide.no.platform.update=true\n-Dide.show.tips.on.startup.default.value=false\n-Dide.experimental.ui.onboarding=false\n", StandardOpenOption.APPEND);
            }
        }
        Files.writeString(out.resolve("NOTICE-OpenData.txt"), """
            OpenData IDE %s
            %s

            OpenData IDE built on the open-source IntelliJ Platform build %s (JetBrains/intellij-community, Apache License 2.0;
            see LICENSE.txt, NOTICE.txt and license/). Bundled components: IntelliJ Platform, grid (Data Editor and Viewer),
            JetBrains Runtime. JDBC drivers in plugins/opendata-db/drivers: PostgreSQL JDBC (BSD-2-Clause; also used for Greenplum
            and Apache Cloudberry), ClickHouse JDBC (Apache 2.0), Apache Arrow Flight SQL JDBC (Apache 2.0), SLF4J (MIT).
            "IntelliJ" and "JetBrains" are trademarks of JetBrains s.r.o.; OpenData IDE is not affiliated with JetBrains.
            """.formatted(version, URL, platformBuild));
    }

    /** bin/idea64.exe → bin/opendata64.exe, bin/idea64.vmoptions → bin/opendata64.vmoptions и т.п. */
    static String renameIn(Path root, String rel) throws IOException {
        if (rel == null) return null;
        Path p = root.resolve(rel);
        String name = p.getFileName().toString();
        String renamed = name.replaceFirst("^idea", SCRIPT);
        if (!renamed.equals(name) && Files.exists(p)) Files.move(p, p.resolveSibling(renamed), StandardCopyOption.REPLACE_EXISTING);
        return rel.substring(0, rel.length() - name.length()) + renamed;
    }

    void writeBrandingJar(Path jar, String version, String platformBuild) throws Exception {
        String[] v = (version + ".0.0").split("\\.");
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String appInfo = """
            <component xmlns="http://jetbrains.org/intellij/schema/application-info">
              <version major="%s" minor="%s" micro="%s"/>
              <company name="%s" url="%s" copyrightStart="2026"/>
              <build number="%s-%s" date="%s" majorReleaseDate="%s"/>
              <logo url="/opendata_logo.png"/>
              <icon svg="/opendata.svg" svg-small="/opendata_16.svg"/>
              <icon-eap svg="/opendata.svg" svg-small="/opendata_16.svg"/>
              <names product="OpenData" fullname="%s" script="%s" motto="Open database IDE"/>
              <essential-plugin>io.opendata.db</essential-plugin>
              <essential-plugin>intellij.grid.core.plugin</essential-plugin>
            </component>
            """.formatted(v[0], v[1], v[2], VENDOR, URL, CODE, platformBuild, date, date.substring(0, 8), PRODUCT, SCRIPT);
        Files.createDirectories(jar.getParent());
        try (ZipOutputStream z = new ZipOutputStream(Files.newOutputStream(jar))) {
            put(z, "idea/IdeaApplicationInfo.xml", appInfo.getBytes(StandardCharsets.UTF_8));
            put(z, "opendata.svg", iconSvg(32).getBytes(StandardCharsets.UTF_8));
            put(z, "opendata_16.svg", iconSvg(16).getBytes(StandardCharsets.UTF_8));
            ByteArrayOutputStream png = new ByteArrayOutputStream();
            ImageIO.write(splash(version, platformBuild), "png", png);
            put(z, "opendata_logo.png", png.toByteArray());
        }
    }

    static void put(ZipOutputStream z, String name, byte[] data) throws IOException {
        z.putNextEntry(new ZipEntry(name));
        z.write(data);
        z.closeEntry();
    }

    // Значок OpenData IDE: скруглённый квадрат с градиентом (бирюзовый → синий), белый «цилиндр» из трёх дисков
    // с янтарной крышкой. Одна геометрия (сетка 256×256) для SVG, PNG и ICO.
    static final String C_TOP = "#12B5A6", C_BOTTOM = "#1D4ED8", C_LID = "#FFC23D";
    static final int[][] BANDS = {{84, 116}, {128, 160}, {172, 204}};

    static String iconSvg(int size) {
        StringBuilder bands = new StringBuilder();
        for (int[] b : BANDS)
            bands.append("  <path d=\"M60 ").append(b[0]).append(" A68 24 0 0 0 196 ").append(b[0]).append(" V").append(b[1])
                .append(" A68 24 0 0 1 60 ").append(b[1]).append(" Z\" fill=\"#FFFFFF\"/>\n");
        return """
            <svg xmlns="http://www.w3.org/2000/svg" width="%1$d" height="%1$d" viewBox="0 0 256 256">
              <defs>
                <linearGradient id="od-bg" x1="0" y1="0" x2="1" y2="1">
                  <stop offset="0" stop-color="%2$s"/>
                  <stop offset="1" stop-color="%3$s"/>
                </linearGradient>
              </defs>
              <rect width="256" height="256" rx="56" fill="url(#od-bg)"/>
            %4$s  <ellipse cx="128" cy="84" rx="68" ry="24" fill="%5$s"/>
            </svg>
            """.formatted(size, C_TOP, C_BOTTOM, bands, C_LID);
    }

    static BufferedImage iconPng(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.scale(size / 256.0, size / 256.0);
        g.setPaint(new java.awt.GradientPaint(0, 0, Color.decode(C_TOP), 256, 256, Color.decode(C_BOTTOM)));
        g.fill(new java.awt.geom.RoundRectangle2D.Double(0, 0, 256, 256, 112, 112));
        g.setColor(Color.WHITE);
        for (int[] b : BANDS) {
            java.awt.geom.Path2D.Double p = new java.awt.geom.Path2D.Double();
            p.moveTo(60, b[0]);
            p.append(new java.awt.geom.Arc2D.Double(60, b[0] - 24, 136, 48, 180, 180, java.awt.geom.Arc2D.OPEN), true);
            p.lineTo(196, b[1]);
            p.append(new java.awt.geom.Arc2D.Double(60, b[1] - 24, 136, 48, 0, -180, java.awt.geom.Arc2D.OPEN), true);
            p.closePath();
            g.fill(p);
        }
        g.setColor(Color.decode(C_LID));
        g.fill(new java.awt.geom.Ellipse2D.Double(60, 60, 136, 48));
        g.dispose();
        return img;
    }

    /** ICO с PNG-кадрами 16–256 px (Windows Vista+): для opendata64.exe (rcedit) и bin/opendata.ico. */
    static byte[] iconIco() throws IOException {
        int[] sizes = {16, 20, 24, 32, 40, 48, 64, 128, 256};
        java.util.List<byte[]> frames = new java.util.ArrayList<>();
        for (int sz : sizes) {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            ImageIO.write(iconPng(sz), "png", b);
            frames.add(b.toByteArray());
        }
        java.nio.ByteBuffer bb = java.nio.ByteBuffer.allocate(6 + 16 * sizes.length + frames.stream().mapToInt(f -> f.length).sum())
            .order(java.nio.ByteOrder.LITTLE_ENDIAN);
        bb.putShort((short) 0).putShort((short) 1).putShort((short) sizes.length);
        int offset = 6 + 16 * sizes.length;
        for (int i = 0; i < sizes.length; i++) {
            bb.put((byte) (sizes[i] >= 256 ? 0 : sizes[i])).put((byte) (sizes[i] >= 256 ? 0 : sizes[i]))
                .put((byte) 0).put((byte) 0).putShort((short) 1).putShort((short) 32)
                .putInt(frames.get(i).length).putInt(offset);
            offset += frames.get(i).length;
        }
        frames.forEach(bb::put);
        return bb.array();
    }

    static BufferedImage splash(String version, String platformBuild) {
        int w = 640, h = 360;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(0x1F2329));
        g.fillRect(0, 0, w, h);
        g.drawImage(iconPng(72), 44, 44, null);
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 40));
        g.drawString(PRODUCT, 48, 170);
        g.setColor(new Color(0xA9B1BA));
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        g.drawString("Version " + version, 48, 205);
        g.drawString("PostgreSQL · Greenplum · Cloudberry · ClickHouse · Dremio", 48, 230);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        g.drawString("Built on the open-source IntelliJ Platform " + platformBuild + " (Apache License 2.0)", 48, h - 40);
        g.dispose();
        return img;
    }

    /**
     * Значок и сведения о версии в ресурсах bin/opendata64.exe (Windows): rcedit (electron/rcedit, MIT).
     * Запускается только на Windows; на других ОС шаг пропускается с предупреждением.
     */
    static void setExeIcon(Path rcedit, Path out, String version) throws Exception {
        Path exe = out.resolve("bin/" + SCRIPT + "64.exe");
        if (!Files.isRegularFile(exe)) return;
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            System.err.println("WARNING: rcedit запускается только на Windows — значок opendata64.exe не изменён");
            return;
        }
        Process p = new ProcessBuilder(rcedit.toString(), exe.toString(),
            "--set-icon", out.resolve("bin/" + SCRIPT + ".ico").toString(),
            "--set-version-string", "ProductName", PRODUCT,
            "--set-version-string", "FileDescription", PRODUCT,
            "--set-version-string", "CompanyName", "OpenData",
            "--set-version-string", "LegalCopyright", "OpenData IDE on the open-source IntelliJ Platform (Apache 2.0)",
            "--set-version-string", "InternalName", SCRIPT + "64",
            "--set-version-string", "OriginalFilename", SCRIPT + "64.exe",
            "--set-product-version", version).inheritIO().start();
        if (p.waitFor() != 0) throw new IOException("rcedit завершился с кодом " + p.exitValue());
        System.out.println("Значок и версия записаны в " + exe);
    }

    static void unzip(Path zip, Path dir) throws IOException {
        try (ZipInputStream in = new ZipInputStream(Files.newInputStream(zip))) {
            for (ZipEntry e; (e = in.getNextEntry()) != null; ) {
                Path t = dir.resolve(e.getName()).normalize();
                if (!t.startsWith(dir)) throw new IOException("bad entry " + e.getName());
                if (e.isDirectory()) Files.createDirectories(t);
                else { Files.createDirectories(t.getParent()); Files.copy(in, t, StandardCopyOption.REPLACE_EXISTING); }
            }
        }
    }

    /** ZIP-дистрибутив (Windows): каталог верхнего уровня [topDir]. */
    static void zip(Path dir, Path zipFile, String topDir) throws IOException {
        Files.createDirectories(zipFile.toAbsolutePath().getParent());
        try (ZipOutputStream z = new ZipOutputStream(Files.newOutputStream(zipFile)); Stream<Path> s = Files.walk(dir)) {
            for (Path p : (Iterable<Path>) s.sorted()::iterator) {
                if (Files.isDirectory(p)) continue;
                z.putNextEntry(new ZipEntry(topDir + "/" + dir.relativize(p).toString().replace('\\', '/')));
                Files.copy(p, z);
                z.closeEntry();
            }
        }
    }

    static void deleteTree(Path p) throws IOException {
        if (!Files.exists(p)) return;
        Files.walkFileTree(p, new SimpleFileVisitor<>() {
            @Override public FileVisitResult visitFile(Path f, BasicFileAttributes a) throws IOException { Files.delete(f); return FileVisitResult.CONTINUE; }
            @Override public FileVisitResult postVisitDirectory(Path d, IOException e) throws IOException { Files.delete(d); return FileVisitResult.CONTINUE; }
        });
    }

    // ------------------------------------------------------------------ минимальный JSON (product-info.json)

    static final class Json {
        final String s; int i;
        Json(String s) { this.s = s; }
        Object parse() { ws(); return value(); }
        Object value() {
            ws();
            char c = s.charAt(i);
            if (c == '{') { i++; Map<String, Object> m = new LinkedHashMap<>(); ws(); if (s.charAt(i) == '}') { i++; return m; }
                while (true) { ws(); String k = str(); ws(); i++; m.put(k, value()); ws(); if (s.charAt(i++) == ',') continue; return m; } }
            if (c == '[') { i++; List<Object> l = new ArrayList<>(); ws(); if (s.charAt(i) == ']') { i++; return l; }
                while (true) { l.add(value()); ws(); if (s.charAt(i++) == ',') continue; return l; } }
            if (c == '"') return str();
            if (s.startsWith("true", i)) { i += 4; return true; }
            if (s.startsWith("false", i)) { i += 5; return false; }
            if (s.startsWith("null", i)) { i += 4; return null; }
            int st = i; while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
            return new Num(s.substring(st, i));
        }
        String str() {
            i++; StringBuilder b = new StringBuilder();
            while (true) { char c = s.charAt(i++); if (c == '"') return b.toString();
                if (c == '\\') { char e = s.charAt(i++); switch (e) { case 'n' -> b.append('\n'); case 't' -> b.append('\t'); case 'r' -> b.append('\r');
                    case 'u' -> { b.append((char) Integer.parseInt(s.substring(i, i + 4), 16)); i += 4; } default -> b.append(e); } }
                else b.append(c); }
        }
        void ws() { while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++; }

        record Num(String text) {}

        static String write(Object v, int indent) {
            String pad = "  ".repeat(indent + 1), end = "  ".repeat(indent);
            if (v == null) return "null";
            if (v instanceof Num n) return n.text();
            if (v instanceof Boolean || v instanceof Number) return v.toString();
            if (v instanceof String s) return quote(s);
            if (v instanceof Map<?, ?> m) {
                if (m.isEmpty()) return "{}";
                StringJoiner j = new StringJoiner(",\n", "{\n", "\n" + end + "}");
                m.forEach((k, x) -> j.add(pad + quote(String.valueOf(k)) + ": " + write(x, indent + 1)));
                return j.toString();
            }
            List<?> l = (List<?>) v;
            if (l.isEmpty()) return "[]";
            StringJoiner j = new StringJoiner(",\n", "[\n", "\n" + end + "]");
            l.forEach(x -> j.add(pad + write(x, indent + 1)));
            return j.toString();
        }

        static String quote(String s) {
            StringBuilder b = new StringBuilder("\"");
            for (char c : s.toCharArray()) {
                switch (c) { case '"' -> b.append("\\\""); case '\\' -> b.append("\\\\"); case '\n' -> b.append("\\n"); case '\t' -> b.append("\\t"); case '\r' -> b.append("\\r");
                    default -> { if (c < 0x20) b.append(String.format("\\u%04x", (int) c)); else b.append(c); } }
            }
            return b.append('"').toString();
        }
    }
}
