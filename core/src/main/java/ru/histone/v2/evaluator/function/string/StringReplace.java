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

package ru.histone.v2.evaluator.function.string;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.data.HistoneRegex;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.BooleanEvalNode;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.RttiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Gali Alykoff
 */
public class StringReplace extends AbstractFunction {
    public static final String NAME = "replace";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        if (args.size() == 1) {
            return CompletableFuture.completedFuture(args.get(0));
        }

        String str = getValue(args, 0);

        EvalNode searchNode = args.get(1);

        if (args.size() > 2) {
            EvalNode replaceNode = args.get(2);
            Matcher matcher;
            boolean isGlobal = false;
            if (searchNode.getType() == HistoneType.T_REGEXP) {
                HistoneRegex regex = (HistoneRegex) searchNode.getValue();
                isGlobal = regex.isGlobal();
                matcher = regex.getPattern().matcher(str);
            } else {
                isGlobal = true;
                matcher = Pattern.compile((String) RttiUtils.callToString(context, searchNode).join().getValue()).matcher(str);
            }


            if (replaceNode.getType() != HistoneType.T_MACRO) {
                String replaceStr = (String) RttiUtils.callToString(context, replaceNode).join().getValue();
                if (isGlobal) {
                    return EvalUtils.getValue(matcher.replaceAll(replaceStr));
                } else {
                    return EvalUtils.getValue(matcher.replaceFirst(replaceStr));
                }
            } else {
                int lastIndex = 0;
                StringBuilder sb = new StringBuilder();
                int count = isGlobal ? Integer.MAX_VALUE : 1;
                int i = 0;
                while (matcher.find(lastIndex) && i < count) {
                    if (matcher.start() > lastIndex) {
                        sb.append(str.substring(lastIndex, matcher.start()));
                    }
                    String found = matcher.group();
                    List<EvalNode> macroArgs = new ArrayList<>();
                    macroArgs.add(replaceNode);
                    macroArgs.add(new BooleanEvalNode(true));
                    for (int gIndex = 0; gIndex <= matcher.groupCount(); gIndex++) {
                        macroArgs.add(EvalUtils.createEvalNode(matcher.group(gIndex)));
                    }
                    EvalNode val = context.macroCall(macroArgs).join();
                    String macroResult = (String) RttiUtils.callToString(context, val).join().getValue();
                    sb.append(macroResult);
                    lastIndex = matcher.start() + found.length();
                    i++;
                }
                if (lastIndex < str.length()) {
                    sb.append(str.substring(lastIndex, str.length()));
                }

                return EvalUtils.getValue(sb.toString());
            }
        } else {
            if (searchNode.getType() == HistoneType.T_REGEXP) {
                HistoneRegex regex = (HistoneRegex) searchNode.getValue();
                Matcher matcher = regex.getPattern().matcher(str);
                if (regex.isGlobal()) {
                    return EvalUtils.getValue(matcher.replaceAll(""));
                } else {
                    return EvalUtils.getValue(matcher.replaceFirst(""));
                }
            }

            String replaced = str.replaceAll((String) RttiUtils.callToString(context, searchNode).join().getValue(), "");
            return EvalUtils.getValue(replaced);
        }
    }
}
