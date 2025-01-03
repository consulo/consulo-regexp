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

package org.intellij.lang.regexp;

import consulo.annotation.component.ExtensionImpl;
import consulo.virtualFileSystem.fileType.FileTypeConsumer;
import consulo.virtualFileSystem.fileType.FileTypeFactory;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class RegExpSupportLoader extends FileTypeFactory {
    public static final RegExpLanguage LANGUAGE = RegExpLanguage.INSTANCE;
    public static final RegExpFileType FILE_TYPE = RegExpFileType.INSTANCE;

    @Override
    public void createFileTypes(final @Nonnull FileTypeConsumer consumer) {
        consumer.consume(FILE_TYPE, FILE_TYPE.getDefaultExtension());
    }
}
