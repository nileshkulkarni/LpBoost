
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;



public class LPBoostMaster{

	public static PrimalVariables solvePrimalModel(Vector<DataPoint> examples,	Vector<Integer> labels, Vector<Stump> hypotheses, double d) {
		try{
			IloCplex cplex = new IloCplex();
			IloNumVar rho = cplex.numVar(0,Double.MAX_VALUE,"rho");
			IloNumVar[] alphas =cplex.numVarArray(hypotheses.size(), 0.0,Double.MAX_VALUE);
			IloNumVar[] zetas =cplex.numVarArray(examples.size(), 0.0,Double.MAX_VALUE);
			
			//constraint
			//IloLinearNumExpr[] exmConstraint = new IloLinearNumExpr[examples.size()];
			for(int i=0;i<examples.size();i++){
				IloLinearNumExpr exmConstraint = cplex.linearNumExpr();
				for(int j=0;j<hypotheses.size();j++){
//					exmConstraint[i].addTerm(hypotheses.get(j).classify(examples.get(i))*labels.get(i), alphas[j]);					
					exmConstraint.addTerm(hypotheses.get(j).classify(examples.get(i))*labels.get(i), alphas[j]);					

				}
//				exmConstraint[i].addTerm(1, zetas[i]);
				exmConstraint.addTerm(1, zetas[i]);
//				cplex.addGe(exmConstraint[i], rho);
				cplex.addGe(exmConstraint, rho);

			}
			
			//sumconstraint
			IloLinearNumExpr sumConstraint = cplex.linearNumExpr();
			for(int i=0;i<hypotheses.size();i++){
				sumConstraint.addTerm(1, alphas[i]);
			}
			cplex.addEq(sumConstraint, 1);
			
			
			//define objective
			IloLinearNumExpr objective = cplex.linearNumExpr();
			objective.addTerm(1, rho);
			for(int i=0;i<examples.size();i++){
				objective.addTerm(-d, zetas[i]);
			}
			
			cplex.addMaximize(objective);
			
			PrimalVariables pV = new PrimalVariables();

			//solve
			if(cplex.solve()){
				pV.rho=(float)cplex.getValue(rho);
				
				
				double[] tmpAlphas=cplex.getValues(alphas);
				for(int i=0;i<tmpAlphas.length;i++){
					if(pV.alphas.size()>i)
						pV.alphas.set(i, new Float(tmpAlphas[i]));
					else
						pV.alphas.add(new Float(tmpAlphas[i]));
				}
				
				return pV;

			}
			else {
				System.out.println("model not solved");
				return pV;
			}
			
			
		}catch (IloException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
