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

import minicp.cp.Factory;
import minicp.engine.core.Constraint;
import minicp.engine.core.IntVar;
import minicp.reversible.ReversibleInt;
import minicp.util.InconsistencyException;
import minicp.util.NotImplementedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Element1D extends Constraint {

    private final int[] T;
    private final IntVar x, y;
    private int n;
    private final ReversibleInt[] nRowsSup;

    private final ReversibleInt low;
    private final ReversibleInt up;
    private final ArrayList<Element1D.Doublet> xy;

    private class Doublet implements Comparable<Element1D.Doublet> {
        protected final int x,y;

        private Doublet(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public int compareTo(Element1D.Doublet t) {
            return y - t.y;
        }
    }
    public Element1D(int[] T, IntVar x, IntVar y) {
        super(y.getSolver());
        this.T = T;
        this.x = x;
        this.y = y;
        n = T.length;

        this.xy = new ArrayList<Element1D.Doublet>();
        for (int i = 0; i < T.length; i++) {
            xy.add(new Element1D.Doublet(i,T[i]));
        }
        Collections.sort(xy);
        low = new ReversibleInt(cp.getTrail(),0);
        up = new ReversibleInt(cp.getTrail(),xy.size()-1);

        nRowsSup = new ReversibleInt[n];
        for (int j = 0; j < n; j++) {
            nRowsSup[j] = new ReversibleInt(cp.getTrail(),1);
        }

    }

    @Override
    public void post() throws InconsistencyException {
        x.removeBelow(0);
        x.removeAbove(n-1);

        x.propagateOnDomainChange(this);
        y.propagateOnBoundChange(this);
        propagate();
    }

    @Override
    public void propagate() throws InconsistencyException { // supprime les x,y des z non compris dans les bornes
        int l = low.getValue();
        int u = up.getValue();
        int yMin = y.getMin();
        int yMax = y.getMax();
        while (xy.get(l).y < yMin || !x.contains(xy.get(l).x)) {
            updateSupports(l);
            l++;
            if (l > u) throw new InconsistencyException();
        }
        while (xy.get(u).y > yMax || !x.contains(xy.get(u).x)) {
            updateSupports(u);
            x.propagateOnDomainChange(this);

            u--;
            if (l > u) throw new InconsistencyException();
        }
        y.removeBelow(xy.get(l).y);
        y.removeAbove(xy.get(u).y);
        low.setValue(l);
        up.setValue(u);
    }

    private void updateSupports(int lostPos) throws InconsistencyException {
        if (nRowsSup[xy.get(lostPos).x].decrement() == 0) {
            x.remove(xy.get(lostPos).x);
        }
    }
}
