package com.kidscademy.dynamicmedia;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import js.util.Strings;

public class DynamicMediaRequest {
    private static final Pattern PATTERN = Pattern.compile("^((?:.+)[^_]+)(?:_([^_]+))+\\.([^\\.]{3,4})$");

    public static DynamicMediaRequest getInstance(File requestFile) throws IOException {
	String requestPath = requestFile.getAbsolutePath();
	Matcher matcher = PATTERN.matcher(requestPath);
	if (!matcher.find()) {
	    return null;
	}
	String basepath = matcher.group(1);
	String extension = matcher.group(matcher.groupCount());

	DynamicMediaRequest request = new DynamicMediaRequest();
	request.source = new File(Strings.concat(basepath, '.', extension));
	request.target = new File(requestPath);
	request.mediaType = Files.probeContentType(request.source.toPath());

	int expressionsCount = matcher.groupCount() - 2;
	request.transformExpressions = new String[expressionsCount];
	for (int i = 0; i < expressionsCount; ++i) {
	    request.transformExpressions[i] = matcher.group(i + 2);
	}
	return request;
    }

    private File source;
    private File target;
    private String mediaType;
    private String[] transformExpressions;

    public File getSource() {
	return source;
    }

    public File getTarget() {
	return target;
    }

    public String getMediaType() {
	return mediaType;
    }

    public String[] getTransformExpressions() {
	return transformExpressions;
    }
}
