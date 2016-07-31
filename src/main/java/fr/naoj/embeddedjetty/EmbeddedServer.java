package fr.naoj.embeddedjetty;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jasper.servlet.JspServlet;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class EmbeddedServer {

	private static final Logger LOGGER = Logger.getLogger(EmbeddedServer.class.getName());
    
    private static final int PORT = 8080;
    
    private static final String CONTEXT_PATH = "/";
    private static final String CONFIG_LOCATION_PACKAGE = "fr.naoj.embeddedjetty.config";
    private static final String MAPPING_URL = "/";
    private static final String WEBAPP_DIRECTORY = "webapp";
	
	public static void main(String[] args) throws Exception {
		new EmbeddedServer().startJetty(PORT);	
	}
	
	private void startJetty(int port) throws Exception {
        LOGGER.info(String.format("Starting server at port %s", port));
        Server server = new Server(port);
        
        server.setHandler(getServletContextHandler());
        
        addShutdownHook(server);
        
        server.start();
        LOGGER.info(String.format("Server started at port %s", port));
        server.join();
    }
	
	private static Handler getServletContextHandler() throws IOException {
        WebAppContext contextHandler = new WebAppContext();
//        contextHandler.setServletHandler(ServletContextHandler.SESSIONS); 
        contextHandler.setErrorHandler(new CustomErrorHandler());
        
        contextHandler.setResourceBase(new ClassPathResource(WEBAPP_DIRECTORY).getURI().toString());        
        contextHandler.setContextPath(CONTEXT_PATH);
        contextHandler.setDescriptor("webapp/WEB-INF/web.xml");
        contextHandler.setWelcomeFiles(new String[]{"index.jsp"});
        
        // JSP
        contextHandler.setClassLoader(Thread.currentThread().getContextClassLoader());
        contextHandler.addServlet(JspServlet.class, "*.jsp");

        // Spring
        WebApplicationContext webAppContext = getWebApplicationContext();
        DispatcherServlet dispatcherServlet = new DispatcherServlet(webAppContext);
        ServletHolder springServletHolder = new ServletHolder("mvc-dispatcher", dispatcherServlet);
        contextHandler.addServlet(springServletHolder, MAPPING_URL);
        
        contextHandler.addEventListener(new ContextLoaderListener(webAppContext));
        
        return contextHandler;
    }
	
	private static WebApplicationContext getWebApplicationContext() {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setConfigLocation(CONFIG_LOCATION_PACKAGE);
        return context;
    }
	
	private static void addShutdownHook(final Server server) {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if (server.isStarted()) {
                	server.setStopAtShutdown(true);
                    try {
                    	server.stop();
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, String.format("Error while stopping jetty server: ", e.getMessage()), e);
                    }
                }
            }
        }));
	}
}
