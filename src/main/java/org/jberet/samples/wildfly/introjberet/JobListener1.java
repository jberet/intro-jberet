/*
 * Copyright (c) 2017 Red Hat, Inc. and/or its affiliates.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Cheng Fang - Initial API and implementation
 */

package org.jberet.samples.wildfly.introjberet;

import javax.batch.api.listener.JobListener;
import javax.inject.Named;

/**
 * Job listener that copies certain database-related environment properties
 * to system properties, which are available to job xml properties.
 */
@Named
public class JobListener1 implements JobListener {

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeJob() throws Exception {
        System.out.printf("Copying certain environment variables to system properties...");
        copyEnv("POSTGRESQL_SERVICE_HOST", "db.host");
        copyEnv("POSTGRESQL_SERVICE_PORT", "db.port");
        copyEnv("POSTGRESQL_DATABASE", "db.name");
        copyEnv("POSTGRESQL_USER", "db.user");
        copyEnv("POSTGRESQL_PASSWORD", "db.password");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterJob() throws Exception {
    }

    private void copyEnv(final String envKey, final String sysKey) {
        if (System.getProperty(sysKey) == null) {
            final String envVal = System.getenv(envKey);
            if (envVal != null) {
                System.setProperty(sysKey, envVal);
                System.out.printf("Copied to system property: %s = %s%n", sysKey, envVal);
            }
        }
    }
}
