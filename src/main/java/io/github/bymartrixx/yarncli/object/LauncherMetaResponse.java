package io.github.bymartrixx.yarncli.object;

import java.util.List;

public class LauncherMetaResponse {
    public final MinecraftLatest latest;
    public final List<?> versions;

    public LauncherMetaResponse(MinecraftLatest latest, List<?> versions) {
        this.latest = latest;
        this.versions = versions;
    }
}
