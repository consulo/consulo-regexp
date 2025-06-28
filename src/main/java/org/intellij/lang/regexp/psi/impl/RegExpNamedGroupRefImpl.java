// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.lang.regexp.psi.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.ast.TokenSet;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiReference;
import consulo.language.psi.SyntaxTraverser;
import consulo.language.util.IncorrectOperationException;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.intellij.lang.regexp.RegExpTT;
import org.intellij.lang.regexp.psi.RegExpElementVisitor;
import org.intellij.lang.regexp.psi.RegExpGroup;
import org.intellij.lang.regexp.psi.RegExpNamedGroupRef;

import java.util.Objects;


public class RegExpNamedGroupRefImpl extends RegExpElementImpl implements RegExpNamedGroupRef {
    private static final TokenSet RUBY_GROUP_REF_TOKENS =
        TokenSet.create(RegExpTT.RUBY_NAMED_GROUP_REF, RegExpTT.RUBY_QUOTED_NAMED_GROUP_REF,
            RegExpTT.RUBY_NAMED_GROUP_CALL, RegExpTT.RUBY_QUOTED_NAMED_GROUP_CALL);

    public RegExpNamedGroupRefImpl(ASTNode node) {
        super(node);
    }

    @Override
    public void accept(RegExpElementVisitor visitor) {
        visitor.visitRegExpNamedGroupRef(this);
    }

    @Override
    public @Nullable RegExpGroup resolve() {
        final String groupName = getGroupName();
        return groupName == null ? null : resolve(groupName, getContainingFile());
    }

    static RegExpGroup resolve(@Nonnull String groupName, PsiFile file) {
        return SyntaxTraverser.psiTraverser(file)
            .filter(RegExpGroup.class)
            .filter(group -> Objects.equals(groupName, group.getGroupName()))
            .first();
    }

    @Override
    public @Nullable String getGroupName() {
        final ASTNode nameNode = getNode().findChildByType(RegExpTT.NAME);
        return nameNode != null ? nameNode.getText() : null;
    }

    @Override
    public boolean isPythonNamedGroupRef() {
        return getNode().findChildByType(RegExpTT.PYTHON_NAMED_GROUP_REF) != null;
    }

    @Override
    public boolean isRubyNamedGroupRef() {
        final ASTNode node = getNode();
        return node.findChildByType(RUBY_GROUP_REF_TOKENS) != null;
    }

    @Override
    public boolean isNamedGroupRef() {
        return getNode().findChildByType(RegExpTT.RUBY_NAMED_GROUP_REF) != null;
    }

    @Override
    public PsiReference getReference() {
        if (getNode().findChildByType(RegExpTT.NAME) == null) {
            return null;
        }
        return new PsiReference() {
            @RequiredReadAction
            @Override
            public @Nonnull PsiElement getElement() {
                return RegExpNamedGroupRefImpl.this;
            }

            @RequiredReadAction
            @Override
            public @Nonnull TextRange getRangeInElement() {
                final ASTNode nameNode = getNode().findChildByType(RegExpTT.NAME);
                assert nameNode != null;
                final int startOffset = getNode().getFirstChildNode().getTextLength();
                return new TextRange(startOffset, startOffset + nameNode.getTextLength());
            }

            @RequiredReadAction
            @Override
            public PsiElement resolve() {
                return RegExpNamedGroupRefImpl.this.resolve();
            }

            @RequiredReadAction
            @Override
            public @Nonnull String getCanonicalText() {
                return getRangeInElement().substring(getText());
            }

            @RequiredWriteAction
            @Override
            public PsiElement handleElementRename(@Nonnull String newElementName) throws IncorrectOperationException {
                throw new UnsupportedOperationException();
            }

            @RequiredWriteAction
            @Override
            public PsiElement bindToElement(@Nonnull PsiElement element) throws IncorrectOperationException {
                throw new UnsupportedOperationException();
            }

            @RequiredReadAction
            @Override
            public boolean isReferenceTo(@Nonnull PsiElement element) {
                return resolve() == element;
            }

            @RequiredReadAction
            @Override
            @Nonnull
            public Object[] getVariants() {
                return SyntaxTraverser.psiTraverser(getContainingFile()).filter(RegExpGroup.class)
                    .filter(RegExpGroup::isAnyNamedGroup).toArray(new RegExpGroup[0]);
            }

            @RequiredReadAction
            @Override
            public boolean isSoft() {
                return false;
            }
        };
    }
}
