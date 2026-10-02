/*
 * OpenData IDE — Stage 0 research tool.
 *
 * Сканирует реальную установку JetBrains IDE / DataGrip (и, опционально, локальный клон
 * intellij-community) и формирует документацию этапа 0 на основе ФАКТИЧЕСКИ найденных
 * дескрипторов, extension points, сервисов, классов и их публичных сигнатур.
 *
 * Ничего не предполагает о существовании классов: все имена из ТЗ — только поисковые подсказки.
 *
 * Запуск (JDK 17+, без компиляции):
 *   java tools/research/Research.java --ide <IDE_HOME> [--community <intellij-community>]
 *        [--out docs] [--state build/research] [--gradle <version>] [--check]
 */

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.spi.ToolProvider;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Research {

    static final String DB_PLUGIN_ID = "com.intellij.database";

    /** Пакеты, классы из которых индексируются. */
    static final List<String> INDEXED_PREFIXES = List.of(
            "com/intellij/database/", "com/intellij/sql/", "com/intellij/grid/",
            "com/intellij/persistence/database/");

    // ------------------------------------------------------------------------------------------
    // Model
    // ------------------------------------------------------------------------------------------

    record ExtPoint(String name, String iface, String area, boolean dynamic) {}
    record Service(String level, String iface, String impl) {}
    record ToolWin(String id, String factory) {}
    record ClassInfo(String fqcn, Path jar, boolean isPublic, boolean isInterface, Set<String> apiStatus) {}

    static final class Descriptor {
        Path jar;           // jar или файл
        String entry;       // путь внутри jar
        String kind;        // plugin | module | config
        String id, name, version, vendor, pkg;
        final List<String> depends = new ArrayList<>();
        final List<String> pluginDeps = new ArrayList<>();
        final List<String> moduleDeps = new ArrayList<>();
        final List<String> contentModules = new ArrayList<>();
        final List<ExtPoint> eps = new ArrayList<>();
        final List<Service> services = new ArrayList<>();
        final List<ToolWin> toolWindows = new ArrayList<>();
        final List<String> actionIds = new ArrayList<>();
        final Map<String, Integer> extensionCounts = new TreeMap<>();
        String ownerPlugin;  // id плагина-владельца (по каталогу plugins/<dir>)

        String displayName() {
            if (id != null) return id;
            String e = entry.endsWith(".xml") ? entry.substring(0, entry.length() - 4) : entry;
            return e.replace("META-INF/", "");
        }
    }

    record Feature(String name, List<String> exact, Pattern regex) {}

    /** Поисковые подсказки из ТЗ (разделы 6, 7, 9). НЕ являются утверждением о существовании классов. */
    static final List<Feature> FEATURES = List.of(
            f("Data Sources", "LocalDataSource|LocalDataSourceManager|DataSourceStorage|DbDataSource|RawDataSource|DataSourceManager",
                    "^(Local)?DataSource(s)?(Manager|Storage|Util|Registry|Configurable)?$"),
            f("Driver Manager", "DatabaseDriver|DatabaseDriverManager|DatabaseDriverImpl|DriverManager",
                    "^Database(Driver|Artifact).*(Manager|Impl)?$"),
            f("Database Connection", "DatabaseConnection|DatabaseConnectionCore|DatabaseConnectionManager|DatabaseConnectionPoint",
                    "^Database(Connection|Session)(Manager|Core|Point|Impl)?$"),
            f("Database Explorer", "DatabaseView|DatabaseViewToolWindowFactory|DatabaseStructure|DatabaseTreeStructure",
                    "^Database(View|Tree|Structure).*$"),
            f("SQL Console", "JdbcConsole|JdbcConsoleProvider|JdbcConsoleCore|DatabaseSessionManager|DatabaseSession",
                    "^(Jdbc)?Console(Provider|Core|Manager|Runner|Util)?$|^JdbcConsole.*$"),
            f("SQL execution", "DataRequest|DataConsumer|SqlStatementRunner|ScriptExecutor",
                    "^.*(Request|Execution)(Executor|Runner|Engine|Manager)?$"),
            f("SQL PSI", "SqlFile|SqlLanguage|SqlElement|SqlStatement|SqlQueryExpression",
                    "^Sql(File|Language|Statement|ElementTypes|TokenType)$"),
            f("SQL dialects", "SqlLanguageDialect|SqlLanguageDialectEx|SqlDialects|PgDialect|PgDialectBase|Dbms",
                    "^(Sql.*Dialect.*|Pg.*Dialect.*|Dbms)$"),
            f("Completion", "SqlCompletionContributor|SqlCompletionProvider",
                    "^Sql.*Completion.*$"),
            f("Navigation", "SqlReferenceProvider|DbNavigationUtil|SqlGotoDeclarationHandler",
                    "^(Sql|Db).*(Navigation|GotoDeclaration|Reference)(Util|Handler|Provider)?$"),
            f("Metadata / Introspection", "DbPsiFacade|DbElement|DasModel|DasTable|DasColumn|BasicModel|Introspector",
                    "^(Das(Model|Table|Column|Object)|Db(PsiFacade|Element)|.*Introspect(or|ion).*)$"),
            f("DataGrid", "DataGrid|DataGridUtil|GridModel|GridColumn|GridRow|GridUtil|TableResultPanel|GridHelper",
                    "^(DataGrid.*|Grid(Model|Column|Row|Util|Helper|Panel).*)$"),
            f("Data modification", "GridDataHookUp|GridDataSupport|GridMutator|DatabaseMutator",
                    "^(Grid(DataHookUp|DataSupport|Mutat).*|.*Mutator)$"),
            f("DDL", "DdlBuilder|ScriptGenerator|SqlScriptGenerator|DbSqlUtil",
                    "^(Ddl.*|.*ScriptGenerator.*)$"),
            f("Transactions", "DatabaseTransactionManager|TransactionController|TxController",
                    "^.*Transaction(Manager|Controller|Mode|Isolation)?$"),
            f("Query history", "QueryHistory|ConsoleHistoryModel|SqlHistory",
                    "^.*(Query|Console|Sql)History.*$"));

    static Feature f(String n, String exact, String regex) {
        return new Feature(n, List.of(exact.split("\\|")), Pattern.compile(regex));
    }

    // ------------------------------------------------------------------------------------------
    // State
    // ------------------------------------------------------------------------------------------

    Path ideArg, root, community, out, state;
    String gradleVersion;
    Map<String, Object> productInfo = Map.of();
    String buildTxt;
    String jbrVersion = "не найден", jbrImpl = "";
    String kotlinVersion = "не определена";
    final List<Path> jars = new ArrayList<>();
    final List<Descriptor> descriptors = new ArrayList<>();
    final Map<String, ClassInfo> classes = new TreeMap<>();          // fqcn -> info
    final Map<String, List<String>> bySimpleName = new HashMap<>();  // simple -> fqcns
    final Map<Path, String> jarOwner = new HashMap<>();               // jar -> plugin id / "core"
    final List<String> warnings = new ArrayList<>();
    final Map<String, Path> apiDumps = new LinkedHashMap<>();         // fqcn -> file

    public static void main(String[] args) throws Exception {
        Map<String, String> o = parseArgs(args);
        if (!o.containsKey("ide")) {
            System.err.println("Usage: java Research.java --ide <IDE_HOME> [--community <dir>] [--out docs] [--state build/research] [--gradle <ver>] [--check]");
            System.exit(2);
        }
        Research r = new Research();
        r.ideArg = Path.of(o.get("ide")).toAbsolutePath().normalize();
        r.community = o.containsKey("community") ? Path.of(o.get("community")).toAbsolutePath().normalize() : null;
        r.out = Path.of(o.getOrDefault("out", "docs"));
        r.state = Path.of(o.getOrDefault("state", "build/research"));
        r.gradleVersion = o.getOrDefault("gradle", "не указана");
        int code = r.run(o.containsKey("check"));
        System.exit(code);
    }

    static Map<String, String> parseArgs(String[] a) {
        Map<String, String> m = new HashMap<>();
        for (int i = 0; i < a.length; i++) {
            if (!a[i].startsWith("--")) continue;
            String k = a[i].substring(2);
            if (i + 1 < a.length && !a[i + 1].startsWith("--")) m.put(k, a[++i]);
            else m.put(k, "true");
        }
        return m;
    }

    int run(boolean checkOnly) throws Exception {
        resolveRoot();
        readProductInfo();
        readJbr();
        collectJars();
        System.out.printf("IDE: %s %s (build %s), jars: %d%n", str(productInfo.get("name")), str(productInfo.get("version")), buildNumber(), jars.size());
        scanJars();
        assignOwners();
        Descriptor db = dbPlugin();
        writeState(db);
        if (checkOnly) {
            System.out.println(db != null
                    ? "Database Tools: FOUND " + db.id + " " + nz(db.version)
                    : "Database Tools: NOT FOUND (plugin id " + DB_PLUGIN_ID + ")");
            return db != null ? 0 : 3;
        }
        readKotlinVersion();
        dumpKeyApis();
        Files.createDirectories(out);
        writeDbAnalysis(db);
        writeGridAnalysis();
        writePluginDependencies(db);
        writeCompatibilityMatrix(db);
        writeRawAppendix();
        System.out.println("Done: " + out.toAbsolutePath() + " (warnings: " + warnings.size() + ", see jetbrains-db-analysis.md section 11)");
                return db != null ? 0 : 3;
    }

    // ------------------------------------------------------------------------------------------
    // IDE layout
    // ------------------------------------------------------------------------------------------

    void resolveRoot() {
        if (Files.isRegularFile(ideArg.resolve("Contents/Resources/product-info.json"))) root = ideArg.resolve("Contents");
        else if (Files.isRegularFile(ideArg.resolve("Resources/product-info.json"))) root = ideArg;
        else root = ideArg;
        if (!Files.isDirectory(root.resolve("lib")))
            throw new IllegalStateException("Not a JetBrains IDE installation (no lib/ directory): " + ideArg);
    }

    Path productInfoFile() {
        for (Path p : List.of(root.resolve("product-info.json"), root.resolve("Resources/product-info.json")))
            if (Files.isRegularFile(p)) return p;
        return null;
    }

    @SuppressWarnings("unchecked")
    void readProductInfo() throws IOException {
        Path p = productInfoFile();
        if (p != null) productInfo = (Map<String, Object>) new Json(Files.readString(p)).parse();
        else warnings.add("product-info.json не найден");
        for (Path b : List.of(root.resolve("build.txt"), root.resolve("Resources/build.txt")))
            if (Files.isRegularFile(b)) { buildTxt = Files.readString(b).trim(); break; }
    }

    String buildNumber() {
        Object b = productInfo.get("buildNumber");
        if (b != null) return b.toString();
        if (buildTxt != null) return buildTxt.replaceFirst("^[A-Z]+-", "");
        return "unknown";
    }

    String productCode() {
        Object c = productInfo.get("productCode");
        if (c != null) return c.toString();
        if (buildTxt != null && buildTxt.contains("-")) return buildTxt.substring(0, buildTxt.indexOf('-'));
        return "?";
    }

    String baseline() {
        String b = buildNumber();
        int dot = b.indexOf('.');
        return dot > 0 ? b.substring(0, dot) : b;
    }

    void readJbr() throws IOException {
        for (Path rel : List.of(root.resolve("jbr/release"), root.resolve("jbr/Contents/Home/release"))) {
            if (!Files.isRegularFile(rel)) continue;
            Properties pr = new Properties();
            try (Reader rd = Files.newBufferedReader(rel)) { pr.load(rd); }
            jbrVersion = unq(pr.getProperty("JAVA_VERSION", "?"));
            jbrImpl = unq(pr.getProperty("IMPLEMENTOR_VERSION", ""));
            return;
        }
        warnings.add("Встроенный JBR не найден (jbr/release)");
    }

    void collectJars() throws IOException {
        for (String d : List.of("lib", "modules", "plugins")) {
            Path dir = root.resolve(d);
            if (!Files.isDirectory(dir)) continue;
            try (Stream<Path> s = Files.walk(dir)) {
                s.filter(p -> p.toString().endsWith(".jar")).sorted().forEach(jars::add);
            }
        }
    }

    // ------------------------------------------------------------------------------------------
    // Scanning
    // ------------------------------------------------------------------------------------------

    void scanJars() {
        DocumentBuilder db = newDocBuilder();
        for (Path jar : jars) {
            try (ZipFile z = new ZipFile(jar.toFile())) {
                Enumeration<? extends ZipEntry> en = z.entries();
                while (en.hasMoreElements()) {
                    ZipEntry e = en.nextElement();
                    String n = e.getName();
                    if (n.endsWith(".class")) {
                        if (indexed(n) && !n.contains("$")) indexClass(z, e, jar);
                        continue;
                    }
                    if (!n.endsWith(".xml") || e.getSize() > 4_000_000) continue;
                    boolean pluginXml = n.equals("META-INF/plugin.xml");
                    boolean rootXml = !n.contains("/");
                    boolean metaXml = n.startsWith("META-INF/") && n.indexOf('/', 9) < 0;
                    if (!(pluginXml || rootXml || metaXml)) continue;
                    byte[] data;
                    try (InputStream in = z.getInputStream(e)) { data = in.readAllBytes(); }
                    if (!startsWithIdeaPlugin(data)) continue;
                    Descriptor d = parseDescriptor(db, data, jar, n, pluginXml ? "plugin" : rootXml ? "module" : "config");
                    if (d != null) descriptors.add(d);
                }
            } catch (IOException ex) {
                warnings.add("Не удалось прочитать " + root.relativize(jar) + ": " + ex.getMessage());
            }
        }
    }

    static boolean indexed(String entry) {
        for (String p : INDEXED_PREFIXES) if (entry.startsWith(p)) return true;
        return false;
    }

    void indexClass(ZipFile z, ZipEntry e, Path jar) throws IOException {
        String fqcn = e.getName().substring(0, e.getName().length() - 6).replace('/', '.');
        if (classes.containsKey(fqcn)) return;
        byte[] data;
        try (InputStream in = z.getInputStream(e)) { data = in.readAllBytes(); }
        ClassHeader h = ClassHeader.parse(data);
        classes.put(fqcn, new ClassInfo(fqcn, jar, h.isPublic, h.isInterface, h.annotations));
        bySimpleName.computeIfAbsent(fqcn.substring(fqcn.lastIndexOf('.') + 1), k -> new ArrayList<>()).add(fqcn);
    }

    static boolean startsWithIdeaPlugin(byte[] data) {
        String head = new String(data, 0, Math.min(data.length, 2048), StandardCharsets.UTF_8);
        return head.contains("<idea-plugin");
    }

    static DocumentBuilder newDocBuilder() {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(false);
            f.setValidating(false);
            f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            f.setXIncludeAware(false);
            f.setExpandEntityReferences(false);
            return f.newDocumentBuilder();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    Descriptor parseDescriptor(DocumentBuilder b, byte[] data, Path jar, String entry, String kind) {
        Document doc;
        try { doc = b.parse(new ByteArrayInputStream(data)); }
        catch (Exception ex) { warnings.add("XML " + entry + " в " + jar.getFileName() + ": " + ex.getMessage()); return null; }
        Element r = doc.getDocumentElement();
        Descriptor d = new Descriptor();
        d.jar = jar; d.entry = entry; d.kind = kind;
        d.id = childText(r, "id");
        d.name = childText(r, "name");
        d.version = childText(r, "version");
        d.vendor = childText(r, "vendor");
        d.pkg = attr(r, "package");
        if ("plugin".equals(kind) && d.id == null) d.id = d.name;   // как в платформе: id по умолчанию = name
        for (Element dep : children(r, "depends")) {
            String opt = attr(dep, "optional");
            String cf = attr(dep, "config-file");
            d.depends.add(dep.getTextContent().trim() + ("true".equals(opt) ? " (optional" + (cf != null ? ", " + cf : "") + ")" : ""));
        }
        for (Element deps : children(r, "dependencies")) {
            for (Element p : children(deps, "plugin")) d.pluginDeps.add(attr(p, "id"));
            for (Element m : children(deps, "module")) d.moduleDeps.add(attr(m, "name"));
        }
        for (Element c : children(r, "content"))
            for (Element m : children(c, "module")) d.contentModules.add(attr(m, "name") + (attr(m, "loading") != null ? " [" + attr(m, "loading") + "]" : ""));
        for (Element eps : children(r, "extensionPoints"))
            for (Element ep : children(eps, "extensionPoint")) {
                String qn = attr(ep, "qualifiedName");
                String name = qn != null ? qn : attr(ep, "name");
                String iface = Optional.ofNullable(attr(ep, "interface")).orElse(attr(ep, "beanClass"));
                d.eps.add(new ExtPoint(name, iface, Optional.ofNullable(attr(ep, "area")).orElse("IDEA_APPLICATION"),
                        "true".equals(attr(ep, "dynamic"))));
            }
        for (Element ex : children(r, "extensions")) {
            String ns = Optional.ofNullable(attr(ex, "defaultExtensionNs")).orElse(attr(ex, "xmlns"));
            for (Element e : childElements(ex)) {
                String tag = e.getTagName();
                String ep = ns != null ? ns + "." + tag : tag;
                d.extensionCounts.merge(ep, 1, Integer::sum);
                switch (tag) {
                    case "applicationService", "projectService", "moduleService" -> d.services.add(new Service(
                            tag.replace("Service", ""), attr(e, "serviceInterface"),
                            Optional.ofNullable(attr(e, "serviceImplementation")).orElse(attr(e, "testServiceImplementation"))));
                    case "toolWindow" -> d.toolWindows.add(new ToolWin(attr(e, "id"), attr(e, "factoryClass")));
                    default -> {}
                }
            }
        }
        for (Element acts : children(r, "actions")) collectActions(acts, d.actionIds);
        return d;
    }

    static void collectActions(Element parent, List<String> ids) {
        for (Element e : childElements(parent)) {
            if (e.getTagName().equals("action") || e.getTagName().equals("group")) {
                String id = attr(e, "id");
                if (id != null) ids.add(id);
            }
            collectActions(e, ids);
        }
    }

    void assignOwners() {
        Path plugins = root.resolve("plugins");
        Map<Path, String> dirToPlugin = new HashMap<>();
        // В одном каталоге плагина бывает несколько plugin.xml (например, frontend-split/database-frontend.jar).
        // Владелец каталога — дескриптор из JAR с наименьшей глубиной (plugins/<dir>/lib/<main>.jar).
        Map<Path, Integer> depth = new HashMap<>();
        for (Descriptor d : descriptors)
            if ("plugin".equals(d.kind) && d.jar.startsWith(plugins)) {
                Path relJar = plugins.relativize(d.jar);
                Path dir = plugins.resolve(relJar.getName(0));
                int dd = relJar.getNameCount();
                if (!depth.containsKey(dir) || dd < depth.get(dir)) {
                    depth.put(dir, dd);
                    dirToPlugin.put(dir, d.id);
                }
            }
        for (Path j : jars) {
            if (j.startsWith(plugins)) {
                Path dir = plugins.resolve(plugins.relativize(j).getName(0));
                jarOwner.put(j, dirToPlugin.getOrDefault(dir, "plugins/" + dir.getFileName()));
            } else jarOwner.put(j, "core (lib/)");
        }
        for (Descriptor d : descriptors) d.ownerPlugin = jarOwner.get(d.jar);
    }

    Descriptor dbPlugin() {
        return descriptors.stream().filter(d -> "plugin".equals(d.kind) && DB_PLUGIN_ID.equals(d.id)).findFirst().orElse(null);
    }

    String rel(Path p) {
        try { return root.relativize(p).toString().replace('\\', '/'); } catch (Exception e) { return p.toString(); }
    }

    // ------------------------------------------------------------------------------------------
    // Kotlin version / javap
    // ------------------------------------------------------------------------------------------

    void readKotlinVersion() {
        for (Path j : jars) {
            if (!j.startsWith(root.resolve("lib"))) continue;
            try (ZipFile z = new ZipFile(j.toFile())) {
                if (z.getEntry("kotlin/KotlinVersion.class") == null) continue;
                try (URLClassLoader cl = new URLClassLoader(new URL[]{j.toUri().toURL()}, null)) {
                    Class<?> kv = Class.forName("kotlin.KotlinVersion", true, cl);
                    kotlinVersion = kv.getField("CURRENT").get(null) + " (" + rel(j) + ")";
                    return;
                }
            } catch (Throwable t) { /* try next */ }
        }
        warnings.add("Версия Kotlin stdlib в IDE не определена");
    }

    /** Все найденные по точному имени классы из подсказок ТЗ. */
    List<ClassInfo> exactHits(Feature f) {
        List<ClassInfo> r = new ArrayList<>();
        for (String s : f.exact())
            for (String fq : bySimpleName.getOrDefault(s, List.of())) r.add(classes.get(fq));
        return r;
    }

    List<ClassInfo> regexHits(Feature f, int limit) {
        return classes.values().stream()
                .filter(c -> c.isPublic() && f.regex().matcher(simple(c.fqcn())).matches())
                .sorted(Comparator.comparing((ClassInfo c) -> c.apiStatus().contains("Internal"))
                        .thenComparing(c -> c.fqcn().contains(".impl.")).thenComparing(ClassInfo::fqcn))
                .limit(limit).toList();
    }

    void dumpKeyApis() throws IOException {
        ToolProvider javap = ToolProvider.findFirst("javap").orElse(null);
        if (javap == null) { warnings.add("javap недоступен (нужен JDK с модулем jdk.jdeps) — сигнатуры не выгружены"); return; }
        Path apiDir = out.resolve("research/api");
        Files.createDirectories(apiDir);
        Set<String> targets = new LinkedHashSet<>();
        for (Feature f : FEATURES) exactHits(f).forEach(c -> targets.add(c.fqcn()));
        for (String fq : targets) {
            ClassInfo c = classes.get(fq);
            StringWriter sw = new StringWriter();
            int rc = javap.run(new PrintWriter(sw), new PrintWriter(sw), "-public", "-cp", c.jar().toString(), fq);
            Path f = apiDir.resolve(fq + ".txt");
            Files.writeString(f, "// jar: " + rel(c.jar()) + "\n// build: " + buildNumber()
                    + "\n// ApiStatus (class-level): " + (c.apiStatus().isEmpty() ? "—" : String.join(", ", c.apiStatus()))
                    + "\n// javap rc=" + rc + "\n\n" + sw);
            apiDumps.put(fq, f);
        }
    }

    // ------------------------------------------------------------------------------------------
    // Output
    // ------------------------------------------------------------------------------------------

    void writeState(Descriptor db) throws IOException {
        Files.createDirectories(state);
        Properties p = new Properties();
        p.setProperty("ide.home", ideArg.toString());
        p.setProperty("ide.name", str(productInfo.get("name")));
        p.setProperty("ide.version", str(productInfo.get("version")));
        p.setProperty("ide.productCode", productCode());
        p.setProperty("ide.buildNumber", buildNumber());
        p.setProperty("ide.baseline", baseline());
        p.setProperty("jbr.version", jbrVersion);
        p.setProperty("database.plugin.found", String.valueOf(db != null));
        if (db != null) {
            p.setProperty("database.plugin.id", db.id);
            p.setProperty("database.plugin.version", nz(db.version));
            p.setProperty("database.plugin.jar", rel(db.jar));
        }
        try (Writer w = Files.newBufferedWriter(state.resolve("ide.properties"))) {
            p.store(w, "Generated by tools/research/Research.java");
        }
    }

    String header(String title) {
        return "# " + title + "\n\n> Сгенерировано `tools/research/Research.java` " + LocalDate.now()
                + " по установке `" + str(productInfo.get("name")) + " " + str(productInfo.get("version"))
                + "` (build `" + productCode() + "-" + buildNumber() + "`).\n"
                + "> Все перечисленные классы/EP/сервисы реально найдены в этой сборке. Повторный запуск перезаписывает файл.\n\n";
    }

    void writeDbAnalysis(Descriptor db) throws IOException {
        StringBuilder s = new StringBuilder(header("Анализ JetBrains DB stack"));
        s.append("## 1. Версии\n\n| Параметр | Значение |\n|---|---|\n");
        row(s, "Продукт", str(productInfo.get("name")) + " " + str(productInfo.get("version")));
        row(s, "Product code", productCode());
        row(s, "Build number (IntelliJ Platform)", buildNumber());
        row(s, "build.txt", nz(buildTxt));
        row(s, "DataGrip build", "DB".equals(productCode()) ? buildNumber() : "— (установка не DataGrip)");
        row(s, "Database Tools and SQL", db != null ? db.id + " " + nz(db.version) : "**НЕ НАЙДЕН**");
        row(s, "JBR (JDK runtime IDE)", jbrVersion + (jbrImpl.isEmpty() ? "" : " / " + jbrImpl));
        row(s, "Kotlin stdlib в IDE", kotlinVersion);
        row(s, "Gradle (проект)", gradleVersion);
        row(s, "intellij-community", community == null ? "не передан (`--community`)" : communityRevision());
        row(s, "Просканировано JAR", String.valueOf(jars.size()));
        row(s, "Проиндексировано классов (com.intellij.database/sql/grid)", String.valueOf(classes.size()));
        s.append('\n');

        s.append("## 2. Плагин Database Tools and SQL\n\n");
        if (db == null) {
            s.append("Плагин `").append(DB_PLUGIN_ID).append("` в установке не найден. Подключение через bundled plugin невозможно — ")
                    .append("установите IntelliJ IDEA (Ultimate / unified) или DataGrip, либо плагин из Marketplace.\n\n");
        } else {
            s.append("| Поле | Значение |\n|---|---|\n");
            row(s, "Plugin ID", "`" + db.id + "`");
            row(s, "Name", nz(db.name));
            row(s, "Version", nz(db.version));
            row(s, "Vendor", nz(db.vendor));
            row(s, "Descriptor", "`" + rel(db.jar) + "!/" + db.entry + "`");
            row(s, "Plugin dir", "`" + rel(db.jar.getParent().getParent()) + "`");
            row(s, "depends", list(db.depends));
            row(s, "dependencies/plugin", list(db.pluginDeps));
            row(s, "dependencies/module", list(db.moduleDeps));
            row(s, "content modules", String.valueOf(db.contentModules.size()));
            s.append('\n');
            if (!db.contentModules.isEmpty()) {
                s.append("<details><summary>Content modules (").append(db.contentModules.size()).append(")</summary>\n\n");
                db.contentModules.forEach(m -> s.append("- `").append(m).append("`\n"));
                s.append("\n</details>\n\n");
            }
        }

        List<Descriptor> dbRelated = relatedDescriptors(Pattern.compile("(?i)database|sql|grid|jdbc"));
        s.append("## 3. Дескрипторы DB/SQL/Grid (plugin, content modules, config-files)\n\n");
        s.append("| Дескриптор | Тип | Владелец | JAR | EP | Сервисы | Tool windows | Actions |\n|---|---|---|---|---|---|---|---|\n");
        for (Descriptor d : dbRelated)
            s.append("| `").append(d.displayName()).append("` | ").append(d.kind).append(" | ").append(d.ownerPlugin)
                    .append(" | `").append(rel(d.jar)).append("` | ").append(d.eps.size()).append(" | ").append(d.services.size())
                    .append(" | ").append(d.toolWindows.stream().map(ToolWin::id).collect(Collectors.joining(", ")))
                    .append(" | ").append(d.actionIds.size()).append(" |\n");
        s.append('\n');

        s.append("## 4. Tool windows\n\n| ID | Factory | Дескриптор |\n|---|---|---|\n");
        for (Descriptor d : dbRelated)
            for (ToolWin t : d.toolWindows)
                s.append("| `").append(t.id()).append("` | `").append(nz(t.factory())).append("` | `").append(d.displayName()).append("` |\n");
        s.append('\n');

        s.append("## 5. Матрица повторного использования (ТЗ, раздел 9)\n\n");
        s.append("Колонки «Компонент» и «Модуль/JAR» заполнены автоматически по реально найденным классам. ")
                .append("Колонка «Собственный код» — предварительное решение: «нет», если компонент найден в bundled-плагине или платформе.\n\n");
        s.append("| Функция | JetBrains компонент (найдено) | Модуль/JAR | Способ подключения | Собственный код |\n|---|---|---|---|---|\n");
        for (Feature f : FEATURES) {
            List<ClassInfo> hits = exactHits(f);
            if (hits.isEmpty()) hits = regexHits(f, 3);
            String comp = hits.isEmpty() ? "не найдено" : hits.stream().limit(3).map(c -> "`" + c.fqcn() + "`" + apiMark(c)).collect(Collectors.joining("<br>"));
            String jar = hits.stream().map(c -> "`" + rel(c.jar()) + "`").distinct().limit(2).collect(Collectors.joining("<br>"));
            String owner = hits.stream().map(c -> jarOwner.get(c.jar())).distinct().collect(Collectors.joining(", "));
            String how = hits.isEmpty() ? "TBD" : owner.startsWith("core") ? "платформа (compile: IDE lib)" : "`bundledPlugin(\"" + owner.split(",")[0].trim() + "\")`";
            String own = hits.isEmpty() ? "TBD — ручной анализ" : "нет (переиспользуем)";
            s.append("| ").append(f.name()).append(" | ").append(comp).append(" | ").append(jar.isEmpty() ? "—" : jar)
                    .append(" | ").append(how).append(" | ").append(own).append(" |\n");
        }
        s.append("\nОбозначения: 🔒 — класс помечен `@ApiStatus.Internal`, 🧪 — `@ApiStatus.Experimental`, ⚠ — `@Deprecated`/`ScheduledForRemoval`, (impl) — пакет `.impl.`.\n\n");

        s.append("## 6. Кандидаты по функциям (детально)\n\n");
        for (Feature f : FEATURES) {
            s.append("### ").append(f.name()).append("\n\n");
            List<ClassInfo> ex = exactHits(f);
            Set<String> missing = new LinkedHashSet<>(f.exact());
            ex.forEach(c -> missing.remove(simple(c.fqcn())));
            s.append("Точные совпадения с подсказками ТЗ:\n\n");
            if (ex.isEmpty()) s.append("- нет\n");
            for (ClassInfo c : ex) s.append(classLine(c));
            if (!missing.isEmpty()) s.append("\nНе найдены в этой сборке (не использовать): ").append(missing.stream().map(m -> "`" + m + "`").collect(Collectors.joining(", "))).append('\n');
            List<ClassInfo> rx = regexHits(f, 12);
            rx = rx.stream().filter(c -> !ex.contains(c)).toList();
            if (!rx.isEmpty()) {
                s.append("\nДругие публичные классы по шаблону `").append(f.regex().pattern()).append("`:\n\n");
                for (ClassInfo c : rx) s.append(classLine(c));
            }
            s.append('\n');
        }

        s.append("## 7. Extension points DB/SQL/Grid\n\n| EP | Interface/Bean | Area | Dynamic | Дескриптор |\n|---|---|---|---|---|\n");
        for (Descriptor d : dbRelated)
            for (ExtPoint e : d.eps)
                s.append("| `").append(qualify(d, e.name())).append("` | `").append(nz(e.iface())).append("` | ").append(e.area().replace("IDEA_", ""))
                        .append(" | ").append(e.dynamic() ? "да" : "нет").append(" | `").append(d.displayName()).append("` |\n");
        s.append('\n');

        s.append("## 8. Сервисы DB/SQL/Grid\n\n| Уровень | Интерфейс | Реализация | Дескриптор |\n|---|---|---|---|\n");
        for (Descriptor d : dbRelated)
            for (Service sv : d.services)
                s.append("| ").append(sv.level()).append(" | `").append(nz(sv.iface())).append("` | `").append(nz(sv.impl())).append("` | `").append(d.displayName()).append("` |\n");
        s.append('\n');

        s.append("## 9. Пакеты com.intellij.database.* / com.intellij.sql.*\n\n| Пакет | Классов | public | 🔒 Internal | JAR |\n|---|---|---|---|---|\n");
        Map<String, List<ClassInfo>> byPkg = classes.values().stream().collect(Collectors.groupingBy(c -> pkg(c.fqcn()), TreeMap::new, Collectors.toList()));
        byPkg.forEach((p, cs) -> {
            if (p.startsWith("com.intellij.grid")) return;
            long pub = cs.stream().filter(ClassInfo::isPublic).count();
            long internal = cs.stream().filter(c -> c.apiStatus().contains("Internal")).count();
            String js = cs.stream().map(c -> rel(c.jar())).distinct().limit(2).collect(Collectors.joining(", "));
            s.append("| `").append(p).append("` | ").append(cs.size()).append(" | ").append(pub).append(" | ").append(internal).append(" | ").append(js).append(" |\n");
        });
        s.append('\n');

        s.append("## 10. Выгруженные сигнатуры (javap -public)\n\n");
        if (apiDumps.isEmpty()) s.append("Нет (javap недоступен или ключевые классы не найдены).\n");
        apiDumps.forEach((fq, f) -> s.append("- [`").append(fq).append("`](").append(out.relativize(f).toString().replace('\\', '/')).append(")\n"));
        s.append("\n## 11. Предупреждения сканирования\n\n");
        if (warnings.isEmpty()) s.append("Нет.\n");
        warnings.forEach(w -> s.append("- ").append(w).append('\n'));
        Files.writeString(out.resolve("jetbrains-db-analysis.md"), s.toString());
    }

    void writeGridAnalysis() throws IOException {
        StringBuilder s = new StringBuilder(header("Анализ intellij.grid / Data Editor and Viewer"));
        List<Descriptor> grid = relatedDescriptors(Pattern.compile("(?i)grid"));
        s.append("## 1. Модули и дескрипторы grid в установке\n\n| Дескриптор | Тип | Владелец | JAR | depends / dependencies |\n|---|---|---|---|---|\n");
        for (Descriptor d : grid)
            s.append("| `").append(d.displayName()).append("` | ").append(d.kind).append(" | ").append(d.ownerPlugin).append(" | `").append(rel(d.jar))
                    .append("` | ").append(list(concat(d.depends, d.pluginDeps, d.moduleDeps))).append(" |\n");
        if (grid.isEmpty()) s.append("| — | — | — | — | — |\n");
        s.append('\n');

        s.append("## 2. Extension points grid\n\n| EP | Interface/Bean | Дескриптор |\n|---|---|---|\n");
        for (Descriptor d : grid) for (ExtPoint e : d.eps)
            s.append("| `").append(qualify(d, e.name())).append("` | `").append(nz(e.iface())).append("` | `").append(d.displayName()).append("` |\n");
        s.append('\n');

        s.append("## 3. Пакеты grid\n\n| Пакет | Классов | public | 🔒 Internal | JAR |\n|---|---|---|---|---|\n");
        classes.values().stream().filter(c -> isGridClass(c.fqcn()))
                .collect(Collectors.groupingBy(c -> pkg(c.fqcn()), TreeMap::new, Collectors.toList()))
                .forEach((p, cs) -> s.append("| `").append(p).append("` | ").append(cs.size()).append(" | ")
                        .append(cs.stream().filter(ClassInfo::isPublic).count()).append(" | ")
                        .append(cs.stream().filter(c -> c.apiStatus().contains("Internal")).count()).append(" | ")
                        .append(cs.stream().map(c -> rel(c.jar())).distinct().limit(2).collect(Collectors.joining(", "))).append(" |\n"));
        s.append('\n');

        s.append("## 4. API для задач раздела 7 ТЗ\n\n");
        Map<String, Pattern> tasks = new LinkedHashMap<>();
        tasks.put("Создание DataGrid", Pattern.compile("^(DataGrid|DataGridUtil|GridUtil|GridHelper|.*GridFactory|.*Grid.*Panel)$"));
        tasks.put("Модель строк и колонок", Pattern.compile("^Grid(Model|Column|Row|ModelEx|ListModel).*$"));
        tasks.put("Передача данных / data hooks", Pattern.compile("^Grid(DataHookUp|DataSupport|Loader|RequestSource|Pager|Sorting).*$"));
        tasks.put("Редактирование", Pattern.compile("^(Grid.*Mutat.*|.*Mutator|Grid.*Edit.*)$"));
        tasks.put("Sorting / filtering", Pattern.compile("^(Grid.*(Sort|Filter).*|.*Filtering.*)$"));
        tasks.put("Copy / paste", Pattern.compile("^(Grid.*(Copy|Paste).*|.*Extractor.*)$"));
        tasks.put("Paging / fetch more", Pattern.compile("^(Grid.*Pag.*|.*FetchMore.*)$"));
        tasks.put("Renderers / editors", Pattern.compile("^(Grid.*(Renderer|CellEditor|Editor)(Factory)?.*)$"));
        for (var t : tasks.entrySet()) {
            s.append("### ").append(t.getKey()).append("\n\n");
            List<ClassInfo> hits = classes.values().stream().filter(c -> isGridClass(c.fqcn()) && c.isPublic() && t.getValue().matcher(simple(c.fqcn())).matches())
                    .limit(15).toList();
            if (hits.isEmpty()) s.append("- не найдено по шаблону `").append(t.getValue().pattern()).append("`\n");
            hits.forEach(c -> s.append(classLine(c)));
            s.append('\n');
        }

        s.append("## 5. Исходники intellij-community: grid/\n\n");
        if (community == null) s.append("Клон intellij-community не передан (`--community <dir>`).\n");
        else s.append(communityGrid());
        Files.writeString(out.resolve("grid-analysis.md"), s.toString());
    }

    boolean isGridClass(String fq) {
        return fq.startsWith("com.intellij.grid") || fq.startsWith("com.intellij.database.datagrid") || fq.contains(".grid.");
    }

    void writePluginDependencies(Descriptor db) throws IOException {
        StringBuilder s = new StringBuilder(header("Зависимости от плагинов и модулей JetBrains"));
        s.append("## 1. Что подключает OpenData\n\n| Plugin/module | Plugin ID | JAR/module path | Version/build | Compile | Runtime | Поиск локальной установки |\n|---|---|---|---|---|---|---|\n");
        if (db != null)
            s.append("| Database Tools and SQL | `").append(db.id).append("` | `").append(rel(db.jar.getParent())).append("/*.jar` | ")
                    .append(nz(db.version)).append(" | да (`bundledPlugin`) | да (`<depends>`) | `JETBRAINS_IDE_HOME` / `DATAGRIP_HOME` → `plugins/` |\n");
        else s.append("| Database Tools and SQL | `").append(DB_PLUGIN_ID).append("` | **не найден** | — | — | — | — |\n");
        s.append('\n');
        if (db != null) {
            s.append("## 2. Транзитивные зависимости Database Tools\n\n");
            s.append("Plugin-level `<depends>`: ").append(list(db.depends)).append("\n\n");
            s.append("`<dependencies><plugin>`: ").append(list(db.pluginDeps)).append("\n\n");
            s.append("`<dependencies><module>`: ").append(list(db.moduleDeps)).append("\n\n");
            s.append("Все из них являются bundled в той же установке и подтягиваются IntelliJ Platform Gradle Plugin автоматически ")
                    .append("при `bundledPlugin(\"").append(db.id).append("\")` — проверьте раздел 3.\n\n");
            s.append("## 3. Проверка наличия зависимостей в установке\n\n| Зависимость | Найдена |\n|---|---|\n");
            Set<String> ids = descriptors.stream().filter(d -> "plugin".equals(d.kind)).map(d -> d.id).filter(Objects::nonNull).collect(Collectors.toSet());
            Set<String> modules = descriptors.stream().filter(d -> "module".equals(d.kind)).map(Descriptor::displayName).collect(Collectors.toSet());
            for (String dep : concat(db.depends, db.pluginDeps)) {
                String id = dep.replaceAll(" \\(.*", "");
                boolean found = ids.contains(id) || id.startsWith("com.intellij.modules.");
                s.append("| `").append(dep).append("` | ").append(found ? "да" : "нет / модуль платформы").append(" |\n");
            }
            for (String m : db.moduleDeps) s.append("| module `").append(m).append("` | ").append(modules.contains(m) ? "да" : "нет").append(" |\n");
            s.append("\n## 4. JAR плагина Database Tools\n\n");
            Path dir = db.jar.getParent();
            jars.stream().filter(j -> j.startsWith(dir.getParent())).forEach(j -> s.append("- `").append(rel(j)).append("`\n"));
        }
        s.append("\n## 5. Все bundled-плагины установки\n\n| Plugin ID | Name | Version | Каталог |\n|---|---|---|---|\n");
        descriptors.stream().filter(d -> "plugin".equals(d.kind)).sorted(Comparator.comparing(d -> nz(d.id)))
                .forEach(d -> s.append("| `").append(d.id).append("` | ").append(nz(d.name)).append(" | ").append(nz(d.version)).append(" | `")
                        .append(rel(d.jar.getParent())).append("` |\n"));
        Files.writeString(out.resolve("plugin-dependencies.md"), s.toString());
    }

    void writeCompatibilityMatrix(Descriptor db) throws IOException {
        Path f = out.resolve("compatibility-matrix.md");
        String begin = "<!-- rows:begin -->", end = "<!-- rows:end -->";
        List<String> rows = new ArrayList<>();
        String tail = "";
        if (Files.isRegularFile(f)) {
            String old = Files.readString(f);
            int b = old.indexOf(begin), e = old.indexOf(end);
            // Всё после rows:end ведётся вручную (матрица продукта OpenData IDE) и сохраняется.
            if (e >= 0) tail = old.substring(e + end.length()).replaceFirst("^\n", "");
            if (b >= 0 && e > b)
                for (String l : old.substring(b + begin.length(), e).split("\n"))
                    if (l.startsWith("|") && !l.startsWith("| Ключ") && !l.startsWith("|---")) rows.add(l);
        }
        String key = "| " + productCode() + "-" + buildNumber() + " |";
        // Статус, выставленный вручную после build/POC, при повторном research сохраняется.
        String status = rows.stream().filter(r -> r.startsWith(key)).findFirst()
                .map(r -> { String[] c = r.split("\\|"); return c[c.length - 1].trim(); })
                .orElse(db != null ? "research OK" : "нет Database Tools");
        rows.removeIf(r -> r.contains(key));
        rows.add("| " + productCode() + "-" + buildNumber() + " | " + LocalDate.now() + " | " + str(productInfo.get("name")) + " " + str(productInfo.get("version"))
                + " | " + buildNumber() + " | " + ("DB".equals(productCode()) ? buildNumber() : "—") + " | " + (db != null ? nz(db.version) : "нет")
                + " | " + jbrVersion + " | " + kotlinVersion.replaceAll(" \\(.*", "") + " | " + gradleVersion + " | "
                + status + " |");
        StringBuilder s = new StringBuilder("# Матрица совместимости\n\n## Установки JetBrains, исследованные на этапе 0\n\n");
        s.append("Строки добавляются `scripts/research.ps1` для каждой проверенной установки. Колонка «Статус» обновляется вручную ")
                .append("после прохождения build/POC (`research OK` → `build OK` → `POC OK`).\n\n")
                .append("Правило: IntelliJ Platform, Database Tools и grid берутся из **одной** установки (одна build-линейка); ")
                .append("версия Kotlin плагина не выше версии Kotlin stdlib в IDE; JDK сборки = major версия JBR.\n\n")
                .append(begin).append('\n')
                .append("| Ключ | Дата | Продукт | IntelliJ build | DataGrip build | Database Tools | JBR | Kotlin (IDE) | Gradle | Статус |\n")
                .append("|---|---|---|---|---|---|---|---|---|---|\n");
        rows.forEach(r -> s.append(r).append('\n'));
        s.append(end).append('\n').append(tail);
        Files.writeString(f, s.toString());
    }

    void writeRawAppendix() throws IOException {
        Path dir = out.resolve("research");
        Files.createDirectories(dir);
        StringBuilder s = new StringBuilder(header("Сырые данные: actions и extensions DB/SQL/Grid"));
        for (Descriptor d : relatedDescriptors(Pattern.compile("(?i)database|sql|grid|jdbc"))) {
            if (d.actionIds.isEmpty() && d.extensionCounts.isEmpty()) continue;
            s.append("## ").append(d.displayName()).append(" (`").append(rel(d.jar)).append("`)\n\n");
            if (!d.extensionCounts.isEmpty()) {
                s.append("Используемые EP (кол-во регистраций):\n\n");
                d.extensionCounts.forEach((k, v) -> s.append("- `").append(k).append("` × ").append(v).append('\n'));
                s.append('\n');
            }
            if (!d.actionIds.isEmpty()) {
                s.append("Actions/groups (").append(d.actionIds.size()).append("): ");
                s.append(d.actionIds.stream().map(a -> "`" + a + "`").collect(Collectors.joining(", "))).append("\n\n");
            }
        }
        Files.writeString(dir.resolve("raw-descriptors.md"), s.toString());
        StringBuilder c = new StringBuilder("fqcn\tpublic\tinterface\tapiStatus\tjar\towner\n");
        classes.values().forEach(ci -> c.append(ci.fqcn()).append('\t').append(ci.isPublic()).append('\t').append(ci.isInterface()).append('\t')
                .append(String.join(",", ci.apiStatus())).append('\t').append(rel(ci.jar())).append('\t').append(jarOwner.get(ci.jar())).append('\n'));
        Files.writeString(dir.resolve("class-index.tsv"), c.toString());
    }

    // ------------------------------------------------------------------------------------------
    // intellij-community
    // ------------------------------------------------------------------------------------------

    String communityRevision() {
        String head = exec(community, "git", "rev-parse", "HEAD");
        String desc = exec(community, "git", "describe", "--tags", "--always");
        String bt = "";
        try { Path b = community.resolve("build.txt"); if (Files.isRegularFile(b)) bt = ", build.txt=" + Files.readString(b).trim(); } catch (IOException ignored) {}
        return "`" + nz(desc) + "` / `" + nz(head) + "`" + bt;
    }

    String communityGrid() throws IOException {
        Path grid = community.resolve("grid");
        StringBuilder s = new StringBuilder();
        s.append("Ревизия: ").append(communityRevision()).append("\n\n");
        if (!Files.isDirectory(grid)) return s.append("Каталог `grid/` отсутствует в этом клоне.\n").toString();
        s.append("| Модуль (.iml) | Путь | Дескрипторы |\n|---|---|---|\n");
        try (Stream<Path> st = Files.walk(grid, 4)) {
            for (Path iml : st.filter(p -> p.toString().endsWith(".iml")).sorted().toList()) {
                Path dir = iml.getParent();
                String xmls;
                try (Stream<Path> x = Files.walk(dir, 3)) {
                    xmls = x.filter(p -> p.toString().endsWith(".xml") && isIdeaPluginFile(p)).map(p -> "`" + dir.relativize(p).toString().replace('\\', '/') + "`")
                            .collect(Collectors.joining(", "));
                }
                s.append("| `").append(iml.getFileName().toString().replace(".iml", "")).append("` | `")
                        .append(community.relativize(dir).toString().replace('\\', '/')).append("` | ").append(xmls).append(" |\n");
            }
        }
        s.append("\nИсходники ключевых классов (по подсказкам ТЗ):\n\n");
        Set<String> names = new HashSet<>(List.of("DataGrid", "GridModel", "GridColumn", "GridRow", "GridDataHookUp", "GridDataSupport", "DataGridUtil", "GridUtil", "GridHelper"));
        try (Stream<Path> st = Files.walk(grid)) {
            List<Path> found = st.filter(p -> {
                String fn = p.getFileName().toString();
                int dot = fn.lastIndexOf('.');
                return dot > 0 && (fn.endsWith(".java") || fn.endsWith(".kt")) && names.contains(fn.substring(0, dot));
            }).sorted().toList();
            if (found.isEmpty()) s.append("- не найдены\n");
            found.forEach(p -> s.append("- `").append(community.relativize(p).toString().replace('\\', '/')).append("`\n"));
        }
        return s.toString();
    }

    static boolean isIdeaPluginFile(Path p) {
        try (InputStream in = Files.newInputStream(p)) { return startsWithIdeaPlugin(in.readNBytes(2048)); }
        catch (IOException e) { return false; }
    }

    static String exec(Path dir, String... cmd) {
        try {
            Process p = new ProcessBuilder(cmd).directory(dir.toFile()).redirectErrorStream(true).start();
            String o = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            return p.waitFor() == 0 ? o : null;
        } catch (Exception e) { return null; }
    }

    // ------------------------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------------------------

    List<Descriptor> relatedDescriptors(Pattern p) {
        return descriptors.stream().filter(d -> p.matcher(d.displayName()).find() || p.matcher(nz(d.ownerPlugin)).find()
                        || p.matcher(d.jar.getFileName().toString()).find() || p.matcher(nz(d.name)).find())
                .sorted(Comparator.comparing((Descriptor d) -> nz(d.ownerPlugin)).thenComparing(Descriptor::displayName)).toList();
    }

    String qualify(Descriptor d, String ep) {
        if (ep == null) return "?";
        if (ep.contains(".")) return ep;
        String ns = "plugin".equals(d.kind) ? d.id : d.ownerPlugin;
        return (ns == null || ns.startsWith("core") || ns.startsWith("plugins/") ? "com.intellij" : ns) + "." + ep;
    }

    String classLine(ClassInfo c) {
        String dump = apiDumps.containsKey(c.fqcn()) ? " — [сигнатуры](" + out.relativize(apiDumps.get(c.fqcn())).toString().replace('\\', '/') + ")" : "";
        return "- `" + c.fqcn() + "`" + apiMark(c) + (c.isInterface() ? " (interface)" : "") + " — `" + rel(c.jar()) + "`" + dump + "\n";
    }

    static String apiMark(ClassInfo c) {
        StringBuilder m = new StringBuilder();
        if (!c.isPublic()) m.append(" (non-public)");
        if (c.apiStatus().contains("Internal")) m.append(" 🔒");
        if (c.apiStatus().contains("Experimental")) m.append(" 🧪");
        if (c.apiStatus().contains("Deprecated") || c.apiStatus().contains("ScheduledForRemoval")) m.append(" ⚠");
        if (c.fqcn().contains(".impl.")) m.append(" (impl)");
        return m.toString();
    }

    static void row(StringBuilder s, String k, String v) { s.append("| ").append(k).append(" | ").append(v).append(" |\n"); }
    static String simple(String fq) { return fq.substring(fq.lastIndexOf('.') + 1); }
    static String pkg(String fq) { int i = fq.lastIndexOf('.'); return i < 0 ? "" : fq.substring(0, i); }
    static String nz(String s) { return s == null || s.isBlank() ? "—" : s; }
    static String str(Object o) { return o == null ? "?" : o.toString(); }
    static String unq(String s) { return s.replaceAll("^\"|\"$", ""); }
    static String list(List<String> l) { return l.isEmpty() ? "—" : l.stream().map(x -> "`" + x + "`").collect(Collectors.joining(", ")); }

    @SafeVarargs
    static List<String> concat(List<String>... ls) { List<String> r = new ArrayList<>(); for (List<String> l : ls) r.addAll(l); return r; }

    static String childText(Element e, String tag) {
        for (Element c : children(e, tag)) { String t = c.getTextContent().trim(); return t.isEmpty() ? null : t; }
        return null;
    }

    static String attr(Element e, String n) { String v = e.getAttribute(n); return v == null || v.isEmpty() ? null : v; }

    static List<Element> childElements(Element e) {
        List<Element> r = new ArrayList<>();
        NodeList nl = e.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) r.add((Element) nl.item(i));
        return r;
    }

    static List<Element> children(Element e, String tag) {
        return childElements(e).stream().filter(c -> c.getTagName().equals(tag)).toList();
    }

    // ------------------------------------------------------------------------------------------
    // Class file header: access flags + class-level annotations (ApiStatus.*, Deprecated)
    // ------------------------------------------------------------------------------------------

    static final class ClassHeader {
        boolean isPublic, isInterface;
        final Set<String> annotations = new TreeSet<>();

        static ClassHeader parse(byte[] b) {
            ClassHeader h = new ClassHeader();
            try {
                DataInputStream in = new DataInputStream(new ByteArrayInputStream(b));
                if (in.readInt() != 0xCAFEBABE) return h;
                in.readUnsignedShort(); in.readUnsignedShort();
                int n = in.readUnsignedShort();
                String[] utf = new String[n];
                for (int i = 1; i < n; i++) {
                    int tag = in.readUnsignedByte();
                    switch (tag) {
                        case 1 -> utf[i] = in.readUTF();
                        case 3, 4 -> in.readInt();
                        case 5, 6 -> { in.readLong(); i++; }
                        case 7, 8, 16, 19, 20 -> in.readUnsignedShort();
                        case 9, 10, 11, 12, 17, 18 -> in.readInt();
                        case 15 -> { in.readUnsignedByte(); in.readUnsignedShort(); }
                        default -> { return h; }
                    }
                }
                int access = in.readUnsignedShort();
                h.isPublic = (access & 0x0001) != 0;
                h.isInterface = (access & 0x0200) != 0;
                in.readUnsignedShort(); in.readUnsignedShort();
                int ifc = in.readUnsignedShort();
                for (int i = 0; i < ifc; i++) in.readUnsignedShort();
                for (int k = 0; k < 2; k++) {          // fields, methods
                    int cnt = in.readUnsignedShort();
                    for (int i = 0; i < cnt; i++) {
                        in.readUnsignedShort(); in.readUnsignedShort(); in.readUnsignedShort();
                        skipAttributes(in);
                    }
                }
                int ac = in.readUnsignedShort();
                for (int i = 0; i < ac; i++) {
                    String name = utf[in.readUnsignedShort()];
                    int len = in.readInt();
                    if ("Deprecated".equals(name)) h.annotations.add("Deprecated");
                    if ("RuntimeVisibleAnnotations".equals(name) || "RuntimeInvisibleAnnotations".equals(name)) {
                        int na = in.readUnsignedShort();
                        for (int a = 0; a < na; a++) {
                            String type = utf[in.readUnsignedShort()];
                            if (type != null) {
                                if (type.contains("ApiStatus$Internal")) h.annotations.add("Internal");
                                else if (type.contains("ApiStatus$Experimental")) h.annotations.add("Experimental");
                                else if (type.contains("ApiStatus$ScheduledForRemoval")) h.annotations.add("ScheduledForRemoval");
                                else if (type.contains("ApiStatus$NonExtendable")) h.annotations.add("NonExtendable");
                                else if (type.contains("ApiStatus$OverrideOnly")) h.annotations.add("OverrideOnly");
                                else if (type.equals("Ljava/lang/Deprecated;") || type.equals("Lkotlin/Deprecated;")) h.annotations.add("Deprecated");
                            }
                            int pairs = in.readUnsignedShort();
                            for (int p = 0; p < pairs; p++) { in.readUnsignedShort(); skipElementValue(in); }
                        }
                    } else in.skipNBytes(len);
                }
            } catch (Exception ignored) { /* best effort */ }
            return h;
        }

        static void skipAttributes(DataInputStream in) throws IOException {
            int n = in.readUnsignedShort();
            for (int i = 0; i < n; i++) { in.readUnsignedShort(); in.skipNBytes(in.readInt()); }
        }

        static void skipElementValue(DataInputStream in) throws IOException {
            int tag = in.readUnsignedByte();
            switch (tag) {
                case 'e' -> { in.readUnsignedShort(); in.readUnsignedShort(); }
                case '@' -> { in.readUnsignedShort(); int p = in.readUnsignedShort(); for (int i = 0; i < p; i++) { in.readUnsignedShort(); skipElementValue(in); } }
                case '[' -> { int c = in.readUnsignedShort(); for (int i = 0; i < c; i++) skipElementValue(in); }
                default -> in.readUnsignedShort();
            }
        }
    }

    // ------------------------------------------------------------------------------------------
    // Minimal JSON parser (product-info.json)
    // ------------------------------------------------------------------------------------------

    static final class Json {
        final String s; int i;
        Json(String s) { this.s = s; }

        Object parse() { ws(); Object v = value(); ws(); return v; }

        Object value() {
            ws();
            char c = s.charAt(i);
            if (c == '{') { i++; Map<String, Object> m = new LinkedHashMap<>(); ws(); if (s.charAt(i) == '}') { i++; return m; }
                while (true) { ws(); String k = string(); ws(); expect(':'); m.put(k, value()); ws(); if (s.charAt(i) == ',') { i++; continue; } expect('}'); return m; } }
            if (c == '[') { i++; List<Object> l = new ArrayList<>(); ws(); if (s.charAt(i) == ']') { i++; return l; }
                while (true) { l.add(value()); ws(); if (s.charAt(i) == ',') { i++; continue; } expect(']'); return l; } }
            if (c == '"') return string();
            if (s.startsWith("true", i)) { i += 4; return true; }
            if (s.startsWith("false", i)) { i += 5; return false; }
            if (s.startsWith("null", i)) { i += 4; return null; }
            int st = i;
            while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
            return s.substring(st, i);
        }

        String string() {
            expect('"');
            StringBuilder b = new StringBuilder();
            while (true) {
                char c = s.charAt(i++);
                if (c == '"') return b.toString();
                if (c == '\\') {
                    char e = s.charAt(i++);
                    switch (e) {
                        case 'n' -> b.append('\n'); case 't' -> b.append('\t'); case 'r' -> b.append('\r');
                        case 'b' -> b.append('\b'); case 'f' -> b.append('\f');
                        case 'u' -> { b.append((char) Integer.parseInt(s.substring(i, i + 4), 16)); i += 4; }
                        default -> b.append(e);
                    }
                } else b.append(c);
            }
        }

        void ws() { while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++; }
        void expect(char c) { if (s.charAt(i) != c) throw new IllegalStateException("JSON: expected '" + c + "' at " + i); i++; }
    }
}
