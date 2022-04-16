/**
 * @author VISTALL
 * @since 08-Mar-22
 */
open module com.intellij.regexp
{
	requires java.desktop;

	requires consulo.language.api;
	requires consulo.language.impl;
	requires consulo.language.editor.api;
	requires consulo.language.editor.impl;
	requires consulo.language.editor.ui.api;
	requires consulo.ui.ex.awt.api;

	exports consulo.regexp.icon;
	exports org.intellij.lang.regexp;
	exports org.intellij.lang.regexp.intention;
	exports org.intellij.lang.regexp.psi;
	exports org.intellij.lang.regexp.psi.impl;
	exports org.intellij.lang.regexp.surroundWith;
	exports org.intellij.lang.regexp.validation;
	exports org.intellij.plugins.intelliLang.inject.config.ui;
	exports org.intellij.plugins.intelliLang.util;
}