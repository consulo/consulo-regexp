/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.AllIcons;
import consulo.application.progress.ProgressManager;
import consulo.codeEditor.Editor;
import consulo.component.util.UnicodeCharacterRegistry;
import consulo.document.Document;
import consulo.document.util.TextRange;
import consulo.language.Language;
import consulo.language.editor.completion.*;
import consulo.language.editor.completion.lookup.*;
import consulo.language.pattern.ElementPattern;
import consulo.language.pattern.PsiElementPattern;
import consulo.language.psi.PsiElement;
import consulo.language.util.ProcessingContext;
import consulo.ui.image.Image;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;

import static consulo.language.pattern.PlatformPatterns.psiElement;
import static consulo.language.pattern.StandardPatterns.or;

/**
 * @author vnikolaenko
 */
@ExtensionImpl
public final class RegExpCompletionContributor extends CompletionContributor {
    private static final Image emptyIcon = Image.empty(Image.DEFAULT_ICON_SIZE);

    public RegExpCompletionContributor() {
        {
            final PsiElementPattern.Capture<PsiElement> namedCharacterPattern = psiElement().withText("\\N");
            extend(CompletionType.BASIC, psiElement().afterLeaf(namedCharacterPattern), new NamedCharacterCompletionProvider(true));
            extend(
                CompletionType.BASIC,
                psiElement().afterLeaf(psiElement(RegExpTT.LBRACE).afterLeaf(namedCharacterPattern)),
                new NamedCharacterCompletionProvider(false)
            );

            extend(CompletionType.BASIC, psiElement().withText("\\I"), new CharacterClassesNameCompletionProvider());

            final ElementPattern<PsiElement> propertyPattern = psiElement().withText("\\p");
            extend(CompletionType.BASIC, psiElement().afterLeaf(propertyPattern), new PropertyCompletionProvider());

            final ElementPattern<PsiElement> propertyNamePattern =
                psiElement().afterLeaf(psiElement().withText("{").afterLeaf(propertyPattern));
            extend(CompletionType.BASIC, propertyNamePattern, new PropertyNameCompletionProvider());

            final ElementPattern<PsiElement> bracketExpressionPattern = psiElement().afterLeaf(or(
                psiElement(RegExpTT.BRACKET_EXPRESSION_BEGIN),
                psiElement(RegExpTT.CARET).afterLeaf(psiElement(RegExpTT.BRACKET_EXPRESSION_BEGIN))
            ));
            extend(CompletionType.BASIC, bracketExpressionPattern, new BracketExpressionCompletionProvider());
        }

        {
            // TODO: backSlashPattern is needed for reg exp in injected context, remove when unescaping will be performed by Injecting framework
            final PsiElementPattern.Capture<PsiElement> namedCharacterPattern = psiElement().withText("\\\\N");
            extend(CompletionType.BASIC, psiElement().afterLeaf(namedCharacterPattern), new NamedCharacterCompletionProvider(true));
            extend(
                CompletionType.BASIC,
                psiElement().afterLeaf(psiElement(RegExpTT.LBRACE).afterLeaf(namedCharacterPattern)),
                new NamedCharacterCompletionProvider(false)
            );

            final ElementPattern<PsiElement> backSlashPattern = psiElement().withText("\\\\I");
            extend(CompletionType.BASIC, backSlashPattern, new CharacterClassesNameCompletionProvider());

            final ElementPattern<PsiElement> propertyPattern = psiElement().withText("\\\\p");
            extend(CompletionType.BASIC, psiElement().afterLeaf(propertyPattern), new PropertyCompletionProvider());

            final ElementPattern<PsiElement> propertyNamePattern =
                psiElement().afterLeaf(psiElement().withText("{").afterLeaf(propertyPattern));
            extend(CompletionType.BASIC, propertyNamePattern, new PropertyNameCompletionProvider());
        }

        {
            // TODO: this seems to be needed only for tests!
            final ElementPattern<PsiElement> backSlashPattern = psiElement().withText("\\\\");
            extend(CompletionType.BASIC, psiElement().afterLeaf(backSlashPattern), new CharacterClassesNameCompletionProvider());

            final ElementPattern<PsiElement> propertyPattern = psiElement().withText("p").afterLeaf(backSlashPattern);
            extend(CompletionType.BASIC, psiElement().afterLeaf(propertyPattern), new PropertyCompletionProvider());

            final ElementPattern<PsiElement> propertyNamePattern =
                psiElement().afterLeaf(psiElement().withText("{").afterLeaf(propertyPattern));
            extend(CompletionType.BASIC, propertyNamePattern, new PropertyNameCompletionProvider());

            final PsiElementPattern.Capture<PsiElement> namedCharacterPattern = psiElement().withText("N");
            extend(CompletionType.BASIC, psiElement().afterLeaf(namedCharacterPattern), new NamedCharacterCompletionProvider(true));
            extend(
                CompletionType.BASIC,
                psiElement().afterLeaf(psiElement(RegExpTT.LBRACE).afterLeaf(namedCharacterPattern)),
                new NamedCharacterCompletionProvider(false)
            );
        }
    }

