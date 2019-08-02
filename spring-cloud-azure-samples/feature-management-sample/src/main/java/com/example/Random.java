package com.example;

import org.springframework.stereotype.Component;

import com.microsoft.azure.spring.cloud.feature.manager.FeatureFilter;
import com.microsoft.azure.spring.cloud.feature.manager.entities.FeatureFilterEvaluationContext;

@Component("Random")
public class Random implements FeatureFilter{

    @Override
    public boolean evaluate(FeatureFilterEvaluationContext context) {
        double rand = Math.random();
        int c = Integer.valueOf((String) context.getParameters().get("chance"));
        double chance = (double) c/100;
        return rand > chance; 
    }

}
