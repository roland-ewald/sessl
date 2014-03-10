package sessl.sbw;

import edu.caltech.sbw.SBWException;

public interface SBWProxySimulator {

	void loadSBML(String sbml) throws SBWException;
	
	void loadTestModel() throws SBWException;
	
	String[] getNamesOfParameters() throws SBWException;
	
	void setParameter(String name, double value) throws SBWException;
	
	void simulate(double start, double end, int numOfRows, String bar) throws SBWException;
	
	double[][] simulate(double start, double end, int numOfRows) throws SBWException;
}
