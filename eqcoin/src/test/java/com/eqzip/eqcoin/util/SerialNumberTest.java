package com.eqzip.eqcoin.util;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SerialNumberTest {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testHashCode() {
		fail("Not yet implemented");
	}

	@Test
	void testSerialNumberByteArray() {
		fail("Not yet implemented");
	}

	@Test
	void testSerialNumberBigInteger() {
		SerialNumber sn0 = new SerialNumber(BigInteger.valueOf(188888));
		byte[] bytes = sn0.getBits();
		SerialNumber sn1 = new SerialNumber(bytes);
	}

	@Test
	void testSerialNumber() {
		fail("Not yet implemented");
	}

	@Test
	void testIsNextSNSerialNumber() {
		fail("Not yet implemented");
	}

	@Test
	void testIsNextSNByteArray() {
		fail("Not yet implemented");
	}

	@Test
	void testGetNextSN() {
		fail("Not yet implemented");
	}

	@Test
	void testGetBits() {
		fail("Not yet implemented");
	}

	@Test
	void testGetSerialNumber() {
		fail("Not yet implemented");
	}

	@Test
	void testSetSerialNumber() {
		fail("Not yet implemented");
	}

	@Test
	void testEqualsObject() {
		fail("Not yet implemented");
	}

}
