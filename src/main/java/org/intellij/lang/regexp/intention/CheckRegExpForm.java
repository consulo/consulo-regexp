/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
import consulo.application.ui.wm.IdeFocusManager;
import consulo.disposer.Disposable;
import consulo.disposer.Disposer;
import consulo.document.Document;
import consulo.document.event.DocumentAdapter;
import consulo.document.event.DocumentEvent;
import consulo.language.editor.ui.awt.EditorTextField;
import consulo.language.inject.InjectedLanguageManagerUtil;
import consulo.language.plain.PlainTextFileType;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiLanguageInjectionHost;
import consulo.project.Project;
import consulo.project.ProjectPropertiesComponent;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.ui.ex.action.CustomShortcutSet;
import consulo.ui.ex.awt.*;
import consulo.ui.ex.awt.util.Alarm;
import org.intellij.lang.regexp.RegExpLanguage;
import org.intellij.lang.regexp.RegExpModifierProvider;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.util.regex.Pattern;

/**
 * @author Konstantin Bulenkov
 */
public class CheckRegExpForm {
    private static final String LAST_EDITED_REGEXP = "last.edited.regexp";
    private final PsiFile myRegexpFile;

    private final EditorTextField mySampleText;
    private final EditorTextField myRegExp;

    private final JPanel myRootPanel;
    private final JBLabel myMessage;
    private final Project myProject;

    public CheckRegExpForm(PsiFile file) {
        myRegexpFile = file;

        myProject = myRegexpFile.getProject();

        Document document = PsiDocumentManager.getInstance(myProject).getDocument(myRegexpFile);

        myRegExp = new EditorTextField(document, myProject, RegExpLanguage.INSTANCE.getAssociatedFileType());
        myRegExp.setPreferredWidth(Math.max(300, myRegExp.getPreferredSize().width));
        final String sampleText = ProjectPropertiesComponent.getInstance(myProject).getValue(LAST_EDITED_REGEXP, "Sample Text");
        mySampleText = new EditorTextField(sampleText, myProject, PlainTextFileType.INSTANCE);

        myRootPanel = new JPanel(new VerticalFlowLayout()) {
            Disposable disposable;

            @Override
            @RequiredReadAction
            public void addNotify() {
                super.addNotify();
                disposable = Disposable.newDisposable();

                IdeFocusManager.getGlobalInstance().requestFocus(mySampleText, true);

                new AnAction() {
                    @RequiredUIAccess
                    @Override
                    public void actionPerformed(@Nonnull AnActionEvent e) {
                        IdeFocusManager.findInstance().requestFocus(myRegExp.getFocusTarget(), true);
                    }
                }.registerCustomShortcutSet(CustomShortcutSet.fromString("shift TAB"), mySampleText);

                final Alarm updater = new Alarm(Alarm.ThreadToUse.SWING_THREAD, disposable);
                DocumentAdapter documentListener = new DocumentAdapter() {
                    @Override
                    public void documentChanged(DocumentEvent e) {
                        updater.cancelAllRequests();
                        if (!updater.isDisposed()) {
                            updater.addRequest(CheckRegExpForm.this::updateBalloon, 200);
                        }
                    }
                };
                myRegExp.addDocumentListener(documentListener);
                mySampleText.addDocumentListener(documentListener);

                updateBalloon();
                mySampleText.selectAll();
            }

            @Override
            public void removeNotify() {
                super.removeNotify();
                Disposer.dispose(disposable);
                ProjectPropertiesComponent.getInstance(myProject).setValue(LAST_EDITED_REGEXP, mySampleText.getText());
            }
        };
        myRootPanel.setOpaque(false);

        myMessage = new JBLabel();

        mySampleText.setOneLineMode(true);

        LabeledComponent<EditorTextField> regExpLabeled = LabeledComponent.create(myRegExp, "RegExp");
        regExpLabeled.setOpaque(false);
        myRootPanel.add(regExpLabeled);
        LabeledComponent<EditorTextField> sampleLabeled = LabeledComponent.create(mySampleText, "Sample");
        sampleLabeled.setOpaque(false);
        myRootPanel.add(sampleLabeled);
        BorderLayoutPanel borderLayoutPanel = new BorderLayoutPanel();
        borderLayoutPanel.setOpaque(false);

        myRootPanel.add(borderLayoutPanel.addToRight(myMessage));
    }

    public JPanel getRootPanel() {
        return myRootPanel;
    }

    @RequiredReadAction
    private void updateBalloon() {
        boolean correct = isMatchingText(myRegexpFile, mySampleText.getText());

        mySampleText.setBackground(correct ? LightColors.GREEN : LightColors.RED);
        myMessage.setText(correct ? "Matches!" : "no match");
        myRootPanel.revalidate();
    }

    @TestOnly
    @RequiredReadAction
    public static boolean isMatchingTextTest(@Nonnull PsiFile regexpFile, @Nonnull String sampleText) {
        return isMatchingText(regexpFile, sampleText);
    }

    @RequiredReadAction
    private static boolean isMatchingText(@Nonnull PsiFile regexpFile, @Nonnull String sampleText) {
        final String regExp = regexpFile.getText();

        PsiLanguageInjectionHost host = InjectedLanguageManagerUtil.findInjectionHost(regexpFile);
        int flags = 0;
        if (host != null) {
            for (RegExpModifierProvider provider : RegExpModifierProvider.forLanguage(host.getLanguage())) {
                flags = provider.getFlags(host, regexpFile);
                if (flags > 0) {
                    break;
                }
            }
        }
        try {
            return Pattern.compile(regExp, flags).matcher(sampleText).matches();
        }
        catch (Exception ignore) {
        }

        return false;
    }
}
