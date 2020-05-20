package com.kidscademy.dynamicmedia;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletResponse;

import js.log.Log;
import js.log.LogFactory;
import js.tiny.container.annotation.TestConstructor;
import js.tiny.container.http.HttpHeader;
import js.tiny.container.servlet.AppServlet;
import js.tiny.container.servlet.RequestContext;
import js.util.Files;

public class DynamicMediaServlet extends AppServlet {
    /** Java serialization version. */
    private static final long serialVersionUID = 4594321485526794107L;

    private static final Log log = LogFactory.getLog(DynamicMediaServlet.class);

    private File mediaDir;
    private TransformerFactory transformerFactory;

    public DynamicMediaServlet() {
	super();
	log.trace("DynamicMediaServlet()");
    }

    @TestConstructor
    public DynamicMediaServlet(File mediaDir, TransformerFactory transformerFactory) {
	super();
	log.trace("DynamicMediaServlet(File,TransformerFactory)");
	this.mediaDir = mediaDir;
	this.transformerFactory = transformerFactory;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
	super.init(config);
	log.trace("init(ServletConfig)");

	// do not use config.getInitParameter("media.repository.path");
	// it is servlet specific and returns <init-param> from servlet descriptor
	// web.xml
	String path = config.getServletContext().getInitParameter("media.repository.path");
	if (path == null) {
	    log.fatal("Missing context property for media repository path. Servlet |%s| permanently unvailable.",
		    config.getServletName());
	    throw new UnavailableException("Missing context property for media repository path.");
	}

	mediaDir = new File(path);
	transformerFactory = new TransformerFactory();
    }

    @Override
    protected void handleRequest(RequestContext context) throws IOException, ServletException {
	HttpServletResponse response = context.getResponse();
	String requestURI = context.getRequestURI();

	String mediaPath = mediaPath(requestURI);
	if (mediaPath == null) {
	    log.warn("Invalid dynamic media request: |%s|.", requestURI);
	    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	    return;
	}

	File requestFile = new File(mediaDir, mediaPath);

	DynamicMediaRequest mediaRequest = DynamicMediaRequest.getInstance(requestFile);
	if (mediaRequest == null) {
	    log.warn("Unknwon dynamic media pattern: |%s|.", requestURI);
	    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	    return;
	}

	if (!mediaRequest.getSource().exists()) {
	    log.warn("Source media not found. Cannot generate requested resource: |%s|.", requestURI);
	    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
	    return;
	}

	File source = mediaRequest.getSource();
	File target = mediaRequest.getTarget();
	for (String transformExpression : mediaRequest.getTransformExpressions()) {
	    TransformerMatcher transformerMatcher = transformerFactory.getTransformer(transformExpression);
	    if (transformerMatcher == null) {
		log.warn("Unknown transform expression |%s|.", transformExpression);
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return;
	    }
	    transformerMatcher.getTransformer().exec(source, target, transformerMatcher.getMatcher());
	    source = target;
	}

	response.setStatus(HttpServletResponse.SC_OK);

	response.setHeader(HttpHeader.CACHE_CONTROL, HttpHeader.NO_CACHE);
	response.addHeader(HttpHeader.CACHE_CONTROL, HttpHeader.NO_STORE);
	response.setHeader(HttpHeader.PRAGMA, HttpHeader.NO_CACHE);
	response.setDateHeader(HttpHeader.EXPIRES, 0);

	response.setContentType(mediaRequest.getMediaType());
	response.setContentLength((int) mediaRequest.getTarget().length());

	Files.copy(mediaRequest.getTarget(), response.getOutputStream());
    }

    public File getMediaDir() {
	return mediaDir;
    }

    public TransformerFactory getTransformerFactory() {
	return transformerFactory;
    }

    private static String mediaPath(String requestURI) {
	// request URI pattern: /context/servlet/media-path

	// context
	int pathSeparatorPosition = requestURI.indexOf('/');
	if (pathSeparatorPosition == -1) {
	    log.warn("Missing leading path separator from request URI |%s|.", requestURI);
	    return null;
	}

	// servlet
	pathSeparatorPosition = requestURI.indexOf('/', pathSeparatorPosition + 1);
	if (pathSeparatorPosition == -1) {
	    log.warn("Missing path separator for servlet name, from request URI |%s|.", requestURI);
	    return null;
	}

	// media path
	pathSeparatorPosition = requestURI.indexOf('/', pathSeparatorPosition + 1);
	if (pathSeparatorPosition == -1) {
	    log.warn("Missing path separator for media path, from request URI |%s|.", requestURI);
	    return null;
	}
	return requestURI.substring(pathSeparatorPosition);
    }
}
