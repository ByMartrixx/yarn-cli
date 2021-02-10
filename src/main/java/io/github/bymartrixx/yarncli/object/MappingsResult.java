package io.github.bymartrixx.yarncli.object;

import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.Descriptored;
import org.jetbrains.annotations.Nullable;

public class MappingsResult {
    public final ClassDef classDef;
    @Nullable
    public final Descriptored member;

    public MappingsResult(ClassDef classDef, @Nullable Descriptored member) {
        this.classDef = classDef;
        this.member = member;
    }
}
