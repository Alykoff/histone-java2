package ru.histone.v2.evaluator.function.number;

import ru.histone.v2.evaluator.Context;
import ru.histone.v2.evaluator.Converter;
import ru.histone.v2.evaluator.function.AbstractFunction;
import ru.histone.v2.evaluator.node.DoubleEvalNode;
import ru.histone.v2.evaluator.node.EvalNode;
import ru.histone.v2.evaluator.node.LongEvalNode;
import ru.histone.v2.exceptions.FunctionExecutionException;
import ru.histone.v2.rtti.HistoneType;
import ru.histone.v2.utils.DateUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Aleksander Melnichnikov
 */
public class ToDate extends AbstractFunction {
    public ToDate(Converter converter) {
        super(converter);
    }

    @Override
    public String getName() {
        return "toDate";
    }

    @Override
    public CompletableFuture<EvalNode> execute(Context context, List<EvalNode> args) throws FunctionExecutionException {
        if (args.size() < 1) {
            return converter.getValue(null);
        }

        long value;
        if (args.get(0) instanceof DoubleEvalNode) {
            double fpVal = ((DoubleEvalNode) args.get(0)).getValue();
            value = fpVal < 0 ? (long) Math.ceil(fpVal) : (long) Math.floor(fpVal);
        } else {
            value = ((LongEvalNode) args.get(0)).getValue();
        }
        LocalDateTime dateTime = LocalDateTime.ofInstant(new Date(value).toInstant(),
                ZoneId.systemDefault());

        if (args.size() > 1 && args.get(1).getType() == HistoneType.T_STRING) {
            final String offset = (String) args.get(1).getValue();
            dateTime = DateUtils.applyOffset(dateTime, offset);
        }

        EvalNode node = converter.createEvalNode(DateUtils.createMapFromDate(converter, dateTime), true);
        return CompletableFuture.completedFuture(node);
    }
}
