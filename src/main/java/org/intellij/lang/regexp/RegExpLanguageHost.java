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

import javax.annotation.Nonnull;

import org.intellij.lang.regexp.psi.RegExpBoundary;
import org.intellij.lang.regexp.psi.RegExpChar;
import org.intellij.lang.regexp.psi.RegExpGroup;
import org.intellij.lang.regexp.psi.RegExpNamedCharacter;
import org.intellij.lang.regexp.psi.RegExpNamedGroupRef;
import org.intellij.lang.regexp.psi.RegExpSimpleClass;

import javax.annotation.Nullable;
import com.intellij.psi.PsiElement;

/**
 * @author yole
 */
public interface RegExpLanguageHost
{
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
