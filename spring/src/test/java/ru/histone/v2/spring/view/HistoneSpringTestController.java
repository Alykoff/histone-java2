package ru.histone.v2.spring.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Aleksander Melnichnikov
 */
@Controller
public class HistoneSpringTestController {

    @RequestMapping(value = "/testGeneral", method = RequestMethod.GET)
    public String getTemplate() {
        return "testTemplate";
    }

    @RequestMapping(value = "/testRequire", method = RequestMethod.GET)
    public String getRequireTemplate() {
        return "testRequire";
    }

    @RequestMapping(value = "/testEval", method = RequestMethod.GET)
    public String getEvalTemplate() {
        return "testEval";
    }

    @RequestMapping(value = "/testPostProcessA", method = RequestMethod.GET)
    public String getPostProcessA() {
        return "postprocessor/testPostProcessATag";
    }

    @RequestMapping(value = "/testPostProcessP", method = RequestMethod.GET)
    public String getPostProcessP() {
        return "postprocessor/testPostProcessPTag";
    }


}
