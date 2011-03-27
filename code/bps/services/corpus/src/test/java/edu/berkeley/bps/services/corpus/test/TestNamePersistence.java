/**
 * 
 */
package edu.berkeley.bps.services.corpus.test;

import java.sql.Connection;

import edu.berkeley.bps.services.common.test.TestDBBase;
import edu.berkeley.bps.services.corpus.Name;
import junit.framework.TestSuite;

/**
 * @author pschmitz
 *
 */
public class TestNamePersistence extends TestDBBase {
	
	private static final String TEST_NAME ="Mike";
	private static final int TEST_NAME_TYPE = Name.NAME_TYPE_PERSON;
	private static final int TEST_GENDER = Name.GENDER_MALE;
	private static final String TEST_NOTES = "Famous for his steam-shovel";
	private static final int TEST_CORPUS = 0;
	
	/**
	 * @param name
	 */
	public TestNamePersistence(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for {@link edu.berkeley.bps.services.corpus.Name#CreateAndPersist(java.sql.Connection, java.lang.String, java.lang.String, java.lang.String, java.lang.String, edu.berkeley.bps.services.corpus.Name)}.
	 */
	public void testCreateAndPersist() {
		Name testName = Name.CreateAndPersist(getConnection(), TEST_CORPUS, 
				TEST_NAME, TEST_NAME_TYPE, TEST_GENDER, TEST_NOTES, null);
		assertNotNull("New Name should be non-null", testName);
		assertTrue("New Name must have id from DB", (testName.getId()>0));
		assertEquals(testName.getName(), TEST_NAME);
		assertEquals(testName.getCorpusId(), TEST_CORPUS);
		assertEquals(testName.getNameType(), TEST_NAME_TYPE);
		assertEquals(testName.getGender(), TEST_GENDER);
		assertEquals(testName.getNotes(), TEST_NOTES);
	}

	/**
	 * Test method for {@link edu.berkeley.bps.services.corpus.Name#persist(java.sql.Connection)}.
	 */
	public void testReadPersistence() {
		Connection dbConn = getConnection();
		Name readName = Name.FindByName(dbConn, TEST_NAME, TEST_CORPUS);
		assertNotNull("Name should be found by Name", readName);
		assertEquals(readName.getName(), TEST_NAME);
		assertEquals(readName.getCorpusId(), TEST_CORPUS);
		assertEquals(readName.getNameType(), TEST_NAME_TYPE);
		assertEquals(readName.getGender(), TEST_GENDER);
		assertEquals(readName.getNotes(), TEST_NOTES);
		Name readName2 = Name.FindById(dbConn, readName.getId());
		assertNotNull("Name should be found by ID", readName);
		assertEquals(readName.getId(), readName2.getId());
	}

	/**
	 * Test method for {@link edu.berkeley.bps.services.corpus.Name#persist(java.sql.Connection)}.
	 */
	public void testPersist() {
		Connection dbConn = getConnection();
		Name testName = Name.FindByName(dbConn, TEST_NAME, TEST_CORPUS);
		testName.setNotes("Updated - "+testName.getNotes());
		testName.persist(dbConn);
		Name updatedName = Name.FindById(dbConn, testName.getId());
		assertEquals(testName.getNotes(), updatedName.getNotes());
	}

	/**
	 * Test method for {@link edu.berkeley.bps.services.corpus.Name#persist(java.sql.Connection)}.
	 */
	public void testDeletePersistence() {
		Connection dbConn = getConnection();
		Name testName = Name.FindByName(dbConn, TEST_NAME, TEST_CORPUS);
		testName.deletePersistence(dbConn);
		Name deletedName = Name.FindById(dbConn, testName.getId());
		assertNull("Name should no longer be found by ID", deletedName);
		deletedName = Name.FindByName(dbConn, TEST_NAME, TEST_CORPUS);
		assertNull("Name should no longer be found by Name", deletedName);
	}

	public static TestSuite suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new TestNamePersistence("testCreateAndPersist"));
		suite.addTest(new TestNamePersistence("testReadPersistence"));
		suite.addTest(new TestNamePersistence("testPersist"));
		suite.addTest(new TestNamePersistence("testDeletePersistence"));
		return suite;
	}


}
