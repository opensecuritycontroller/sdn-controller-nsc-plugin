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
package org.osc.controller.nsc;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.service.transaction.control.ScopedWorkException;
import org.osgi.service.transaction.control.TransactionContext;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.TransactionException;
import org.osgi.service.transaction.control.TransactionRolledBackException;

@RunWith(MockitoJUnitRunner.class)
abstract class TestTransactionControl implements TransactionControl, TransactionContext {

    private EntityManager txCtrlEm;
    private EntityTransaction tx;

    private AtomicBoolean txActive;

    public void init(EntityManager em) {
        this.txCtrlEm = em;
        this.txActive = new AtomicBoolean();
    }

    @Override
    public <T> T required(Callable<T> arg0)
            throws TransactionException, TransactionRolledBackException, ScopedWorkException {
        if(this.txActive.getAndSet(true)) {
            // inherit the existing tran
            try {
                return arg0.call();
            } catch (Exception e) {
                if(e instanceof ScopedWorkException) {
                    throw (ScopedWorkException) e;
                }
                throw new ScopedWorkException("The work failed", e, getCurrentContext());
            }
        } else {
            return runInTran(arg0);
        }
    }

    private <T> T runInTran(Callable<T> arg0) {
        this.tx = this.txCtrlEm.getTransaction();
        try {
            this.tx.begin();
            T o = arg0.call();
            this.tx.commit();

            return o;
        } catch (Exception e) {
            this.tx.rollback();
            if(e instanceof ScopedWorkException) {
                throw (ScopedWorkException) e;
            }
            throw new ScopedWorkException("The work failed", e, getCurrentContext());
        } finally {
            this.txActive.compareAndSet(true, false);
            this.txCtrlEm.clear();
        }
    }

    @Override
    public <T> T requiresNew(Callable<T> arg0)
            throws TransactionException, TransactionRolledBackException, ScopedWorkException {
        if(this.txActive.getAndSet(true)) {
            Assert.fail("The test transaction control does not support nested transactions");

            // This line is never actually reached but is needed by the compiler
            return null;
        } else {
            return runInTran(arg0);
        }
    }

    @Override
    public boolean activeScope() {
        return this.txActive.get();
    }

    @Override
    public boolean activeTransaction() {
        return this.txActive.get();
    }
}
