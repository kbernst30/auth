package io.keystash;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
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

//            Tomcat.addServlet(ctx, "jersey-container-servlet", new ServletContainer(new App()));
//            ctx.addServletMapping("/auth/*", "jersey-container-servlet");

            String filterName = "jersey-container-servlet";
            FilterDef def = new FilterDef();
            def.setFilterName(filterName);
            def.setFilter(new ServletContainer(new App()));
            ctx.addFilterDef(def);
            FilterMap map = new FilterMap();
            map.setFilterName(filterName);
            map.addURLPattern("/auth/*");
            ctx.addFilterMap(map);

            tomcat.start();
            tomcat.getServer().await();

        } catch (ServletException | LifecycleException e) {
            System.out.println("Failed to startup tomcat");
            e.printStackTrace();
            System.exit(1);
        }
    }

}