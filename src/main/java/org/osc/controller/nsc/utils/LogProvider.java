/*******************************************************************************
 * Copyright (c) Intel Corporation
 * Copyright (c) 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.osc.controller.nsc.utils;

import static org.osgi.service.component.annotations.ReferencePolicyOption.GREEDY;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

@Component
public class LogProvider {
    static ILoggerFactory loggerFactory;

    @Reference(cardinality=ReferenceCardinality.OPTIONAL, policyOption=GREEDY)
    public void setLoggerFactoryInst(ILoggerFactory instance) {
        setLoggerFactory(instance);
    }

    public static Logger getLogger(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Attempt to get logger for null class!!");
        }

        return new LoggerProxy(clazz.getName());
    }

    public static void setLoggerFactory(ILoggerFactory instance) {
        loggerFactory = instance;
    }
}
