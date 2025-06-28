package org.intellij.lang.regexp;

import consulo.language.Language;
import consulo.language.version.LanguageVersion;
import jakarta.annotation.Nonnull;

import java.util.EnumSet;

/**
 * @author VISTALL
 * @since 2025-06-28
 */
public abstract class RegExpLanguageVersion extends LanguageVersion {
    public RegExpLanguageVersion(@Nonnull String id, @Nonnull String name, @Nonnull Language language, String[] mimeTypes) {
        super(id, name, language, mimeTypes);
    }

    @Nonnull
    public EnumSet<RegExpCapability> getCapabilities() {
        return RegExpCapability.DEFAULT_CAPABILITIES;
    }
}
