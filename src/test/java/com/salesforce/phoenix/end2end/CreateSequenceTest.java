package com.salesforce.phoenix.end2end;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import com.salesforce.phoenix.jdbc.PhoenixConnection;
import com.salesforce.phoenix.jdbc.PhoenixStatement;
import com.salesforce.phoenix.parse.TableName;
import com.salesforce.phoenix.schema.SequenceAlreadyExistsException;
import com.salesforce.phoenix.schema.SequenceNotFoundException;
import com.salesforce.phoenix.util.PhoenixRuntime;

public class CreateSequenceTest extends BaseClientMangedTimeTest {	

	@Test
	public void testSystemTable() throws Exception {		
		Connection conn = getConnectionNextTimestamp();
		String query = "SELECT sequence_schema, sequence_name, current_value, increment_by FROM SYSTEM.\"SEQUENCE\"";
		ResultSet rs = conn.prepareStatement(query).executeQuery();
		assertTrue(rs.next());
	}

	@Test
	public void testDuplicateSequences() throws Exception {
		Connection conn = getConnectionNextTimestamp();		
		conn.createStatement().execute("CREATE SEQUENCE alpha.beta START WITH 2 INCREMENT BY 4\n");

		conn = getConnectionNextTimestamp();
		try {
			conn.createStatement().execute("CREATE SEQUENCE alpha.beta START WITH 2 INCREMENT BY 4\n");
			Assert.fail("Duplicate sequences");
		} catch (SequenceAlreadyExistsException e){

		}
	}

	@Test
	public void testSequenceNotFound() throws Exception {
		Connection conn = getConnectionNextTimestamp();
		String query = "SELECT NEXT value FOR qwert.asdf FROM SYSTEM.\"SEQUENCE\"";
		try {
			conn.prepareStatement(query).executeQuery();
			Assert.fail("Sequence not found");
		}catch(SequenceNotFoundException e){

		}
	}

	@Test
	public void testCreateSequence() throws Exception {	
		Connection conn = getConnectionNextTimestamp();
		conn.createStatement().execute("CREATE SEQUENCE alpha.omega START WITH 2 INCREMENT BY 4");
		conn = getConnectionNextTimestamp();		
		String query = "SELECT sequence_schema, sequence_name, current_value, increment_by FROM SYSTEM.\"SEQUENCE\" WHERE sequence_name='OMEGA'";
		ResultSet rs = conn.prepareStatement(query).executeQuery();
		assertTrue(rs.next());
		assertEquals("ALPHA", rs.getString("sequence_schema"));
		assertEquals("OMEGA", rs.getString("sequence_name"));
		assertEquals(2, rs.getInt("current_value"));
		assertEquals(4, rs.getInt("increment_by"));
		assertFalse(rs.next());
	}

	@Test
	public void testSelectNextValueFor() throws Exception {
		Connection conn = getConnectionNextTimestamp();
		conn.createStatement().execute("CREATE SEQUENCE foo.bar START WITH 3 INCREMENT BY 2");
		conn = getConnectionNextTimestamp();
		String query = "SELECT NEXT VALUE FOR foo.bar FROM SYSTEM.\"SEQUENCE\"";
		ResultSet rs = conn.prepareStatement(query).executeQuery();
		assertTrue(rs.next());
		assertEquals(3, rs.getInt(1));

		conn = getConnectionNextTimestamp();		
		rs = conn.prepareStatement(query).executeQuery();
		assertTrue(rs.next());
		assertEquals(5, rs.getInt(1));

		conn = getConnectionNextTimestamp();		
		rs = conn.prepareStatement(query).executeQuery();
		assertTrue(rs.next());
		assertEquals(7, rs.getInt(1));
	}

	@Test
	public void testInsertNextValueFor() throws Exception {
		Connection conn = getConnectionNextTimestamp();
		conn.createStatement().execute("CREATE TABLE test.sequence_number ( id INTEGER NOT NULL PRIMARY KEY)");
		conn = getConnectionNextTimestamp();
		conn.createStatement().execute("CREATE SEQUENCE alpha.tau START WITH 2 INCREMENT BY 1");
		conn = getConnectionNextTimestamp();
		conn.createStatement().execute("UPSERT INTO test.sequence_number (id) VALUES (NEXT VALUE FOR alpha.tau)");
		conn.commit();
		conn = getConnectionNextTimestamp();
		String query = "SELECT id FROM test.sequence_number";		
		ResultSet rs = conn.prepareStatement(query).executeQuery();
		assertTrue(rs.next());
		assertEquals(2, rs.getInt(1));
	}

