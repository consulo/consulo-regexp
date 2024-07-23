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
package org.intellij.lang.regexp.validation;

import consulo.annotation.access.RequiredReadAction;
import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.editor.annotation.AnnotationHolder;
import consulo.language.editor.annotation.Annotator;
import consulo.language.editor.annotation.HighlightSeverity;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.psi.PsiComment;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import org.intellij.lang.regexp.RegExpLanguageHosts;
import org.intellij.lang.regexp.RegExpTT;
import org.intellij.lang.regexp.psi.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

public final class RegExpAnnotator extends RegExpElementVisitor implements Annotator {
    private static final Set<String> POSIX_CHARACTER_CLASSES = Set.of(
        "alnum", "alpha", "ascii", "blank",
        "cntrl", "digit", "graph", "lower",
        "print", "punct", "space", "upper",
        "word", "xdigit"
    );
    private static final String ILLEGAL_CHARACTER_RANGE_TO_FROM = "Illegal character range (to < from)";
    private AnnotationHolder myHolder;
    private final RegExpLanguageHosts myLanguageHosts;

    public RegExpAnnotator() {
        myLanguageHosts = RegExpLanguageHosts.INSTANCE;
    }

    @Override
    public void annotate(@Nonnull PsiElement psiElement, @Nonnull AnnotationHolder holder) {
        assert myHolder == null : "unsupported concurrent annotator invocation";
        try {
            myHolder = holder;
            psiElement.accept(this);
        }
        finally {
            myHolder = null;
        }
    }

    @Override
    @RequiredReadAction
    public void visitRegExpOptions(RegExpOptions options) {
        checkValidFlag(options.getOptionsOn(), options);
        checkValidFlag(options.getOptionsOff(), options);
    }

    @RequiredReadAction
    private void checkValidFlag(@Nullable ASTNode optionsNode, @Nonnull RegExpOptions context) {
        if (optionsNode == null) {
            return;
        }
        final String text = optionsNode.getText();
        final int start = (optionsNode.getElementType() == RegExpTT.OPTIONS_OFF) ? 1 : 0; // skip '-' if necessary
        for (int i = start, length = text.length(); i < length; i++) {
            final int c = text.codePointAt(i);
            if (!Character.isBmpCodePoint(c) || !myLanguageHosts.supportsInlineOptionFlag((char)c, context)) {
                final int offset = optionsNode.getStartOffset() + i;
                myHolder.newAnnotation(HighlightSeverity.ERROR, "Unknown inline option flag")
                    .range(new TextRange(offset, offset + 1))
                    .create();
            }
        }
    }

    @Override
    @RequiredReadAction
    public void visitRegExpCharRange(RegExpCharRange range) {
        final RegExpCharRange.Endpoint from = range.getFrom();
        final RegExpCharRange.Endpoint to = range.getTo();
        final boolean a = from instanceof RegExpChar;
        final boolean b = to instanceof RegExpChar;
        if (a && b) {
            final Character t = ((RegExpChar)to).getValue();
            final Character f = ((RegExpChar)from).getValue();
            if (t != null && f != null) {
                if (t < f) {
                    if (handleSurrogates(range, f, t)) {
                        return;
                    }
                    myHolder.newAnnotation(HighlightSeverity.ERROR, ILLEGAL_CHARACTER_RANGE_TO_FROM).range(range).create();
                }
                else if (t == f) {
                    myHolder.newAnnotation(HighlightSeverity.WARNING, "Redundant character range").range(range).create();
                }
            }
        }
        else if (a != b) {
            myHolder.newAnnotation(HighlightSeverity.ERROR, "Character class (e.g. '\\\\w') may not be used inside character range")
                .range(range)
                .create();
        }
        else if (from.getText().equals(to.getText())) {
            myHolder.newAnnotation(HighlightSeverity.WARNING, "Redundant character range").range(range).create();
        }
    }

