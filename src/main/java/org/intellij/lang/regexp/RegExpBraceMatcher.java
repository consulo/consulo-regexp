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
import consulo.language.BracePair;
import consulo.language.Language;
import consulo.language.PairedBraceMatcher;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiFile;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@ExtensionImpl
public class RegExpBraceMatcher implements PairedBraceMatcher {
    @Override
    public BracePair[] getPairs() {
        return new BracePair[]{
            new BracePair(RegExpTT.GROUP_BEGIN, RegExpTT.GROUP_END, true),
            new BracePair(RegExpTT.SET_OPTIONS, RegExpTT.GROUP_END, true),
            new BracePair(RegExpTT.NON_CAPT_GROUP, RegExpTT.GROUP_END, true),
            new BracePair(RegExpTT.POS_LOOKAHEAD, RegExpTT.GROUP_END, true),
            new BracePair(RegExpTT.NEG_LOOKAHEAD, RegExpTT.GROUP_END, true),
            new BracePair(RegExpTT.POS_LOOKBEHIND, RegExpTT.GROUP_END, true),
            new BracePair(RegExpTT.NEG_LOOKBEHIND, RegExpTT.GROUP_END, true),
            new BracePair(RegExpTT.PYTHON_NAMED_GROUP, RegExpTT.GROUP_END, true),
            new BracePair(RegExpTT.PYTHON_NAMED_GROUP_REF, RegExpTT.GROUP_END, true),
            new BracePair(RegExpTT.CLASS_BEGIN, RegExpTT.CLASS_END, false),
            new BracePair(RegExpTT.LBRACE, RegExpTT.RBRACE, false),
            new BracePair(RegExpTT.QUOTE_BEGIN, RegExpTT.QUOTE_END, false),
        };
    }

    @Override
    public boolean isPairedBracesAllowedBeforeType(@Nonnull IElementType lbraceType, @Nullable IElementType contextType) {
        return false;
    }

    @Override
    public int getCodeConstructStart(PsiFile file, int openingBraceOffset) {
        return openingBraceOffset;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return RegExpLanguage.INSTANCE;
    }
}
