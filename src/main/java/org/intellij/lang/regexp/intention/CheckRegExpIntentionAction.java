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

import javax.annotation.Nonnull;
import javax.swing.Icon;
import javax.swing.JComponent;

import org.intellij.lang.regexp.RegExpLanguage;
import com.intellij.codeInsight.intention.impl.QuickEditAction;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import consulo.awt.TargetAWT;
import consulo.ui.image.Image;

/**
 * @author Konstantin Bulenkov
 * @author Anna Bulenkova
 */
public class CheckRegExpIntentionAction extends QuickEditAction implements Iconable {

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
  protected boolean isShowInBalloon() {
    return true;
  }

  @Override
  protected JComponent createBalloonComponent(PsiFile file) {
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

  @Nonnull
  @Override
  public String getFamilyName() {
    return getText();
  }

  @Override
  public Image getIcon(int flags) {
    return RegExpLanguage.INSTANCE.getAssociatedFileType().getIcon();
  }
}
