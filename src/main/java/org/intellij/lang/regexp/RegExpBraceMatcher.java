// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
    @Nonnull
    public BracePair[] getPairs() {
        return new BracePair[]{
            new BracePair(RegExpTT.GROUP_BEGIN, RegExpTT.GROUP_END, true),
            new BracePair(RegExpTT.SET_OPTIONS, RegExpTT.GROUP_END, true), new BracePair(RegExpTT.NON_CAPT_GROUP, RegExpTT.GROUP_END, true),
            new BracePair(RegExpTT.ATOMIC_GROUP, RegExpTT.GROUP_END, true),
            new BracePair(RegExpTT.POS_LOOKAHEAD, RegExpTT.GROUP_END, true), new BracePair(RegExpTT.NEG_LOOKAHEAD, RegExpTT.GROUP_END, true),
            new BracePair(RegExpTT.POS_LOOKBEHIND, RegExpTT.GROUP_END, true), new BracePair(RegExpTT.NEG_LOOKBEHIND, RegExpTT.GROUP_END, true),
            new BracePair(RegExpTT.PYTHON_NAMED_GROUP, RegExpTT.GROUP_END, true),
            new BracePair(RegExpTT.PYTHON_NAMED_GROUP_REF, RegExpTT.GROUP_END, true),
            new BracePair(RegExpTT.PCRE_RECURSIVE_NAMED_GROUP_REF, RegExpTT.GROUP_END, true),
            new BracePair(RegExpTT.CONDITIONAL, RegExpTT.GROUP_END, true),
            new BracePair(RegExpTT.PCRE_BRANCH_RESET, RegExpTT.GROUP_END, true),
            new BracePair(RegExpTT.RUBY_NAMED_GROUP, RegExpTT.GROUP_END, true),
            new BracePair(RegExpTT.RUBY_QUOTED_NAMED_GROUP, RegExpTT.GROUP_END, true),
            new BracePair(RegExpTT.RUBY_NAMED_GROUP_REF, RegExpTT.GT, true),
            new BracePair(RegExpTT.RUBY_QUOTED_NAMED_GROUP_REF, RegExpTT.QUOTE, true),
            new BracePair(RegExpTT.RUBY_NAMED_GROUP_CALL, RegExpTT.GT, true),
            new BracePair(RegExpTT.RUBY_QUOTED_NAMED_GROUP_CALL, RegExpTT.QUOTE, true),
            new BracePair(RegExpTT.CLASS_BEGIN, RegExpTT.CLASS_END, false), new BracePair(RegExpTT.LBRACE, RegExpTT.RBRACE, false),
            new BracePair(RegExpTT.QUOTE_BEGIN, RegExpTT.QUOTE_END, false),
            new BracePair(RegExpTT.BRACKET_EXPRESSION_BEGIN, RegExpTT.BRACKET_EXPRESSION_END, false)};
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
