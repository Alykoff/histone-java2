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

package ru.histone.v2;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.extension.ExtendWith;
import ru.histone.v2.acceptance.StringParamResolver;
import ru.histone.v2.acceptance.TestRunner;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.evaluator.resource.SchemaResourceLoader;
import ru.histone.v2.evaluator.resource.loader.DataLoader;
import ru.histone.v2.evaluator.resource.loader.FileLoader;
import ru.histone.v2.evaluator.resource.loader.HttpLoader;
import ru.histone.v2.java_compiler.bcompiler.StdLibrary;
import ru.histone.v2.java_compiler.java_evaluator.JavaHistoneClassLoader;
import ru.histone.v2.java_compiler.java_evaluator.function.JavaMacroCall;
import ru.histone.v2.java_compiler.java_evaluator.function.JavaRequire;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.rtti.RunTimeTypeInfo;
import ru.histone.v2.support.CompilerTestConsumer;
import ru.histone.v2.support.StopExecutionExceptionFunction;
import ru.histone.v2.support.ThrowExceptionFunction;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * @author Alexey Nevinsky
 */
@ExtendWith(StringParamResolver.class)
public class BaseCompilerTest {

    protected static ExecutorService executor = Executors.newFixedThreadPool(20);
    protected static RunTimeTypeInfo rtti;
    protected static Evaluator evaluator;
    protected static Parser parser;
    protected static StdLibrary library;


    @BeforeAll
    public static void doInitSuite() throws MalformedURLException {
        parser = new Parser();
        evaluator = new Evaluator();

        SchemaResourceLoader loader = new SchemaResourceLoader();
        loader.addLoader(new DataLoader());
        loader.addLoader(new HttpLoader(executor));
        loader.addLoader(new FileLoader());
        loader.addLoader(new JavaHistoneClassLoader(new URL("file:///")));

        library = new StdLibrary();

        rtti = new RunTimeTypeInfo(executor, loader, evaluator, parser);
        rtti.register(HistoneType.T_MACRO, new JavaMacroCall(executor, loader, evaluator, parser));
        rtti.register(HistoneType.T_GLOBAL, new JavaRequire(executor, loader, evaluator, parser, library));
        rtti.register(HistoneType.T_GLOBAL, new ThrowExceptionFunction());
        rtti.register(HistoneType.T_GLOBAL, new StopExecutionExceptionFunction());
    }

    public Stream<DynamicTest> loadCases(String param) throws IOException, URISyntaxException {
        TestRunner runner = new TestRunner();
        CompilerTestConsumer consumer = new CompilerTestConsumer(rtti, library);
        return runner.loadCases(param, consumer);
    }
}
