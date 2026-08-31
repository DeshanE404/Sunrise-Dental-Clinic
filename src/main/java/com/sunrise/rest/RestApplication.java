package com.sunrise.rest;

import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class RestApplication extends Application {
    private final Set<Class<?>> classes = new HashSet<>();
    private final Set<Object> singletons = new HashSet<>();

    public RestApplication() {
        // Register all REST resource classes
        classes.add(AppointmentResource.class);
        classes.add(BillResource.class);
        classes.add(DentistResource.class);
        classes.add(TreatmentResource.class);
        classes.add(ReportResource.class);
        classes.add(GenericExceptionMapper.class);
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}
