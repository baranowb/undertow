package test;

import javax.servlet.HttpConstraintElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletSecurityElement;
import javax.servlet.annotation.ServletSecurity.TransportGuarantee;

public class ContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        ctx.declareRoles("admin");
        ServletRegistration.Dynamic admin = ctx.addServlet("admin", AdminServlet.class);
        admin.addMapping("/admin");
        admin.setLoadOnStartup(1);
        admin.setServletSecurity(new ServletSecurityElement(new HttpConstraintElement(TransportGuarantee.NONE, "admin")));
    }
}