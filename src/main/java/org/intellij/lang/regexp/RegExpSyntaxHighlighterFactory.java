/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
import consulo.language.Language;
import consulo.language.editor.highlight.SingleLazyInstanceSyntaxHighlighterFactory;
import consulo.language.editor.highlight.SyntaxHighlighter;
import consulo.language.parser.ParserDefinition;
import jakarta.inject.Inject;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class RegExpSyntaxHighlighterFactory extends SingleLazyInstanceSyntaxHighlighterFactory {
    @Nonnull
    private final Language myLanguage;

    @Inject
    public RegExpSyntaxHighlighterFactory() {
        this(RegExpLanguage.INSTANCE);
    }

    protected RegExpSyntaxHighlighterFactory(@Nonnull Language language) {
        myLanguage = language;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return RegExpLanguage.INSTANCE;
    }

    @Nonnull
    @Override
    protected SyntaxHighlighter createHighlighter() {
        return new RegExpHighlighter(ParserDefinition.forLanguage(myLanguage));
    }
}
