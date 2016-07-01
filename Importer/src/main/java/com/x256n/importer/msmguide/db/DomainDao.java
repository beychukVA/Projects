package com.x256n.importer.msmguide.db;

import com.x256n.core.msmguide.domain.*;
import com.x256n.importer.msmguide.ImporterLibrary;
import com.x256n.importer.msmguide.common.HibernateUtil;
import com.x256n.importer.msmguide.common.Utils;
import org.hibernate.*;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class DomainDao {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(DomainDao.class);
    private Session session;
    private Transaction tx;

    public DomainDao() {
        HibernateUtil.buildIfNeeded();
    }

    public <T> void create(T event) throws Exception {
        try {
            startOperation();
            session.save(event);
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateUtil.close(session);
        }
    }


    public <T> void delete(T event) throws Exception {
        try {
            startOperation();
            session.delete(event);
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateUtil.close(session);
        }
    }
    public <T> T find(Long id, Class<T> clazz) throws Exception {
        T event = null;
        try {
            startOperation();
            event = (T) session.load(clazz, id);
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateUtil.close(session);
        }
        return event;
    }

    public <T> void update(T event) throws Exception {
        try {
            startOperation();
            session.update(event);
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    public <T> List<T> findAll(Class<T> clazz) throws Exception{
        List events = null;
        try {
            startOperation();
            Query query = session.createQuery("from " + clazz.getSimpleName());
            events =  query.list();
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateUtil.close(session);
        }
        return events;
    }

    public <T> List<T> findQuery(String queryString) throws Exception{
        List events = null;
        try {
            startOperation();
            Query query = session.createQuery(queryString);
            events =  query.list();
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateUtil.close(session);
        }
        return events;
    }

    public <T> T findByGuid(Class<T> clazz, String guid) throws Exception{
        List events = null;
        try {
            startOperation();
            Query query = session.createQuery("from " + clazz.getSimpleName() + " where guid='" + guid + "'");
            events =  query.list();
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateUtil.close(session);
        }
        return events.size() > 0 ? (T) events.get(0) : null;
    }

    public IconEntity findIcon(File iconFile) throws Exception
    {
        String iconUrl = Utils.readFileToString(iconFile);
        String guid = ImporterLibrary.urlToGuidMy(iconUrl);
        List events = null;
        try {
            startOperation();
            Query query = session.createQuery("from " + IconEntity.class.getSimpleName() + " where guid='" + guid + "'");
            events =  query.list();
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateUtil.close(session);
        }
        return events != null && events.size() > 0 ? (IconEntity) events.get(0) : null;
    }

    public MonsterEntity findByIcon(String icon) throws Exception{
        List events = null;
        try {
            startOperation();
            Query query = session.createQuery("from " + MonsterEntity.class.getSimpleName() + " where icon='" + icon + "'");
            events =  query.list();
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateUtil.close(session);
        }
        return events != null && events.size() > 0 ? (MonsterEntity) events.get(0) : null;
    }

    public MonsterelementEntity monsterElementByMonsterAndElement(String monster, Byte element) throws Exception{
        List events = null;
        try {
            startOperation();
            Query query = session.createQuery("from " + MonsterelementEntity.class.getSimpleName() + " where monster='" + monster + "' and element=" + Byte.toString(element));
            events =  query.list();
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateUtil.close(session);
        }
        return events != null && events.size() > 0 ? (MonsterelementEntity) events.get(0) : null;
    }

    private void handleException(HibernateException e) throws Exception {
        HibernateUtil.rollback(tx);
        logger.error("handleException: {}", e);
        throw new Exception(e);
    }

    private void startOperation() throws HibernateException {
        session = HibernateUtil.openSession();
        tx = session.beginTransaction();
    }

    public IslandEntity islandByName(String islandName) throws Exception {
        List events = null;
        try {
            startOperation();
            Query query = session.createQuery(
                    "select ie from " + IslandEntity.class.getSimpleName() + " as ie, " +
                            LocalizedEntity.class.getSimpleName() + " as le where le.text like '" + islandName + " остров' "
                    + "or le.text like 'остров " + islandName + "' and le.guid=ie.name");
            events =  query.list();
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateUtil.close(session);
        }
        return events != null && events.size() > 0 ? (IslandEntity) events.get(0) : null;
    }

    public void deleteAll(Class clazz) throws Exception {
        try {
            startOperation();
            Query query = session.createQuery(
                    "delete from " + clazz.getSimpleName());
            query.executeUpdate();
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    public void deleteAllCreateTimesForMonster(String guid) throws Exception {
        try {
            startOperation();
            Query query = session.createQuery(
                    "delete from " + MonstercreatetimeEntity.class.getSimpleName() + " where monster='" + guid + "'");
            query.executeUpdate();
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    public void deleteAllDeductionsForMonster(String guid) throws Exception {
        try {
            startOperation();
            Query query = session.createQuery(
                    "delete from " + MonstercreateEntity.class.getSimpleName() + " where monster='" + guid + "'");
            query.executeUpdate();
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    public MonsterEntity monsterByName(String name) throws Exception {
        List events = null;
        try {
            startOperation();
            Query query = session.createQuery(
                    "select ie from " + MonsterEntity.class.getSimpleName() + " as ie, " +
                            LocalizedEntity.class.getSimpleName() + " as le where le.text like '" + name + "' and le.guid=ie.name");
            events =  query.list();
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateUtil.close(session);
        }
        return events != null && events.size() > 0 ? (MonsterEntity) events.get(0) : null;
    }


    public String findMonsterByName(String name) throws Exception {
        List events = null;
        try {
            startOperation();
            SQLQuery query = session.createSQLQuery(
                    "SELECT monster.guid " +
                    "FROM monster, localized " +
                    "WHERE monster.name = localized.guid AND localized.text LIKE '" + name + "'");
            events =  query.list();
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateUtil.close(session);
        }
        return events != null && events.size() > 0 ?  events.get(0).toString() : null;
    }

    public String findDecorationByName(String name) throws Exception {
        List events = null;
        try {
            startOperation();
            SQLQuery query = session.createSQLQuery(
                    "SELECT decorations.guid " +
                            "FROM decorations, localized " +
                            "WHERE decorations.name = localized.guid AND localized.text LIKE '" + name + "'");
            events =  query.list();
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateUtil.close(session);
        }
        return events != null && events.size() > 0 ? events.get(0).toString() : null;
    }
    public String findIslandByName(String name) throws Exception {
        List events = null;
        try {
            startOperation();
            SQLQuery query = session.createSQLQuery(
                    "SELECT island.guid " +
                            "FROM island, localized " +
                            "WHERE island.name = localized.guid AND localized.text LIKE '" + name + "'");
            events =  query.list();
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateUtil.close(session);
        }
        return events != null && events.size() > 0 ? events.get(0).toString() : "";
    }

    public void deleteAllLocationsForMonster(String guid) throws Exception {
        try {
            startOperation();
            Query query = session.createQuery(
                    "delete from " + MonsterliveEntity.class.getSimpleName() + " where monster='" + guid + "'");
            query.executeUpdate();
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    public void deleteAllAppetenceDecorationsForMonster(String guid) throws Exception {
        try {
            startOperation();
            Query query = session.createQuery(
                    "delete from " + MonsterappetencedecorationsEntity.class.getSimpleName() + " where monster='" + guid + "'");
            query.executeUpdate();
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    public void deleteAllAppetenceMonstersForMonster(String guid) throws Exception {
        try {
            startOperation();
            Query query = session.createQuery(
                    "delete from " + MonsterappetencemonsterEntity.class.getSimpleName() + " where monster='" + guid + "'");
            query.executeUpdate();
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateUtil.close(session);
        }
    }

    public DecorationsEntity decorationByName(String name) throws Exception {
        List events = null;
        try {
            startOperation();
            Query query = session.createQuery(
                    "select ie from " + DecorationsEntity.class.getSimpleName() + " as ie, " +
                            LocalizedEntity.class.getSimpleName() + " as le where le.text='" + name + "' and le.guid=ie.name");
            events =  query.list();
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateUtil.close(session);
        }
        return events != null && events.size() > 0 ? (DecorationsEntity) events.get(0) : null;
    }

    public void deleteAllIslandmonsterForIsland(String guid) throws Exception {
        try {
            startOperation();
            Query query = session.createQuery(
                    "delete from " + IslandmonsterEntity.class.getSimpleName() + " where island='" + guid + "'");
            query.executeUpdate();
            tx.commit();
        } catch (HibernateException e) {
            handleException(e);
        } finally {
            HibernateUtil.close(session);
        }
    }
}
