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
package org.intellij.lang.regexp.psi.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.*;
import org.intellij.lang.regexp.RegExpTT;
import org.intellij.lang.regexp.psi.RegExpChar;
import org.intellij.lang.regexp.psi.RegExpElementVisitor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RegExpCharImpl extends RegExpElementImpl implements RegExpChar {
    private static final TokenSet OCT_CHARS = TokenSet.create(RegExpTT.OCT_CHAR, RegExpTT.BAD_OCT_VALUE);
    private static final TokenSet HEX_CHARS = TokenSet.create(RegExpTT.HEX_CHAR, RegExpTT.BAD_HEX_VALUE);
    private static final TokenSet UNICODE_CHARS = TokenSet.create(RegExpTT.HEX_CHAR, StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN);

    public RegExpCharImpl(ASTNode astNode) {
        super(astNode);
    }

    @Nonnull
    public Type getType() {
        final ASTNode child = getNode().getFirstChildNode();
        assert child != null;
        final IElementType t = child.getElementType();
        if (OCT_CHARS.contains(t)) {
            return Type.OCT;
        }
        else if (HEX_CHARS.contains(t)) {
            return Type.HEX;
        }
        else if (UNICODE_CHARS.contains(t)) {
            return Type.UNICODE;
        }
        else if (t == TokenType.ERROR_ELEMENT) {
            return Type.INVALID;
        }
        else {
            return Type.CHAR;
        }
    }

    @Nullable
    @RequiredReadAction
    public Character getValue() {
        final String s = getUnescapedText();
        if (s.equals("\\") && getType() == Type.CHAR) {
            return '\\';
        }
        // special case for valid octal escaped sequences (see RUBY-12161)
        if (s.startsWith("\\") && s.length() > 1) {
            final ASTNode child = getNode().getFirstChildNode();
            assert child != null;
            final IElementType t = child.getElementType();
            if (t == RegExpTT.OCT_CHAR) {
                try {
                    return (char)Integer.parseInt(s.substring(1), 8);
                }
                catch (NumberFormatException e) {
                    // do nothing
                }
            }
        }
        return unescapeChar(s);
    }

    @Nullable
    private static Character unescapeChar(String s) {
        assert s.length() > 0;

        boolean escaped = false;
        for (int idx = 0; idx < s.length(); idx++) {
            char ch = s.charAt(idx);
            if (!escaped) {
                if (ch == '\\') {
                    escaped = true;
                }
                else {
                    return ch;
                }
            }
            else {
                return switch (ch) {
                    case 'n' -> '\n';
                    case 'r' -> '\r';
                    case 't' -> '\t';
                    case 'a' -> '\u0007';
                    case 'e' -> '\u001b';
                    case 'f' -> '\f';
                    case 'b' -> '\b';
                    case 'c' -> (char)(ch ^ 64);
                    case 'x' -> parseNumber(idx, s, 16, 2, true);
                    case 'u' -> parseNumber(idx, s, 16, 4, true);
                    case '0' -> parseNumber(idx, s, 8, 3, false);
                    default -> Character.isLetter(ch) ? null : ch;
                };
            }
        }

        return null;
    }

    private static Character parseNumber(int idx, String s, int radix, int len, boolean strict) {
        final int start = idx + 1;
        final int end = start + len;
        try {
            int sum = 0;
            int i;
            for (i = start; i < end && i < s.length(); i++) {
                sum *= radix;
                sum += Integer.valueOf(s.substring(i, i + 1), radix);
            }
            if (i - start == 0) {
                return null;
            }
            return i - start < len && strict ? null : (char)sum;
        }
        catch (NumberFormatException e1) {
            return null;
        }
    }

    public void accept(RegExpElementVisitor visitor) {
        visitor.visitRegExpChar(this);
    }
}
