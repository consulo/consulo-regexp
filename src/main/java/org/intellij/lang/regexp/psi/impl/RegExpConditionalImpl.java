// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.intellij.lang.regexp.psi.impl;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import org.intellij.lang.regexp.psi.*;

public class RegExpConditionalImpl extends RegExpElementImpl implements RegExpConditional {
    public RegExpConditionalImpl(ASTNode node) {
        super(node);
    }

    @Override
    public void accept(RegExpElementVisitor visitor) {
        visitor.visitRegExpConditional(this);
    }

    @Override
    public RegExpAtom getCondition() {
        final PsiElement sibling = getFirstChild().getNextSibling();
        if (!(sibling instanceof RegExpBackref) && !(sibling instanceof RegExpNamedGroupRef) && !(sibling instanceof RegExpGroup)) {
            return null;
        }
        return (RegExpAtom) sibling;
    }
}
