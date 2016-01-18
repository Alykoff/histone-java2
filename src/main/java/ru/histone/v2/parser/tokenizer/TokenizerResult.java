package ru.histone.v2.parser.tokenizer;

import org.apache.commons.collections.CollectionUtils;
import ru.histone.tokenizer.Token;

import java.util.Arrays;
import java.util.List;

/**
 * Created by alexey.nevinsky on 12.01.2016.
 */
public class TokenizerResult {
    private List<Token> tokens;
    private boolean found = false;

    public TokenizerResult(Token... tokens) {
        found = tokens != null;
        if (tokens != null) {
            this.tokens = Arrays.asList(tokens);
        }
    }

    public TokenizerResult(List<Token> tokens) {
        found = CollectionUtils.isNotEmpty(tokens);
        if (tokens != null) {
            this.tokens = tokens;
        }
    }

    public TokenizerResult(boolean found) {
        this.found = found;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public boolean isFound() {
        return found;
    }

    public Token first() {
        return tokens.get(0);
    }

    public String firstValue() {
        return tokens.get(0).getValue();
    }
}
