package com.onetick.dto.request;

import com.onetick.entity.enums.GoalStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class UpdateGoalProgressRequest {
    @NotNull
    @PositiveOrZero
    private Integer currentValue;

    private GoalStatus status;

    public Integer getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(Integer currentValue) {
        this.currentValue = currentValue;
    }

    public GoalStatus getStatus() {
        return status;
    }

    public void setStatus(GoalStatus status) {
        this.status = status;
    }
}
