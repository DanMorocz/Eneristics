package com.eneristics.test;

import eneristics.mongo.api.TestHeatingCoolingMode;

public class TestMongoAPI2 {

	public static void main(String[] args) {
		TestHeatingCoolingMode tester = new TestHeatingCoolingMode();
		System.out.println("CURRENT HEATING/COOLING MODE: " + tester.getCurrentMode(System.currentTimeMillis(), 3));
	}
}