	@Test
	public void testSequenceCaching() throws Exception {		
		Connection conn = getConnectionNextTimestamp();
		conn.createStatement().execute("CREATE SEQUENCE alpha.gamma START WITH 2 INCREMENT BY 1");
		conn = getConnectionNextTimestamp();
		TableName tableName = TableName.createNormalized("ALPHA", "GAMMA");
		Long value = conn.unwrap(PhoenixConnection.class).getPMetaData().getSequenceIncrementValue(tableName);
		Assert.assertNull(value);
		conn = getConnectionNextTimestamp();
		final String query = "SELECT NEXT VALUE FOR alpha.gamma FROM SYSTEM.\"SEQUENCE\"";
		conn.prepareStatement(query).executeQuery();
		conn = getConnectionNextTimestamp();
		value = conn.unwrap(PhoenixConnection.class).getPMetaData().getSequenceIncrementValue(tableName);
		Assert.assertNotNull(value);
	}

	@Test
	public void testMultipleSequenceValues() throws Exception {
		Connection conn = getConnectionNextTimestamp();
		conn.createStatement().execute("CREATE SEQUENCE alpha.zeta START WITH 4 INCREMENT BY 7");
		conn.createStatement().execute("CREATE SEQUENCE alpha.kappa START WITH 9 INCREMENT BY 2");
		conn = getConnectionNextTimestamp();
		String query = "SELECT NEXT VALUE FOR alpha.zeta, NEXT VALUE FOR alpha.kappa FROM SYSTEM.\"SEQUENCE\"";
		ResultSet rs = conn.prepareStatement(query).executeQuery();
		assertTrue(rs.next());
		assertEquals(4, rs.getInt(1));
		assertEquals(9, rs.getInt(2));
	}
	
	@Test
	public void testCompilerOptimization() throws Exception {
		Connection conn = getConnectionNextTimestamp();
		conn.createStatement().execute("CREATE TABLE t (k INTEGER NOT NULL PRIMARY KEY, v1 VARCHAR, v2 VARCHAR) IMMUTABLE_ROWS=true");
		conn = getConnectionNextTimestamp();
        conn.createStatement().execute("CREATE INDEX idx ON t(v1) INCLUDE (v2)");
        conn = getConnectionNextTimestamp();
        conn.createStatement().execute("CREATE SEQUENCE seq.perf START WITH 3 INCREMENT BY 2");        
        PhoenixStatement stmt = conn.createStatement().unwrap(PhoenixStatement.class);
        stmt.optimizeQuery("SELECT k, NEXT VALUE FOR seq.perf FROM t WHERE v1 = 'bar'");
	}
	
	@Test
	public void testSelectRowAndSequence() throws Exception {
		Connection conn = getConnectionNextTimestamp();
		conn.createStatement().execute("CREATE TABLE test.foo ( id INTEGER NOT NULL PRIMARY KEY)");
		conn = getConnectionNextTimestamp();
		conn.createStatement().execute("CREATE SEQUENCE alpha.epsilon START WITH 1 INCREMENT BY 4");
		conn = getConnectionNextTimestamp();
		conn.createStatement().execute("UPSERT INTO test.foo (id) VALUES (NEXT VALUE FOR alpha.epsilon)");
		conn.commit();
		conn = getConnectionNextTimestamp();
		String query = "SELECT NEXT VALUE FOR alpha.epsilon, id FROM test.foo";
		ResultSet rs = conn.prepareStatement(query).executeQuery();
		assertTrue(rs.next());
		assertEquals(5, rs.getInt(1));
		assertEquals(1, rs.getInt(2));
	}

	private Connection getConnectionNextTimestamp() throws Exception {
		long ts = nextTimestamp();
		Properties props = new Properties();
		props.setProperty(PhoenixRuntime.CURRENT_SCN_ATTRIB, Long.toString(ts));
		Connection conn = DriverManager.getConnection(getUrl(), props);
		return conn;
	}	
}