    @RequiredReadAction
    private boolean handleSurrogates(RegExpCharRange range, Character f, Character t) {
        // \ud800\udc00-\udbff\udfff
        PsiElement prevSibling = range.getPrevSibling();
        PsiElement nextSibling = range.getNextSibling();

        if (prevSibling instanceof RegExpChar prevSiblingChar && nextSibling instanceof RegExpChar nextSiblingChar) {
            Character prevSiblingValue = prevSiblingChar.getValue();
            Character nextSiblingValue = nextSiblingChar.getValue();

            if (prevSiblingValue != null && nextSiblingValue != null &&
                Character.isSurrogatePair(prevSiblingValue, f) && Character.isSurrogatePair(t, nextSiblingValue)) {
                if (Character.toCodePoint(prevSiblingValue, f) > Character.toCodePoint(t, nextSiblingValue)) {
                    TextRange prevSiblingRange = prevSibling.getTextRange();
                    TextRange nextSiblingRange = nextSibling.getTextRange();
                    myHolder.newAnnotation(HighlightSeverity.ERROR, ILLEGAL_CHARACTER_RANGE_TO_FROM)
                        .range(new TextRange(prevSiblingRange.getStartOffset(), nextSiblingRange.getEndOffset()))
                        .create();
                }
                return true;
            }
        }
        return false;
    }

    @Override
    @RequiredReadAction
    public void visitRegExpBoundary(RegExpBoundary boundary) {
        if (!myLanguageHosts.supportsBoundary(boundary)) {
            myHolder.newAnnotation(HighlightSeverity.ERROR, "Unsupported boundary").range(boundary).create();
        }
    }

    @Override
    @RequiredReadAction
    public void visitSimpleClass(RegExpSimpleClass simpleClass) {
        if (!myLanguageHosts.supportsSimpleClass(simpleClass)) {
            myHolder.newAnnotation(HighlightSeverity.ERROR, "Illegal/unsupported escape sequence").range(simpleClass).create();
        }
    }

    @Override
    @RequiredReadAction
    public void visitRegExpClass(RegExpClass regExpClass) {
        if (!(regExpClass.getParent() instanceof RegExpClass)) {
            checkForDuplicates(regExpClass, new HashSet<>());
        }
    }

    @RequiredReadAction
    private void checkForDuplicates(RegExpClassElement element, Set<Character> seen) {
        if (element instanceof RegExpChar regExpChar) {
            final Character value = regExpChar.getValue();
            if (value != null && !seen.add(value)) {
                myHolder.newAnnotation(HighlightSeverity.WARNING, "Duplicate character '" + regExpChar.getText() + "' in character class")
                    .range(regExpChar)
                    .create();
            }
        }
        else if (element instanceof RegExpClass regExpClass) {
            for (RegExpClassElement classElement : regExpClass.getElements()) {
                checkForDuplicates(classElement, seen);
            }
        }
        else if (element instanceof RegExpUnion union) {
            for (RegExpClassElement classElement : union.getElements()) {
                checkForDuplicates(classElement, seen);
            }
        }
    }

    @Override
    @RequiredReadAction
    public void visitRegExpChar(final RegExpChar ch) {
        final Character value = ch.getValue();
        if (value == null || (value == '\b' && !myLanguageHosts.supportsLiteralBackspace(ch))) {
            switch (ch.getType()) {
                case CHAR:
                    myHolder.newAnnotation(HighlightSeverity.ERROR, "Illegal/unsupported escape sequence").create();
                    break;
                case HEX:
                    myHolder.newAnnotation(HighlightSeverity.ERROR, "Illegal hexadecimal escape sequence").create();
                    break;
                case OCT:
                    myHolder.newAnnotation(HighlightSeverity.ERROR, "Illegal octal escape sequence").create();
                    break;
                case UNICODE:
                    myHolder.newAnnotation(HighlightSeverity.ERROR, "Illegal unicode escape sequence").create();
                    break;
                case INVALID:
                    // produces a parser error. already handled by IDEA and possibly suppressed by IntelliLang
                    break;
            }
        }
        else {
            final String text = ch.getUnescapedText();
            if (text.startsWith("\\") && myLanguageHosts.isRedundantEscape(ch, text)) {
                final ASTNode astNode = ch.getNode().getFirstChildNode();
                if (astNode != null && astNode.getElementType() == RegExpTT.REDUNDANT_ESCAPE) {
                    myHolder.newAnnotation(HighlightSeverity.WEAK_WARNING, "Redundant character escape")
                        .range(ch)
                        .withFix(new RemoveRedundantEscapeAction(ch))
                        .create();
                }
            }
            if (ch.getType() == RegExpChar.Type.HEX && text.charAt(text.length() - 1) == '}'
                && !myLanguageHosts.supportsExtendedHexCharacter(ch)) {
                myHolder.newAnnotation(HighlightSeverity.ERROR, "This hex character syntax is not supported").range(ch).create();
            }
        }
    }

