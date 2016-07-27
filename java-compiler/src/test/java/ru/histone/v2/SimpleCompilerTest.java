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
import org.junit.jupiter.api.Test;
import org.testng.Assert;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.evaluator.resource.SchemaResourceLoader;
import ru.histone.v2.evaluator.resource.loader.DataLoader;
import ru.histone.v2.evaluator.resource.loader.FileLoader;
import ru.histone.v2.evaluator.resource.loader.HttpLoader;
import ru.histone.v2.java_compiler.bcompiler.Compiler;
import ru.histone.v2.java_compiler.bcompiler.data.Template;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.property.DefaultPropertyHolder;
import ru.histone.v2.rtti.RunTimeTypeInfo;
import ru.histone.v2.support.ByteClassLoader;
import ru.histone.v2.utils.AstJsonProcessor;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Alexey Nevinsky
 */
public class SimpleCompilerTest {

    private static final Locale US_LOCALE = Locale.US;
    protected static ExecutorService executor = Executors.newFixedThreadPool(20);
    protected static RunTimeTypeInfo rtti;
    protected static Evaluator evaluator;
    protected static Parser parser;

    @BeforeAll
    public static void setUp() {
        parser = new Parser();
        evaluator = new Evaluator();

        SchemaResourceLoader loader = new SchemaResourceLoader(executor);
        loader.addLoader(SchemaResourceLoader.DATA_SCHEME, new DataLoader());
        loader.addLoader(SchemaResourceLoader.HTTP_SCHEME, new HttpLoader(executor));
        loader.addLoader(SchemaResourceLoader.FILE_SCHEME, new FileLoader());

        rtti = new RunTimeTypeInfo(executor, loader, evaluator, parser);
//        rtti.register(HistoneType.T_GLOBAL, new ThrowExceptionFunction());
//        rtti.register(HistoneType.T_GLOBAL, new StopExecutionExceptionFunction());
    }


    // "input": "{{'10' - 2}}"
    // "expectedAST": "[29,[10,\"10\",2]]"
    // "expectedResult": "8"
    @Test
    public void doTest() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        String expectedAST = "[29,[10,\"10\",2]]";
        String expectedResult = "8";

        Compiler compiler = new Compiler();
        byte[] classBytes = compiler.compile("Template1", AstJsonProcessor.read(expectedAST));
        Map<String, byte[]> classes = Collections.singletonMap("Template1", classBytes);

        ByteClassLoader loader = new ByteClassLoader(new URL[]{}, getClass().getClassLoader(), classes);
        Class<?> t = loader.loadClass("Template1");

        Template template = (Template) t.newInstance();

        Assert.assertEquals(template.getStringAst(), expectedAST);

        Context context = Context.createRoot("", US_LOCALE, rtti, new DefaultPropertyHolder());

        String result = (String) template.render(context).join().getValue();

        Assert.assertEquals(result, expectedResult);
    }

}
