/*
 * Copyright (c) 2018 Zte Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.detnet.common.util;

import com.google.common.base.Optional;

import java.util.concurrent.ExecutionException;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class DataOperator {

    private static final Logger LOG = LoggerFactory.getLogger(DataOperator.class);

    private DataOperator() {}

    public static <T extends DataObject> T readData(DataBroker dataBroker,
                                                    InstanceIdentifier<T> path) {
        return readData(dataBroker, path, LogicalDatastoreType.CONFIGURATION);
    }

    public static <T extends DataObject> T readData(DataBroker dataBroker,
                                                    InstanceIdentifier<T> path, LogicalDatastoreType datastoreType) {
        T data = null;
        final ReadOnlyTransaction transaction = dataBroker.newReadOnlyTransaction();
        Optional<T> optionalData;
        try {
            optionalData = transaction.read(datastoreType, path).checkedGet();
            if (optionalData.isPresent()) {
                data = optionalData.get();
            }
        } catch (ReadFailedException e) {
            LOG.warn("Failed to read {} ", path, e);
        }
        transaction.close();
        return data;
    }

    public static <T extends DataObject> T readNetconfData(
            String nodeId, InstanceIdentifier<T> path, LogicalDatastoreType datastoreType) {
        DataBroker nodeDataBroker = NodeDataBroker.getInstance().getNodeDataBroker(nodeId);
        if (null == nodeDataBroker) {
            return null;
        }
        return readData(nodeDataBroker, path, datastoreType);
    }


    public static <T extends DataObject> boolean writeData(
            OperateType type, DataBroker dataBroker, InstanceIdentifier<T> path, T data) {
        return writeData(type, dataBroker, path, data, LogicalDatastoreType.CONFIGURATION);
    }

    public static <T extends DataObject> boolean writeData(
            OperateType type, DataBroker dataBroker, InstanceIdentifier<T> path,
            T data, LogicalDatastoreType datastoreType) {

        final WriteTransaction writeTransaction = dataBroker.newWriteOnlyTransaction();
        switch (type) {
            case PUT:
                writeTransaction.put(datastoreType, path, data, true);
                break;
            case MERGE:
                writeTransaction.merge(datastoreType, path, data, true);
                break;
            case DELETE:
                writeTransaction.delete(datastoreType, path);
                break;
            default:
                break;
        }
        try {
            writeTransaction.submit().get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.info("Filed to write {}", path);
            return false;
        }

        return true;
    }

    public static <T extends DataObject> boolean writeNetconfData(
            String nodeId, OperateType type, InstanceIdentifier<T> path, T data, LogicalDatastoreType datastoreType) {
        DataBroker nodeDataBroker = NodeDataBroker.getInstance().getNodeDataBroker(nodeId);
        LOG.debug("Node data broker for nodeId:{} is: {}", nodeId, nodeDataBroker);
        return null != nodeDataBroker && writeData(type, nodeDataBroker, path, data, datastoreType);
    }

    public enum OperateType {
        PUT,
        MERGE,
        DELETE
    }
}
