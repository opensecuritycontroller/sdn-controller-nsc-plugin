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

import static java.util.Arrays.asList;

import org.osc.controller.nsc.entities.InspectionHookEntity;
import org.osc.controller.nsc.entities.InspectionPortEntity;
import org.osc.controller.nsc.entities.NetworkElementEntity;

public class SampleSdnRedirectionApiTestData {

    public static final String TEST_DB_URL_PREFIX = "jdbc:h2:";

    public static final String TEST_DB_FILENAME = "./nscPlugin_OSGiIntegrationTest";

    public static final String TEST_DB_URL_SUFFIX = ";MVCC\\=FALSE;LOCK_TIMEOUT\\=10000;MV_STORE=FALSE;";

    public static final String TEST_DB_URL = TEST_DB_URL_PREFIX + TEST_DB_FILENAME + TEST_DB_URL_SUFFIX;

    public static final String EADDR2_STR = "192.168.0.12";

    public static final String EADDR1_STR = "192.168.0.11";

    public static final String IADDR2_STR = "10.4.3.2";

    public static final String IADDR1_STR = "10.4.3.1";

    public static final String EMAC2_STR = "ee:ff:aa:bb:cc:02";

    public static final String EMAC1_STR = "ee:ff:aa:bb:cc:01";

    public static final String IMAC1_STR = "ff:ff:aa:bb:cc:01";

    public static final String IMAC2_STR = "ff:ff:aa:bb:cc:02";

    public static final String INSPMAC1_STR = "aa:aa:aa:bb:cc:01";

    public static InspectionHookEntity inspectionHook;
    public static InspectionPortEntity inspectionPort;

    public static NetworkElementEntity ingress;
    public static NetworkElementEntity egress;
    public static NetworkElementEntity inspected;

    public static void setupDataObjects() {
        inspectionHook = new InspectionHookEntity();

        inspectionPort = new InspectionPortEntity();

        ingress = new NetworkElementEntity();
        egress = new NetworkElementEntity();
        inspected = new NetworkElementEntity();

        ingress.setElementId(IMAC1_STR + IMAC1_STR);
        egress.setElementId(EMAC1_STR + EMAC1_STR);
        inspected.setElementId("iNsPeCtEdPoRt");

        ingress.setMacAddresses(asList(IMAC1_STR, IMAC2_STR));
        ingress.setPortIPs(asList(IADDR1_STR, IADDR2_STR));

        egress.setMacAddresses(asList(EMAC1_STR, EMAC2_STR));
        egress.setPortIPs(asList(EADDR1_STR, EADDR2_STR));

        inspected.setMacAddresses(asList(INSPMAC1_STR));

        inspected.setInspectionHook(inspectionHook);

        inspectionPort.setIngressPort(ingress);
        inspectionPort.setEgressPort(egress);
        inspectionHook.setInspectedPort(inspected);

        inspectionHook.setInspectionPort(inspectionPort);
    }
}