    private static void addLookupElement(final CompletionResultSet result, @NonNls final String name, String type, Image icon) {
        result.addElement(createLookupElement(name, type, icon));
    }

    private static LookupElement createLookupElement(String name, String type, Image icon) {
        return LookupElementBuilder.create(name).withTypeText(type).withIcon(icon);
    }

    private static class BracketExpressionCompletionProvider implements CompletionProvider {
        @RequiredReadAction
        @Override
        public void addCompletions(
            @Nonnull CompletionParameters parameters,
            ProcessingContext context,
            @Nonnull CompletionResultSet result
        ) {

            for (String[] completion : RegExpLanguageHosts.INSTANCE.getPosixCharacterClasses(parameters.getPosition())) {
                result.addElement(
                    LookupElementBuilder.create(completion[0])
                        .withTypeText((completion.length > 1) ? completion[1] : null)
                        .withIcon(emptyIcon)
                        .withInsertHandler((context1, item) -> {
                            context1.setAddCompletionChar(false);
                            final Editor editor = context1.getEditor();
                            final Document document = editor.getDocument();
                            final int tailOffset = context1.getTailOffset();
                            if (document.getTextLength() < tailOffset + 2 || !document.getText(new TextRange(
                                tailOffset,
                                tailOffset + 2
                            )).equals(":]")) {
                                document.insertString(tailOffset, ":]");
                            }
                            editor.getCaretModel().moveCaretRelatively(2, 0, false, false, true);
                        })
                );
            }
        }
    }

    private static class PropertyNameCompletionProvider implements CompletionProvider {
        @RequiredReadAction
        @Override
        public void addCompletions(
            @Nonnull final CompletionParameters parameters,
            final ProcessingContext context,
            @Nonnull final CompletionResultSet result
        ) {
            for (String[] stringArray : RegExpLanguageHosts.INSTANCE.getAllKnownProperties(parameters.getPosition())) {
                result.addElement(TailTypeDecorator.withTail(
                    createLookupElement(stringArray[0], null, emptyIcon),
                    TailType.createSimpleTailType('}')
                ));
            }
        }
    }

    private static class PropertyCompletionProvider implements CompletionProvider {
        @RequiredReadAction
        @Override
        public void addCompletions(
            @Nonnull final CompletionParameters parameters,
            final ProcessingContext context,
            @Nonnull final CompletionResultSet result
        ) {
            for (String[] stringArray : RegExpLanguageHosts.INSTANCE.getAllKnownProperties(parameters.getPosition())) {
                addLookupElement(
                    result,
                    "{" + stringArray[0] + "}",
                    stringArray.length > 1 ? stringArray[1] : null,
                    AllIcons.Nodes.Property
                );
            }
        }
    }

    private static class CharacterClassesNameCompletionProvider implements CompletionProvider {
        @RequiredReadAction
        @Override
        public void addCompletions(
            @Nonnull final CompletionParameters parameters,
            final ProcessingContext context,
            @Nonnull final CompletionResultSet result
        ) {
            for (final String[] completion : RegExpLanguageHosts.INSTANCE.getKnownCharacterClasses(parameters.getPosition())) {
                addLookupElement(result, completion[0], completion[1], emptyIcon);
            }

            for (String[] stringArray : RegExpLanguageHosts.INSTANCE.getAllKnownProperties(parameters.getPosition())) {
                addLookupElement(
                    result,
                    "p{" + stringArray[0] + "}",
                    stringArray.length > 1 ? stringArray[1] : null,
                    AllIcons.Nodes.Property
                );
            }
        }
    }

    private static class NamedCharacterCompletionProvider implements CompletionProvider {
        private final boolean myEmbrace;

        public NamedCharacterCompletionProvider(boolean embrace) {
            myEmbrace = embrace;
        }

        @RequiredReadAction
        @Override
        public void addCompletions(
            @Nonnull CompletionParameters parameters,
            ProcessingContext context,
            @Nonnull CompletionResultSet result
        ) {
            UnicodeCharacterRegistry.listCharacters().forEach(character -> {
                String name = character.getName();
                if (result.getPrefixMatcher().prefixMatches(name)) {
                    final String type = new String(new int[]{character.getCodePoint()}, 0, 1);
                    if (myEmbrace) {
                        result.addElement(createLookupElement("{" + character + "}", type, emptyIcon));
                    }
                    else {
                        result.addElement(TailTypeDecorator.withTail(
                            createLookupElement(name, type, emptyIcon),
                            TailType.createSimpleTailType('}')
                        ));
                    }
                }
                ProgressManager.checkCanceled();
            });
        }
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return RegExpLanguage.INSTANCE;
    }
}
