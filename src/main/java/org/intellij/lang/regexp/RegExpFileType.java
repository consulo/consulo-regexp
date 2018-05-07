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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.NonNls;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import consulo.ui.image.Image;
import consulo.ui.image.ImageEffects;
import icons.RegExpSupportIcons;

public class RegExpFileType extends LanguageFileType {
    public static final RegExpFileType INSTANCE = new RegExpFileType();

    private RegExpFileType() {
        super(RegExpLanguage.INSTANCE);
    }

    @Nonnull
    @NonNls
    public String getId() {
        return "RegExp";
    }

    @Nonnull
    public String getDescription() {
        return "Regular Expression";
    }

    @Nonnull
    @NonNls
    public String getDefaultExtension() {
        return "regexp";
    }

    @Nullable
    public Image getIcon() {
        return ImageEffects.layered(AllIcons.FileTypes.Text, RegExpSupportIcons.Regexp_filetype_icon);
    }
}
