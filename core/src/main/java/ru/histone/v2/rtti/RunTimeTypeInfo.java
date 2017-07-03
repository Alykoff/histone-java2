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

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.Converter;
import ru.histone.v2.evaluator.Evaluator;
import ru.histone.v2.evaluator.Function;
import ru.histone.v2.evaluator.function.any.*;
import ru.histone.v2.evaluator.function.array.*;
import ru.histone.v2.evaluator.function.global.*;
import ru.histone.v2.evaluator.function.macro.MacroBind;
import ru.histone.v2.evaluator.function.macro.MacroCall;
import ru.histone.v2.evaluator.function.macro.MacroExtend;
import ru.histone.v2.evaluator.function.number.*;
import ru.histone.v2.evaluator.function.regex.Test;
import ru.histone.v2.evaluator.function.string.*;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.resource.HistoneResourceLoader;
import ru.histone.v2.parser.Parser;
import ru.histone.v2.utils.AsyncUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

/**
 * RTTI used to storing {@link Function} for global and default types and user defined functions. Create it ones and
 * use it as parameter to create a {@link Context}.
 *
 * @author Gali Alykoff
 */
public class RunTimeTypeInfo implements Irtti {
    protected final Map<HistoneType, Map<String, Function>> userTypes = new ConcurrentHashMap<>();
    protected final Map<HistoneType, Map<String, Function>> typeMembers = new ConcurrentHashMap<>();

    protected final Executor executor;
    protected final HistoneResourceLoader loader;
    protected final Evaluator evaluator;
    protected final Parser parser;
    protected final Converter converter;
    protected ConcurrentMap<String, CompletableFuture<EvalNode>> cache;

    /**
     * Creates RTTI object
     *
     * @param executor  for running async functions
     * @param loader    for loading resources
     * @param evaluator for processing templates in eval function and other
     * @param parser    for convert string templates to AST-tree
     */
    public RunTimeTypeInfo(Executor executor, HistoneResourceLoader loader, Evaluator evaluator, Parser parser) {
        this.executor = executor;
        this.loader = loader;
        this.evaluator = evaluator;
        this.parser = parser;
        converter = new Converter();
        cache = new ConcurrentHashMap<>();

        for (HistoneType type : HistoneType.values()) {
            typeMembers.put(type, new HashMap<>());
            userTypes.put(type, new HashMap<>());
        }

        registerCommonFunctions();
    }

