package org.osc.controller.nsc;

import static org.osgi.service.jdbc.DataSourceFactory.JDBC_PASSWORD;
import static org.osgi.service.jdbc.DataSourceFactory.JDBC_URL;
import static org.osgi.service.jdbc.DataSourceFactory.JDBC_USER;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.Session;
import org.h2.tools.RunScript;
import org.hibernate.jdbc.Work;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osc.controller.nsc.entities.PortIpNSCEntity;

public class DummyTests {

    protected static EntityManagerFactory emf;
    protected static EntityManager em;

    @BeforeClass
    public static void init() throws FileNotFoundException, SQLException {
    	
        emf = Persistence.createEntityManagerFactory("nsc-mgr");
    	Map<String, String> props = new HashMap<>();

    	props.put(JDBC_URL, "testDB.db");        
    	props.put(JDBC_USER, "admin");
    	props.put(JDBC_PASSWORD, "admin123");

        em = emf.createEntityManager(props);
    }

    @Before
    public void initializeDatabase(){
        Session session = em.unwrap(Session.class);
        (session).doWork(new Work() {
	           @Override
	            public void execute(Connection connection) throws SQLException {
	                try {
	                    File script = new File(getClass().getResource("./create.sql").getFile());
	                    RunScript.execute(connection, new FileReader(script));
	                } catch (FileNotFoundException e) {
	                    throw new RuntimeException("could not initialize with script");
	                }
	            }
        });
    }

    @AfterClass
    public static void tearDown(){
        em.clear();
        em.close();
        emf.close();
    }
	
	
	@Test
	public void test() {
		PortIpNSCEntity portIp = new PortIpNSCEntity();
		
		
		portIp.setId(42L);
		portIp.setPortIp("10.2.3.4");
		
		em.persist(portIp);
		
		
		
	}
}
