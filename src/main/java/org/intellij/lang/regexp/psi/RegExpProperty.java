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
package org.intellij.lang.regexp.psi;

import consulo.language.ast.ASTNode;
import jakarta.annotation.Nullable;

/**
 * Represents a character property as in \p{Digit}
 */
public interface RegExpProperty extends RegExpAtom, RegExpClassElement {
    /**
     * True, if \P, false if \p
     */
    boolean isNegated();

    /**
     * The node the represents the category name, e.g. "Digit"
     */
    @Nullable
    ASTNode getCategoryNode();

    /**
     * The node that represents the category value, e.g 'Latin' in \p{Script=Latin}
     */
    @Nullable
    ASTNode getValueNode();
}
