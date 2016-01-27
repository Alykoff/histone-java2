package ru.histone.v2.evaluator.function.global;

import ru.histone.v2.evaluator.EvalUtils;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.LongEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Created by inv3r on 25/01/16.
 */
public class LoadJson extends AbstractFunction {

    public LoadJson(Executor executor) {
        super(executor);
    }

    @Override
    public String getName() {
        return "loadJson";
    }

    @Override
    public CompletableFuture<EvalNode> execute(List<EvalNode> args) throws FunctionExecutionException {
        return CompletableFuture
                .completedFuture(null)
                .thenComposeAsync(x -> {
                    System.out.println("Started on " + new Date() + "ms");
                    LongEvalNode node = (LongEvalNode) args.get(0);
                    try {
                        Thread.sleep(node.getValue());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Wake up after " + node.getValue() + "ms");
                    return EvalUtils.getValue(node.getValue());
                }, executor);
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public boolean isClear() {
        return false;
    }
}