    @Override
    @RequiredReadAction
    public void visitRegExpProperty(RegExpProperty property) {
        final ASTNode category = property.getCategoryNode();
        if (category == null) {
            return;
        }
        if (!myLanguageHosts.isValidCategory(category.getPsi(), category.getText())) {
            myHolder.newAnnotation(HighlightSeverity.ERROR, "Unknown character category")
                .range(category)
                .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                .create();
        }
    }

    @Override
    @RequiredReadAction
    public void visitRegExpNamedCharacter(RegExpNamedCharacter namedCharacter) {
        if (!myLanguageHosts.supportsNamedCharacters(namedCharacter)) {
            myHolder.newAnnotation(HighlightSeverity.ERROR, "Named Unicode characters are not allowed in this regular expression dialect")
                .range(namedCharacter)
                .create();
        }
        else if (!myLanguageHosts.isValidNamedCharacter(namedCharacter)) {
            final ASTNode node = namedCharacter.getNameNode();
            if (node != null) {
                myHolder.newAnnotation(HighlightSeverity.ERROR, "Unknown character name")
                    .range(node)
                    .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                    .create();
            }
        }
    }

    @Override
    @RequiredReadAction
    public void visitRegExpBackref(final RegExpBackref backref) {
        final RegExpGroup group = backref.resolve();
        if (group == null) {
            myHolder.newAnnotation(HighlightSeverity.ERROR, "Unresolved back reference")
                .range(backref)
                .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                .create();
        }
        else if (PsiTreeUtil.isAncestor(group, backref, true)) {
            myHolder.newAnnotation(HighlightSeverity.WARNING, "Back reference is nested into the capturing group it refers to")
                .range(backref)
                .create();
        }
    }

    @Override
    @RequiredReadAction
    public void visitRegExpIntersection(RegExpIntersection intersection) {
        if (intersection.getOperands().length == 0) {
            myHolder.newAnnotation(HighlightSeverity.ERROR, "Illegal empty intersection").range(intersection).create();
        }
    }

    @Override
    @RequiredReadAction
    public void visitRegExpGroup(RegExpGroup group) {
        final RegExpPattern pattern = group.getPattern();
        if (pattern != null) {
            final RegExpBranch[] branches = pattern.getBranches();
            if (isEmpty(branches)) {
                // catches "()" as well as "(|)"
                myHolder.newAnnotation(HighlightSeverity.WARNING, "Empty group").range(group).create();
            }
            else if (branches.length == 1) {
                final RegExpAtom[] atoms = branches[0].getAtoms();
                if (atoms.length == 1 && atoms[0] instanceof RegExpGroup innerGroup
                    && group.isSimple() && group.isCapturing() == innerGroup.isCapturing()) {
                    myHolder.newAnnotation(HighlightSeverity.WARNING, "Redundant group nesting").range(group).create();
                }
            }
        }
        if (group.isPythonNamedGroup() || group.isRubyNamedGroup()) {
            if (!myLanguageHosts.supportsNamedGroupSyntax(group)) {
                myHolder.newAnnotation(HighlightSeverity.ERROR, "This named group syntax is not supported").range(group).create();
            }
        }
        final String name = group.getName();
        if (name != null && !myLanguageHosts.isValidGroupName(name, group)) {
            final ASTNode node = group.getNode().findChildByType(RegExpTT.NAME);
            if (node != null) {
                myHolder.newAnnotation(HighlightSeverity.ERROR, "Invalid group name").range(node).create();
            }
        }
    }

    @Override
    @RequiredReadAction
    public void visitRegExpNamedGroupRef(RegExpNamedGroupRef groupRef) {
        if (!myLanguageHosts.supportsNamedGroupRefSyntax(groupRef)) {
            myHolder.newAnnotation(HighlightSeverity.ERROR, "This named group reference syntax is not supported")
                .range(groupRef)
                .create();
            return;
        }
        if (groupRef.getGroupName() == null) {
            return;
        }
        final RegExpGroup group = groupRef.resolve();
        if (group == null) {
            final ASTNode node = groupRef.getNode().findChildByType(RegExpTT.NAME);
            if (node != null) {
                myHolder.newAnnotation(HighlightSeverity.ERROR, "Unresolved named group reference")
                    .range(node)
                    .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                    .create();
            }
        }
        else if (PsiTreeUtil.isAncestor(group, groupRef, true)) {
            myHolder.newAnnotation(HighlightSeverity.WARNING, "Group reference is nested into the named group it refers to")
                .range(groupRef)
                .create();
        }
    }

