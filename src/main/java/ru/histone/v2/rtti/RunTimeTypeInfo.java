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

package ru.histone.v2.rtti;

import org.apache.commons.lang.NotImplementedException;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.Function;
import ru.histone.v2.evaluator.function.any.*;
import ru.histone.v2.evaluator.function.array.Keys;
import ru.histone.v2.evaluator.function.array.Reverse;
import ru.histone.v2.evaluator.function.array.Size;
import ru.histone.v2.evaluator.function.global.*;
import ru.histone.v2.evaluator.function.macro.MacroCall;
import ru.histone.v2.evaluator.function.number.*;
import ru.histone.v2.evaluator.function.regex.Test;
import ru.histone.v2.evaluator.function.string.Case;
import ru.histone.v2.evaluator.function.string.StringSize;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import static ru.histone.v2.rtti.HistoneType.*;

/**
 * RTTI used to storing {@link Function} for global and default types and user defined functions. Create it ones and
 * use it as parameter to create a {@link Context}.
 *
 * @author gali.alykoff on 22/01/16.
 */
public class RunTimeTypeInfo implements Irtti, Serializable {
    private final Map<HistoneType, Map<String, Function>> userTypes = new ConcurrentHashMap<>();
    private final Map<HistoneType, Map<String, Function>> typeMembers = new ConcurrentHashMap<>();

    private final Executor executor;

    public RunTimeTypeInfo(Executor executor) {
        this.executor = executor;

        for (HistoneType type : HistoneType.values()) {
            typeMembers.put(type, new HashMap<>());
            userTypes.put(type, new HashMap<>());
        }

        registerCommonFunctions();
    }

    private void registerCommonFunctions() {
        registerForAlltypes(new ToJson());
        registerForAlltypes(new ToString());
        registerForAlltypes(new ToBoolean());
        registerForAlltypes(new IsUndefined());
        registerForAlltypes(new IsNull());
        registerForAlltypes(new IsBoolean());
        registerForAlltypes(new IsNumber());
        registerForAlltypes(new IsInt());
        registerForAlltypes(new IsFloat());
        registerForAlltypes(new ToNumber());
        registerForAlltypes(new IsString());

        registerCommon(T_NUMBER, new ToAbs());
        registerCommon(T_NUMBER, new ToCeil());
        registerCommon(T_NUMBER, new ToChar());
        registerCommon(T_NUMBER, new ToFloor());
        registerCommon(T_NUMBER, new ToRound());
        registerCommon(T_NUMBER, new ToFixed());

        registerCommon(T_ARRAY, new Size());
        registerCommon(T_ARRAY, new Keys(true));
        registerCommon(T_ARRAY, new Keys(false));
        registerCommon(T_ARRAY, new Reverse());

        registerCommon(T_GLOBAL, new Range());
        registerCommon(T_GLOBAL, new LoadJson(executor));
        registerCommon(T_GLOBAL, new GetBaseUri());
        registerCommon(T_GLOBAL, new GetUniqueId());
        registerCommon(T_GLOBAL, new ResolveURI());
        registerCommon(T_GLOBAL, new GetWeekDayName(true));
        registerCommon(T_GLOBAL, new GetWeekDayName(false));
        registerCommon(T_GLOBAL, new GetMonthName(true));
        registerCommon(T_GLOBAL, new GetMonthName(false));
        registerCommon(T_GLOBAL, new GetRand());
        registerCommon(T_GLOBAL, new GetMax());

        registerCommon(T_REGEXP, new Test());

        registerCommon(T_STRING, new StringSize());
        registerCommon(T_STRING, new Case(false));
        registerCommon(T_STRING, new Case(true));

        registerCommon(T_MACRO, new MacroCall());
    }

    private void registerForAlltypes(Function function) {
        for (HistoneType type : HistoneType.values()) {
            registerCommon(type, function);
        }
    }

    private void registerCommon(HistoneType type, Function function) {
        typeMembers.get(type).put(function.getName(), function);
    }

    @Override
    public Function getFunc(HistoneType type, String funcName) {
        Function f = userTypes.get(type).get(funcName);
        if (f == null) {
            f = typeMembers.get(type).get(funcName);
            if (f == null) {
                throw new FunctionExecutionException("Couldn't find function '" + funcName + "' for type " + type);
            }
        }
        return f;
    }

    @Override
    public void register(HistoneType type, String funcName, Function func) {
        throw new NotImplementedException();
    }

    @Override
    public void unregistered(HistoneType type, String funcName) {
        throw new NotImplementedException();
    }

    @Override
    public CompletableFuture<EvalNode> callFunction(String baseUri, Locale locale, HistoneType type, String funcName, List<EvalNode> args) {
        final Function f = getFunc(type, funcName);
        if (f.isAsync()) {
            return CompletableFuture
                    .completedFuture(null)
                    .thenComposeAsync((x) -> f.execute(baseUri, locale, args), executor);
        }
        return f.execute(baseUri, locale, args);
    }

    @Override
    public CompletableFuture<EvalNode> callFunction(String baseUri, Locale locale, EvalNode node, String funcName, List<EvalNode> args) {
        return callFunction(baseUri, locale, node.getType(), funcName, args);
    }
}
