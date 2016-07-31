package fr.naoj.embeddedjetty;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ErrorHandler;

public class CustomErrorHandler extends ErrorHandler {

	private final int HTTP_404 = 404;
	
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (response.getStatus() == HTTP_404) {
			try {
				request.getRequestDispatcher("errors/404").forward(request, response);
			} catch (ServletException e) {
				e.printStackTrace();
			}
		} else {
			super.handle(target, baseRequest, request, response);
		}
	}

}
