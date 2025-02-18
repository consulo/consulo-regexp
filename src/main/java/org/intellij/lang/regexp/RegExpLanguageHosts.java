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

import consulo.language.psi.PsiComment;
import consulo.language.psi.PsiElement;
import org.intellij.lang.regexp.psi.*;
import org.jetbrains.annotations.Contract;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author yole
 */
public final class RegExpLanguageHosts {
    public static final RegExpLanguageHosts INSTANCE = new RegExpLanguageHosts();

    private final DefaultRegExpPropertiesProvider myDefaultProvider = DefaultRegExpPropertiesProvider.getInstance();

    @Contract("null -> null")
    @Nullable
    private static RegExpLanguageHost findRegExpHost(@Nullable final PsiElement element) {
        return RegExpLanguageHost.findRegExpHost(element);
    }

    public boolean isRedundantEscape(@Nonnull final RegExpChar ch, @Nonnull final String text) {
        if (text.length() <= 1) {
            return false;
        }
        final RegExpLanguageHost host = findRegExpHost(ch);
        if (host != null) {
            final char c = text.charAt(1);
            return !host.characterNeedsEscaping(c);
        }
        else {
            return !("\\]".equals(text) || "\\}".equals(text));
        }
    }

    public boolean supportsInlineOptionFlag(char flag, PsiElement context) {
        final RegExpLanguageHost host = findRegExpHost(context);
        return host == null || host.supportsInlineOptionFlag(flag, context);
    }

    public boolean supportsExtendedHexCharacter(@Nullable RegExpChar regExpChar) {
        final RegExpLanguageHost host = findRegExpHost(regExpChar);
        try {
            return host != null && host.supportsExtendedHexCharacter(regExpChar);
        }
        catch (AbstractMethodError e) {
            // supportsExtendedHexCharacter not present
            return false;
        }
    }

    public boolean supportsLiteralBackspace(@Nullable RegExpChar regExpChar) {
        final RegExpLanguageHost host = findRegExpHost(regExpChar);
        return host != null && host.supportsLiteralBackspace(regExpChar);
    }

    public boolean supportsNamedGroupSyntax(@Nullable final RegExpGroup group) {
        final RegExpLanguageHost host = findRegExpHost(group);
        return host != null && host.supportsNamedGroupSyntax(group);
    }

    public boolean supportsNamedGroupRefSyntax(@Nullable final RegExpNamedGroupRef ref) {
        final RegExpLanguageHost host = findRegExpHost(ref);
        try {
            return host != null && host.supportsNamedGroupRefSyntax(ref);
        }
        catch (AbstractMethodError e) {
            // supportsNamedGroupRefSyntax() not present
            return false;
        }
    }

    public boolean isValidGroupName(String name, @Nullable final PsiElement context) {
        final RegExpLanguageHost host = findRegExpHost(context);
        return host != null && host.isValidGroupName(name, context);
    }

    public boolean supportsPerl5EmbeddedComments(@Nullable final PsiComment comment) {
        final RegExpLanguageHost host = findRegExpHost(comment);
        return host != null && host.supportsPerl5EmbeddedComments();
    }

    public boolean supportsPythonConditionalRefs(@Nullable final RegExpPyCondRef condRef) {
        final RegExpLanguageHost host = findRegExpHost(condRef);
        return host != null && host.supportsPythonConditionalRefs();
    }

    public boolean supportsPossessiveQuantifiers(@Nullable final RegExpQuantifier quantifier) {
        final RegExpLanguageHost host = findRegExpHost(quantifier);
        return host == null || host.supportsPossessiveQuantifiers();
    }

    public boolean supportsBoundary(@Nullable final RegExpBoundary boundary) {
        final RegExpLanguageHost host = findRegExpHost(boundary);
        return host == null || host.supportsBoundary(boundary);
    }

    public boolean supportsSimpleClass(@Nullable final RegExpSimpleClass simpleClass) {
        final RegExpLanguageHost host = findRegExpHost(simpleClass);
        return host == null || host.supportsSimpleClass(simpleClass);
    }

    public boolean isValidCategory(@Nonnull final PsiElement element, @Nonnull String category) {
        final RegExpLanguageHost host = findRegExpHost(element);
        return host != null ? host.isValidCategory(category) : myDefaultProvider.isValidCategory(category);
    }

    public boolean supportsNamedCharacters(@Nonnull final RegExpNamedCharacter namedCharacter) {
        final RegExpLanguageHost host = findRegExpHost(namedCharacter);
        return host != null && host.supportsNamedCharacters(namedCharacter);
    }

    public boolean isValidNamedCharacter(@Nonnull final RegExpNamedCharacter namedCharacter) {
        final RegExpLanguageHost host = findRegExpHost(namedCharacter);
        return host != null && host.isValidNamedCharacter(namedCharacter);
    }

    @Nonnull
    public String[][] getAllKnownProperties(@Nonnull final PsiElement element) {
        final RegExpLanguageHost host = findRegExpHost(element);
        return host != null ? host.getAllKnownProperties() : myDefaultProvider.getAllKnownProperties();
    }

    @Nullable
    String getPropertyDescription(@Nonnull final PsiElement element, @Nullable final String name) {
        final RegExpLanguageHost host = findRegExpHost(element);
        return host != null ? host.getPropertyDescription(name) : myDefaultProvider.getPropertyDescription(name);
    }

    @Nonnull
    String[][] getKnownCharacterClasses(@Nonnull final PsiElement element) {
        final RegExpLanguageHost host = findRegExpHost(element);
        return host != null ? host.getKnownCharacterClasses() : myDefaultProvider.getKnownCharacterClasses();
    }

    String[][] getPosixCharacterClasses(@Nonnull final PsiElement element) {
        return myDefaultProvider.getPosixCharacterClasses();
    }
}
