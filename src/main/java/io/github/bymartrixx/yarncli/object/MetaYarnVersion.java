package io.github.bymartrixx.yarncli.object;

public class MetaYarnVersion {
    public final String gameVersion;
    public final String separator;
    public final int build;
    public final String maven;
    public final String version;
    public final boolean stable;

    public MetaYarnVersion(String gameVersion, String separator, int build, String maven, String version, boolean stable) {
        this.gameVersion = gameVersion;
        this.separator = separator;
        this.build = build;
        this.maven = maven;
        this.version = version;
        this.stable = stable;
    }
}
