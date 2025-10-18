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
package org.intellij.lang.regexp.surroundWith;

import consulo.annotation.access.RequiredReadAction;
import consulo.codeEditor.Editor;
import consulo.document.Document;
import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.editor.surroundWith.Surrounder;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileFactory;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.util.lang.StringUtil;
import org.intellij.lang.regexp.RegExpFileType;
import org.intellij.lang.regexp.psi.RegExpAtom;
import org.intellij.lang.regexp.psi.RegExpPattern;
import org.intellij.lang.regexp.psi.impl.RegExpElementImpl;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

class GroupSurrounder implements Surrounder {
    private final LocalizeValue myTitle;
    private final String myGroupStart;

    public GroupSurrounder(LocalizeValue title, String groupStart) {
        myTitle = title;
        myGroupStart = groupStart;
    }

    @Nonnull
    @Override
    public LocalizeValue getTemplateDescription() {
        return myTitle;
    }

    @Override
    public boolean isApplicable(@Nonnull PsiElement[] elements) {
        return elements.length == 1 || PsiTreeUtil.findCommonParent(elements) == elements[0].getParent();
    }

    @Nullable
    @Override
    @RequiredReadAction
    public TextRange surroundElements(@Nonnull Project project, @Nonnull Editor editor, @Nonnull PsiElement[] elements)
        throws IncorrectOperationException {
        assert elements.length == 1 || PsiTreeUtil.findCommonParent(elements) == elements[0].getParent();
        final PsiElement e = elements[0];
        final ASTNode node = e.getNode();
        assert node != null;

        final ASTNode parent = node.getTreeParent();

        final StringBuilder s = new StringBuilder();
        for (int i = 0; i < elements.length; i++) {
            final PsiElement element = elements[i];
            if (element instanceof RegExpElementImpl regExpElement) {
                s.append(regExpElement.getUnescapedText());
            }
            else {
                s.append(element.getText());
            }
            if (i > 0) {
                final ASTNode child = element.getNode();
                assert child != null;
                parent.removeChild(child);
            }
        }
        final PsiFileFactory factory = PsiFileFactory.getInstance(project);

        final PsiFile f = factory.createFileFromText("dummy.regexp", RegExpFileType.INSTANCE, makeReplacement(s));
        final RegExpPattern pattern = PsiTreeUtil.getChildOfType(f, RegExpPattern.class);
        assert pattern != null;

        final RegExpAtom element = pattern.getBranches()[0].getAtoms()[0];

        if (isInsideStringLiteral(e)) {
            final Document doc = editor.getDocument();
            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(doc);
            final TextRange tr = e.getTextRange();
            doc.replaceString(tr.getStartOffset(), tr.getEndOffset(),
                StringUtil.escapeStringCharacters(element.getText())
            );

            return TextRange.from(e.getTextRange().getEndOffset(), 0);
        }
        else {
            final PsiElement n = e.replace(element);
            return TextRange.from(n.getTextRange().getEndOffset(), 0);
        }
    }

    @RequiredReadAction
    private static boolean isInsideStringLiteral(PsiElement context) {
        while (context != null) {
            if (RegExpElementImpl.isLiteralExpression(context)) {
                return true;
            }
            context = context.getContext();
        }
        return false;
    }

    private String makeReplacement(StringBuilder s) {
        return myGroupStart + s + ")";
    }
}
