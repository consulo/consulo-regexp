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
import consulo.application.AllIcons;
import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.editor.completion.lookup.LookupValueFactory;
import consulo.language.editor.completion.lookup.LookupValueWithPriority;
import consulo.language.editor.completion.lookup.LookupValueWithUIHint;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.util.IncorrectOperationException;
import consulo.util.collection.ArrayUtil;
import org.intellij.lang.regexp.RegExpLanguageHosts;
import org.intellij.lang.regexp.RegExpTT;
import org.intellij.lang.regexp.psi.RegExpElementVisitor;
import org.intellij.lang.regexp.psi.RegExpProperty;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class RegExpPropertyImpl extends RegExpElementImpl implements RegExpProperty {
    public RegExpPropertyImpl(ASTNode astNode) {
        super(astNode);
    }

    @Override
    public PsiReference getReference() {
        final ASTNode lbrace = getNode().findChildByType(RegExpTT.LBRACE);
        return lbrace == null ? null : new MyPsiReference();
    }

    @Override
    public boolean isNegated() {
        final ASTNode node = getNode().findChildByType(RegExpTT.PROPERTY);
        return node != null && node.textContains('P');
    }

    @Nullable
    @Override
    public ASTNode getCategoryNode() {
        return getNode().findChildByType(RegExpTT.NAME);
    }

    @Override
    public void accept(RegExpElementVisitor visitor) {
        visitor.visitRegExpProperty(this);
    }

    private class MyPsiReference implements PsiReference {
        @Override
        @RequiredReadAction
        public PsiElement getElement() {
            return RegExpPropertyImpl.this;
        }

        @Nonnull
        @Override
        @RequiredReadAction
        public TextRange getRangeInElement() {
            final ASTNode lbrace = getNode().findChildByType(RegExpTT.LBRACE);
            assert lbrace != null;
            final ASTNode rbrace = getNode().findChildByType(RegExpTT.RBRACE);
            int to = rbrace == null ? getTextRange().getEndOffset() : rbrace.getTextRange().getEndOffset() - 1;

            final TextRange t = new TextRange(lbrace.getStartOffset() + 1, to);
            return t.shiftRight(-getTextRange().getStartOffset());
        }

        @Nullable
        @Override
        @RequiredReadAction
        public PsiElement resolve() {
            return RegExpPropertyImpl.this;
        }

        @Nonnull
        @Override
        @RequiredReadAction
        public String getCanonicalText() {
            return getRangeInElement().substring(getElement().getText());
        }

        @Override
        @RequiredWriteAction
        public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
            throw new IncorrectOperationException();
        }

        @Override
        @RequiredWriteAction
        public PsiElement bindToElement(@Nonnull PsiElement element) throws IncorrectOperationException {
            throw new IncorrectOperationException();
        }

        @Override
        @RequiredReadAction
        public boolean isReferenceTo(PsiElement element) {
            return false;
        }

        @Nonnull
        @Override
        @RequiredReadAction
        public Object[] getVariants() {
            final ASTNode categoryNode = getCategoryNode();
            if (categoryNode != null && categoryNode.getText().startsWith("In")
                && !categoryNode.getText().startsWith("Intelli")) {
                return UNICODE_BLOCKS;
            }
            else {
                final String[][] knownProperties = RegExpLanguageHosts.INSTANCE.getAllKnownProperties(getElement());
                final Object[] objects = new Object[knownProperties.length];
                for (int i = 0; i < objects.length; i++) {
                    final String[] prop = knownProperties[i];
                    objects[i] = new MyLookupValue(prop);
                }
                return objects;
            }
        }

        @Override
        @RequiredReadAction
        public boolean isSoft() {
            return true;
        }

        private class MyLookupValue extends LookupValueFactory.LookupValueWithIcon
            implements LookupValueWithPriority, LookupValueWithUIHint {
            private final String[] myProp;

            public MyLookupValue(String[] prop) {
                super(prop[0], AllIcons.Nodes.Property);
                myProp = prop;
            }

            @Override
            public String getPresentation() {
                final ASTNode categoryNode = getCategoryNode();
                if (categoryNode != null && categoryNode.getText().startsWith("Is")) {
                    return "Is" + super.getPresentation();
                }
                return super.getPresentation();
            }

            @Override
            public int getPriority() {
                final String name = myProp[0];
                if (name.equals("all")) {
                    return HIGH + 1;
                }
                if (name.startsWith("java")) {
                    return HIGHER;
                }
                return name.length() > 2 ? HIGH : NORMAL;
            }

            @Override
            public String getTypeHint() {
                return myProp.length > 1 ? myProp[1] : ("Character.is" + myProp[0].substring("java".length()) + "()");
            }

            @Nullable
            @Override
            public Color getColorHint() {
                return null;
            }

            @Override
            public boolean isBold() {
                return false;
            }
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
