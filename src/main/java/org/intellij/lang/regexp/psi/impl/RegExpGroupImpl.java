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
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import org.intellij.lang.regexp.RegExpElementTypes;
import org.intellij.lang.regexp.RegExpTT;
import org.intellij.lang.regexp.psi.RegExpElementVisitor;
import org.intellij.lang.regexp.psi.RegExpGroup;
import org.intellij.lang.regexp.psi.RegExpPattern;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;

public class RegExpGroupImpl extends RegExpElementImpl implements RegExpGroup {
    public RegExpGroupImpl(ASTNode astNode) {
        super(astNode);
    }

    @Override
    public void accept(RegExpElementVisitor visitor) {
        visitor.visitRegExpGroup(this);
    }

    @Override
    public boolean isCapturing() {
        final ASTNode node = getNode().getFirstChildNode();
        if (node == null) {
            return false;
        }
        final IElementType type = node.getElementType();
        return type == RegExpTT.GROUP_BEGIN || type == RegExpTT.RUBY_NAMED_GROUP ||
            type == RegExpTT.RUBY_QUOTED_NAMED_GROUP || type == RegExpTT.PYTHON_NAMED_GROUP;
    }

    @Override
    public boolean isSimple() {
        final ASTNode node = getNode().getFirstChildNode();
        return node != null && (node.getElementType() == RegExpTT.GROUP_BEGIN || node.getElementType() == RegExpTT.NON_CAPT_GROUP);
    }

    @Override
    public RegExpPattern getPattern() {
        final ASTNode node = getNode().findChildByType(RegExpElementTypes.PATTERN);
        return node != null ? (RegExpPattern)node.getPsi() : null;
    }

    @Override
    public boolean isPythonNamedGroup() {
        return getNode().findChildByType(RegExpTT.PYTHON_NAMED_GROUP) != null;
    }

    @Override
    public boolean isRubyNamedGroup() {
        return getNode().findChildByType(RegExpTT.RUBY_NAMED_GROUP) != null
            || getNode().findChildByType(RegExpTT.RUBY_QUOTED_NAMED_GROUP) != null;
    }

    @Override
    public boolean isNamedGroup() {
        return getNode().findChildByType(RegExpTT.RUBY_NAMED_GROUP) != null;
    }

    @Override
    public String getGroupName() {
        if (!isPythonNamedGroup() && !isRubyNamedGroup()) {
            return null;
        }
        final ASTNode nameNode = getNode().findChildByType(RegExpTT.NAME);
        return nameNode != null ? nameNode.getText() : null;
    }

    @Override
    @RequiredReadAction
    public String getName() {
        return getGroupName();
    }

    @Override
    @RequiredWriteAction
    public PsiElement setName(@NonNls @Nonnull String name) throws IncorrectOperationException {
        throw new IncorrectOperationException();
    }
}
