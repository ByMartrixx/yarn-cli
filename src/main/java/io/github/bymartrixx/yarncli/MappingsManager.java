package io.github.bymartrixx.yarncli;

import com.google.gson.Gson;
import io.github.bymartrixx.yarncli.object.LauncherMetaResponse;
import io.github.bymartrixx.yarncli.object.MappingsResult;
import io.github.bymartrixx.yarncli.object.MetaYarnVersion;
import io.github.bymartrixx.yarncli.object.MinecraftLatest;
import net.fabricmc.mapping.tree.*;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class MappingsManager {
    private static final String YARN_URL = "https://meta.fabricmc.net/v2/versions/yarn/{VERSION}?limit=1";
    private static final String MAVEN_URL = "https://maven.fabricmc.net/net/fabricmc/yarn/{VERSION}/yarn-{VERSION}-mergedv2.jar";
    private static final String LAUNCHERMETA_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    public static final String NS_OFFICIAL = "official";
    public static final String NS_INTERMEDIARY = "intermediary";
    public static final String NS_NAMED = "named";
    public static final String VERSION_REGEX = "[A-z0-9.]+";

    private final Gson gson;

    private final Map<String, @Nullable TinyTree> versionCache = new HashMap<>();
    public final Map<String, String> versionNames = new HashMap<>();
    public final Map<String, String> yarnVersions = new HashMap<>();
    private String selectedVersion;

    public MappingsManager() {
        this.gson = new Gson();
    }

    private boolean exactMatches(Mapped mapped, String query) {
        return mapped.getName(NS_INTERMEDIARY).equals(query)
                || mapped.getName(NS_NAMED).equals(query)
                || mapped.getName(NS_OFFICIAL).equals(query);
    }

    private boolean matches(Mapped mapped, String query) {
        return mapped.getName(NS_INTERMEDIARY).endsWith(query)
                || mapped.getName(NS_NAMED).endsWith(query)
                || mapped.getName(NS_OFFICIAL).endsWith(query);
    }

    public @Nullable List<MappingsResult> getClassMappings(String query) {
        return this.getClassMappings(this.selectedVersion, query);
    }

    public @Nullable List<MappingsResult> getClassMappings(String mcVersion, String query) {
        TinyTree mappings = openMappings(mcVersion);
        if (mappings == null) {
            return null;
        }

        String rewrittenQuery = preProcessClassQuery(query);

        return mappings.getClasses().stream().filter(classDef -> matches(classDef, rewrittenQuery)).map(classDef -> new MappingsResult(classDef, null)).collect(Collectors.toList());
    }

    private String preProcessClassQuery(String query) {
        if (query.matches("[\\d]")) {
            return "class_" + query;
        }

        return query;
    }

    public @Nullable List<MappingsResult> getMethodMappings(String query) {
        return this.getMethodMappings(this.selectedVersion, query);
    }

    public @Nullable List<MappingsResult> getMethodMappings(String mcVersion, String query) {
        TinyTree mappings = openMappings(mcVersion);
        if (mappings == null) {
            return null;
        }

        return getMappingsResults(mappings, preProcessMethodQuery(query), ClassDef::getMethods);
    }

    private String preProcessMethodQuery(String query) {
        if (query.matches("[\\d]")) {
            return "method_" + query;
        }

        return query;
    }

    public @Nullable List<MappingsResult> getFieldMappings(String query) {
        return this.getFieldMappings(this.selectedVersion, query);
    }

    public @Nullable List<MappingsResult> getFieldMappings(String mcVersion, String query) {
        TinyTree mappings = openMappings(mcVersion);
        if (mappings == null) {
            return null;
        }

        return getMappingsResults(mappings, preProcessFieldQuery(query), ClassDef::getFields);
    }

    private String preProcessFieldQuery(String query) {
        if (query.matches("[\\d]")) {
            return "field_" + query;
        }

        return query;
    }

    private List<MappingsResult> getMappingsResults(TinyTree tree, String query, DescriptoredProvider body) {
        List<MappingsResult> result = new ArrayList<>();

        Collection<ClassDef> classes = tree.getClasses();
        for (ClassDef classDef : classes) {
            Collection<? extends Descriptored> descriptors = body.provide(classDef);
            for (Descriptored descriptor : descriptors) {
                if (this.exactMatches(descriptor, query)) {
                    result.add(new MappingsResult(classDef, descriptor));
                }
            }
        }

        return result;
    }

    public void cacheMappings(String release, String snapshot) {
        String releaseYarnVersion = this.getLatestYarnVersion(release);
        String snapshotYarnVersion = this.getLatestYarnVersion(snapshot);

        if (yarnVersions.get(release) != null
                && yarnVersions.get(release).equals(releaseYarnVersion)
                && yarnVersions.get(snapshot) != null
                && yarnVersions.get(snapshot).equals(snapshotYarnVersion)) {
            return;
        }

        if (versionNames.isEmpty()) {
            this.selectedVersion = release;
            versionNames.put("release", release);
            versionNames.put("snapshot", snapshot);
        }

        if (!versionCache.containsKey(release)) {
            if (releaseYarnVersion != null) {
                TinyTree releaseVersion = openMappings(release);

                if (releaseVersion != null) {
                    versionCache.remove(versionNames.get("release"));
                    versionCache.put(release, releaseVersion);

                    this.selectedVersion = release;
                    versionNames.put("release", release);
                    releaseVersion = null;

                    yarnVersions.put(release, releaseYarnVersion);
                }
            }
        }

        if (!versionCache.containsKey(snapshot)) {
            if (snapshotYarnVersion != null) {
                TinyTree snaphotVersion = openMappings(snapshot);

                if (snaphotVersion != null) {
                    versionCache.remove(versionNames.get("snapshot"));
                    versionCache.put(snapshot, snaphotVersion);

                    versionNames.put("snapshot", snapshot);
                    snaphotVersion = null;

                    yarnVersions.put(snapshot, snapshotYarnVersion);
                }
            }
        }
    }

    @Nullable
    private TinyTree openMappings(String mcVersion) {
        return this.openMappings(mcVersion, () -> {}, () -> {});
    }

    @Nullable
    private TinyTree openMappings(String mcVersion, Callback startDownloadCallback, Callback finishDownloadCallback) {
        String latestYarnVersion = this.getLatestYarnVersion(mcVersion);
        if (latestYarnVersion == null) {
            return null;
        }

        if (this.versionCache.containsKey(mcVersion)) {
            return versionCache.get(mcVersion);
        }

        Path appTinyPath = Paths.get(YarnCli.appPath.toString(), latestYarnVersion + ".tiny");
        Path appJarPath = Paths.get(YarnCli.appPath.toString(), latestYarnVersion + ".jar");
        File loomTinyFile = new File(YarnCli.loomPath.toFile(), String.format("yarn-%s-v2.tiny", latestYarnVersion));

        if (!appTinyPath.toFile().exists()) {
            if (loomTinyFile.exists()) {
                try {
                    BufferedReader bufferedReader = Files.newBufferedReader(loomTinyFile.toPath(), StandardCharsets.UTF_8);
                    return TinyMappingFactory.loadWithDetection(bufferedReader);
                } catch (IOException e) {
                    e.printStackTrace(); // If it fails, try to use the app folder tiny file
                }
            }

            try {
                startDownloadCallback.callback();
                CloseableHttpClient client = HttpClients.createDefault();

                HttpGet request = new HttpGet(MAVEN_URL.replace("{VERSION}", latestYarnVersion));
                CloseableHttpResponse response = client.execute(request);

                Files.copy(response.getEntity().getContent(), appJarPath, StandardCopyOption.REPLACE_EXISTING);

                FileSystem fileSystem = FileSystems.newFileSystem(appJarPath, null);
                Files.copy(fileSystem.getPath("mappings/mappings.tiny"), appTinyPath, StandardCopyOption.REPLACE_EXISTING);

                finishDownloadCallback.callback();
                fileSystem.close();
                response.close();
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
                finishDownloadCallback.callback(); // If not called, a progress bar would spin indefinitely
                return null;
            }
        }

        try {
            BufferedReader bufferedReader = Files.newBufferedReader(appTinyPath, StandardCharsets.UTF_8);
            return TinyMappingFactory.loadWithDetection(bufferedReader);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    private String getLatestYarnVersion(String mcVersion) {
        String encodedVersion = null;
        try {
            //noinspection CharsetObjectCanBeUsed // encode(String, Charset) not in java 8
            encodedVersion = URLEncoder.encode(mcVersion, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return null; // Should not happen
        }
        String url = YARN_URL.replace("{VERSION}", encodedVersion);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            InputStreamReader responseReader = new InputStreamReader(client.execute(request).getEntity().getContent());

            MetaYarnVersion metaYarnVersion = this.gson.fromJson(responseReader, MetaYarnVersion[].class)[0];
            responseReader.close();
            client.close();

            return metaYarnVersion.version;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ArrayIndexOutOfBoundsException ignored) {
            return null;
        }
    }

    public MinecraftLatest getLatestMinecraftVersions() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();

        HttpGet request = new HttpGet(LAUNCHERMETA_URL);
        InputStreamReader responseReader = new InputStreamReader(client.execute(request).getEntity().getContent());

        LauncherMetaResponse response = this.gson.fromJson(responseReader, LauncherMetaResponse.class);
        responseReader.close();
        client.close();

        return response.latest;
    }

    public void selectVersion(@NotNull String version) {
        if (!version.matches(VERSION_REGEX) && !version.equals("")) {
            OutputUtil.yellow();
            OutputUtil.printf("Invalid minecraft version specified: \"%s\"%n", version);
            OutputUtil.reset();
            return;
        }

        // Get the minecraft version
        String mcVersion;
        switch (version.toLowerCase()) {
            case "":
                mcVersion = YarnCli.latest.release;
                break;
            case "release":
                mcVersion = versionNames.getOrDefault(version.toLowerCase(), YarnCli.latest.release);
                break;
            case "snapshot":
                mcVersion = versionNames.getOrDefault(version.toLowerCase(), YarnCli.latest.snapshot);
                break;
            default:
                mcVersion = version;
        }

        // A progress bar to show the mappings download
        YarnCli.SpinningProgressBar downloadProgressBar = new YarnCli.SpinningProgressBar(String.format("Downloading Yarn mappings for %s... ", mcVersion), "[ done ]\n", 50, YarnCli.SPINNING_PROGRESS_BAR_FRAMES);

        // Check if there are Yarn mappings for the version, if there are they will be downloaded if required
        TinyTree dummy = this.openMappings(mcVersion, downloadProgressBar::startSpinning, downloadProgressBar::stopSpinning);
        if (dummy == null) {
            OutputUtil.red();
            OutputUtil.printf("Unable to find Yarn mappings for Minecraft %s%n", mcVersion);
            OutputUtil.reset();
            return;
        }

        this.selectedVersion = mcVersion;
        OutputUtil.lightBlue();
        OutputUtil.printf("Changed to version \"%s\"%n", mcVersion);
        OutputUtil.reset();
    }

    public String getSelectedVersion() {
        return this.selectedVersion;
    }

    @FunctionalInterface
    private interface DescriptoredProvider {
        Collection<? extends Descriptored> provide(ClassDef classDef);
    }

    @FunctionalInterface
    interface Callback {
        void callback();
    }
}
