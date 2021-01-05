package com.kidscademy.dynamicmedia.unit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.kidscademy.dynamicmedia.ResizeTransformer;
import com.kidscademy.dynamicmedia.Transformer;
import com.kidscademy.process.ImageMagickProcess;

@RunWith(MockitoJUnitRunner.class)
public class ResizeTransformerTest {
    @Mock
    private ImageMagickProcess imagick;

    private Transformer transformer;

    @Before
    public void beforeTest() {
	transformer = new ResizeTransformer(imagick);
    }

    @Test
    public void exec() throws IOException {
	Pattern pattern = transformer.pattern();
	Matcher matcher = pattern.matcher("96x96");
	assertThat(matcher.find(), equalTo(true));

	File source = new File("src/test/resources/media/picture.jpg");
	File target = new File("src/test/resources/media/picture_96x96.jpg");
	transformer.exec(source, target, matcher);

	ArgumentCaptor<String> commandCaptor = ArgumentCaptor.forClass(String.class);
	verify(imagick, times(1)).exec(commandCaptor.capture());
	assertThat(commandCaptor.getValue(),
		equalTo(String.format("%s -resize 96x96 %s", source.getAbsolutePath(), target.getAbsolutePath())));
    }

    @Test(expected = IllegalArgumentException.class)
    public void exec_nullMatcher() throws IOException {
	File source = new File("src/test/resources/media/picture.jpg");
	File target = new File("src/test/resources/media/picture_96x96.jpg");
	transformer.exec(source, target, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exec_nullSource() throws IOException {
	Pattern pattern = transformer.pattern();
	Matcher matcher = pattern.matcher("96x96");
	assertThat(matcher.find(), equalTo(true));

	File target = new File("src/test/resources/media/picture_96x96.jpg");
	transformer.exec(null, target, matcher);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exec_notExistingSource() throws IOException {
	Pattern pattern = transformer.pattern();
	Matcher matcher = pattern.matcher("96x96");
	assertThat(matcher.find(), equalTo(true));

	File source = new File("src/test/resources/media/fake.jpg");
	File target = new File("src/test/resources/media/picture_96x96.jpg");
	transformer.exec(source, target, matcher);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exec_nullTarget() throws IOException {
	Pattern pattern = transformer.pattern();
	Matcher matcher = pattern.matcher("96x96");
	assertThat(matcher.find(), equalTo(true));

	File source = new File("src/test/resources/media/picture.jpg");
	transformer.exec(source, null, matcher);
    }
}
