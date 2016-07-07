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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import static ru.histone.v2.rtti.HistoneType.*;

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
        registerForAlltypes(new ToNumber());
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

        registerCommon(T_NUMBER, new ToAbs());
        registerCommon(T_NUMBER, new ToCeil());
        registerCommon(T_NUMBER, new ToChar());
        registerCommon(T_NUMBER, new ToDate());
        registerCommon(T_NUMBER, new ToFloor());
        registerCommon(T_NUMBER, new ToRound());
        registerCommon(T_NUMBER, new ToFixed());

        registerCommon(T_ARRAY, new ArrayLength());
        registerCommon(T_ARRAY, new Keys(true));
        registerCommon(T_ARRAY, new Keys(false));
        registerCommon(T_ARRAY, new Reverse());
        registerCommon(T_ARRAY, new ArrayMap());
        registerCommon(T_ARRAY, new ArrayFilter());
        registerCommon(T_ARRAY, new ArraySome());
        registerCommon(T_ARRAY, new ArrayEvery());
        registerCommon(T_ARRAY, new ArrayJoin());
        registerCommon(T_ARRAY, new ArrayLast());
        registerCommon(T_ARRAY, new ArrayFirst());
        registerCommon(T_ARRAY, new ArrayChunk());
        registerCommon(T_ARRAY, new ArrayReduce());
        registerCommon(T_ARRAY, new ArrayFind());
        registerCommon(T_ARRAY, new ArrayForEach());
        registerCommon(T_ARRAY, new ArrayGroup());
        registerCommon(T_ARRAY, new ArraySort());
        registerCommon(T_ARRAY, new ArraySlice());
        registerCommon(T_ARRAY, new ArrayHtmlEntities());
        registerCommon(T_ARRAY, new ArrayHas());
        registerCommon(T_ARRAY, new ArrayToDate());

        registerCommon(T_GLOBAL, new Range());
        registerCommon(T_GLOBAL, new LoadJson(executor, loader, evaluator, parser));
        registerCommon(T_GLOBAL, new LoadText(executor, loader, evaluator, parser));
        registerCommon(T_GLOBAL, new AsyncLoadText(executor, loader, evaluator, parser));
        registerCommon(T_GLOBAL, new AsyncLoadJson(executor, loader, evaluator, parser));
        registerCommon(T_GLOBAL, new GetBaseUri());
        registerCommon(T_GLOBAL, new GetUniqueId());
        registerCommon(T_GLOBAL, new ResolveURI());
        registerCommon(T_GLOBAL, new GetWeekDayName(true));
        registerCommon(T_GLOBAL, new GetWeekDayName(false));
        registerCommon(T_GLOBAL, new GetMonthName(true));
        registerCommon(T_GLOBAL, new GetMonthName(false));
        registerCommon(T_GLOBAL, new GetRand());
        registerCommon(T_GLOBAL, new GetMinMax(false));
        registerCommon(T_GLOBAL, new GetMinMax(true));
        registerCommon(T_GLOBAL, new GetDate());
        registerCommon(T_GLOBAL, new GetTimeStamp());
        registerCommon(T_GLOBAL, new Wait());
        registerCommon(T_GLOBAL, new GetDayOfWeek());
        registerCommon(T_GLOBAL, new GetDaysInMonth());
        registerCommon(T_GLOBAL, new Require(executor, loader, evaluator, parser));
        registerCommon(T_GLOBAL, new GetMethod());
        registerCommon(T_GLOBAL, new Eval(executor, loader, evaluator, parser));

        registerCommon(T_REGEXP, new Test());

        registerCommon(T_STRING, new StringLength());
        registerCommon(T_STRING, new Case(false));
        registerCommon(T_STRING, new Case(true));
        registerCommon(T_STRING, new StringHtmlEntities());
        registerCommon(T_STRING, new StringCharCodeAt());
        registerCommon(T_STRING, new StringReplace());
        registerCommon(T_STRING, new StringSlice());
        registerCommon(T_STRING, new StringSplit());
        registerCommon(T_STRING, new StringStrip());
        registerCommon(T_STRING, new StringToDate());

        registerCommon(T_MACRO, new MacroCall(executor, loader, evaluator, parser));
        registerCommon(T_MACRO, new MacroBind());
        registerCommon(T_MACRO, new MacroExtend());
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
//                throw new FunctionExecutionException("Couldn't find function '" + funcName + "' for type " + type);
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
        return callFunction(context, node.getType(), funcName, args);
    }

    public Executor getExecutor() {
        return executor;
    }
}
