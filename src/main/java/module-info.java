/**
 * @author VISTALL
 * @since 2022-03-08
 */
open module com.intellij.regexp
{
    requires java.desktop;

    requires consulo.application.api;
    requires consulo.application.ui.api;
    requires consulo.base.icon.library;
    requires consulo.code.editor.api;
    requires consulo.color.scheme.api;
    requires consulo.component.api;
    requires consulo.disposer.api;
    requires consulo.document.api;
    requires consulo.language.api;
    requires consulo.language.impl;
    requires consulo.language.editor.api;
    requires consulo.language.editor.impl;
    requires consulo.language.editor.ui.api;
    requires consulo.localize.api;
    requires consulo.project.api;
    requires consulo.ui.api;
    requires consulo.ui.ex.api;
    requires consulo.ui.ex.awt.api;
    requires consulo.util.collection;
    requires consulo.util.dataholder;
    requires consulo.util.lang;
    requires consulo.virtual.file.system.api;

    exports consulo.regexp.icon;
    exports consulo.regexp.localize;

    exports org.intellij.lang.regexp;
    exports org.intellij.lang.regexp.intention;
    exports org.intellij.lang.regexp.psi;
    exports org.intellij.lang.regexp.psi.impl;
    exports org.intellij.lang.regexp.surroundWith;
    exports org.intellij.lang.regexp.validation;
    exports org.intellij.plugins.intelliLang.inject.config.ui;
    exports org.intellij.plugins.intelliLang.util;
}
