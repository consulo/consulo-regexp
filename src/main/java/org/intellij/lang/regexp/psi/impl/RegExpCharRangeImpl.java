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

import consulo.language.ast.ASTNode;
import consulo.language.ast.TokenSet;
import consulo.language.psi.PsiElement;
import org.intellij.lang.regexp.RegExpElementTypes;
import org.intellij.lang.regexp.psi.RegExpCharRange;
import org.intellij.lang.regexp.psi.RegExpElementVisitor;

import javax.annotation.Nonnull;

public class RegExpCharRangeImpl extends RegExpElementImpl implements RegExpCharRange {
    private static final TokenSet E = TokenSet.create(RegExpElementTypes.CHAR, RegExpElementTypes.SIMPLE_CLASS);

    public RegExpCharRangeImpl(ASTNode astNode) {
        super(astNode);
    }

    @Nonnull
    public Endpoint getFrom() {
        return (Endpoint)getCharNode(0);
    }
    @Nonnull
    public Endpoint getTo() {
        return (Endpoint)getCharNode(1);
    }

    private PsiElement getCharNode(int idx) {
        final ASTNode[] ch = getNode().getChildren(E);
        assert ch.length == 2;
        return ch[idx].getPsi();
    }

    public void accept(RegExpElementVisitor visitor) {
        visitor.visitRegExpCharRange(this);
    }
}
