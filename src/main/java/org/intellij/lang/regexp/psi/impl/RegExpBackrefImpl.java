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
package org.intellij.lang.regexp.psi.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiReference;
import consulo.language.psi.SyntaxTraverser;
import consulo.language.psi.resolve.PsiElementProcessor;
import consulo.language.psi.util.PsiElementFilter;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.util.collection.ArrayUtil;
import consulo.util.lang.Comparing;
import org.intellij.lang.regexp.psi.RegExpBackref;
import org.intellij.lang.regexp.psi.RegExpElement;
import org.intellij.lang.regexp.psi.RegExpElementVisitor;
import org.intellij.lang.regexp.psi.RegExpGroup;

import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class RegExpBackrefImpl extends RegExpElementImpl implements RegExpBackref {
    public RegExpBackrefImpl(ASTNode astNode) {
        super(astNode);
    }

    @Override
    @RequiredReadAction
    public int getIndex() {
        final String s = getUnescapedText();
        assert s.charAt(0) == '\\';
        return Integer.parseInt(s.substring(1));
    }

    @Override
    public void accept(RegExpElementVisitor visitor) {
        visitor.visitRegExpBackref(this);
    }

    @Override
    @RequiredReadAction
    public @Nullable RegExpGroup resolve() {
        return resolve(getIndex(), getContainingFile());
    }

    static @Nullable RegExpGroup resolve(int index, PsiFile file) {
        if (index < 0) {
            return resolveRelativeGroup(Math.abs(index), file);
        }

        return SyntaxTraverser.psiTraverser(file)
            .filter(RegExpGroup.class)
            .filter(RegExpGroup::isCapturing)
            .skip(index - 1)
            .first();
    }

    private static @Nullable RegExpGroup resolveRelativeGroup(int index, PsiFile file) {
        List<RegExpGroup> groups = SyntaxTraverser.psiTraverser(file)
            .filter(RegExpGroup.class)
            .filter(RegExpGroup::isCapturing)
            .toList();
        return index <= groups.size() ? groups.get(groups.size() - index) : null;
    }

    @Nonnull
    @Override
    public PsiReference getReference() {
        return new PsiReference() {
            @RequiredReadAction
            @Override
            public PsiElement getElement() {
                return RegExpBackrefImpl.this;
            }

            @RequiredReadAction
            @Override
            public TextRange getRangeInElement() {
                return TextRange.from(0, getElement().getTextLength());
            }

            @RequiredReadAction
            @Override
            public String getCanonicalText() {
                return getElement().getText();
            }

            @RequiredWriteAction
            @Nonnull
            @Override
            public PsiElement handleElementRename(@Nonnull String newElementName) throws IncorrectOperationException {
                throw new IncorrectOperationException();
            }

            @RequiredWriteAction
            @Override
            public PsiElement bindToElement(@Nonnull PsiElement element) throws IncorrectOperationException {
                throw new IncorrectOperationException();
            }

            @RequiredReadAction
            @Override
            public boolean isReferenceTo(@Nonnull PsiElement element) {
                return Comparing.equal(element, resolve());
            }

            @RequiredReadAction
            @Override
            public boolean isSoft() {
                return false;
            }

            @RequiredReadAction
            @Override
            public @Nullable PsiElement resolve() {
                return RegExpBackrefImpl.this.resolve();
            }
        };
    }
}
