/*
 *  Copyright (c) 2022 sovity GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       sovity GmbH - initial API and implementation
 *
 */
package de.sovity.extension.clearinghouse.service;

import de.sovity.extension.clearinghouse.sender.message.LogMessage;
import org.eclipse.edc.connector.contract.spi.event.contractnegotiation.ContractNegotiationFinalized;
import org.eclipse.edc.connector.contract.spi.negotiation.store.ContractNegotiationStore;
import org.eclipse.edc.connector.contract.spi.types.agreement.ContractAgreement;
import org.eclipse.edc.connector.transfer.spi.event.TransferProcessTerminated;
import org.eclipse.edc.connector.transfer.spi.store.TransferProcessStore;
import org.eclipse.edc.connector.transfer.spi.types.TransferProcess;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.event.Event;
import org.eclipse.edc.spi.event.EventEnvelope;
import org.eclipse.edc.spi.event.EventSubscriber;
import org.eclipse.edc.spi.message.RemoteMessageDispatcherRegistry;
import org.eclipse.edc.spi.monitor.Monitor;
import org.eclipse.edc.spi.system.Hostname;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

public class IdsClearingHouseServiceImpl implements IdsClearingHouseService, EventSubscriber {

    private static final String CONTEXT_CLEARINGHOUSE = "ClearingHouse";

    private final RemoteMessageDispatcherRegistry dispatcherRegistry;
    private final URI connectorBaseUrl;
    private final URL clearingHouseLogUrl;
    private final ContractNegotiationStore contractNegotiationStore;
    private final TransferProcessStore transferProcessStore;
    private final Monitor monitor;

    public IdsClearingHouseServiceImpl(
            RemoteMessageDispatcherRegistry dispatcherRegistry,
            Hostname hostname,
            URL clearingHouseLogUrl,
            ContractNegotiationStore contractNegotiationStore,
            TransferProcessStore transferProcessStore,
            Monitor monitor) {
        this.dispatcherRegistry = dispatcherRegistry;
        this.clearingHouseLogUrl = clearingHouseLogUrl;
        this.contractNegotiationStore = contractNegotiationStore;
        this.transferProcessStore = transferProcessStore;
        this.monitor = monitor;

        try {
            connectorBaseUrl = getConnectorBaseUrl(hostname);
        } catch (URISyntaxException e) {
            throw new EdcException("Could not create connectorBaseUrl. Hostname can be set using:" +
                    " edc.hostname", e);
        }
    }

    @Override
    public void logContractAgreement(ContractAgreement contractAgreement, URL clearingHouseLogUrl) {
        monitor.info("Logging contract agreement to ClearingHouse");
        var logMessage = new LogMessage(clearingHouseLogUrl, connectorBaseUrl, contractAgreement);
        dispatcherRegistry.send(Object.class, logMessage, () -> CONTEXT_CLEARINGHOUSE);
    }

    @Override
    public void logTransferProcess(TransferProcess transferProcess, URL clearingHouseLogUrl) {
        monitor.info("Logging transferprocess to ClearingHouse");
        var logMessage = new LogMessage(clearingHouseLogUrl, connectorBaseUrl, transferProcess);
        dispatcherRegistry.send(Object.class, logMessage, () -> CONTEXT_CLEARINGHOUSE);
    }

    @Override
    public <E extends Event> void on(EventEnvelope<E> event) {
        try {
            if (event.getPayload() instanceof ContractNegotiationFinalized contractNegotiationFinalized) {
                var contractAgreement = resolveContractAgreement(contractNegotiationFinalized);
                var pid = UUID.nameUUIDFromBytes(contractAgreement.getId().getBytes()).toString();
                var extendedUrl = new URL(clearingHouseLogUrl + "/" + pid);
                logContractAgreement(contractAgreement, extendedUrl);
            } else if (event.getPayload() instanceof TransferProcessTerminated transferProcessTerminated) {
                var transferProcess = resolveTransferProcess(transferProcessTerminated);
                var pid = UUID.nameUUIDFromBytes(transferProcess.getId().getBytes()).toString();
                var extendedUrl = new URL(clearingHouseLogUrl + "/" + pid);
                logTransferProcess(transferProcess, extendedUrl);
            }
        } catch (Exception e) {
            throw new EdcException("Could not create extended clearinghouse url.");
        }
    }

    private ContractAgreement resolveContractAgreement(ContractNegotiationFinalized contractNegotiationFinalized) {
        var contractNegotiationId = contractNegotiationFinalized.getContractNegotiationId();
        var contractNegotiation = contractNegotiationStore.findById(contractNegotiationId);
        return contractNegotiationFinalized.getContractAgreement();
    }

    private TransferProcess resolveTransferProcess(TransferProcessTerminated trransferProcessTerminated) {
        var transferProcessId = trransferProcessTerminated.getTransferProcessId();
        return transferProcessStore.findById(transferProcessId);
    }

    private URI getConnectorBaseUrl(Hostname hostname) throws URISyntaxException {
        return new URI(String.format("http://%s/", hostname.get()));
    }
}
