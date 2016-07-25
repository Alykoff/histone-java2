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

import org.apache.commons.lang3.NotImplementedException;
import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
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

    public RunTimeTypeInfo(Executor executor, HistoneResourceLoader loader, Evaluator evaluator, Parser parser) {
        this.executor = executor;
        this.loader = loader;
        this.evaluator = evaluator;
        this.parser = parser;

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
        registerForAlltypes(new IsRegExp());
        registerForAlltypes(new IsString());
        registerForAlltypes(new IsArray());
        registerForAlltypes(new IsMacro());
        registerForAlltypes(new ToMacro());
        registerForAlltypes(new ToArray());
        registerForAlltypes(new GetFunction());
        registerForAlltypes(new CallFunction());
        registerForAlltypes(new HasMethod());
        registerForAlltypes(new IsDate());
        registerForAlltypes(new GetMethod());

        ToNumber toNumber = new ToNumber();

        registerCommon(HistoneType.T_BOOLEAN, toNumber);

        registerCommon(HistoneType.T_NUMBER, new ToAbs());
        registerCommon(HistoneType.T_NUMBER, new ToCeil());
        registerCommon(HistoneType.T_NUMBER, new ToChar());
        registerCommon(HistoneType.T_NUMBER, new ToDate());
        registerCommon(HistoneType.T_NUMBER, new ToFloor());
        registerCommon(HistoneType.T_NUMBER, new ToRound());
        registerCommon(HistoneType.T_NUMBER, new ToFixed());
        registerCommon(HistoneType.T_NUMBER, toNumber);

        registerCommon(HistoneType.T_ARRAY, new ArrayLength());
        registerCommon(HistoneType.T_ARRAY, new Keys(true));
        registerCommon(HistoneType.T_ARRAY, new Keys(false));
        registerCommon(HistoneType.T_ARRAY, new Reverse());
        registerCommon(HistoneType.T_ARRAY, new ArrayMap());
        registerCommon(HistoneType.T_ARRAY, new ArrayFilter());
        registerCommon(HistoneType.T_ARRAY, new ArraySome());
        registerCommon(HistoneType.T_ARRAY, new ArrayEvery());
        registerCommon(HistoneType.T_ARRAY, new ArrayJoin());
        registerCommon(HistoneType.T_ARRAY, new ArrayLast());
        registerCommon(HistoneType.T_ARRAY, new ArrayFirst());
        registerCommon(HistoneType.T_ARRAY, new ArrayChunk());
        registerCommon(HistoneType.T_ARRAY, new ArrayReduce());
        registerCommon(HistoneType.T_ARRAY, new ArrayFind());
        registerCommon(HistoneType.T_ARRAY, new ArrayForEach());
        registerCommon(HistoneType.T_ARRAY, new ArrayGroup());
        registerCommon(HistoneType.T_ARRAY, new ArraySort());
        registerCommon(HistoneType.T_ARRAY, new ArraySlice());
        registerCommon(HistoneType.T_ARRAY, new ArrayHtmlEntities());
        registerCommon(HistoneType.T_ARRAY, new ArrayHas());
        registerCommon(HistoneType.T_ARRAY, new ArrayToDate());

        registerCommon(HistoneType.T_DATE, toNumber);

        registerCommon(HistoneType.T_GLOBAL, new Range());
        registerCommon(HistoneType.T_GLOBAL, new LoadJson(executor, loader, evaluator, parser));
        registerCommon(HistoneType.T_GLOBAL, new LoadText(executor, loader, evaluator, parser));
        registerCommon(HistoneType.T_GLOBAL, new AsyncLoadText(executor, loader, evaluator, parser));
        registerCommon(HistoneType.T_GLOBAL, new AsyncLoadJson(executor, loader, evaluator, parser));
        registerCommon(HistoneType.T_GLOBAL, new GetBaseUri());
        registerCommon(HistoneType.T_GLOBAL, new GetUniqueId());
        registerCommon(HistoneType.T_GLOBAL, new ResolveURI());
        registerCommon(HistoneType.T_GLOBAL, new GetWeekDayName(true));
        registerCommon(HistoneType.T_GLOBAL, new GetWeekDayName(false));
        registerCommon(HistoneType.T_GLOBAL, new GetMonthName(true));
        registerCommon(HistoneType.T_GLOBAL, new GetMonthName(false));
        registerCommon(HistoneType.T_GLOBAL, new GetRand());
        registerCommon(HistoneType.T_GLOBAL, new GetMinMax(false));
        registerCommon(HistoneType.T_GLOBAL, new GetMinMax(true));
        registerCommon(HistoneType.T_GLOBAL, new GetDate());
        registerCommon(HistoneType.T_GLOBAL, new Wait());
        registerCommon(HistoneType.T_GLOBAL, new GetDayOfWeek());
        registerCommon(HistoneType.T_GLOBAL, new GetDaysInMonth());
        registerCommon(HistoneType.T_GLOBAL, new Require(executor, loader, evaluator, parser));
        registerCommon(HistoneType.T_GLOBAL, new Eval(executor, loader, evaluator, parser));

        registerCommon(HistoneType.T_REGEXP, new Test());

        registerCommon(HistoneType.T_STRING, new StringLength());
        registerCommon(HistoneType.T_STRING, new Case(false));
        registerCommon(HistoneType.T_STRING, new Case(true));
        registerCommon(HistoneType.T_STRING, new StringHtmlEntities());
        registerCommon(HistoneType.T_STRING, new StringCharCodeAt());
        registerCommon(HistoneType.T_STRING, new StringReplace());
        registerCommon(HistoneType.T_STRING, new StringSlice());
        registerCommon(HistoneType.T_STRING, new StringSplit());
        registerCommon(HistoneType.T_STRING, new StringStrip());
        registerCommon(HistoneType.T_STRING, new StringToDate());
        registerCommon(HistoneType.T_STRING, toNumber);

        registerCommon(HistoneType.T_MACRO, new MacroCall(executor, loader, evaluator, parser));
        registerCommon(HistoneType.T_MACRO, new MacroBind());
        registerCommon(HistoneType.T_MACRO, new MacroExtend());
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
    public void unregistered(HistoneType type, String funcName) {
        throw new NotImplementedException("");
    }

    @Override
    public CompletableFuture<EvalNode> callFunction(Context context, HistoneType type, String funcName, List<EvalNode> args) {
        final Optional<Function> fRaw = getFunc(type, funcName);
        if (!fRaw.isPresent()) {
            return EvalUtils.getValue(null);
        }
        final Function f = fRaw.get();
        if (f.isAsync()) {
            return runAsync(context, args, f);

        }
        return f.execute(context, args);
    }

    protected CompletableFuture<EvalNode> runAsync(Context context, List<EvalNode> args, Function f) {
        // TODO it should be more compact
        return AsyncUtils.initFuture()
                .thenComposeAsync((x) -> f.execute(context, args), executor);
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

    public Executor getExecutor() {
        return executor;
    }
}
