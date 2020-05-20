package com.kidscademy.dynamicmedia.unit;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.io.FileMatchers.aFileWithSize;
import static org.hamcrest.io.FileMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.regex.Matcher;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.kidscademy.dynamicmedia.DynamicMediaServlet;
import com.kidscademy.dynamicmedia.Transformer;
import com.kidscademy.dynamicmedia.TransformerFactory;
import com.kidscademy.dynamicmedia.TransformerMatcher;

import js.tiny.container.ContainerSPI;
import js.tiny.container.servlet.RequestContext;
import js.tiny.container.servlet.TinyContainer;
import js.util.Classes;
import js.util.Files;

@RunWith(MockitoJUnitRunner.class)
public class DynamicMediaServletTest {
    @Mock
    private TransformerFactory transformerFactory;
    @Mock
    private TransformerMatcher transformerMatcher;
    @Mock
    private Transformer transformer;

    private File mediaDir;
    private DynamicMediaServlet servlet;

    @Before
    public void beforeTest() {
	mediaDir = new File("src/test/resources");
	servlet = new DynamicMediaServlet(mediaDir, transformerFactory);
    }

    @Test
    public void init() throws ServletException {
	ServletConfig config = Mockito.mock(ServletConfig.class);
	ServletContext context = Mockito.mock(ServletContext.class);
	ContainerSPI container = Mockito.mock(ContainerSPI.class);

	when(config.getServletContext()).thenReturn(context);
	when(context.getAttribute(TinyContainer.ATTR_INSTANCE)).thenReturn(container);
	when(context.getInitParameter("media.repository.path")).thenReturn(".");

	servlet.init(config);

	assertThat(servlet.getMediaDir(), anExistingDirectory());
	assertThat(servlet.getMediaDir(), aFileNamed(equalTo(".")));
	assertThat(servlet.getMediaDir(), aFileWithAbsolutePath(containsString("dynamic-media")));
	assertThat(servlet.getTransformerFactory(), notNullValue());
	assertThat(servlet.getTransformerFactory(), not(equalTo(transformerFactory)));
    }

    @Test
    public void handleRequest() throws Exception {
	String requestURI = "/dynamic-media/processor/media/picture_96x96.jpg";
	
	File targetFile = new File(mediaDir, "/media/picture_96x96.jpg");
	assertThat(targetFile, not(anExistingFile()));

	RequestContext context = Mockito.mock(RequestContext.class);
	HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
	ServletOutputStream stream = Mockito.mock(ServletOutputStream.class);

	when(context.getResponse()).thenReturn(response);
	when(context.getRequestURI()).thenReturn(requestURI);
	when(transformerFactory.getTransformer("96x96")).thenReturn(transformerMatcher);
	when(transformerMatcher.getTransformer()).thenReturn(transformer);
	when(response.getOutputStream()).thenReturn(stream);

	doAnswer(new Answer<Void>() {
	    @Override
	    public Void answer(InvocationOnMock invocation) throws Throwable {
		Object[] args = invocation.getArguments();
		File source = (File) args[0];
		File target = (File) args[1];
		Files.copy(source, target);
		return null;
	    }
	}).when(transformer).exec(any(File.class), any(File.class), (Matcher) isNull());

	CollectBytes collectBytes = new CollectBytes();
	doAnswer(collectBytes).when(stream).write(any(byte[].class), anyInt(), anyInt());

	Classes.invoke(servlet, "handleRequest", context);

	verify(response, times(1)).setStatus(200);
	verify(response, times(1)).setHeader("Cache-Control", "no-cache");
	verify(response, times(1)).addHeader("Cache-Control", "no-store");
	verify(response, times(1)).setHeader("Pragma", "no-cache");
	verify(response, times(1)).setDateHeader("Expires", 0L);
	verify(response, times(1)).setContentType("image/jpeg");
	verify(response, times(1)).setContentLength(7);

	assertThat(collectBytes.toString(), equalTo("picture"));
	assertThat(targetFile, anExistingFile());
	assertThat(targetFile, aFileWithSize(7));
	assertThat(targetFile.delete(), equalTo(true));
    }

    // --------------------------------------------------------------------------------------------

    private static class CollectBytes implements Answer<Void> {
	private ByteArrayOutputStream bytes = new ByteArrayOutputStream();

	@Override
	public Void answer(InvocationOnMock invocation) throws Throwable {
	    Object[] args = invocation.getArguments();
	    byte[] buf = (byte[]) args[0];
	    int off = (int) args[1];
	    int len = (int) args[2];
	    bytes.write(buf, off, len);
	    return null;
	}

	public String toString() {
	    return bytes.toString();
	}
    }
}
