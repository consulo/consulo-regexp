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

import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.impl.psi.ASTWrapperPsiElement;
import consulo.language.inject.InjectedLanguageManager;
import consulo.language.inject.InjectedLanguageManagerUtil;
import consulo.language.parser.LanguageParserDefinitions;
import consulo.language.parser.ParserDefinition;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.util.IncorrectOperationException;
import org.intellij.lang.regexp.RegExpLanguage;
import org.intellij.lang.regexp.psi.RegExpElement;
import org.intellij.lang.regexp.psi.RegExpElementVisitor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class RegExpElementImpl extends ASTWrapperPsiElement implements RegExpElement {
    public RegExpElementImpl(ASTNode node) {
        super(node);
    }

    @Nonnull
    public Language getLanguage() {
        return RegExpLanguage.INSTANCE;
    }

    @Nonnull
    @SuppressWarnings({ "ConstantConditions", "EmptyMethod" })
    public ASTNode getNode() {
        return super.getNode();
    }

    public String toString() {
        return getClass().getSimpleName() + ": <" + getText() + ">";
    }

    public void accept(@Nonnull PsiElementVisitor visitor) {
        if (visitor instanceof RegExpElementVisitor) {
            accept((RegExpElementVisitor)visitor);
        } else {
            super.accept(visitor);
        }
    }

    public void accept(RegExpElementVisitor visitor) {
        visitor.visitRegExpElement(this);
    }

    public PsiElement replace(@Nonnull PsiElement psiElement) throws IncorrectOperationException {
        final ASTNode node = psiElement.getNode();
        assert node != null;
        getNode().getTreeParent().replaceChild(getNode(), node);
        return psiElement;
    }

    public void delete() throws IncorrectOperationException {
        getNode().getTreeParent().removeChild(getNode());
    }

    public final String getUnescapedText() {
        if (InjectedLanguageManagerUtil.isInInjectedLanguagePrefixSuffix(this)) {
            // do not attempt to decode text if PsiElement is part of prefix/suffix
            return getText();
        }
        return InjectedLanguageManager.getInstance(getProject()).getUnescapedText(this);
    }

  public static boolean isLiteralExpression(@Nullable PsiElement context) {
    if (context == null) return false;
    final ASTNode astNode = context.getNode();
    if (astNode == null) {
      return false;
    }
    final IElementType elementType = astNode.getElementType();
    final ParserDefinition parserDefinition = LanguageParserDefinitions.INSTANCE.forLanguage(context.getLanguage());
    return parserDefinition.getStringLiteralElements(context.getLanguageVersion()).contains(elementType);
  }
}