    private void registerCommonFunctions() {
        registerForAlltypes(new ToJson(converter));
        registerForAlltypes(new ToString(converter));
        registerForAlltypes(new ToBoolean(converter));
        registerForAlltypes(new IsUndefined(converter));
        registerForAlltypes(new IsNull(converter));
        registerForAlltypes(new IsBoolean(converter));
        registerForAlltypes(new IsNumber(converter));
        registerForAlltypes(new IsInt(converter));
        registerForAlltypes(new IsFloat(converter));
        registerForAlltypes(new IsRegExp(converter));
        registerForAlltypes(new IsString(converter));
        registerForAlltypes(new IsArray(converter));
        registerForAlltypes(new IsMacro(converter));
        registerForAlltypes(new ToMacro(converter));
        registerForAlltypes(new ToArray(converter));
        registerForAlltypes(new GetFunction(converter));
        registerForAlltypes(new CallFunction(converter));
        registerForAlltypes(new HasMethod(converter));
        registerForAlltypes(new IsDate(converter));
        registerForAlltypes(new GetMethod(converter));

        ToNumber toNumber = new ToNumber(converter);

        registerCommon(HistoneType.T_BOOLEAN, toNumber);

        registerCommon(HistoneType.T_NUMBER, new ToAbs(converter));
        registerCommon(HistoneType.T_NUMBER, new ToCeil(converter));
        registerCommon(HistoneType.T_NUMBER, new ToChar(converter));
        registerCommon(HistoneType.T_NUMBER, new ToDate(converter));
        registerCommon(HistoneType.T_NUMBER, new ToFloor(converter));
        registerCommon(HistoneType.T_NUMBER, new ToRound(converter));
        registerCommon(HistoneType.T_NUMBER, new ToFixed(converter));
        registerCommon(HistoneType.T_NUMBER, toNumber);

        registerCommon(HistoneType.T_ARRAY, new ArrayLength(converter));
        registerCommon(HistoneType.T_ARRAY, new Keys(converter, true));
        registerCommon(HistoneType.T_ARRAY, new Keys(converter, false));
        registerCommon(HistoneType.T_ARRAY, new Reverse(converter));
        registerCommon(HistoneType.T_ARRAY, new ArrayMap(converter));
        registerCommon(HistoneType.T_ARRAY, new ArrayFilter(converter));
        registerCommon(HistoneType.T_ARRAY, new ArraySome(converter));
        registerCommon(HistoneType.T_ARRAY, new ArrayEvery(converter));
        registerCommon(HistoneType.T_ARRAY, new ArrayJoin(converter));
        registerCommon(HistoneType.T_ARRAY, new ArrayLast(converter));
        registerCommon(HistoneType.T_ARRAY, new ArrayFirst(converter));
        registerCommon(HistoneType.T_ARRAY, new ArrayChunk(converter));
        registerCommon(HistoneType.T_ARRAY, new ArrayReduce(converter));
        registerCommon(HistoneType.T_ARRAY, new ArrayFind(converter));
        registerCommon(HistoneType.T_ARRAY, new ArrayForEach(converter));
        registerCommon(HistoneType.T_ARRAY, new ArrayGroup(converter));
        registerCommon(HistoneType.T_ARRAY, new ArraySort(converter));
        registerCommon(HistoneType.T_ARRAY, new ArraySlice(converter));
        registerCommon(HistoneType.T_ARRAY, new ArrayHtmlEntities(converter));
        registerCommon(HistoneType.T_ARRAY, new ArrayHas(converter));
        registerCommon(HistoneType.T_ARRAY, new ArrayToDate(converter));

        registerCommon(HistoneType.T_DATE, toNumber);

        registerCommon(HistoneType.T_GLOBAL, new Range(converter));
        registerCommon(HistoneType.T_GLOBAL, new LoadJson(executor, loader, evaluator, parser, converter, cache));
        registerCommon(HistoneType.T_GLOBAL, new LoadText(executor, loader, evaluator, parser, converter, cache));
        registerCommon(HistoneType.T_GLOBAL, new AsyncLoadText(executor, loader, evaluator, parser, converter, cache));
        registerCommon(HistoneType.T_GLOBAL, new AsyncLoadJson(executor, loader, evaluator, parser, converter, cache));
        registerCommon(HistoneType.T_GLOBAL, new GetBaseUri(converter));
        registerCommon(HistoneType.T_GLOBAL, new GetUniqueId(converter));
        registerCommon(HistoneType.T_GLOBAL, new ResolveURI(converter));
        registerCommon(HistoneType.T_GLOBAL, new GetWeekDayName(converter, true));
        registerCommon(HistoneType.T_GLOBAL, new GetWeekDayName(converter, false));
        registerCommon(HistoneType.T_GLOBAL, new GetMonthName(converter, true));
        registerCommon(HistoneType.T_GLOBAL, new GetMonthName(converter, false));
        registerCommon(HistoneType.T_GLOBAL, new GetRand(converter));
        registerCommon(HistoneType.T_GLOBAL, new GetMinMax(converter, false));
        registerCommon(HistoneType.T_GLOBAL, new GetMinMax(converter, true));
        registerCommon(HistoneType.T_GLOBAL, new GetDate(converter));
        registerCommon(HistoneType.T_GLOBAL, new Wait(converter));
        registerCommon(HistoneType.T_GLOBAL, new GetDayOfWeek(converter));
        registerCommon(HistoneType.T_GLOBAL, new GetDaysInMonth(converter));
        registerCommon(HistoneType.T_GLOBAL, new Require(executor, loader, evaluator, parser, converter));
        registerCommon(HistoneType.T_GLOBAL, new Eval(executor, loader, evaluator, parser, converter));

        registerCommon(HistoneType.T_REGEXP, new Test(converter));

        registerCommon(HistoneType.T_STRING, new StringLength(converter));
        registerCommon(HistoneType.T_STRING, new Case(converter, false));
        registerCommon(HistoneType.T_STRING, new Case(converter, true));
        registerCommon(HistoneType.T_STRING, new StringHtmlEntities(converter));
        registerCommon(HistoneType.T_STRING, new StringCharCodeAt(converter));
        registerCommon(HistoneType.T_STRING, new StringReplace(converter));
        registerCommon(HistoneType.T_STRING, new StringSlice(converter));
        registerCommon(HistoneType.T_STRING, new StringSplit(converter));
        registerCommon(HistoneType.T_STRING, new StringStrip(converter));
        registerCommon(HistoneType.T_STRING, new StringToDate(converter));
        registerCommon(HistoneType.T_STRING, toNumber);

        registerCommon(HistoneType.T_MACRO, new MacroCall(executor, loader, evaluator, parser, converter));
        registerCommon(HistoneType.T_MACRO, new MacroBind(converter));
        registerCommon(HistoneType.T_MACRO, new MacroExtend(converter));
    }

    public Converter getConverter() {
        return converter;
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
    public Optional<Function> getFunc(HistoneType type, String funcName) {
        Function f = userTypes.get(type).get(funcName);
        if (f == null) {
            f = typeMembers.get(type).get(funcName);
            if (f == null) {
                return Optional.empty();
            }
        }
        return Optional.of(f);
    }

    @Override
    public void register(HistoneType type, Function func) {
        userTypes.get(type).put(func.getName(), func);
    }

    @Override
    public CompletableFuture<EvalNode> callFunction(Context context, HistoneType type, String funcName, List<EvalNode> args) {
        final Optional<Function> fRaw = getFunc(type, funcName);
        if (!fRaw.isPresent()) {
            return converter.getValue(null);
        }
        final Function f = fRaw.get();
        if (f.isAsync()) {
            return runAsync(context, args, f);

        }
        return f.execute(context, args);
    }

    protected CompletableFuture<EvalNode> runAsync(Context context, List<EvalNode> args, Function f) {
        return AsyncUtils.initFuture().thenComposeAsync(x -> f.execute(context, args), executor);
    }

    @Override
    public CompletableFuture<EvalNode> callFunction(Context context, EvalNode node, String funcName, List<EvalNode> args) {
        List<HistoneType> additional = node.getAdditionalTypes();
        ListIterator<HistoneType> iterator = additional.listIterator(additional.size());
        while (iterator.hasPrevious()) {
            HistoneType type = iterator.previous();
            if (getFunc(type, funcName).isPresent()) {
                return callFunction(context, type, funcName, args);
            }
        }

        return callFunction(context, node.getType(), funcName, args);
    }
}
