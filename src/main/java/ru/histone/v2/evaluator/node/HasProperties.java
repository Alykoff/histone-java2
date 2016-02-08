package ru.histone.v2.evaluator.node;

import ru.histone.HistoneException;

/**
 * @author gali.alykoff on 08/02/16.
 */
public interface HasProperties {
    EvalNode getProperty(Object propertyName) throws HistoneException;
}
