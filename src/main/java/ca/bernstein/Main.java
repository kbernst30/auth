package ca.bernstein;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.servlet.ServletException;
import java.io.File;

public class Main {

    public static void main(String[] args) {
        // TODO we should make all of this configurable
        try {
            String webappDirLocation = "src/main/webapp/";
            Tomcat tomcat = new Tomcat();
            int port = 8080;

            tomcat.setPort(port);

            Context ctx = tomcat.addWebapp("", new File(webappDirLocation).getAbsolutePath());

            Tomcat.addServlet(ctx, "jersey-container-servlet", new ServletContainer(new App()));
            ctx.addServletMapping("/auth/*", "jersey-container-servlet");

            // TODO set up database

            tomcat.start();
            tomcat.getServer().await();

        } catch (ServletException | LifecycleException e) {
            System.out.println("Failed to startup tomcat");
            e.printStackTrace();
            System.exit(1);
        }
    }

}