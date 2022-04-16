/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.lang.regexp.psi.impl;

import consulo.document.util.TextRange;
import consulo.language.ast.ASTNode;
import consulo.language.ast.TokenSet;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.resolve.PsiElementProcessor;
import consulo.language.psi.util.PsiElementFilter;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.util.lang.Comparing;
import org.intellij.lang.regexp.RegExpTT;
import org.intellij.lang.regexp.psi.RegExpElementVisitor;
import org.intellij.lang.regexp.psi.RegExpGroup;
import org.intellij.lang.regexp.psi.RegExpNamedGroupRef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author yole
 */
public class RegExpNamedGroupRefImpl extends RegExpElementImpl implements RegExpNamedGroupRef
{
	private static final TokenSet RUBY_GROUP_REF_TOKENS = TokenSet.create(RegExpTT.RUBY_NAMED_GROUP_REF, RegExpTT.RUBY_QUOTED_NAMED_GROUP_REF, RegExpTT.RUBY_NAMED_GROUP_CALL, RegExpTT
			.RUBY_QUOTED_NAMED_GROUP_CALL);
	private static final TokenSet GROUP_REF_TOKENS = TokenSet.create(RegExpTT.PYTHON_NAMED_GROUP_REF, RegExpTT.RUBY_NAMED_GROUP_REF, RegExpTT.RUBY_QUOTED_NAMED_GROUP_REF, RegExpTT
			.RUBY_NAMED_GROUP_CALL, RegExpTT.RUBY_QUOTED_NAMED_GROUP_CALL);

	public RegExpNamedGroupRefImpl(ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(RegExpElementVisitor visitor)
	{
		visitor.visitRegExpNamedGroupRef(this);
	}

	@Nullable
	public RegExpGroup resolve()
	{
		final PsiElementProcessor.FindFilteredElement<RegExpGroup> processor = new PsiElementProcessor.FindFilteredElement<>(new PsiElementFilter()
		{
			public boolean isAccepted(PsiElement element)
			{
				if(!(element instanceof RegExpGroup))
				{
					return false;
				}
				final RegExpGroup regExpGroup = (RegExpGroup) element;
				return (regExpGroup.isPythonNamedGroup() || regExpGroup.isRubyNamedGroup()) && Comparing.equal(getGroupName(), regExpGroup.getGroupName());
			}
		});
		PsiTreeUtil.processElements(getContainingFile(), processor);
		return processor.getFoundElement();
	}

	@Nullable
	public String getGroupName()
	{
		final ASTNode nameNode = getNode().findChildByType(RegExpTT.NAME);
		return nameNode != null ? nameNode.getText() : null;
	}

	@Override
	public boolean isPythonNamedGroupRef()
	{
		return getNode().findChildByType(RegExpTT.PYTHON_NAMED_GROUP_REF) != null;
	}

	@Override
	public boolean isRubyNamedGroupRef()
	{
		final ASTNode node = getNode();
		return node.findChildByType(RUBY_GROUP_REF_TOKENS) != null;
	}

	@Override
	public boolean isNamedGroupRef()
	{
		return getNode().findChildByType(RegExpTT.RUBY_NAMED_GROUP_REF) != null;
	}

	@Override
	public PsiReference getReference()
	{
		return new PsiReference()
		{
			public PsiElement getElement()
			{
				return RegExpNamedGroupRefImpl.this;
			}

			@Override
			public TextRange getRangeInElement()
			{
				final ASTNode groupNode = getNode().findChildByType(GROUP_REF_TOKENS);
				assert groupNode != null;
				return new TextRange(groupNode.getTextLength(), getTextLength() - 1);
			}

			public PsiElement resolve()
			{
				return RegExpNamedGroupRefImpl.this.resolve();
			}

			@Nonnull
			public String getCanonicalText()
			{
				return getRangeInElement().substring(getText());
			}

			public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException
			{
				throw new UnsupportedOperationException();
			}

			public PsiElement bindToElement(@Nonnull PsiElement element) throws IncorrectOperationException
			{
				throw new UnsupportedOperationException();
			}

			public boolean isReferenceTo(PsiElement element)
			{
				return resolve() == element;
			}

			@Override
			@Nonnull
			public Object[] getVariants()
			{
				final PsiElementProcessor.CollectFilteredElements<RegExpGroup> processor = new PsiElementProcessor.CollectFilteredElements<>(new PsiElementFilter()
				{
					@Override
					public boolean isAccepted(PsiElement element)
					{
						if(!(element instanceof RegExpGroup))
						{
							return false;
						}
						final RegExpGroup regExpGroup = (RegExpGroup) element;
						return regExpGroup.isPythonNamedGroup() || regExpGroup.isRubyNamedGroup();
					}
				});
				PsiTreeUtil.processElements(getContainingFile(), processor);
				return processor.toArray();
			}

			public boolean isSoft()
			{
				return false;
			}
		};
	}
}
