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
import org.osc.controller.nsc.entities.PortEntity;

class TestData {

    private static final String EADDR2_STR = "192.168.0.12";

    private static final String EADDR1_STR = "192.168.0.11";

    private static final String IADDR2_STR = "10.4.3.2";

    private static final String IADDR1_STR = "10.4.3.1";

    private static final String EMAC2_STR = "ee:ff:aa:bb:cc:02";

    private static final String EMAC1_STR = "ee:ff:aa:bb:cc:01";

    private static final String IMAC1_STR = "ff:ff:aa:bb:cc:01";

    private static final String IMAC2_STR = "ff:ff:aa:bb:cc:02";

    private static final String ADDR1_STR = "192.168.0.14";

    private static final String ADDR2_STR = "10.4.3.3";

    private static final String MAC1_STR = "dd:ff:aa:bb:cc:01";

    private static final String MAC2_STR = "dd:ff:aa:bb:cc:02";

    private static final String INSPMAC1_STR = "aa:aa:aa:bb:cc:01";

    public static InspectionHookEntity inspectionHook;
    public static InspectionPortEntity inspectionPort;

    public static PortEntity ingress;
    public static PortEntity egress;
    public static PortEntity inspected;
    public static PortEntity port;

    public static void setupDataObjects() {
        inspectionHook = new InspectionHookEntity();

        inspectionPort = new InspectionPortEntity();

        ingress = new PortEntity();
        egress = new PortEntity();
        inspected = new PortEntity();
        port = new PortEntity();

        ingress.setElementId(IMAC1_STR + IMAC1_STR);
        egress.setElementId(EMAC1_STR + EMAC1_STR);
        inspected.setElementId("iNsPeCtEdPoRt");
        port.setElementId(MAC1_STR + MAC2_STR);

        ingress.setMacAddresses(asList(IMAC1_STR, IMAC2_STR));
        ingress.setPortIPs(asList(IADDR1_STR, IADDR2_STR));

        egress.setMacAddresses(asList(EMAC1_STR, EMAC2_STR));
        egress.setPortIPs(asList(EADDR1_STR, EADDR2_STR));

        port.setMacAddresses(asList(MAC1_STR, MAC2_STR));
        port.setPortIPs(asList(ADDR1_STR, ADDR2_STR));

        inspected.setMacAddresses(asList(INSPMAC1_STR));

        inspected.setInspectionHook(inspectionHook);

        inspectionPort.setIngressPort(ingress);
        inspectionPort.setEgressPort(egress);
        inspectionHook.setInspectedPort(inspected);

        inspectionHook.setInspectionPort(inspectionPort);
    }
}
