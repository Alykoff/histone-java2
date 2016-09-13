/*
 * Copyright (c) 2016 MegaFon
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

package ru.histone.v2.java_compiler.java_evaluator;

import ru.histone.v2.java_compiler.bcompiler.data.Template;

import javax.tools.JavaFileObject;
import java.util.Collection;

/**
 * @author Alexey Nevinsky
 */
public interface HistoneClassRegistry {
    Template loadInstance(String className);

    Template loadInstanceFromTpl(String name, String tpl);

    String getOriginBasePath();

    String getRealBasePath();

    void add(String className, JavaFileObject file);

    void remove(final String qualifiedClassName);

    Collection<? extends JavaFileObject> files();
}
