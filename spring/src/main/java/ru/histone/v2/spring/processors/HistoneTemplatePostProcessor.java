package ru.histone.v2.spring.processors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Aleksander Melnichnikov
 */
public interface HistoneTemplatePostProcessor {

    String postProcess(String template, HttpServletRequest request, HttpServletResponse response);

}
