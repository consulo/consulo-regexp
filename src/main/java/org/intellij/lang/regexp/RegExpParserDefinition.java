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
package org.intellij.lang.regexp;

import java.util.EnumSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.intellij.lang.regexp.psi.impl.*;
import org.jetbrains.annotations.TestOnly;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import consulo.disposer.Disposable;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import consulo.disposer.Disposer;
import consulo.lang.LanguageVersion;

public class RegExpParserDefinition implements ParserDefinition
{
	private static final TokenSet COMMENT_TOKENS = TokenSet.create(RegExpTT.COMMENT);
	private static final EnumSet<RegExpCapability> CAPABILITIES = EnumSet.of(RegExpCapability.NESTED_CHARACTER_CLASSES, RegExpCapability.ALLOW_HORIZONTAL_WHITESPACE_CLASS, RegExpCapability
			.UNICODE_CATEGORY_SHORTHAND);

	@TestOnly
	public static void setTestCapability(@Nullable RegExpCapability capability, @Nonnull Disposable parentDisposable)
	{
		if(!CAPABILITIES.contains(capability))
		{
			CAPABILITIES.add(capability);
			Disposer.register(parentDisposable, () -> CAPABILITIES.remove(capability));
		}
	}

	@Nonnull
	public Lexer createLexer(LanguageVersion languageVersion)
	{
		return new RegExpLexer(CAPABILITIES);
	}

	public PsiParser createParser(LanguageVersion languageVersion)
	{
		return new RegExpParser(CAPABILITIES);
	}

	public IFileElementType getFileNodeType()
	{
		return RegExpElementTypes.REGEXP_FILE;
	}

	@Nonnull
	public TokenSet getWhitespaceTokens(LanguageVersion languageVersion)
	{
		// trick to hide quote tokens from parser... should actually go into the lexer
		return TokenSet.create(RegExpTT.QUOTE_BEGIN, RegExpTT.QUOTE_END, TokenType.WHITE_SPACE);
	}

	@Nonnull
	public TokenSet getStringLiteralElements(LanguageVersion languageVersion)
	{
		return TokenSet.EMPTY;
	}

	@Nonnull
	public TokenSet getCommentTokens(LanguageVersion languageVersion)
	{
		return COMMENT_TOKENS;
	}

	@Nonnull
	public PsiElement createElement(ASTNode node)
	{
		final IElementType type = node.getElementType();
		if(type == RegExpElementTypes.PATTERN)
		{
			return new RegExpPatternImpl(node);
		}
		else if(type == RegExpElementTypes.BRANCH)
		{
			return new RegExpBranchImpl(node);
		}
		else if(type == RegExpElementTypes.SIMPLE_CLASS)
		{
			return new RegExpSimpleClassImpl(node);
		}
		else if(type == RegExpElementTypes.CLASS)
		{
			return new RegExpClassImpl(node);
		}
		else if(type == RegExpElementTypes.CHAR_RANGE)
		{
			return new RegExpCharRangeImpl(node);
		}
		else if(type == RegExpElementTypes.CHAR)
		{
			return new RegExpCharImpl(node);
		}
		else if(type == RegExpElementTypes.GROUP)
		{
			return new RegExpGroupImpl(node);
		}
		else if(type == RegExpElementTypes.PROPERTY)
		{
			return new RegExpPropertyImpl(node);
		}
		else if(type == RegExpElementTypes.NAMED_CHARACTER_ELEMENT)
		{
			return new RegExpNamedCharacterImpl(node);
		}
		else if(type == RegExpElementTypes.SET_OPTIONS)
		{
			return new RegExpSetOptionsImpl(node);
		}
		else if(type == RegExpElementTypes.OPTIONS)
		{
			return new RegExpOptionsImpl(node);
		}
		else if(type == RegExpElementTypes.BACKREF)
		{
			return new RegExpBackrefImpl(node);
		}
		else if(type == RegExpElementTypes.CLOSURE)
		{
			return new RegExpClosureImpl(node);
		}
		else if(type == RegExpElementTypes.QUANTIFIER)
		{
			return new RegExpQuantifierImpl(node);
		}
		else if(type == RegExpElementTypes.BOUNDARY)
		{
			return new RegExpBoundaryImpl(node);
		}
		else if(type == RegExpElementTypes.INTERSECTION)
		{
			return new RegExpIntersectionImpl(node);
		}
		else if(type == RegExpElementTypes.UNION)
		{
			return new RegExpUnionImpl(node);
		}
		else if(type == RegExpElementTypes.NAMED_GROUP_REF)
		{
			return new RegExpNamedGroupRefImpl(node);
		}
		else if(type == RegExpElementTypes.PY_COND_REF)
		{
			return new RegExpPyCondRefImpl(node);
		}
		else if(type == RegExpElementTypes.POSIX_BRACKET_EXPRESSION)
		{
			return new RegExpPosixBracketExpressionImpl(node);
		}

		return new ASTWrapperPsiElement(node);
	}

	public PsiFile createFile(FileViewProvider viewProvider)
	{
		return new RegExpFile(viewProvider, RegExpLanguage.INSTANCE);
	}

	public SpaceRequirements spaceExistanceTypeBetweenTokens(ASTNode left, ASTNode right)
	{
		return SpaceRequirements.MUST_NOT;
	}
}
