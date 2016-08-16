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

package ru.histone.v2.java_compiler.bcompiler;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.java_compiler.bcompiler.data.Template;
import ru.histone.v2.parser.node.AstNode;
import ru.histone.v2.utils.AstJsonProcessor;

import javax.lang.model.element.Modifier;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

/**
 * @author Alexey Nevinsky
 */
public class Compiler {

    public static final String GET_STRING_AST_METHOD_NAME = "getStringAst";
    public static final String RENDER_METHOD_NAME = "render";
    private String AST_TREE_NAME = "AST_TREE";

    private TemplateProcessor templateProcessor = new TemplateProcessor();

    /**
     * Method compiles AST-tree to java-bytecode.
     *
     * @param root root of AST-tree
     * @return byte array of java-bytecode
     */
    public byte[] compile(String name, AstNode root) throws IOException {
        JavaFile file = createFile(name, root);

        File resDir = new File("./src/test/java/");
        boolean exists = resDir.createNewFile();
        file.writeTo(resDir);

        JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        String filePath = resDir.getAbsolutePath() + "/ru/histone/v2/acceptance/" + name;
        int result = javaCompiler.run(null, null, null, filePath + ".java");


        Path path = Paths.get(filePath + ".class");
        byte[] res = Files.readAllBytes(path);
        return res;
    }

    public JavaFile createFile(String name, AstNode root) throws IOException {
        JavaFile javaFile = JavaFile
                .builder("ru.histone.v2.acceptance", createClass(name, root))
                //todo check this
//                .addStaticImport(StdLibrary.class)
                .build();
        return javaFile;
    }

    public JavaFile createFile(String packageName, String name, AstNode root) throws IOException {
        JavaFile javaFile = JavaFile
                .builder(packageName, createClass(name, root))
                //todo check this
//                .addStaticImport(StdLibrary.class)
                .build();
        return javaFile;
    }

    private TypeSpec createClass(String templateName, AstNode root) throws IOException {
        TypeSpec spec = TypeSpec.classBuilder(templateName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(Template.class)
                .addField(createAstTreeField(root))
                .addMethod(createGetAstTreeMethod())
                .addMethod(createRenderMethod(root))
                .build();
        return spec;
    }

    protected MethodSpec createRenderMethod(AstNode root) throws IOException {
        MethodSpec.Builder builder = MethodSpec
                .methodBuilder(RENDER_METHOD_NAME)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(Context.class, "ctx")
                .returns(CompletableFuture.class);

        templateProcessor.processTemplate(builder, root);

        MethodSpec res = builder.build();
        return res;
    }

    private FieldSpec createAstTreeField(AstNode root) {
        FieldSpec res = FieldSpec
                .builder(String.class, AST_TREE_NAME, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", AstJsonProcessor.write(root))
                .build();
        return res;
    }

    private MethodSpec createGetAstTreeMethod() {
        MethodSpec res = MethodSpec
                .methodBuilder(GET_STRING_AST_METHOD_NAME)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(String.class)
                .addStatement("return " + AST_TREE_NAME)
                .build();
        return res;
    }
}
