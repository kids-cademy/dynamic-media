package com.kidscademy.dynamicmedia;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.kidscademy.process.Command;
import com.kidscademy.process.ImageMagickProcess;

import js.tiny.container.annotation.TestConstructor;
import js.util.Params;

public class ResizeTransformer implements Transformer {
    private final ImageMagickProcess imagick;

    public ResizeTransformer() {
	this(new ImageMagickProcess());
    }

    @TestConstructor
    public ResizeTransformer(ImageMagickProcess imagick) {
	this.imagick = imagick;
    }

    @Override
    public Pattern pattern() {
	return PATTERN;
    }

    private static final Pattern PATTERN = Pattern.compile("(\\d+)?x(\\d+)?");

    @Override
    public void exec(File source, File target, Matcher matcher) throws IOException {
	Params.notNull(source, "Media source file");
	Params.isFile(source, "Media source file");
	Params.notNull(target, "Media target file");
	Params.notNull(matcher, "Expression matcher");

	String width = matcher.group(1);
	String height = matcher.group(2);
	imagick.exec(Command.format("${source} -resize ${width}x${height} ${target}", source, width, height, target));
    }
}
