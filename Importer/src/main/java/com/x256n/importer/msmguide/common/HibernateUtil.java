package com.x256n.importer.msmguide.common;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

/**
 * @author Aleksey Permyakov (03.12.2015).
 */
public class HibernateUtil {
    private static SessionFactory factory;
    private static Configuration config;
    private static Session session;

    public static Session buildIfNeeded() {
        try {
            if (session != null) {
                return session;
            } else {
                config = new Configuration();
                config.configure("hibernate.cfg.xml"/*"persistence.xml"*//*"hibernate.cfg.xml"*/);
                factory = config.configure().buildSessionFactory();
                session = factory.openSession();
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.close();
        }
        return session;
    }

    public static void close(Session session) {
        session.close();
    }

    public static void rollback(Transaction transaction) {
        transaction.rollback();
    }

    public static Session openSession() {
        return session.isConnected() && session.isOpen() ? session : (session = factory.openSession());
    }
}
