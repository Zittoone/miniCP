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

package minicp.search;

import minicp.reversible.Trail;
import minicp.util.InconsistencyException;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class DFSearch {

    private Choice choice;
    private Trail state;

    private List<SolutionListener> solutionListeners = new LinkedList<SolutionListener>();
    private List<FailListener> failListeners = new LinkedList<FailListener>();


    @FunctionalInterface
    public interface SolutionListener {
        void solutionFound();
    }
    public DFSearch onSolution(SolutionListener listener) {
        solutionListeners.add(listener);
        return this;
    }

    public void notifySolutionFound() {
        solutionListeners.forEach(s -> s.solutionFound());
    }

    @FunctionalInterface
    public interface FailListener {
        void failure();
    }

    public DFSearch onFail(FailListener listener) {
        failListeners.add(listener);
        return this;
    }

    public void notifyFailure() {
        failListeners.forEach(s -> s.failure());
    }

    public DFSearch(Trail state, Choice branching) {
        this.state = state;
        this.choice = branching;
    }

    public SearchStatistics start(SearchLimit limit) {
        SearchStatistics statistics = new SearchStatistics();
        int level = state.getLevel();
        try {
            dfs(statistics,limit);
        } catch (StopSearchException e) {}
        state.popUntil(level);
        return statistics;
    }

    public SearchStatistics start() {
        return start(statistics -> false);
    }


    /*
    * Recursive version
    */
    private void dfs(SearchStatistics statistics, SearchLimit limit) {
        if (limit.stopSearch(statistics)) throw new StopSearchException();
        Alternative [] alternatives = choice.call();
        if (alternatives.length == 0) {
            statistics.nSolutions++;
            notifySolutionFound();
        }
        else {
            for (Alternative alt : alternatives) {
                state.push();
                try {
                    statistics.nNodes++;
                    alt.call();
                    dfs(statistics,limit);
                } catch (InconsistencyException e) {
                    notifyFailure();
                    statistics.nFailures++;
                }
                state.pop();
            }
        }
    }

    /*
    * Iterative version using stacks
    */
    private void dfs_iterative(SearchStatistics statistics, SearchLimit limit) {

        Stack<Alternative> alternativeStack = new Stack<>();
        Stack<Integer> levelStack = new Stack<>();

        Alternative current;

        do {
            // Stopping condition
            if (limit.stopSearch(statistics)) throw new StopSearchException();

            Alternative[] alternatives = choice.call();

            // If this is not a leaf
            if (alternatives.length > 0) {

                state.push();
                levelStack.push(alternatives.length);

                for (Alternative alt : alternatives) {
                    alternativeStack.push(alt);
                    statistics.nNodes++;
                }

                // Current visited node is a the top of the stack
                current = alternativeStack.peek();

                // Only call on non-leafs
                try {
                    current.call();
                } catch (InconsistencyException e) {
                    notifyFailure();
                    statistics.nFailures++;
                }
            }
            // If it is a leaf
            else {
                //
                state.pop();
                alternativeStack.pop();
                statistics.nSolutions++;
                notifySolutionFound();
                state.push();
            }
        }
        while (!alternativeStack.isEmpty());
    }

}



