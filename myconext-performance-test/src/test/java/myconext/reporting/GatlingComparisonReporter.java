package myconext.reporting;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GatlingComparisonReporter {

    private static final Path GATLING_RESULTS_DIR = Path.of("target", "gatling");
    private static final String METADATA_FILE = "myconext-run.properties";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withZone(ZoneId.systemDefault());

    private static final Pattern SIMULATION_PATTERN =
            Pattern.compile("<div class=\"onglet\">\\s*(.*?)\\s*</div>", Pattern.DOTALL);
    private static final Pattern LABEL_PATTERN = Pattern.compile(
            "<span class=\"simulation-information-label\">\\s*%s\\s*</span>\\s*<span>(.*?)</span>",
            Pattern.DOTALL);
    private static final Pattern ROW_PATTERN =
            Pattern.compile("<tr id=\"([^\"]+)\"[^>]*>(.*?)</tr>", Pattern.DOTALL);
    private static final Pattern VALUE_PATTERN =
            Pattern.compile("<td class=\"value [^\"]*\">(.*?)</td>", Pattern.DOTALL);
    private static final Pattern NAME_PATTERN =
            Pattern.compile("class=\"ellipsed-name\">(.*?)</span>", Pattern.DOTALL);
    private static final Pattern HREF_PATTERN =
            Pattern.compile("<a[^>]+href=\"([^\"]+)\"", Pattern.DOTALL);
    private static final Pattern RANGE_PATTERN = Pattern.compile(
            "text: '<span class=\"chart_title\">Response Time Ranges</span>'.*?data:\\s*\\[\\{\\s*color: '[^']+',\\s*y: (\\d+).*?\\{\\s*color: '[^']+',\\s*y: (\\d+).*?\\{\\s*color: '[^']+',\\s*y: (\\d+).*?\\{\\s*color: '[^']+',\\s*y: (\\d+)",
            Pattern.DOTALL);

    private GatlingComparisonReporter() {
    }

    public static void registerShutdownHook(String simulationSimpleName, String amountUsers) {
        Runtime.getRuntime().addShutdownHook(new Thread(
                () -> generateForLatestRun(simulationSimpleName, amountUsers),
                "gatling-compare-generator"));
    }

    public static void main(String[] args) {
        String simulationSimpleName = args.length > 0 ? args[0] : "DatabaseGrowthBenchmarkSimulation";
        generateForSimulation(simulationSimpleName);
    }

    private static void generateForLatestRun(String simulationSimpleName, String amountUsers) {
        try {
            Optional<Path> latestRun = findLatestRunDirectory(simulationSimpleName);
            latestRun.ifPresent(path -> writeMetadata(path, amountUsers));
            generateForSimulation(simulationSimpleName);
        } catch (Exception e) {
            System.err.println("Failed to generate Gatling compare report: " + e.getMessage());
        }
    }

    public static void generateForSimulation(String simulationSimpleName) {
        try {
            String simulationPrefix = simulationSimpleName.toLowerCase(Locale.ROOT) + "-";
            List<RunReport> reports = readReports(simulationPrefix);
            if (reports.isEmpty()) {
                return;
            }

            Path output = GATLING_RESULTS_DIR.resolve("compare.html");
            Files.createDirectories(output.getParent());
            Files.writeString(output, renderHtml(reports), StandardCharsets.UTF_8);
            System.out.println("Generated Gatling compare page: " + output);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Optional<Path> findLatestRunDirectory(String simulationSimpleName) throws IOException {
        String simulationPrefix = simulationSimpleName.toLowerCase(Locale.ROOT) + "-";
        if (!Files.isDirectory(GATLING_RESULTS_DIR)) {
            return Optional.empty();
        }

        try (var stream = Files.list(GATLING_RESULTS_DIR)) {
            return stream
                    .filter(Files::isDirectory)
                    .filter(path -> path.getFileName().toString().startsWith(simulationPrefix))
                    .filter(path -> Files.exists(path.resolve("index.html")))
                    .max(Comparator.comparingLong(GatlingComparisonReporter::lastModifiedSafe));
        }
    }

    private static long lastModifiedSafe(Path path) {
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException e) {
            return 0L;
        }
    }

    private static void writeMetadata(Path runDir, String amountUsers) {
        Properties properties = new Properties();
        Path metadataPath = runDir.resolve(METADATA_FILE);

        if (Files.exists(metadataPath)) {
            try (var in = Files.newInputStream(metadataPath)) {
                properties.load(in);
            } catch (IOException ignored) {
            }
        }

        if (amountUsers != null && !amountUsers.isBlank()) {
            properties.setProperty("amountUsers", amountUsers.trim());
        }
        properties.setProperty("folder", runDir.getFileName().toString());
        properties.setProperty("updatedAt", Instant.now().toString());

        try (var out = Files.newOutputStream(metadataPath)) {
            properties.store(out, "MyConext Gatling comparison metadata");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<RunReport> readReports(String simulationPrefix) throws IOException {
        List<RunReport> reports = new ArrayList<>();

        try (var stream = Files.list(GATLING_RESULTS_DIR)) {
            for (Path runDir : stream.filter(Files::isDirectory).toList()) {
                String folderName = runDir.getFileName().toString();
                if (!folderName.startsWith(simulationPrefix)) {
                    continue;
                }

                Path indexPath = runDir.resolve("index.html");
                if (!Files.exists(indexPath)) {
                    continue;
                }

                reports.add(parseReport(runDir, simulationPrefix, Files.readString(indexPath, StandardCharsets.UTF_8)));
            }
        }

        reports.sort(Comparator.comparingLong(report -> report.sortKey));
        return reports;
    }

    private static RunReport parseReport(Path runDir, String simulationPrefix, String html) throws IOException {
        String folderName = runDir.getFileName().toString();
        String folderSuffix = folderName.startsWith(simulationPrefix) ? folderName.substring(simulationPrefix.length()) : folderName;
        String simulationName = extract(html, SIMULATION_PATTERN).orElse("Unknown Simulation");
        String runDate = extractLabel(html, "Date:");
        String duration = extractLabel(html, "Duration:");

        RequestStat total = null;
        List<RequestStat> requestStats = new ArrayList<>();
        RangeStats totalRanges = parseRanges(html);

        Matcher rowMatcher = ROW_PATTERN.matcher(html);
        while (rowMatcher.find()) {
            String rowId = rowMatcher.group(1);
            ParsedRow parsedRow = parseRow(rowMatcher.group(2));
            RequestStat stat = parsedRow.stat();
            if ("ROOT".equals(rowId)) {
                total = stat.withRanges(totalRanges);
            } else {
                RangeStats requestRanges = null;
                if (parsedRow.detailHref() != null) {
                    Path detailPath = runDir.resolve(parsedRow.detailHref());
                    if (Files.exists(detailPath)) {
                        requestRanges = parseRanges(Files.readString(detailPath, StandardCharsets.UTF_8));
                    }
                }
                requestStats.add(stat.withRanges(requestRanges));
            }
        }

        if (total == null) {
            throw new IOException("No ROOT stats found in " + runDir.resolve("index.html"));
        }

        Properties properties = new Properties();
        Path metadataPath = runDir.resolve(METADATA_FILE);
        if (Files.exists(metadataPath)) {
            try (var in = Files.newInputStream(metadataPath)) {
                properties.load(in);
            }
        }

        Integer amountUsers = null;
        String amountUsersRaw = properties.getProperty("amountUsers");
        if (amountUsersRaw != null && !amountUsersRaw.isBlank()) {
            try {
                amountUsers = Integer.parseInt(amountUsersRaw.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        return new RunReport(
                folderName,
                folderSuffix,
                simulationName,
                runDate,
                duration,
                total,
                requestStats,
                Optional.ofNullable(amountUsers),
                parseSortKey(folderSuffix)
        );
    }

    private static long parseSortKey(String folderSuffix) {
        try {
            return Long.parseLong(folderSuffix);
        } catch (NumberFormatException e) {
            return Long.MAX_VALUE;
        }
    }

    private static ParsedRow parseRow(String rowHtml) throws IOException {
        Matcher nameMatcher = NAME_PATTERN.matcher(rowHtml);
        if (!nameMatcher.find()) {
            throw new IOException("Could not parse request name");
        }

        String detailHref = null;
        Matcher hrefMatcher = HREF_PATTERN.matcher(rowHtml);
        if (hrefMatcher.find()) {
            detailHref = hrefMatcher.group(1);
        }

        List<String> values = new ArrayList<>();
        Matcher valueMatcher = VALUE_PATTERN.matcher(rowHtml);
        while (valueMatcher.find()) {
            values.add(stripTags(valueMatcher.group(1)));
        }

        if (values.size() != 13) {
            throw new IOException("Unexpected number of values in stats row: " + values.size());
        }

        return new ParsedRow(new RequestStat(
                stripTags(nameMatcher.group(1)),
                parseInt(values.get(0)),
                parseInt(values.get(1)),
                parseInt(values.get(2)),
                parseDouble(values.get(3)),
                parseDouble(values.get(4)),
                parseInt(values.get(5)),
                parseInt(values.get(6)),
                parseInt(values.get(7)),
                parseInt(values.get(8)),
                parseInt(values.get(9)),
                parseInt(values.get(10)),
                parseInt(values.get(11)),
                parseInt(values.get(12)),
                null
        ), detailHref);
    }

    private static RangeStats parseRanges(String html) {
        Matcher matcher = RANGE_PATTERN.matcher(html);
        if (!matcher.find()) {
            return null;
        }
        int lt800 = Integer.parseInt(matcher.group(1));
        int between = Integer.parseInt(matcher.group(2));
        int gte1200 = Integer.parseInt(matcher.group(3));
        int failed = Integer.parseInt(matcher.group(4));
        return new RangeStats(lt800, between, gte1200, failed);
    }

    private static String extractLabel(String html, String label) {
        Pattern pattern = Pattern.compile(String.format(LABEL_PATTERN.pattern(), Pattern.quote(label)), Pattern.DOTALL);
        return extract(html, pattern).orElse("-");
    }

    private static Optional<String> extract(String html, Pattern pattern) {
        Matcher matcher = pattern.matcher(html);
        if (!matcher.find()) {
            return Optional.empty();
        }
        return Optional.of(stripTags(matcher.group(1)));
    }

    private static int parseInt(String value) {
        String normalized = value.replace(",", "").trim();
        return normalized.isEmpty() ? 0 : (int) Math.round(Double.parseDouble(normalized));
    }

    private static double parseDouble(String value) {
        String normalized = value.replace(",", "").trim();
        return normalized.isEmpty() ? 0D : Double.parseDouble(normalized);
    }

    private static String stripTags(String value) {
        return value
                .replace("&mdash;", "-")
                .replaceAll("<[^>]+>", "")
                .replace("&amp;", "&")
                .trim()
                .replaceAll("\\s+", " ");
    }

    private static String renderHtml(List<RunReport> reports) {
        RunReport base = reports.get(0);
        List<String> requestNames = new ArrayList<>(new TreeSet<>(reports.stream()
                .flatMap(report -> report.requests.stream().map(request -> request.name))
                .toList()));

        String headers = reports.stream()
                .map(report -> "<th class=\"r rh\">" + escape(report.headerLabel()) + buildBaseTag(report, base) + "</th>")
                .reduce("", String::concat);

        String tableRows = String.join("",
                sectionRow("Run info", reports.size()),
                metricRow("Folder", reports, report -> report.folderSuffix, false),
                metricRow("Database users", reports, report -> report.amountUsers.map(String::valueOf).orElse("-"), false),
                metricRow("Run date", reports, report -> report.runDate, false),
                metricRow("Duration", reports, report -> report.duration, false),
                sectionRow("Global", reports.size()),
                metricRow("Totaal", reports, report -> formatInt(report.total.total), false),
                metricRow("Geslaagd", reports, report -> formatInt(report.total.ok), false),
                metricRowWithDelta("Mislukt", reports, base, report -> formatInt(report.total.ko), report -> (double) report.total.ko, true, true),
                metricRowWithDelta("Foutpercentage", reports, base, report -> formatPct(report.total.koPct), report -> report.total.koPct, true, true),
                metricRowWithDelta("Req / sec", reports, base, report -> formatDecimal(report.total.cntPerSec), report -> report.total.cntPerSec, false, false),
                metricRowWithDelta("Gemiddeld", reports, base, report -> formatMs(report.total.meanMs), report -> (double) report.total.meanMs, true, false),
                metricRowWithDelta("p50", reports, base, report -> formatMs(report.total.p50Ms), report -> (double) report.total.p50Ms, true, false),
                metricRowWithDelta("p75", reports, base, report -> formatMs(report.total.p75Ms), report -> (double) report.total.p75Ms, true, false),
                metricRowWithDelta("p95", reports, base, report -> formatMs(report.total.p95Ms), report -> (double) report.total.p95Ms, true, false),
                metricRowWithDelta("p99", reports, base, report -> formatMs(report.total.p99Ms), report -> (double) report.total.p99Ms, true, false),
                metricRowWithDelta("Maximum", reports, base, report -> formatMs(report.total.maxMs), report -> (double) report.total.maxMs, true, false),
                rangeCountRow("t < 800 ms", reports, report -> report.total.ranges, RangeValueType.UNDER_800),
                rangePctRow("t < 800 ms %", reports, report -> report.total.ranges, base, RangeValueType.UNDER_800),
                rangeCountRow("800 ms <= t < 1200 ms", reports, report -> report.total.ranges, RangeValueType.BETWEEN_800_1200),
                rangePctRow("800 ms <= t < 1200 ms %", reports, report -> report.total.ranges, base, RangeValueType.BETWEEN_800_1200),
                rangeCountRow("t >= 1200 ms", reports, report -> report.total.ranges, RangeValueType.GTE_1200),
                rangePctRow("t >= 1200 ms %", reports, report -> report.total.ranges, base, RangeValueType.GTE_1200),
                rangeCountRow("Failed", reports, report -> report.total.ranges, RangeValueType.FAILED),
                rangePctRow("Failed %", reports, report -> report.total.ranges, base, RangeValueType.FAILED)
        );

        for (String requestName : requestNames) {
            tableRows += sectionRow("Request: " + requestName, reports.size());
            tableRows += metricRowWithDelta(
                    "Count",
                    reports,
                    base,
                    report -> formatRequestInt(report, requestName, stat -> stat.total),
                    report -> requestValue(report, requestName, stat -> (double) stat.total),
                    false,
                    false
            );
            tableRows += metricRowWithDelta(
                    "Mean",
                    reports,
                    base,
                    report -> formatRequestMs(report, requestName, stat -> stat.meanMs),
                    report -> requestValue(report, requestName, stat -> (double) stat.meanMs),
                    true,
                    false
            );
            tableRows += metricRowWithDelta(
                    "p95",
                    reports,
                    base,
                    report -> formatRequestMs(report, requestName, stat -> stat.p95Ms),
                    report -> requestValue(report, requestName, stat -> (double) stat.p95Ms),
                    true,
                    false
            );
            tableRows += metricRowWithDelta(
                    "p99",
                    reports,
                    base,
                    report -> formatRequestMs(report, requestName, stat -> stat.p99Ms),
                    report -> requestValue(report, requestName, stat -> (double) stat.p99Ms),
                    true,
                    false
            );
            tableRows += rangeCountRow("t < 800 ms", reports, report -> requestRanges(report, requestName), RangeValueType.UNDER_800);
            tableRows += rangePctRow("t < 800 ms %", reports, report -> requestRanges(report, requestName), base, RangeValueType.UNDER_800);
            tableRows += rangeCountRow("800 ms <= t < 1200 ms", reports, report -> requestRanges(report, requestName), RangeValueType.BETWEEN_800_1200);
            tableRows += rangePctRow("800 ms <= t < 1200 ms %", reports, report -> requestRanges(report, requestName), base, RangeValueType.BETWEEN_800_1200);
            tableRows += rangeCountRow("t >= 1200 ms", reports, report -> requestRanges(report, requestName), RangeValueType.GTE_1200);
            tableRows += rangePctRow("t >= 1200 ms %", reports, report -> requestRanges(report, requestName), base, RangeValueType.GTE_1200);
            tableRows += rangeCountRow("Failed", reports, report -> requestRanges(report, requestName), RangeValueType.FAILED);
            tableRows += rangePctRow("Failed %", reports, report -> requestRanges(report, requestName), base, RangeValueType.FAILED);
        }

        String template = """
                <!DOCTYPE html>
                <html lang="nl">
                <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta http-equiv="refresh" content="30">
                <title>Gatling Rapport Vergelijker</title>
                <style>
                :root {
                  --bg:#15191f; --panel:#1c2128; --panel-2:#232933;
                  --border:#313845; --border2:#414b5b;
                  --text:#e6ebf2; --text2:#b2bcc9; --text3:#7f8b99;
                  --accent:#4f8ef7; --accent-dim:#223a62;
                  --success:#68b65c; --success-dim:#233826;
                  --danger:#f15b4f; --danger-dim:#4d2928;
                  --warn:#ffa900; --warn-dim:#4e3b18;
                  --r:12px; --rs:8px;
                  --mono:Menlo,Consolas,monospace; --sans:-apple-system,BlinkMacSystemFont,"Segoe UI",sans-serif;
                }
                *{box-sizing:border-box;margin:0;padding:0;}
                body{background:var(--bg);color:var(--text);font-family:var(--sans);font-size:14px;line-height:1.6;min-height:100vh;}
                .frise{height:10px;background:linear-gradient(90deg,#3b82f6 0%,#7cc7ff 40%,#68b65c 100%);}
                header{border-bottom:1px solid var(--border);padding:18px 32px;display:flex;align-items:center;gap:14px;background:var(--panel);}
                .logo{width:30px;height:30px;background:var(--accent);border-radius:7px;display:flex;align-items:center;justify-content:center;font-family:var(--mono);font-size:13px;color:#fff;font-weight:700;}
                header h1{font-size:16px;font-weight:700;letter-spacing:-0.01em;}
                header p{font-size:12px;color:var(--text3);font-family:var(--mono);}
                .container{max-width:1380px;margin:0 auto;padding:28px 24px 40px;}
                .report-list{background:var(--panel);border:1px solid var(--border);border-radius:var(--r);padding:18px 20px;margin-bottom:20px;}
                .report-list h2{font-size:14px;margin-bottom:12px;}
                .report-list ul{display:flex;flex-direction:column;gap:6px;list-style:none;}
                .report-list a{display:inline-block;color:var(--accent);text-decoration:none;font-family:var(--mono);font-size:12px;padding:4px 0;}
                .report-list a:hover{text-decoration:underline;}
                .tbl-wrap{overflow-x:auto;border:1px solid var(--border);border-radius:var(--r);margin-bottom:20px;background:var(--panel);}
                table{width:100%;border-collapse:collapse;font-size:12px;font-family:var(--mono);}
                thead th{text-align:left;padding:10px 14px;font-weight:600;font-size:10px;color:var(--text3);letter-spacing:.06em;text-transform:uppercase;border-bottom:1px solid var(--border);white-space:nowrap;background:var(--panel-2);position:sticky;top:0;}
                thead th.r{text-align:right;}
                thead th.rh{color:var(--text2);text-transform:none;letter-spacing:0;font-size:11px;}
                tbody td{padding:7px 14px;border-bottom:1px solid var(--border);color:var(--text);white-space:nowrap;}
                tbody tr:last-child td{border-bottom:none;}
                tbody td.metric{color:var(--text3);font-size:10px;letter-spacing:.05em;text-transform:uppercase;}
                tbody td.metric.req-name{text-transform:none;font-size:11px;color:var(--text2);}
                tbody td.val{text-align:right;font-variant-numeric:tabular-nums;}
                tbody tr:hover td{background:#202631;}
                tbody tr.sec td{background:#262d38;font-weight:700;font-size:10px;color:var(--text3);padding-top:10px;text-transform:uppercase;letter-spacing:.06em;}
                .badge{display:inline-block;font-size:10px;font-weight:500;padding:1px 6px;border-radius:4px;margin-left:5px;vertical-align:middle;}
                .badge.good{background:var(--success-dim);color:var(--success);}
                .badge.bad{background:var(--danger-dim);color:var(--danger);}
                .badge.neu{background:#2f3744;color:var(--text3);}
                </style>
                </head>
                <body>
                <div class="frise"></div>
                <header>
                  <div class="logo">G</div>
                  <div>
                    <h1>Gatling Rapport Vergelijker</h1>
                    <p>__SIMULATION__ · automatisch opgebouwd na elke run · laatste update __UPDATED_AT__</p>
                  </div>
                </header>
                <div class="container">
                  <section class="report-list">
                    <h2>Rapporten</h2>
                    <ul>__REPORT_LINKS__</ul>
                  </section>
                  <div class="tbl-wrap">
                    <table>
                      <thead>
                        <tr><th>Metric</th>__HEADERS__</tr>
                      </thead>
                      <tbody>__TABLE_ROWS__</tbody>
                    </table>
                  </div>
                </div>
                </body>
                </html>
                """;

        return template
                .replace("__SIMULATION__", escape(reports.get(0).simulationName))
                .replace("__UPDATED_AT__", escape(TIMESTAMP_FORMATTER.format(Instant.now())))
                .replace("__REPORT_LINKS__", reports.stream()
                        .map(report -> "<li><a href=\"" + escape(report.folderName + "/index.html") + "\">" + escape(report.folderName)
                                + report.amountUsers.map(value -> " (" + formatInt(value) + " users)").orElse("") + "</a></li>")
                        .reduce("", String::concat))
                .replace("__HEADERS__", headers)
                .replace("__TABLE_ROWS__", tableRows);
    }

    private static String metricRow(String label, List<RunReport> reports, ValueFormatter formatter, boolean errorMetric) {
        StringBuilder row = new StringBuilder("<tr><td class=\"metric\">").append(escape(label)).append("</td>");
        for (RunReport report : reports) {
            String value = formatter.render(report);
            row.append("<td class=\"val");
            if (errorMetric && report.total.ko > 0) {
                row.append(" err-high");
            }
            row.append("\">").append(escape(value)).append("</td>");
        }
        row.append("</tr>");
        return row.toString();
    }

    private static String rangeCountRow(
            String label,
            List<RunReport> reports,
            RangeExtractor rangeExtractor,
            RangeValueType rangeValueType) {
        StringBuilder row = new StringBuilder("<tr><td class=\"metric\">").append(escape(label)).append("</td>");
        for (RunReport report : reports) {
            RangeStats stats = rangeExtractor.extract(report);
            row.append("<td class=\"val\">").append(stats == null ? "-" : formatInt(rangeValueType.value(stats))).append("</td>");
        }
        row.append("</tr>");
        return row.toString();
    }

    private static String rangePctRow(
            String label,
            List<RunReport> reports,
            RangeExtractor rangeExtractor,
            RunReport base,
            RangeValueType rangeValueType) {
        StringBuilder row = new StringBuilder("<tr><td class=\"metric\">").append(escape(label)).append("</td>");
        Double baseValue = rangePercentage(rangeExtractor.extract(base), rangeValueType);
        for (RunReport report : reports) {
            Double current = rangePercentage(rangeExtractor.extract(report), rangeValueType);
            row.append("<td class=\"val\">").append(current == null ? "-" : formatPct(current));
            if (report != base) {
                row.append(deltaBadge(percentDelta(baseValue, current), rangeValueType == RangeValueType.UNDER_800 ? false : true));
            }
            row.append("</td>");
        }
        row.append("</tr>");
        return row.toString();
    }

    private static String metricRowWithDelta(
            String label,
            List<RunReport> reports,
            RunReport base,
            ValueFormatter formatter,
            NumericValueExtractor numericValueExtractor,
            boolean lowerIsBetter,
            boolean errorMetric) {
        StringBuilder row = new StringBuilder("<tr><td class=\"metric\">").append(escape(label)).append("</td>");
        Double baseValue = numericValueExtractor.extract(base);
        for (RunReport report : reports) {
            String value = formatter.render(report);
            row.append("<td class=\"val");
            if (errorMetric && report.total.ko > 0) {
                row.append(" err-high");
            }
            row.append("\">").append(escape(value));

            if (report != base) {
                row.append(deltaBadge(percentDelta(baseValue, numericValueExtractor.extract(report)), lowerIsBetter));
            }
            row.append("</td>");
        }
        row.append("</tr>");
        return row.toString();
    }

    private static Double rangePercentage(RangeStats rangeStats, RangeValueType rangeValueType) {
        if (rangeStats == null || rangeStats.total() == 0) {
            return null;
        }
        return (rangeValueType.value(rangeStats) * 100D) / rangeStats.total();
    }

    private static String formatRequestInt(RunReport report, String requestName, IntExtractor extractor) {
        RequestStat stat = findRequest(report, requestName);
        return stat == null ? "-" : formatInt(extractor.extract(stat));
    }

    private static String formatRequestMs(RunReport report, String requestName, IntExtractor extractor) {
        RequestStat stat = findRequest(report, requestName);
        return stat == null ? "-" : formatMs(extractor.extract(stat));
    }

    private static Double requestValue(RunReport report, String requestName, NumericStatExtractor extractor) {
        RequestStat stat = findRequest(report, requestName);
        return stat == null ? null : extractor.extract(stat);
    }

    private static RangeStats requestRanges(RunReport report, String requestName) {
        RequestStat stat = findRequest(report, requestName);
        return stat == null ? null : stat.ranges;
    }

    private static String sectionRow(String title, int columnCount) {
        return "<tr class=\"sec\"><td>" + escape(title) + "</td>" + "<td></td>".repeat(columnCount) + "</tr>";
    }

    private static String buildBaseTag(RunReport report, RunReport base) {
        return report == base ? "<br><span style=\"font-size:10px;color:var(--text3);font-weight:400\">(basis)</span>" : "";
    }

    private static String deltaBadge(Double delta, boolean lowerIsBetter) {
        if (delta == null) {
            return "";
        }
        if (Math.abs(delta) < 1.0) {
            return "<span class=\"badge neu\">~</span>";
        }
        boolean good = lowerIsBetter ? delta < 0 : delta > 0;
        return "<span class=\"badge " + (good ? "good" : "bad") + "\">"
                + (delta > 0 ? "+" : "")
                + Math.round(delta)
                + "%</span>";
    }

    private static Double percentDelta(Double base, Double current) {
        if (base == null || current == null || base == 0D) {
            return null;
        }
        return ((current - base) / base) * 100D;
    }

    private static RequestStat findRequest(RunReport report, String name) {
        return report.requestMap().get(name);
    }

    private static String formatInt(int value) {
        return String.format(Locale.US, "%,d", value);
    }

    private static String formatDecimal(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private static String formatPct(double value) {
        return String.format(Locale.US, "%.1f%%", value);
    }

    private static String formatMs(int value) {
        return value + " ms";
    }

    private static String shortLabel(String input, int maxLength) {
        return input.length() <= maxLength ? input : input.substring(0, maxLength - 1) + "…";
    }

    private static String escape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    @FunctionalInterface
    private interface ValueFormatter {
        String render(RunReport report);
    }

    @FunctionalInterface
    private interface NumericValueExtractor {
        Double extract(RunReport report);
    }

    @FunctionalInterface
    private interface NumericStatExtractor {
        Double extract(RequestStat requestStat);
    }

    @FunctionalInterface
    private interface IntExtractor {
        int extract(RequestStat requestStat);
    }

    @FunctionalInterface
    private interface RangeExtractor {
        RangeStats extract(RunReport report);
    }

    private enum RangeValueType {
        UNDER_800 {
            @Override
            int value(RangeStats rangeStats) {
                return rangeStats.under800;
            }
        },
        BETWEEN_800_1200 {
            @Override
            int value(RangeStats rangeStats) {
                return rangeStats.between800And1200;
            }
        },
        GTE_1200 {
            @Override
            int value(RangeStats rangeStats) {
                return rangeStats.gte1200;
            }
        },
        FAILED {
            @Override
            int value(RangeStats rangeStats) {
                return rangeStats.failed;
            }
        };

        abstract int value(RangeStats rangeStats);
    }

    private record ParsedRow(RequestStat stat, String detailHref) {
    }

    private record RunReport(
            String folderName,
            String folderSuffix,
            String simulationName,
            String runDate,
            String duration,
            RequestStat total,
            List<RequestStat> requests,
            Optional<Integer> amountUsers,
            long sortKey
    ) {
        private Map<String, RequestStat> requestMap() {
            Map<String, RequestStat> map = new LinkedHashMap<>();
            for (RequestStat request : requests) {
                map.put(request.name, request);
            }
            return map;
        }

        private String headerLabel() {
            return amountUsers
                    .map(value -> folderSuffix + " (" + formatInt(value) + " users)")
                    .orElse(folderSuffix);
        }

        private static String formatInt(int value) {
            return String.format(Locale.US, "%,d", value);
        }
    }

    private record RequestStat(
            String name,
            int total,
            int ok,
            int ko,
            double koPct,
            double cntPerSec,
            int minMs,
            int p50Ms,
            int p75Ms,
            int p95Ms,
            int p99Ms,
            int maxMs,
            int meanMs,
            int stdDevMs,
            RangeStats ranges
    ) {
        private RequestStat withRanges(RangeStats newRanges) {
            return new RequestStat(name, total, ok, ko, koPct, cntPerSec, minMs, p50Ms, p75Ms, p95Ms, p99Ms, maxMs, meanMs, stdDevMs, newRanges);
        }
    }

    private record RangeStats(int under800, int between800And1200, int gte1200, int failed) {
        private int total() {
            return under800 + between800And1200 + gte1200 + failed;
        }
    }
}
