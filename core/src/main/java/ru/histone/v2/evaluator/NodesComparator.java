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

package ru.histone.v2.evaluator;

import ru.histone.v2.evaluator.global.BooleanEvalNodeComparator;
import ru.histone.v2.evaluator.global.NumberComparator;
import ru.histone.v2.evaluator.global.StringEvalNodeLenComparator;
import ru.histone.v2.evaluator.global.StringEvalNodeStrongComparator;
import ru.histone.v2.evaluator.node.BooleanEvalNode;
import ru.histone.v2.evaluator.node.DateEvalNode;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.StringEvalNode;
import ru.histone.v2.parser.node.AstType;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.AsyncUtils;
import ru.histone.v2.utils.DateUtils;
import ru.histone.v2.utils.RttiUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static ru.histone.v2.evaluator.EvalUtils.*;

/**
 * @author Alexey Nevinsky
 */
public class NodesComparator {

    protected static final Comparator<Number> NUMBER_COMPARATOR = new NumberComparator();
    protected static final Comparator<StringEvalNode> STRING_EVAL_NODE_LEN_COMPARATOR = new StringEvalNodeLenComparator();
    protected static final Comparator<StringEvalNode> STRING_EVAL_NODE_STRONG_COMPARATOR = new StringEvalNodeStrongComparator();
    protected static final Comparator<BooleanEvalNode> BOOLEAN_EVAL_NODE_COMPARATOR = new BooleanEvalNodeComparator();

    public CompletableFuture<EvalNode> processRelation(CompletableFuture<EvalNode> leftFuture,
                                                       CompletableFuture<EvalNode> rightFuture,
                                                       AstType type, Context context) {
        final CompletableFuture<List<EvalNode>> leftRightDone = AsyncUtils.sequence(leftFuture, rightFuture);
        return leftRightDone.thenCompose(evalNodeList -> {
            final EvalNode left = evalNodeList.get(0);
            final EvalNode right = evalNodeList.get(1);
            final CompletableFuture<Integer> compareResultRaw = compareNodes(left, right, context, getComparator(type));

            return compareResultRaw.thenApply(compareResult -> processRelationComparatorHelper(type, compareResult));
        });
    }

    private Comparator<StringEvalNode> getComparator(AstType type) {
        if (type == AstType.AST_EQ || type == AstType.AST_NEQ) {
            return STRING_EVAL_NODE_STRONG_COMPARATOR;
        }
        return STRING_EVAL_NODE_LEN_COMPARATOR;
    }

    private CompletableFuture<Integer> compareNodes(EvalNode left, EvalNode right, Context context,
                                                    Comparator<StringEvalNode> stringNodeComparator) {
        final CompletableFuture<Integer> result;
        if (isStringNode(left) && isNumberNode(right)) {
            final StringEvalNode stringLeft = (StringEvalNode) left;
            if (isNumeric(stringLeft)) {
                result = processRelationNumberHelper(left, right);
            } else {
                result = processRelationToString(stringLeft, right, context, stringNodeComparator, false);
            }
        } else if (isNumberNode(left) && isStringNode(right)) {
            final StringEvalNode stringRight = (StringEvalNode) right;
            if (isNumeric(stringRight)) {
                result = processRelationNumberHelper(left, right);
            } else {
                result = processRelationToString(stringRight, left, context, stringNodeComparator, true);
            }
        } else if (left.hasAdditionalType(HistoneType.T_DATE) && right.hasAdditionalType(HistoneType.T_DATE)) {
            LocalDateTime leftValue = DateUtils.createDate(((DateEvalNode) left).getValue());
            LocalDateTime rightValue = DateUtils.createDate(((DateEvalNode) right).getValue());
            //leftValue and rightValue always has value, bcz functions which create DateEvalNode checks it
            // or return EmptyEvalNode
            result = CompletableFuture.completedFuture(leftValue.compareTo(rightValue));
        } else if (!isNumberNode(left) || !isNumberNode(right)) {
            if (isStringNode(left) && isStringNode(right)) {
                result = processRelationStringHelper(left, right, stringNodeComparator);
            } else {
                result = processRelationBooleanHelper(left, right, context);
            }
        } else {
            result = processRelationNumberHelper(left, right);
        }
        return result;
    }

    private CompletableFuture<Integer> processRelationToString(StringEvalNode left, EvalNode right, Context context,
                                                               Comparator<StringEvalNode> stringNodeComparator, boolean isInvert
    ) {
        final CompletableFuture<EvalNode> rightFuture = RttiUtils.callToString(context, right);
        final int inverter = isInvert ? -1 : 1;
        return rightFuture.thenApply(stringRight ->
                inverter * stringNodeComparator.compare(left, (StringEvalNode) stringRight)
        );
    }

    private CompletableFuture<Integer> processRelationStringHelper(
            EvalNode left, EvalNode right, Comparator<StringEvalNode> stringNodeComparator
    ) {
        final StringEvalNode stringRight = (StringEvalNode) right;
        final StringEvalNode stringLeft = (StringEvalNode) left;
        return completedFuture(
                stringNodeComparator.compare(stringLeft, stringRight)
        );
    }

    private CompletableFuture<Integer> processRelationNumberHelper(
            EvalNode left, EvalNode right
    ) {
        final Number rightValue = getNumberValue(right);
        final Number leftValue = getNumberValue(left);
        return completedFuture(
                NUMBER_COMPARATOR.compare(leftValue, rightValue)
        );
    }

    private CompletableFuture<Integer> processRelationBooleanHelper(
            EvalNode left, EvalNode right, Context context
    ) {
        final CompletableFuture<EvalNode> leftF = RttiUtils.callToBoolean(context, left);
        final CompletableFuture<EvalNode> rightF = RttiUtils.callToBoolean(context, right);

        return leftF.thenCompose(leftBooleanRaw -> rightF.thenApply(rightBooleanRaw ->
                BOOLEAN_EVAL_NODE_COMPARATOR.compare(
                        (BooleanEvalNode) leftBooleanRaw, (BooleanEvalNode) rightBooleanRaw
                )
        ));
    }

    private EvalNode processRelationComparatorHelper(AstType astType, int compareResult) {
        switch (astType) {
            case AST_LT:
                return new BooleanEvalNode(compareResult < 0);
            case AST_GT:
                return new BooleanEvalNode(compareResult > 0);
            case AST_LE:
                return new BooleanEvalNode(compareResult <= 0);
            case AST_GE:
                return new BooleanEvalNode(compareResult >= 0);
            case AST_EQ:
                return new BooleanEvalNode(compareResult == 0);
            case AST_NEQ:
                return new BooleanEvalNode(compareResult != 0);
        }
        throw new RuntimeException("Unknown type for this case");
    }

}
