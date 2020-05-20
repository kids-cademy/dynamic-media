package com.kidscademy.dynamicmedia;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import js.tiny.container.servlet.AppServlet;
import js.tiny.container.servlet.RequestContext;

public class FileNotFoundServlet extends AppServlet {
    /** Java serialization version. */
    private static final long serialVersionUID = 8163824104594079557L;

    @Override
    protected void handleRequest(RequestContext context) throws IOException, ServletException {
	HttpServletRequest request = context.getRequest();
	HttpServletResponse response = context.getResponse();
	String requestURI = (String) request.getAttribute("javax.servlet.error.request_uri");
	request.getRequestDispatcher("/processor" + requestURI).forward(request, response);
    }
}
