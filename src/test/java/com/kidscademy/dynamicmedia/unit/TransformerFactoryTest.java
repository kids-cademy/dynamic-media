package com.kidscademy.dynamicmedia.unit;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.kidscademy.dynamicmedia.ResizeTransformer;
import com.kidscademy.dynamicmedia.TransformerFactory;
import com.kidscademy.dynamicmedia.TransformerMatcher;

public class TransformerFactoryTest {
    private TransformerFactory factory;

    @Before
    public void beforeTest() {
	factory = new TransformerFactory();
    }

    @Test
    public void getTransformer() {
	TransformerMatcher matcher = factory.getTransformer("96x96");
	assertThat(matcher, notNullValue());
	assertThat(matcher.getTransformer(), is(instanceOf(ResizeTransformer.class)));
	assertThat(matcher.getMatcher(), notNullValue());
    }

    @Test
    public void getTransformer_InvalidExpression() {
	TransformerMatcher matcher = factory.getTransformer("96+96");
	assertThat(matcher, nullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getTransformer_NullExpression() {
	TransformerMatcher matcher = factory.getTransformer(null);
	assertThat(matcher, nullValue());
    }
}
