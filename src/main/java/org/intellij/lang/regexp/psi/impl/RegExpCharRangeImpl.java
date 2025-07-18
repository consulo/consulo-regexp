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
import consulo.language.psi.PsiElement;
import jakarta.annotation.Nullable;
import org.intellij.lang.regexp.psi.RegExpChar;
import org.intellij.lang.regexp.psi.RegExpCharRange;
import org.intellij.lang.regexp.psi.RegExpElementVisitor;
import jakarta.annotation.Nonnull;

public class RegExpCharRangeImpl extends RegExpElementImpl implements RegExpCharRange {

    public RegExpCharRangeImpl(ASTNode astNode) {
        super(astNode);
    }

    @Override
    public @Nonnull RegExpChar getFrom() {
        return (RegExpChar) getFirstChild();
    }

    @Override
    public @Nullable RegExpChar getTo() {
        final PsiElement child = getLastChild();
        return child instanceof RegExpChar ? (RegExpChar) child : null;
    }

    @Override
    public void accept(RegExpElementVisitor visitor) {
        visitor.visitRegExpCharRange(this);
    }
}
