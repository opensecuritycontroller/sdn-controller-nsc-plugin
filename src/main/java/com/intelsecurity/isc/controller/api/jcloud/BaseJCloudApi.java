package com.intelsecurity.isc.controller.api.jcloud;

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
