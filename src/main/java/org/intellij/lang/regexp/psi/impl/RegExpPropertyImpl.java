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
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.completion.lookup.LookupElementBuilder;
import consulo.language.editor.completion.lookup.PrioritizedLookupElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.util.IncorrectOperationException;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.util.collection.ArrayUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.intellij.lang.regexp.RegExpLanguageHosts;
import org.intellij.lang.regexp.RegExpTT;
import org.intellij.lang.regexp.psi.RegExpElementVisitor;
import org.intellij.lang.regexp.psi.RegExpProperty;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RegExpPropertyImpl extends RegExpElementImpl implements RegExpProperty {
    public RegExpPropertyImpl(ASTNode astNode) {
        super(astNode);
    }

    @Override
    public PsiReference getReference() {
        final ASTNode lbrace = getNode().findChildByType(RegExpTT.LBRACE);
        if (lbrace == null) {
            return null;
        }
        return new MyPsiReference();
    }

    @Override
    public boolean isNegated() {
        final ASTNode node1 = getNode().findChildByType(RegExpTT.PROPERTY);
        final ASTNode node2 = getNode().findChildByType(RegExpTT.CARET);
        return (node1 != null && node1.textContains('P')) ^ (node2 != null);
    }

    @Override
    public @Nullable ASTNode getCategoryNode() {
        return getNode().findChildByType(RegExpTT.NAME);
    }

    @Override
    public @Nullable ASTNode getValueNode() {
        ASTNode node = getNode();
        ASTNode eq = node.findChildByType(RegExpTT.EQ);
        return eq != null ? node.findChildByType(RegExpTT.NAME, eq) : null;
    }

    @Override
    public void accept(RegExpElementVisitor visitor) {
        visitor.visitRegExpProperty(this);
    }

    private class MyPsiReference implements PsiReference {
        @RequiredReadAction
        @Override
        public @Nonnull PsiElement getElement() {
            return RegExpPropertyImpl.this;
        }

        @RequiredReadAction
        @Override
        public @Nonnull TextRange getRangeInElement() {
            ASTNode node = getNode();
            ASTNode firstNode = node.findChildByType(RegExpTT.CARET);
            if (firstNode == null) {
                firstNode = node.findChildByType(RegExpTT.LBRACE);
            }
            assert firstNode != null;
            ASTNode eq = node.findChildByType(RegExpTT.EQ);
            final ASTNode rbrace = node.findChildByType(RegExpTT.RBRACE);
            int to;
            if (eq != null) {
                to = eq.getTextRange().getEndOffset() - 1;
            }
            else if (rbrace != null) {
                to = rbrace.getTextRange().getEndOffset() - 1;
            }
            else {
                to = getTextRange().getEndOffset();
            }

            final TextRange t = new TextRange(firstNode.getStartOffset() + 1, to);
            return t.shiftRight(-getTextRange().getStartOffset());
        }

        @RequiredReadAction
        @Override
        public @Nullable PsiElement resolve() {
            return RegExpPropertyImpl.this;
        }

        @RequiredReadAction
        @Override
        public @Nonnull String getCanonicalText() {
            return getRangeInElement().substring(getElement().getText());
        }

        @RequiredWriteAction
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
            return false;
        }

        @RequiredReadAction
        @Override
        @Nonnull
        public Object[] getVariants() {
            final ASTNode categoryNode = getCategoryNode();
            if (categoryNode != null && categoryNode.getText().startsWith("In") && !categoryNode.getText().startsWith("Intelli")) {
                return UNICODE_BLOCKS;
            }
            else {
                boolean startsWithIs = categoryNode != null && categoryNode.getText().startsWith("Is");
                Collection<LookupElement> result = new ArrayList<>();
                for (String[] properties : RegExpLanguageHosts.INSTANCE.getAllKnownProperties(getElement())) {
                    String name = ArrayUtil.getFirstElement(properties);
                    if (name != null) {
                        String typeText = properties.length > 1 ? properties[1] : ("Character.is" + name.substring("java".length()) + "()");
                        result.add(PrioritizedLookupElement.withPriority(LookupElementBuilder.create(name)
                            .withPresentableText(startsWithIs ? "Is" + name : name)
                            .withIcon(PlatformIconGroup.nodesProperty())
                            .withTypeText(typeText), getPriority(name)));
                    }
                }
                return ArrayUtil.toObjectArray(result);
            }
        }

        private int getPriority(@Nonnull String propertyName) {
            if (propertyName.equals("all")) {
                return 3;
            }
            if (propertyName.startsWith("java")) {
                return 1;
            }
            if (propertyName.length() > 2) {
                return 2;
            }
            return 0;
        }

        @RequiredReadAction
        @Override
        public boolean isSoft() {
            return true;
        }
    }

    private static final String[] UNICODE_BLOCKS;

    static {
        final Field[] fields = Character.UnicodeBlock.class.getFields();
        final List<String> unicodeBlocks = new ArrayList<>(fields.length);
        for (Field field : fields) {
            if (field.getType().equals(Character.UnicodeBlock.class)
                && Modifier.isStatic(field.getModifiers())
                && Modifier.isFinal(field.getModifiers())) {
                unicodeBlocks.add("In" + field.getName());
            }
        }
        UNICODE_BLOCKS = ArrayUtil.toStringArray(unicodeBlocks);
    }
}
