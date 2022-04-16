/*
 * Copyright 2006 Sascha Weinreuter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.lang.regexp;

import consulo.language.file.LanguageFileType;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;
import consulo.regexp.icon.RegExpIconGroup;

import javax.annotation.Nonnull;

public class RegExpFileType extends LanguageFileType {
    public static final RegExpFileType INSTANCE = new RegExpFileType();

    private RegExpFileType() {
        super(RegExpLanguage.INSTANCE);
    }

    @Nonnull
    public String getId() {
        return "RegExp";
    }

    @Nonnull
    public LocalizeValue getDescription() {
        return LocalizeValue.localizeTODO("Regular Expression");
    }

    @Nonnull
    public String getDefaultExtension() {
        return "regexp";
    }

    @Nonnull
    public Image getIcon() {
        return RegExpIconGroup.regexp();
    }
}
