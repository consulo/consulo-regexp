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


import consulo.codeEditor.DefaultLanguageHighlighterColors;
import consulo.codeEditor.HighlighterColors;
import consulo.colorScheme.TextAttributesKey;
import consulo.language.ast.IElementType;
import consulo.language.ast.StringEscapesTokenTypes;
import consulo.language.editor.highlight.SyntaxHighlighterBase;
import consulo.language.lexer.Lexer;
import consulo.language.parser.ParserDefinition;
import consulo.language.version.LanguageVersionUtil;
import jakarta.annotation.Nonnull;

import java.util.HashMap;
import java.util.Map;

public class RegExpHighlighter extends SyntaxHighlighterBase {
    private static final Map<IElementType, TextAttributesKey> ourMap = new HashMap<>();

    static final TextAttributesKey CHARACTER = TextAttributesKey.of("REGEXP.CHARACTER", DefaultLanguageHighlighterColors.STRING);
    static final TextAttributesKey DOT = TextAttributesKey.of("REGEXP.DOT", DefaultLanguageHighlighterColors.DOT);
    public static final TextAttributesKey META = TextAttributesKey.of("REGEXP.META", DefaultLanguageHighlighterColors.KEYWORD);
    static final TextAttributesKey INVALID_CHARACTER_ESCAPE = TextAttributesKey.of("REGEXP.INVALID_STRING_ESCAPE", DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE);
    static final TextAttributesKey BAD_CHARACTER = TextAttributesKey.of("REGEXP.BAD_CHARACTER", HighlighterColors.BAD_CHARACTER);
    static final TextAttributesKey REDUNDANT_ESCAPE = TextAttributesKey.of("REGEXP.REDUNDANT_ESCAPE", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE);
    static final TextAttributesKey PARENTHS = TextAttributesKey.of("REGEXP.PARENTHS", DefaultLanguageHighlighterColors.PARENTHESES);
    static final TextAttributesKey BRACES = TextAttributesKey.of("REGEXP.BRACES", DefaultLanguageHighlighterColors.BRACES);
    static final TextAttributesKey BRACKETS = TextAttributesKey.of("REGEXP.BRACKETS", DefaultLanguageHighlighterColors.BRACKETS);
    static final TextAttributesKey COMMA = TextAttributesKey.of("REGEXP.COMMA", DefaultLanguageHighlighterColors.COMMA);
    public static final TextAttributesKey ESC_CHARACTER = TextAttributesKey.of("REGEXP.ESC_CHARACTER", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE);
    static final TextAttributesKey CHAR_CLASS = TextAttributesKey.of("REGEXP.CHAR_CLASS", DefaultLanguageHighlighterColors.MARKUP_ENTITY);
    static final TextAttributesKey QUOTE_CHARACTER = TextAttributesKey.of("REGEXP.QUOTE_CHARACTER", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE);
    static final TextAttributesKey COMMENT = TextAttributesKey.of("REGEXP.COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT);
    static final TextAttributesKey QUANTIFIER = TextAttributesKey.of("REGEXP.QUANTIFIER", DefaultLanguageHighlighterColors.NUMBER);
    static final TextAttributesKey OPTIONS = TextAttributesKey.of("REGEXP.OPTIONS", DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL);
    static final TextAttributesKey NAME = TextAttributesKey.of("REGEXP.NAME", DefaultLanguageHighlighterColors.IDENTIFIER);
    public static final TextAttributesKey MATCHED_GROUPS = TextAttributesKey.of("REGEXP_MATCHED_GROUPS");

    private final ParserDefinition myParserDefinition;

    public RegExpHighlighter(ParserDefinition parserDefinition) {
        myParserDefinition = parserDefinition;
    }

    static {
        ourMap.put(RegExpTT.CHARACTER, CHARACTER);
        ourMap.put(RegExpTT.COLON, CHARACTER);
        ourMap.put(RegExpTT.MINUS, CHARACTER);
        ourMap.put(RegExpTT.DOT, DOT);

        ourMap.put(RegExpTT.NAME, NAME);
        ourMap.put(RegExpTT.BACKREF, NAME);

        ourMap.put(RegExpTT.UNION, META);
        ourMap.put(RegExpTT.CARET, META);
        ourMap.put(RegExpTT.DOLLAR, META);
        ourMap.put(RegExpTT.ANDAND, META);

        ourMap.put(StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN, INVALID_CHARACTER_ESCAPE);
        ourMap.put(StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN, INVALID_CHARACTER_ESCAPE);

        ourMap.put(RegExpTT.BAD_CHARACTER, BAD_CHARACTER);
        ourMap.put(RegExpTT.BAD_HEX_VALUE, INVALID_CHARACTER_ESCAPE);
        ourMap.put(RegExpTT.BAD_OCT_VALUE, INVALID_CHARACTER_ESCAPE);

        ourMap.put(RegExpTT.ESC_CHARACTER, ESC_CHARACTER);
        ourMap.put(RegExpTT.UNICODE_CHAR, ESC_CHARACTER);
        ourMap.put(RegExpTT.HEX_CHAR, ESC_CHARACTER);
        ourMap.put(RegExpTT.OCT_CHAR, ESC_CHARACTER);
        ourMap.put(RegExpTT.CTRL_CHARACTER, ESC_CHARACTER);

        ourMap.put(RegExpTT.PROPERTY, CHAR_CLASS);
        ourMap.put(RegExpTT.CHAR_CLASS, CHAR_CLASS);
        ourMap.put(RegExpTT.BOUNDARY, CHAR_CLASS);
        ourMap.put(RegExpTT.CTRL, CHAR_CLASS);
        ourMap.put(RegExpTT.ESC_CTRL_CHARACTER, CHAR_CLASS);
        ourMap.put(RegExpTT.NAMED_CHARACTER, CHAR_CLASS);
        ourMap.put(RegExpTT.CATEGORY_SHORT_HAND, CHAR_CLASS);
        ourMap.put(RegExpTT.RUBY_NAMED_GROUP_REF, CHAR_CLASS);
        ourMap.put(RegExpTT.RUBY_NAMED_GROUP_CALL, CHAR_CLASS);
        ourMap.put(RegExpTT.RUBY_QUOTED_NAMED_GROUP_REF, CHAR_CLASS);
        ourMap.put(RegExpTT.RUBY_QUOTED_NAMED_GROUP_CALL, CHAR_CLASS);

        ourMap.put(RegExpTT.REDUNDANT_ESCAPE, REDUNDANT_ESCAPE);

        ourMap.put(RegExpTT.QUOTE_BEGIN, QUOTE_CHARACTER);
        ourMap.put(RegExpTT.QUOTE_END, QUOTE_CHARACTER);

        ourMap.put(RegExpTT.NON_CAPT_GROUP, PARENTHS);
        ourMap.put(RegExpTT.ATOMIC_GROUP, PARENTHS);
        ourMap.put(RegExpTT.POS_LOOKBEHIND, PARENTHS);
        ourMap.put(RegExpTT.NEG_LOOKBEHIND, PARENTHS);
        ourMap.put(RegExpTT.POS_LOOKAHEAD, PARENTHS);
        ourMap.put(RegExpTT.PCRE_BRANCH_RESET, PARENTHS);
        ourMap.put(RegExpTT.NEG_LOOKAHEAD, PARENTHS);
        ourMap.put(RegExpTT.SET_OPTIONS, PARENTHS);
        ourMap.put(RegExpTT.PYTHON_NAMED_GROUP, PARENTHS);
        ourMap.put(RegExpTT.PYTHON_NAMED_GROUP_REF, PARENTHS);
        ourMap.put(RegExpTT.PCRE_RECURSIVE_NAMED_GROUP_REF, PARENTHS);
        ourMap.put(RegExpTT.CONDITIONAL, PARENTHS);
        ourMap.put(RegExpTT.RUBY_NAMED_GROUP, PARENTHS);
        ourMap.put(RegExpTT.RUBY_QUOTED_NAMED_GROUP, PARENTHS);
        ourMap.put(RegExpTT.GROUP_BEGIN, PARENTHS);
        ourMap.put(RegExpTT.GROUP_END, PARENTHS);
        ourMap.put(RegExpTT.GT, PARENTHS);
        ourMap.put(RegExpTT.QUOTE, PARENTHS);

        ourMap.put(RegExpTT.LBRACE, BRACES);
        ourMap.put(RegExpTT.RBRACE, BRACES);

        ourMap.put(RegExpTT.CLASS_BEGIN, BRACKETS);
        ourMap.put(RegExpTT.CLASS_END, BRACKETS);
        ourMap.put(RegExpTT.BRACKET_EXPRESSION_BEGIN, BRACKETS);
        ourMap.put(RegExpTT.BRACKET_EXPRESSION_END, BRACKETS);

        ourMap.put(RegExpTT.COMMA, COMMA);

        ourMap.put(RegExpTT.NUMBER, QUANTIFIER);
        ourMap.put(RegExpTT.STAR, QUANTIFIER);
        ourMap.put(RegExpTT.PLUS, QUANTIFIER);
        ourMap.put(RegExpTT.QUEST, QUANTIFIER);

        ourMap.put(RegExpTT.COMMENT, COMMENT);

        ourMap.put(RegExpTT.OPTIONS_ON, OPTIONS);
        ourMap.put(RegExpTT.OPTIONS_OFF, OPTIONS);
    }

    @Override
    public @Nonnull Lexer getHighlightingLexer() {
        return myParserDefinition.createLexer(LanguageVersionUtil.findDefaultVersion(RegExpLanguage.INSTANCE));
    }

    @Override
    @Nonnull
    public TextAttributesKey[] getTokenHighlights(IElementType tokenType) {
        return pack(ourMap.get(tokenType));
    }
}
