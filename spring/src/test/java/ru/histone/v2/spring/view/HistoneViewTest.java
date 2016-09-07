package ru.histone.v2.spring.view;

import org.junit.Test;
import org.springframework.http.MediaType;
import ru.histone.v2.evaluator.data.HistoneMacro;
import ru.histone.v2.evaluator.data.HistoneRegex;
import ru.histone.v2.evaluator.node.NullEvalNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Aleksander Melnichnikov
 */
public class HistoneViewTest extends HistoneSpringTestSupport {

    private static final String TEST_GENERAL_CONTENT = "<div>This is test template<div>\n\n<div> I = 10</div>";
    private static final String TEST_REQUIRE_CONTENT = "\n<div>Required Content</div>";
    private static final String TEST_EVAL_CONTENT = "true";
    private static final String TEST_POSTPROCESS_ATAG = "<div>WITHOUT A TAG</div>\n";
    private static final String TEST_POSTPROCESS_PTAG = "<div>WITHOUT P TAG</div>\n";

    @Test
    public void testRenderMergedOutputModel_general() throws Exception {
        mockMvc.perform(get("/testGeneral").contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("testTemplate"))
                .andExpect(content().string(TEST_GENERAL_CONTENT));
    }

    @Test
    public void testRenderMergedOutputModel_require() throws Exception {
        mockMvc.perform(get("/testRequire").contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("testRequire"))
                .andExpect(content().string(TEST_REQUIRE_CONTENT));
    }

    @Test
    public void testRenderMergedOutputModel_eval() throws Exception {
        mockMvc.perform(get("/testEval").contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("testEval"))
                .andExpect(content().string(TEST_EVAL_CONTENT));
    }

    @Test
    public void testPostProcessTemplate_ATag() throws Exception {
        mockMvc.perform(get("/testPostProcessA").contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("postprocessor/testPostProcessATag"))
                .andExpect(content().string(TEST_POSTPROCESS_ATAG));
    }

    @Test
    public void testPostProcessTemplate_PTag() throws Exception {
        mockMvc.perform(get("/testPostProcessP").contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("postprocessor/testPostProcessPTag"))
                .andExpect(content().string(TEST_POSTPROCESS_PTAG));
    }


    @Test
    public void testFilterHistoneParams() throws Exception {
        HistoneView templateView = new HistoneView();
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("Integer", 1);
        parameterMap.put("String", "Param");
        parameterMap.put("Boolean", Boolean.TRUE);
        parameterMap.put("Double", 1.2D);
        parameterMap.put("Long", 1L);
        parameterMap.put("Float", 1.3F);
        parameterMap.put("Map", new HashMap<>());
        parameterMap.put("HistoneRegex", new HistoneRegex(false, false, false, Pattern.compile("")));
        parameterMap.put("HistoneMacro", new HistoneMacro(new NullEvalNode()));
        parameterMap.put("EvalNode", new NullEvalNode());
        parameterMap.put("NotSupportedArg", new ArrayList<>());
        parameterMap = templateView.filterHistoneParams(parameterMap);
        assertNull(parameterMap.get("NotSupportedArg"));
        assertThat(parameterMap.size(), is(10));
    }

}