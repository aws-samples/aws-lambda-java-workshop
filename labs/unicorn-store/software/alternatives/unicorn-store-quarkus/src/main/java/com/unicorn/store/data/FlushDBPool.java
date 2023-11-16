package com.unicorn.store.data;

import io.agroal.api.AgroalDataSource;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.crac.Core;
import org.crac.Resource;

@Startup
@ApplicationScoped
public class FlushDBPool implements Resource {
    private AgroalDataSource dataSource;

    public FlushDBPool(AgroalDataSource dataSource) {
        this.dataSource = dataSource;
    }

    void onStart(@Observes StartupEvent ev) {
        Core.getGlobalContext().register(this);
    }

    @Override
    public void beforeCheckpoint(org.crac.Context<? extends Resource> context)
            throws Exception {
        //Nothing to do here
    }
    @Override
    public void afterRestore(org.crac.Context<? extends Resource> context)
            throws Exception {
        dataSource.flush(AgroalDataSource.FlushMode.ALL);
    }
}