package org.geogebra.common.kernel.stepbystep.steps;

import org.geogebra.common.kernel.stepbystep.steptree.*;

import java.util.ArrayList;
import java.util.List;

public class SolveTracker {

    private StepLogical restriction;
    private StepSet undefinedPoints;
    private List<StepSolvable> conditions;

    private int arbConstTracker;

    private boolean shouldCheckSolutions;
    private Boolean approximate;

    public Boolean isApproximate() {
        return approximate;
    }

    public void setApproximate(Boolean approximate) {
        this.approximate = approximate;
    }

    public void setShouldCheckSolutions() {
        shouldCheckSolutions = true;
    }

    public boolean shouldCheckSolutions() {
        return shouldCheckSolutions;
    }

    public void addRestriction(StepInterval restriction) {
        this.restriction = StepNode.intersect(this.restriction, restriction);
    }

    public void addCondition(StepSolvable condition) {
        if (conditions == null) {
            conditions = new ArrayList<>();
        }

        conditions.add(condition);
    }

    public List<StepSolvable> getConditions() {
        return conditions;
    }

    public void addUndefinedPoint(StepExpression point) {
        if (undefinedPoints == null) {
            undefinedPoints = new StepSet(point);
        } else {
            undefinedPoints.addElement(point);
        }
    }

    public StepLogical getRestriction() {
        if (restriction == null) {
            return StepInterval.R;
        }

        return restriction;
    }

    public StepSet getUndefinedPoints() {
        return undefinedPoints;
    }

    public boolean shouldCheck() {
        return shouldCheckSolutions;
    }

    public StepArbitraryConstant getNextArbInt() {
        return new StepArbitraryConstant("k", ++arbConstTracker, StepArbitraryConstant.ConstantType.INTEGER);
    }
}