    @Override
    @RequiredReadAction
    public void visitComment(PsiComment comment) {
        if (comment.getText().startsWith("(?#")) {
            if (!myLanguageHosts.supportsPerl5EmbeddedComments(comment)) {
                myHolder.newAnnotation(HighlightSeverity.ERROR, "Embedded comments are not supported").range(comment).create();
            }
        }
    }

    @Override
    @RequiredReadAction
    public void visitRegExpPyCondRef(RegExpPyCondRef condRef) {
        if (!myLanguageHosts.supportsPythonConditionalRefs(condRef)) {
            myHolder.newAnnotation(HighlightSeverity.ERROR, "Conditional references are not supported").range(condRef).create();
        }
    }

    private static boolean isEmpty(RegExpBranch[] branches) {
        for (RegExpBranch branch : branches) {
            if (branch.getAtoms().length > 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    @RequiredReadAction
    public void visitRegExpQuantifier(RegExpQuantifier quantifier) {
        final RegExpQuantifier.Count count = quantifier.getCount();
        if (!(count instanceof RegExpQuantifier.SimpleCount)) {
            String min = count.getMin();
            String max = count.getMax();
            if (max.equals(min)) {
                if ("1".equals(max)) { // TODO: is this safe when reluctant or possessive modifier is present?
                    myHolder.newAnnotation(HighlightSeverity.WEAK_WARNING, "Single repetition")
                        .range(quantifier)
                        .withFix(new SimplifyQuantifierAction(quantifier, null))
                        .create();
                }
                else {
                    final ASTNode node = quantifier.getNode();
                    if (node.findChildByType(RegExpTT.COMMA) != null) {
                        myHolder.newAnnotation(HighlightSeverity.WEAK_WARNING, "Fixed repetition range")
                            .range(quantifier)
                            .withFix(new SimplifyQuantifierAction(quantifier, "{" + max + "}"))
                            .create();
                    }
                }
            }
            else if ("0".equals(min) && "1".equals(max)) {
                myHolder.newAnnotation(HighlightSeverity.WEAK_WARNING, "Repetition range replaceable by '?'")
                    .range(quantifier)
                    .withFix(new SimplifyQuantifierAction(quantifier, "?"))
                    .create();
            }
            else if ("0".equals(min) && max.isEmpty()) {
                myHolder.newAnnotation(HighlightSeverity.WEAK_WARNING, "Repetition range replaceable by '*'")
                    .range(quantifier)
                    .withFix(new SimplifyQuantifierAction(quantifier, "*"))
                    .create();
            }
            else if ("1".equals(min) && max.isEmpty()) {
                myHolder.newAnnotation(HighlightSeverity.WEAK_WARNING, "Repetition range replaceable by '+'")
                    .range(quantifier)
                    .withFix(new SimplifyQuantifierAction(quantifier, "+"))
                    .create();
            }
            else if (!min.isEmpty() && !max.isEmpty()) {
                try {
                    BigInteger minInt = new BigInteger(min);
                    BigInteger maxInt = new BigInteger(max);
                    if (maxInt.compareTo(minInt) < 0) {
                        myHolder.newAnnotation(HighlightSeverity.ERROR, "Illegal repetition range").range(quantifier).create();
                    }
                }
                catch (NumberFormatException ex) {
                    myHolder.newAnnotation(HighlightSeverity.ERROR, "Illegal repetition value").range(quantifier).create();
                }
            }
        }
        if (quantifier.getType() == RegExpQuantifier.Type.POSSESSIVE) {
            if (!myLanguageHosts.supportsPossessiveQuantifiers(quantifier)) {
                myHolder.newAnnotation(HighlightSeverity.ERROR, "Nested quantifier in regexp").range(quantifier).create();
            }
        }
    }

    @Override
    @RequiredReadAction
    public void visitPosixBracketExpression(RegExpPosixBracketExpression posixBracketExpression) {
        final String className = posixBracketExpression.getClassName();
        if (!POSIX_CHARACTER_CLASSES.contains(className)) {
            final ASTNode node = posixBracketExpression.getNode().findChildByType(RegExpTT.NAME);
            if (node != null) {
                myHolder.newAnnotation(HighlightSeverity.ERROR, "Unknown POSIX character class")
                    .range(node)
                    .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                    .create();
            }
        }
    }
}
