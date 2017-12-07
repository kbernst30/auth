package io.keystash.dist;

import io.keystash.AuthCore;
import io.keystash.dist.debug.AdminConsoleDebugServlet;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.jsp.JettyJspServlet;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.Configuration;
import org.glassfish.jersey.servlet.ServletContainer;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class Main {

    public static void main(String[] args) {

        Set<String> argsSet = Stream.of(args).collect(Collectors.toSet());

        Server server = new Server(8080);

        ServletContextHandler authCoreContext = getAuthCoreContext(server);
        ServletContextHandler adminConsoleContext = getAdminConsoleContext(argsSet.contains("debug")); // todo more robust

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[] { authCoreContext, adminConsoleContext });

        // Set Servlet context to server
        server.setHandler(contexts);

        try {
            server.start();
            server.join();
        } catch (Exception e) {
            log.error("Server failed", e);
            server.destroy();
        }
    }

    private static ServletContextHandler getAuthCoreContext(Server server) {
        // Initialize Main Jersey AuthCore
        AuthCore authCore = new AuthCore();

        // Initialize Jetty Server and map Jersey application to /auth/
        // TODO Add back in the Authentication Filter
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);

        ServletHolder servlet = new ServletHolder(new ServletContainer(authCore));
        context.addServlet(servlet, "/auth/*");

        // Set Resources directory for JSPs and webapps
        context.setContextPath("/");
        context.setBaseResource(Resource.newResource(Main.class.getClassLoader().getResource("auth-core-web")));

        ServletHolder defaultServlet = new ServletHolder("default", DefaultServlet.class);
        defaultServlet.setInitParameter("dirAllowed", "true");
        context.addServlet(defaultServlet, "/");

        // This needs to be enabled manually as this an embedded application
        // The following will enable JSP and JSTL tag lib
        enableEmbeddedJspSupport(context);

        Configuration.ClassList classlist = Configuration.ClassList.setServerDefault(server);
        classlist.addAfter("org.eclipse.jetty.webapp.FragmentConfiguration", "org.eclipse.jetty.plus.webapp.EnvConfiguration", "org.eclipse.jetty.plus.webapp.PlusConfiguration");
        classlist.addBefore("org.eclipse.jetty.webapp.JettyWebXmlConfiguration", "org.eclipse.jetty.annotations.AnnotationConfiguration" );

        context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", ".*/[^/]*servlet-api-[^/]*\\.jar$|.*/javax.servlet.jsp.jstl-.*\\.jar$|.*/[^/]*taglibs.*\\.jar$");

        return context;
    }

    private static ServletContextHandler getAdminConsoleContext(boolean isDebug) {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);

        context.setContextPath("/admin");

        if (!isDebug) {
            context.setBaseResource(Resource.newResource(Main.class.getClassLoader().getResource("admin-web")));

            ServletHolder defaultServlet = new ServletHolder("default", DefaultServlet.class);
            defaultServlet.setInitParameter("dirAllowed", "true");
            context.addServlet(defaultServlet, "/");

        } else {
            ServletHolder servlet = new ServletHolder(new AdminConsoleDebugServlet());
            context.addServlet(servlet, "/*");
        }

        return context;
    }

    // See https://github.com/jetty-project/embedded-jetty-jsp/blob/master/src/main/java/org/eclipse/jetty/demo/Main.java
    private static void enableEmbeddedJspSupport(ServletContextHandler servletContextHandler) {
        // Establish Scratch directory for the servlet context (used by JSP compilation)
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File scratchDir = new File(tempDir.toString(), "embedded-jetty-jsp");

        if (!scratchDir.exists()) {
            if (!scratchDir.mkdirs()) {
                throw new RuntimeException("Unable to create scratch directory: " + scratchDir);
            }
        }

        servletContextHandler.setAttribute("javax.servlet.context.tempdir", scratchDir);

        // Set Classloader of Context to be sane (needed for JSTL)
        // JSP requires a non-System classloader, this simply wraps the
        // embedded System classloader in a way that makes it suitable
        // for JSP to use
        ClassLoader jspClassLoader = new URLClassLoader(new URL[0], Main.class.getClassLoader());
        servletContextHandler.setClassLoader(jspClassLoader);

        // Manually call JettyJasperInitializer on context startup
        servletContextHandler.addBean(new JspStarter(servletContextHandler));

        // Create / Register JSP Servlet (must be named "jsp" per spec)
        ServletHolder holderJsp = new ServletHolder("jsp", JettyJspServlet.class);
        holderJsp.setInitOrder(0);
        holderJsp.setInitParameter("logVerbosityLevel", "DEBUG");
        holderJsp.setInitParameter("fork", "false");
        holderJsp.setInitParameter("xpoweredBy", "false");
        holderJsp.setInitParameter("compilerTargetVM", "1.8");
        holderJsp.setInitParameter("compilerSourceVM", "1.8");
        holderJsp.setInitParameter("keepgenerated", "true");
        servletContextHandler.addServlet(holderJsp, "*.jsp");
    }

    public static class JspStarter extends AbstractLifeCycle implements ServletContextHandler.ServletContainerInitializerCaller {
        JettyJasperInitializer jettyJasperInitializer;
        ServletContextHandler context;

         private JspStarter (ServletContextHandler context) {
            this.jettyJasperInitializer = new JettyJasperInitializer();
            this.context = context;
            this.context.setAttribute("org.apache.tomcat.JarScanner", new StandardJarScanner());
        }

        @Override
        protected void doStart() throws Exception {
            ClassLoader old = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(context.getClassLoader());
            try {
                jettyJasperInitializer.onStartup(null, context.getServletContext());
                super.doStart();
            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }
        }
    }
}