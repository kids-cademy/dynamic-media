package com.kidscademy.dynamicmedia;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import js.util.Params;

public class TransformerFactory {
    private static final Map<Pattern, Transformer> TRANSFORMERS = new HashMap<>();
    static {
	registerTransfomer(new ResizeTransformer());
    }

    private static void registerTransfomer(Transformer transformer) {
	TRANSFORMERS.put(transformer.pattern(), transformer);
    }

    public TransformerMatcher getTransformer(String expression) {
	Params.notNull(expression, "Transformer expression");
	for (Pattern pattern : TRANSFORMERS.keySet()) {
	    Matcher matcher = pattern.matcher(expression);
	    if (matcher.find()) {
		return new TransformerMatcher(TRANSFORMERS.get(pattern), matcher);
	    }
	}
	return null;
    }
}
