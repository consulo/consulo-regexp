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

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.application.Application;
import consulo.component.extension.ByClassGrouper;
import consulo.component.extension.ExtensionPointCacheKey;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import org.intellij.lang.regexp.psi.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * @author yole
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public interface RegExpLanguageHost
{
	ExtensionPointCacheKey<RegExpLanguageHost, Function<Class, RegExpLanguageHost>> KEY =
			ExtensionPointCacheKey.create("RegExpLanguageHost", ByClassGrouper.build(RegExpLanguageHost::getHostClass));

	@Nullable
	@SuppressWarnings("unchecked")
	public static RegExpLanguageHost findRegExpHost(@Nullable PsiElement element)
	{
		if(element == null)
		{
			return null;
		}

		final PsiFile file = element.getContainingFile();
		final PsiElement context = file.getContext();
		if(context instanceof RegExpLanguageHost)
		{
			return (RegExpLanguageHost) context;
		}

		if(context != null)
		{
			Function<Class, RegExpLanguageHost> call = Application.get().getExtensionPoint(RegExpLanguageHost.class).getOrBuildCache(KEY);
			return call.apply(context.getClass());
		}
		return null;
	}

	@Nonnull
	Class getHostClass();

	boolean characterNeedsEscaping(char c);

	boolean supportsPerl5EmbeddedComments();

	boolean supportsPossessiveQuantifiers();

	boolean supportsPythonConditionalRefs();

	boolean supportsNamedGroupSyntax(RegExpGroup group);

	boolean supportsNamedGroupRefSyntax(RegExpNamedGroupRef ref);

	boolean supportsExtendedHexCharacter(RegExpChar regExpChar);

	default boolean isValidGroupName(String name, @Nonnull PsiElement context)
	{
		for(int i = 0, length = name.length(); i < length; i++)
		{
			final char c = name.charAt(i);
			if(!AsciiUtil.isLetterOrDigit(c) && c != '_')
			{
				return false;
			}
		}
		return true;
	}

	default boolean supportsSimpleClass(RegExpSimpleClass simpleClass)
	{
		return true;
	}

	default boolean supportsNamedCharacters(RegExpNamedCharacter namedCharacter)
	{
		return false;
	}

	default boolean isValidNamedCharacter(RegExpNamedCharacter namedCharacter)
	{
		return supportsNamedCharacters(namedCharacter);
	}

	default boolean supportsBoundary(RegExpBoundary boundary)
	{
		switch(boundary.getType())
		{
			case UNICODE_EXTENDED_GRAPHEME:
				return false;
			case LINE_START:
			case LINE_END:
			case WORD:
			case NON_WORD:
			case BEGIN:
			case END:
			case END_NO_LINE_TERM:
			case PREVIOUS_MATCH:
			default:
				return true;
		}
	}

	default boolean supportsLiteralBackspace(RegExpChar aChar)
	{
		return true;
	}

	default boolean supportsInlineOptionFlag(char flag, PsiElement context)
	{
		return true;
	}

	boolean isValidCategory(@Nonnull String category);

	@Nonnull
	String[][] getAllKnownProperties();

	@Nullable
	String getPropertyDescription(@Nullable final String name);

	@Nonnull
	String[][] getKnownCharacterClasses();
}
