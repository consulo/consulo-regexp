/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
package org.intellij.lang.regexp.intention;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.component.util.Iconable;
import consulo.document.Document;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.editor.impl.intention.QuickEditAction;
import consulo.language.editor.intention.IntentionMetaData;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.regexp.icon.RegExpIconGroup;
import consulo.ui.image.Image;
import consulo.util.lang.Pair;
import org.intellij.lang.regexp.RegExpLanguage;

import jakarta.annotation.Nonnull;
import javax.swing.*;

/**
 * @author Konstantin Bulenkov
 * @author Anna Bulenkova
 */
@ExtensionImpl
@IntentionMetaData(ignoreId = "regexp.CheckRegExpIntentionAction", categories = "RegExp", fileExtensions = "regexp")
public class CheckRegExpIntentionAction extends QuickEditAction implements Iconable {
    @RequiredReadAction
    @Override
    public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file) {
        final Pair<PsiElement, TextRange> pair = getRangePair(file, editor);
        /*super.isAvailable(project, editor, file) && */
        if (pair != null && pair.first != null) {
            Language language = pair.first.getLanguage();
            Language baseLanguage = language.getBaseLanguage();
            return language == RegExpLanguage.INSTANCE || baseLanguage == RegExpLanguage.INSTANCE;
        }
        return false;
    }

    @Override
    public boolean isShowInBalloon() {
        return true;
    }

    @Override
    public JComponent createBalloonComponent(PsiFile file) {
        final Project project = file.getProject();
        final Document document = PsiDocumentManager.getInstance(project).getDocument(file);
        if (document != null) {
            return new CheckRegExpForm(file).getRootPanel();
        }
        return null;
    }

    @Nonnull
    @Override
    public String getText() {
        return "Check RegExp";
    }

    @Override
    public Image getIcon(int flags) {
        return RegExpIconGroup.regexp();
    }
}
