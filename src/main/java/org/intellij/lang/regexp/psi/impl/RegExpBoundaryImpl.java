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
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import org.intellij.lang.regexp.RegExpTT;
import org.intellij.lang.regexp.psi.RegExpBoundary;
import org.intellij.lang.regexp.psi.RegExpElementVisitor;

import javax.annotation.Nonnull;

public class RegExpBoundaryImpl extends RegExpElementImpl implements RegExpBoundary {
    public RegExpBoundaryImpl(ASTNode astNode) {
        super(astNode);
    }

    @Nonnull
    @Override
    @RequiredReadAction
    public Type getType() {
        final ASTNode child = getNode().getFirstChildNode();
        assert child != null;
        final IElementType type = child.getElementType();
        if (type == RegExpTT.CARET) {
            return Type.LINE_START;
        }
        else if (type == RegExpTT.DOLLAR) {
            return Type.LINE_END;
        }
        else if (type == RegExpTT.BOUNDARY) {
            return switch (getUnescapedText()) {
                case "\\b" -> Type.WORD;
                case "\\b{g}" -> Type.UNICODE_EXTENDED_GRAPHEME;
                case "\\B" -> Type.NON_WORD;
                case "\\A" -> Type.BEGIN;
                case "\\Z" -> Type.END_NO_LINE_TERM;
                case "\\z" -> Type.END;
                case "\\G" -> Type.PREVIOUS_MATCH;
                default -> throw new AssertionError();
            };
        }
        assert false;
        return null;
    }

    @Override
    public void accept(RegExpElementVisitor visitor) {
        visitor.visitRegExpBoundary(this);
    }
}