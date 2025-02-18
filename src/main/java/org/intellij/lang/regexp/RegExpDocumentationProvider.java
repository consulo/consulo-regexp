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
import consulo.language.ast.ASTNode;
import consulo.language.editor.documentation.AbstractDocumentationProvider;
import consulo.language.editor.documentation.LanguageDocumentationProvider;
import consulo.language.psi.PsiElement;
import org.intellij.lang.regexp.psi.RegExpGroup;
import org.intellij.lang.regexp.psi.RegExpProperty;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author vnikolaenko
 */
@ExtensionImpl
public final class RegExpDocumentationProvider extends AbstractDocumentationProvider implements LanguageDocumentationProvider {
    @Override
    @Nullable
    public String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        if (element instanceof RegExpProperty prop) {
            final ASTNode node = prop.getCategoryNode();
            if (node != null) {
                final String description = RegExpLanguageHosts.INSTANCE.getPropertyDescription(node.getPsi(), node.getText());
                if (description != null) {
                    return prop.isNegated()
                        ? "Property block stands for characters not matching " + description
                        : "Property block stands for " + description;
                }
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        return element instanceof RegExpGroup regExpGroup ? "Capturing Group: " + regExpGroup.getUnescapedText() : null;
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return RegExpLanguage.INSTANCE;
    }
}
