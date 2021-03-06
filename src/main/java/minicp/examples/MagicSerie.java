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

package minicp.examples;

import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.search.SearchStatistics;
import minicp.util.InconsistencyException;

import java.util.Arrays;

import static minicp.cp.Factory.*;
import static minicp.cp.Factory.notEqual;
import static minicp.search.Selector.branch;
import static minicp.search.Selector.selectMin;


public class MagicSerie {
    public static void main(String[] args) throws InconsistencyException {

        int n = 8;
        Solver cp = makeSolver();

        IntVar[] s = makeIntVarArray(cp, n, n);

        for (int i = 0; i < n; i++) {
            final int fi = i;
            cp.post(sum(all(0,n-1,j -> isEqual(s[j],fi)),s[i]));
        }
        cp.post(sum(all(0,n-1,i -> mul(s[i],i)),n));
        cp.post(sum(all(0,n-1,i -> mul(s[i],i-1)),0));

        SearchStatistics stats = makeDfs(cp,
                selectMin(s,
                        si -> si.getSize() > 1,
                        si -> si.getSize(),
                        si -> {
                            int v = si.getMin();
                            return branch(() -> equal(si,v),
                                          () -> notEqual(si,v));
                        }
                )
        ).onSolution(() ->
                System.out.println("solution:"+ Arrays.toString(s))
        ).start();

        System.out.format("#Solutions: %s\n", stats.nSolutions);
        System.out.format("Statistics: %s\n", stats);

    }}
