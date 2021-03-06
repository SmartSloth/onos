/*
 * Copyright 2019-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.k8snode.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.onosproject.k8snode.api.K8sNode;
import org.onosproject.net.Device;
import org.onosproject.net.behaviour.BridgeConfig;
import org.onosproject.net.behaviour.BridgeName;
import org.onosproject.net.device.DeviceService;
import org.onosproject.ovsdb.controller.OvsdbClientService;
import org.onosproject.ovsdb.controller.OvsdbController;
import org.onosproject.ovsdb.controller.OvsdbNodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Dictionary;

import static org.onlab.util.Tools.get;

/**
 * An utility that used in kubernetes node app.
 */
public final class K8sNodeUtil {
    private static final Logger log = LoggerFactory.getLogger(K8sNodeUtil.class);

    /**
     * Prevents object installation from external.
     */
    private K8sNodeUtil() {
    }

    /**
     * Checks whether the controller has a connection with an OVSDB that resides
     * inside the given kubernetes node.
     *
     * @param node              kubernetes node
     * @param ovsdbPort         OVSDB port
     * @param ovsdbController   OVSDB controller
     * @param deviceService     device service
     * @return true if the controller is connected to the OVSDB, false otherwise
     */
    public static boolean isOvsdbConnected(K8sNode node,
                                           int ovsdbPort,
                                           OvsdbController ovsdbController,
                                           DeviceService deviceService) {
        OvsdbClientService client = getOvsdbClient(node, ovsdbPort, ovsdbController);
        return deviceService.isAvailable(node.ovsdb()) &&
                client != null &&
                client.isConnected();
    }

    /**
     * Gets the ovsdb client with supplied openstack node.
     *
     * @param node              kubernetes node
     * @param ovsdbPort         ovsdb port
     * @param ovsdbController   ovsdb controller
     * @return ovsdb client
     */
    public static OvsdbClientService getOvsdbClient(K8sNode node,
                                                    int ovsdbPort,
                                                    OvsdbController ovsdbController) {
        OvsdbNodeId ovsdb = new OvsdbNodeId(node.managementIp(), ovsdbPort);
        return ovsdbController.getOvsdbClient(ovsdb);
    }

    /**
     * Adds or removes a network interface (aka port) into a given bridge of kubernetes node.
     *
     * @param k8sNode       kubernetes node
     * @param bridgeName    bridge name
     * @param intfName      interface name
     * @param deviceService device service
     * @param addOrRemove   add port is true, remove it otherwise
     */
    public static synchronized void addOrRemoveSystemInterface(K8sNode k8sNode,
                                                               String bridgeName,
                                                               String intfName,
                                                               DeviceService deviceService,
                                                               boolean addOrRemove) {


        Device device = deviceService.getDevice(k8sNode.ovsdb());
        if (device == null || !device.is(BridgeConfig.class)) {
            log.info("device is null or this device if not ovsdb device");
            return;
        }
        BridgeConfig bridgeConfig =  device.as(BridgeConfig.class);

        if (addOrRemove) {
            bridgeConfig.addPort(BridgeName.bridgeName(bridgeName), intfName);
        } else {
            bridgeConfig.deletePort(BridgeName.bridgeName(bridgeName), intfName);
        }
    }

    /**
     * Gets Boolean property from the propertyName
     * Return null if propertyName is not found.
     *
     * @param properties   properties to be looked up
     * @param propertyName the name of the property to look up
     * @return value when the propertyName is defined or return null
     */
    public static Boolean getBooleanProperty(Dictionary<?, ?> properties,
                                             String propertyName) {
        Boolean value;
        try {
            String s = get(properties, propertyName);
            value = Strings.isNullOrEmpty(s) ? null : Boolean.valueOf(s);
        } catch (ClassCastException e) {
            value = null;
        }
        return value;
    }

    /**
     * Prints out the JSON string in pretty format.
     *
     * @param mapper        Object mapper
     * @param jsonString    JSON string
     * @return pretty formatted JSON string
     */
    public static String prettyJson(ObjectMapper mapper, String jsonString) {
        try {
            Object jsonObject = mapper.readValue(jsonString, Object.class);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
        } catch (IOException e) {
            log.debug("Json string parsing exception caused by {}", e);
        }
        return null;
    }
}
