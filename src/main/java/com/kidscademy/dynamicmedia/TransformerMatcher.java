package com.kidscademy.dynamicmedia;

import java.util.regex.Matcher;

public class TransformerMatcher {
    private final Transformer transformer;
    private final Matcher matcher;

    public TransformerMatcher(Transformer transformer, Matcher matcher) {
	this.transformer = transformer;
	this.matcher = matcher;
    }

    public Transformer getTransformer() {
	return transformer;
    }

    public Matcher getMatcher() {
	return matcher;
    }
}
