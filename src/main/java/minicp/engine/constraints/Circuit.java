/*
 * mini-cp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License  v3
 * as published by the Free Software Foundation.
 *
 * mini-cp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY.
 * See the GNU Lesser General Public License  for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with mini-cp. If not, see http://www.gnu.org/licenses/lgpl-3.0.en.html
 *
 * Copyright (c)  2017. by Laurent Michel, Pierre Schaus, Pascal Van Hentenryck
 */


package minicp.engine.constraints;

import static minicp.cp.Factory.*;

import com.sun.org.apache.regexp.internal.RE;
import minicp.engine.core.Constraint;
import minicp.engine.core.ConstraintClosure;
import minicp.engine.core.IntVar;
import minicp.engine.core.IntVarImpl;
import minicp.reversible.ReversibleInt;
import minicp.util.InconsistencyException;
import minicp.util.NotImplementedException;

public class Circuit extends Constraint {

    private final IntVar [] x;
    private final ReversibleInt [] dest;
    private final ReversibleInt [] orig;
    private final ReversibleInt [] lengthToDest;
    private final int nbNodes;

    /**
     * x represents an Hamiltonian circuit on the cities {0..x.length-1}
     * where x[i] is the city visited after city i
     * @param x
     */
    public Circuit(IntVar [] x) {
        super(x[0].getSolver());
        this.x = x;
        this.nbNodes = x.length;
        dest = new ReversibleInt[x.length];
        orig = new ReversibleInt[x.length];
        lengthToDest = new ReversibleInt[x.length];
        for (int i = 0; i < x.length; i++) {
            dest[i] = new ReversibleInt(cp.getTrail(),i);
            orig[i] = new ReversibleInt(cp.getTrail(),i);
            lengthToDest[i] = new ReversibleInt(cp.getTrail(),0);
        }
    }


    @Override
    public void post() throws InconsistencyException {
        cp.post(allDifferent(x));
        if (nbNodes > 1){
            for (int i = 0; i < nbNodes; i++) {
                if (x[i].isBound()){
                    bind(i);
                } else {
                    final int j = i;
                    x[i].remove(i);
                    x[i].removeAbove(nbNodes - 1);
                    x[i].removeBelow(0);
                    x[i].whenBind(() -> {
                        bind(j);
                    });
                }
            }
        }
    }

    private void bind(int i) throws InconsistencyException {
        int length = lengthToDest[x[i].getMin()].getValue() + 1;
        for (int k = 0; k < nbNodes; k++){
            if (orig[k].getValue() == x[i].getMin()){
                orig[k].setValue(orig[i].getValue());
            }
            if (dest[k].getValue() == i){
                dest[k].setValue(dest[x[i].getMin()].getValue());
                lengthToDest[k].setValue(lengthToDest[k].getValue() + length);
            }
            if ((lengthToDest[k].getValue() == 0) && (lengthToDest[orig[k].getValue()].getValue() < nbNodes - 1)){
                x[k].remove(orig[k].getValue());
            }
        }
    }


}
