/*
 * Copyright 2000-2010 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.lang.regexp;

import consulo.annotation.component.ExtensionImpl;
import consulo.colorScheme.TextAttributesKey;
import consulo.colorScheme.setting.AttributesDescriptor;
import consulo.language.editor.colorScheme.setting.ColorSettingsPage;
import consulo.language.editor.highlight.SyntaxHighlighter;
import consulo.language.editor.highlight.SyntaxHighlighterFactory;
import consulo.localize.LocalizeValue;
import consulo.regexp.localize.RegExpLocalize;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NonNls;

import java.util.HashMap;
import java.util.Map;

/**
 * @author traff
 */
@ExtensionImpl
public class RegExpColorsPage implements ColorSettingsPage {
    private static final AttributesDescriptor[] ATTRS = new AttributesDescriptor[]{
        new AttributesDescriptor("Keyword", RegExpHighlighter.META),
        new AttributesDescriptor("Escaped character", RegExpHighlighter.ESC_CHARACTER),
        new AttributesDescriptor("Invalid escape sequence", RegExpHighlighter.INVALID_CHARACTER_ESCAPE),
        new AttributesDescriptor("Redundant escape sequence", RegExpHighlighter.REDUNDANT_ESCAPE),
        new AttributesDescriptor("Brace", RegExpHighlighter.BRACES),
        new AttributesDescriptor("Bracket", RegExpHighlighter.BRACKETS),
        new AttributesDescriptor("Parenthesis", RegExpHighlighter.PARENTHS),
        new AttributesDescriptor("Comma", RegExpHighlighter.COMMA),
        new AttributesDescriptor("Bad character", RegExpHighlighter.BAD_CHARACTER),
        new AttributesDescriptor("Character class", RegExpHighlighter.CHAR_CLASS),
        new AttributesDescriptor("Quote character", RegExpHighlighter.QUOTE_CHARACTER),
        new AttributesDescriptor("Comment", RegExpHighlighter.COMMENT)
    };

    @NonNls
    private static final HashMap<String, TextAttributesKey> ourTagToDescriptorMap = new HashMap<>();

    @Nonnull
    @Override
    public LocalizeValue getDisplayName() {
        return RegExpLocalize.colorSettingsName();
    }

    @Nonnull
    @Override
    public AttributesDescriptor[] getAttributeDescriptors() {
        return ATTRS;
    }
    @Nonnull
    @Override
    public SyntaxHighlighter getHighlighter() {
        final SyntaxHighlighter highlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(RegExpFileType.INSTANCE, null, null);
        assert highlighter != null;
        return highlighter;
    }

    @Nonnull
    @Override
    public String getDemoText() {
        return "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}\\x0g\\#\\p{alpha}\\Q\\E$";
    }

    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return ourTagToDescriptorMap;
    }
}
