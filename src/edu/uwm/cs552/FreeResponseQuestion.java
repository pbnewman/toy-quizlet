package edu.uwm.cs552;

public class FreeResponseQuestion extends Question {

	@Override
	protected void printResponse(StringBuilder sb) {
		sb.append("Your answer: ");
	}

	@Override
	protected String getXMLelementName() {
		return "FreeResponseQuestion";
	}

}
