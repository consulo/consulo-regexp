// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.intellij.lang.regexp;

import consulo.annotation.internal.MigratedExtensionsTo;
import consulo.component.util.localize.AbstractBundle;
import consulo.regexp.localize.RegExpLocalize;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.PropertyKey;

@Deprecated
@MigratedExtensionsTo(RegExpLocalize.class)
public final class RegExpBundle extends AbstractBundle {
    private static final String BUNDLE = "messages.RegExpBundle";
    private static final RegExpBundle INSTANCE = new RegExpBundle();

    private RegExpBundle() {
        super(BUNDLE);
    }

    public static String message(@Nonnull @PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return INSTANCE.getMessage(key, params);
    }
}
