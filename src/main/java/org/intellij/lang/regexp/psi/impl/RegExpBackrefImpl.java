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
import consulo.language.psi.PsiReference;
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

import javax.annotation.Nonnull;

public class RegExpBackrefImpl extends RegExpElementImpl implements RegExpBackref {
    public RegExpBackrefImpl(ASTNode astNode) {
        super(astNode);
    }

    @RequiredReadAction
    public int getIndex() {
        final String s = getUnescapedText();
        assert s.charAt(0) == '\\';
        return Integer.parseInt(s.substring(1));
    }

    public void accept(RegExpElementVisitor visitor) {
        visitor.visitRegExpBackref(this);
    }

    @RequiredReadAction
    public RegExpGroup resolve() {
        final int index = getIndex();

        final PsiElementProcessor.FindFilteredElement<RegExpElement> processor =
            new PsiElementProcessor.FindFilteredElement<>(new PsiElementFilter() {
                int groupCount;

                public boolean isAccepted(PsiElement element) {
                    if (element instanceof RegExpGroup group) {
                        if (group.isCapturing() && ++groupCount == index) {
                            return true;
                        }
                    }
                    return element == RegExpBackrefImpl.this;
                }
            });

        PsiTreeUtil.processElements(getContainingFile(), processor);
        return processor.getFoundElement() instanceof RegExpGroup group ? group : null;
    }

    public PsiReference getReference() {
        return new PsiReference() {
            @RequiredReadAction
            public PsiElement getElement() {
                return RegExpBackrefImpl.this;
            }

            @Nonnull
            @RequiredReadAction
            public TextRange getRangeInElement() {
                return TextRange.from(0, getElement().getTextLength());
            }

            @Nonnull
            @RequiredReadAction
            public String getCanonicalText() {
                return getElement().getText();
            }

            @RequiredWriteAction
            public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
                throw new IncorrectOperationException();
            }

            @RequiredWriteAction
            public PsiElement bindToElement(@Nonnull PsiElement element) throws IncorrectOperationException {
                throw new IncorrectOperationException();
            }

            @RequiredReadAction
            public boolean isReferenceTo(PsiElement element) {
                return Comparing.equal(element, resolve());
            }

            @RequiredReadAction
            public boolean isSoft() {
                return false;
            }

            @RequiredReadAction
            public PsiElement resolve() {
                return RegExpBackrefImpl.this.resolve();
            }

            @Nonnull
            @RequiredReadAction
            public Object[] getVariants() {
                return ArrayUtil.EMPTY_OBJECT_ARRAY;
            }
        };
    }
}
