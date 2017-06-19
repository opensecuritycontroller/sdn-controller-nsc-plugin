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
package org.osc.controller.nsc.api.jcloud;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import com.google.common.io.Closeables;

/**
 * Desgined to be a base class for all jcloud API wrappers in the code.
 */
public abstract class BaseJCloudApi implements Closeable {

    protected Endpoint endPoint;

    public BaseJCloudApi(Endpoint endPoint) {
        this.endPoint = endPoint;
    }

    @Override
    public void close() throws IOException {
        for (Closeable api : getApis()) {
            Closeables.close(api, true);
        }
    }

    /**
     * List of API's the subclass uses.
     *
     * @return the API's used by the subclass.
     */
    protected abstract List<? extends Closeable> getApis();

}
