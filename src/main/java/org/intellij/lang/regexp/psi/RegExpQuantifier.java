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

public interface RegExpQuantifier extends RegExpAtom {

    /**
     * @return true, if the quantifier is of the {min, max} variety, false if it is ?, * or +
     */
    boolean isCounted();

    /**
     * @return the ?, * or + token, or null if isCounted() is true.
     */
    @Nullable
    ASTNode getToken();

    /**
     * @return the min element, or null when not present
     */
    @Nullable
    RegExpNumber getMin();

    /**
     * @return the max element, or null when not present. Returns the min element when no max element or comma is present (i.e. {n}).
     */
    @Nullable
    RegExpNumber getMax();

    /**
     * @return optional reluctant '?' or possessive modifier '+'
     */
    @Nullable
    ASTNode getModifier();

    boolean isReluctant();

    boolean isPossessive();
}
