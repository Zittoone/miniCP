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

import minicp.engine.core.IntVar;
import minicp.engine.core.Solver;
import minicp.util.InconsistencyException;
import org.junit.Test;

import static org.junit.Assert.*;
import static minicp.cp.Factory.*;


public class NotEqualTest {

    @Test
    public void notEqualTest() {
        Solver cp  = makeSolver();

        IntVar x = makeIntVar(cp,10);
        IntVar y = makeIntVar(cp,10);

        try {
            cp.post(notEqual(x,y));

            equal(x,6);

            assertFalse(y.contains(6));
            assertEquals(9,y.getSize());

        } catch (InconsistencyException e) {
            assert(false);
        }
        assertFalse(y.contains(6));
    }

}
