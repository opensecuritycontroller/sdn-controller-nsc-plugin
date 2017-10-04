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

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

public class LoggerProxy implements Logger {
    private Logger properLogger;
    private final Logger FALLBACK_IMPL;
    private final String className;
    private final ILoggerFactory loggerFactory;

    public LoggerProxy(String className, ILoggerFactory atomicFactoryRef) {
        this.className = className;
        this.FALLBACK_IMPL = LoggerFactory.getLogger(className);
        this.loggerFactory = atomicFactoryRef;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean isTraceEnabled() {
        if (this.properLogger != null) {
            return this.properLogger.isTraceEnabled();
        } else {
            return findImplToUse().isTraceEnabled();
        }
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        if (this.properLogger != null) {
            return this.properLogger.isTraceEnabled(marker);
        } else {
            return findImplToUse().isTraceEnabled(marker);
        }
    }

    @Override
    public void trace(String msg) {
        if (this.properLogger != null) {
            this.properLogger.trace(msg);
        } else {
            findImplToUse().trace(msg);
        }
    }

    @Override
    public void trace(String format, Object arg) {
        if (this.properLogger != null) {
            this.properLogger.trace(format, arg);
        } else {
            findImplToUse().trace(format, arg);
        }
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        if (this.properLogger != null) {
            this.properLogger.trace(format, arg1, arg2);
        } else {
            findImplToUse().trace(format, arg1, arg2);
        }
    }

    @Override
    public void trace(String format, Object... arguments) {
        if (this.properLogger != null) {
            this.properLogger.trace(format, arguments);
        } else {
            findImplToUse().trace(format, arguments);
        }
    }

    @Override
    public void trace(String msg, Throwable t) {
        if (this.properLogger != null) {
            this.properLogger.trace(msg, t);
        } else {
            findImplToUse().trace(msg, t);
        }
    }

    @Override
    public void trace(Marker marker, String msg) {
        if (this.properLogger != null) {
            this.properLogger.trace(marker, msg);
        } else {
            findImplToUse().trace(marker, msg);
        }
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        if (this.properLogger != null) {
            this.properLogger.trace(marker, format, arg);
        } else {
            findImplToUse().trace(marker, format, arg);
        }
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        if (this.properLogger != null) {
            this.properLogger.trace(marker, format, arg1, arg2);
        } else {
            findImplToUse().trace(marker, format, arg1, arg2);
        }
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        if (this.properLogger != null) {
            this.properLogger.trace(marker, format, argArray);
        } else {
            findImplToUse().trace(marker, format, argArray);
        }
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        if (this.properLogger != null) {
            this.properLogger.trace(marker, msg, t);
        } else {
            findImplToUse().trace(marker, msg, t);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        if (this.properLogger != null) {
            return this.properLogger.isDebugEnabled();
        } else {
            return findImplToUse().isDebugEnabled();
        }
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        if (this.properLogger != null) {
            return this.properLogger.isDebugEnabled(marker);
        } else {
            return findImplToUse().isDebugEnabled(marker);
        }
    }

    @Override
    public void debug(String msg) {
        if (this.properLogger != null) {
            this.properLogger.debug(msg);
        } else {
            findImplToUse().debug(msg);
        }
    }

    @Override
    public void debug(String format, Object arg) {
        if (this.properLogger != null) {
            this.properLogger.debug(format, arg);
        } else {
            findImplToUse().debug(format, arg);
        }
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (this.properLogger != null) {
            this.properLogger.debug(format, arg1, arg2);
        } else {
            findImplToUse().debug(format, arg1, arg2);
        }
    }

    @Override
    public void debug(String format, Object... arguments) {
        if (this.properLogger != null) {
            this.properLogger.debug(format, arguments);
        } else {
            findImplToUse().debug(format, arguments);
        }
    }

    @Override
    public void debug(String msg, Throwable t) {
        if (this.properLogger != null) {
            this.properLogger.debug(msg, t);
        } else {
            findImplToUse().debug(msg, t);
        }
    }

    @Override
    public void debug(Marker marker, String msg) {
        if (this.properLogger != null) {
            this.properLogger.debug(marker, msg);
        } else {
            findImplToUse().debug(marker, msg);
        }
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        if (this.properLogger != null) {
            this.properLogger.debug(marker, format, arg);
        } else {
            findImplToUse().debug(marker, format, arg);
        }
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        if (this.properLogger != null) {
            this.properLogger.debug(marker, format, arg1, arg2);
        } else {
            findImplToUse().debug(marker, format, arg1, arg2);
        }
    }

    @Override
    public void debug(Marker marker, String format, Object... argArray) {
        if (this.properLogger != null) {
            this.properLogger.debug(marker, format, argArray);
        } else {
            findImplToUse().debug(marker, format, argArray);
        }
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        if (this.properLogger != null) {
            this.properLogger.debug(marker, msg, t);
        } else {
            findImplToUse().debug(marker, msg, t);
        }
    }

    @Override
    public boolean isInfoEnabled() {
        if (this.properLogger != null) {
            return this.properLogger.isInfoEnabled();
        } else {
            return findImplToUse().isInfoEnabled();
        }
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        if (this.properLogger != null) {
            return this.properLogger.isInfoEnabled(marker);
        } else {
            return findImplToUse().isInfoEnabled(marker);
        }
    }

    @Override
    public void info(String msg) {
        if (this.properLogger != null) {
            this.properLogger.info(msg);
        } else {
            findImplToUse().info(msg);
        }
    }

    @Override
    public void info(String format, Object arg) {
        if (this.properLogger != null) {
            this.properLogger.info(format, arg);
        } else {
            findImplToUse().info(format, arg);
        }
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (this.properLogger != null) {
            this.properLogger.info(format, arg1, arg2);
        } else {
            findImplToUse().info(format, arg1, arg2);
        }
    }

    @Override
    public void info(String format, Object... arguments) {
        if (this.properLogger != null) {
            this.properLogger.info(format, arguments);
        } else {
            findImplToUse().info(format, arguments);
        }
    }

    @Override
    public void info(String msg, Throwable t) {
        if (this.properLogger != null) {
            this.properLogger.info(msg, t);
        } else {
            findImplToUse().info(msg, t);
        }
    }

    @Override
    public void info(Marker marker, String msg) {
        if (this.properLogger != null) {
            this.properLogger.info(marker, msg);
        } else {
            findImplToUse().info(marker, msg);
        }
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        if (this.properLogger != null) {
            this.properLogger.info(marker, format, arg);
        } else {
            findImplToUse().info(marker, format, arg);
        }
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        if (this.properLogger != null) {
            this.properLogger.info(marker, format, arg1, arg2);
        } else {
            findImplToUse().info(marker, format, arg1, arg2);
        }
    }

    @Override
    public void info(Marker marker, String format, Object... argArray) {
        if (this.properLogger != null) {
            this.properLogger.info(marker, format, argArray);
        } else {
            findImplToUse().info(marker, format, argArray);
        }
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        if (this.properLogger != null) {
            this.properLogger.info(marker, msg, t);
        } else {
            findImplToUse().info(marker, msg, t);
        }
    }

    @Override
    public boolean isWarnEnabled() {
        if (this.properLogger != null) {
            return this.properLogger.isWarnEnabled();
        } else {
            return findImplToUse().isWarnEnabled();
        }
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        if (this.properLogger != null) {
            return this.properLogger.isWarnEnabled(marker);
        } else {
            return findImplToUse().isWarnEnabled(marker);
        }
    }

    @Override
    public void warn(String msg) {
        if (this.properLogger != null) {
            this.properLogger.warn(msg);
        } else {
            findImplToUse().warn(msg);
        }
    }

    @Override
    public void warn(String format, Object arg) {
        if (this.properLogger != null) {
            this.properLogger.warn(format, arg);
        } else {
            findImplToUse().warn(format, arg);
        }
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (this.properLogger != null) {
            this.properLogger.warn(format, arg1, arg2);
        } else {
            findImplToUse().warn(format, arg1, arg2);
        }
    }

    @Override
    public void warn(String format, Object... arguments) {
        if (this.properLogger != null) {
            this.properLogger.warn(format, arguments);
        } else {
            findImplToUse().warn(format, arguments);
        }
    }

    @Override
    public void warn(String msg, Throwable t) {
        if (this.properLogger != null) {
            this.properLogger.warn(msg, t);
        } else {
            findImplToUse().warn(msg, t);
        }
    }

    @Override
    public void warn(Marker marker, String msg) {
        if (this.properLogger != null) {
            this.properLogger.warn(marker, msg);
        } else {
            findImplToUse().warn(marker, msg);
        }
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        if (this.properLogger != null) {
            this.properLogger.warn(marker, format, arg);
        } else {
            findImplToUse().warn(marker, format, arg);
        }
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        if (this.properLogger != null) {
            this.properLogger.warn(marker, format, arg1, arg2);
        } else {
            findImplToUse().warn(marker, format, arg1, arg2);
        }
    }

    @Override
    public void warn(Marker marker, String format, Object... argArray) {
        if (this.properLogger != null) {
            this.properLogger.warn(marker, format, argArray);
        } else {
            findImplToUse().warn(marker, format, argArray);
        }
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        if (this.properLogger != null) {
            this.properLogger.warn(marker, msg, t);
        } else {
            findImplToUse().warn(marker, msg, t);
        }
    }

    @Override
    public boolean isErrorEnabled() {
        if (this.properLogger != null) {
            return this.properLogger.isErrorEnabled();
        } else {
            return findImplToUse().isErrorEnabled();
        }
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        if (this.properLogger != null) {
            return this.properLogger.isErrorEnabled(marker);
        } else {
            return findImplToUse().isErrorEnabled(marker);
        }
    }

    @Override
    public void error(String msg) {
        if (this.properLogger != null) {
            this.properLogger.error(msg);
        } else {
            findImplToUse().error(msg);
        }
    }

    @Override
    public void error(String format, Object arg) {
        if (this.properLogger != null) {
            this.properLogger.error(format, arg);
        } else {
            findImplToUse().error(format, arg);
        }
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        if (this.properLogger != null) {
            this.properLogger.error(format, arg1, arg2);
        } else {
            findImplToUse().error(format, arg1, arg2);
        }
    }

    @Override
    public void error(String format, Object... arguments) {
        if (this.properLogger != null) {
            this.properLogger.error(format, arguments);
        } else {
            findImplToUse().error(format, arguments);
        }
    }

    @Override
    public void error(String msg, Throwable t) {
        if (this.properLogger != null) {
            this.properLogger.error(msg, t);
        } else {
            findImplToUse().error(msg, t);
        }
    }

    @Override
    public void error(Marker marker, String msg) {
        if (this.properLogger != null) {
            this.properLogger.error(marker, msg);
        } else {
            findImplToUse().error(marker, msg);
        }
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        if (this.properLogger != null) {
            this.properLogger.error(marker, format, arg);
        } else {
            findImplToUse().error(marker, format, arg);
        }
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        if (this.properLogger != null) {
            this.properLogger.error(marker, format, arg1, arg2);
        } else {
            findImplToUse().error(marker, format, arg1, arg2);
        }
    }

    @Override
    public void error(Marker marker, String format, Object... argArray) {
        if (this.properLogger != null) {
            this.properLogger.error(marker, format, argArray);
        } else {
            findImplToUse().error(marker, format, argArray);
        }
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        if (this.properLogger != null) {
            this.properLogger.error(marker, msg, t);
        } else {
            findImplToUse().error(marker, msg, t);
        }
    }

    private Logger findImplToUse() {
        if (this.loggerFactory != null) {
            Logger implToUse = this.loggerFactory.getLogger(this.className);
            synchronized (this) {
                if (this.properLogger == null) {
                    this.properLogger = implToUse;
                }
            }
            return this.properLogger;
        } else {
            return this.FALLBACK_IMPL;
        }
    }
}
