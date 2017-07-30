package cz.muni.fi.web;


import cz.muni.fi.car.CarManagerImpl;
import cz.muni.fi.customer.CustomerManagerImpl;
import cz.muni.fi.lease.LeaseManagerImpl;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static java.time.Month.*;

@WebListener
public class StartListener implements ServletContextListener {

    private final static ZonedDateTime NOW = LocalDateTime.of(2016, FEBRUARY,
            29, 14, 00).atZone(ZoneId.of("UTC"));

    @Override
    public void contextInitialized(ServletContextEvent ev) {
        ServletContext servletContext = ev.getServletContext();
        DataSource dataSource = new EmbeddedDatabaseBuilder().setType(
                EmbeddedDatabaseType.DERBY).addScript(
                        "createTables.sql").build();
        servletContext.setAttribute("carManager",
                new CarManagerImpl(dataSource));
        servletContext.setAttribute("customerManager",
                new CustomerManagerImpl(dataSource));
        servletContext.setAttribute("leaseManager",
                new LeaseManagerImpl(dataSource, Clock.fixed(NOW.toInstant(),
                        NOW.getZone())));
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

}