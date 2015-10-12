/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tribalj;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;

/**
 * Hibernate Utility class with a convenient method to get Session Factory
 * object.
 *
 * @author Mitja
 */
public class NewHibernateUtil {

    private static final SessionFactory sessionFactory;
    private static Logger  logger;
    private static ConsoleHandler consoleHandler;
       
    static {
        logger = Logger.getLogger(Meteo.class.getName());
        consoleHandler = new ConsoleHandler();
         logger.addHandler(consoleHandler);
        try {
            // Create the SessionFactory from standard (hibernate.cfg.xml) 
            // config file.
            sessionFactory = new AnnotationConfiguration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            // Log the exception. 
            logger.log(Level.WARNING, "Initial SessionFactory creation failed.{0}", ex);
     //       System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }
    
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
