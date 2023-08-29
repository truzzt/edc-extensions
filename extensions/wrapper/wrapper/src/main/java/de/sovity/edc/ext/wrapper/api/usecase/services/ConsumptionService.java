package de.sovity.edc.ext.wrapper.api.usecase.services;

import de.sovity.edc.ext.wrapper.api.usecase.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.edc.connector.contract.spi.negotiation.store.ContractNegotiationStore;
import org.eclipse.edc.connector.contract.spi.types.negotiation.ContractNegotiation;
import org.eclipse.edc.connector.contract.spi.types.negotiation.ContractRequest;
import org.eclipse.edc.connector.contract.spi.types.offer.ContractOffer;
import org.eclipse.edc.connector.spi.contractnegotiation.ContractNegotiationService;
import org.eclipse.edc.connector.spi.transferprocess.TransferProcessService;
import org.eclipse.edc.connector.transfer.spi.store.TransferProcessStore;
import org.eclipse.edc.connector.transfer.spi.types.TransferRequest;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.spi.EdcException;
import org.eclipse.edc.spi.result.Result;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.edc.transform.spi.TypeTransformerRegistry;
import org.eclipse.edc.web.spi.exception.InvalidRequestException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.eclipse.edc.spi.CoreConstants.EDC_NAMESPACE;

/**
 * Service for managing consumption processes (= contract negotiation and subsequent data transfer).
 *
 * @author Ronja Quensel
 */
@RequiredArgsConstructor
@Slf4j
public class ConsumptionService {

    private final Map<String, ConsumptionDto> consumptionProcesses = new HashMap<>(); //TODO persist?

    private final ContractNegotiationService contractNegotiationService;
    private final TransferProcessService transferProcessService;
    private final ContractNegotiationStore contractNegotiationStore;
    private final TransferProcessStore transferProcessStore;
    private final TypeTransformerRegistry transformerRegistry;

    /**
     * Starts a consumption process for the asset specified in the input. Validates the input and
     * then triggers a contract negotiation with the specified provider. After the negotiation
     * is finalized, the corresponding transfer will be started after a call-back.
     *
     * @param consumptionInputDto the input for the process.
     * @return the process id.
     */
    public String startConsumptionProcess(ConsumptionInputDto consumptionInputDto) {
        var id = randomUUID().toString();
        var consumeDto = new ConsumptionDto(consumptionInputDto);
        consumptionProcesses.put(id, consumeDto);

        validateInput(consumptionInputDto);

        var policy = transformerRegistry.transform(consumptionInputDto.getPolicy(), Policy.class)
                .map(content -> content.withTarget(consumptionInputDto.getAssetId()))
                .orElseThrow(e -> new EdcException(format("Failed to transform PolicyDto: %s", e.getFailureDetail())));

        var contractOffer = ContractOffer.Builder.newInstance()
                .id(consumptionInputDto.getOfferId())
                .assetId(consumptionInputDto.getAssetId())
                .policy(policy)
                .build();

        var contractRequest = ContractRequest.Builder.newInstance()
                .contractOffer(contractOffer)
                .protocol("dataspace-protocol-http")
                .providerId(consumptionInputDto.getConnectorId())
                .counterPartyAddress(consumptionInputDto.getConnectorAddress())
                .build();

        var contractNegotiation = contractNegotiationService.initiateNegotiation(
                contractRequest);
        consumeDto.setContractNegotiationId(contractNegotiation.getId());

        return id;
    }

    /**
     * Method used for callback after the contract negotiation has been finalized. Will be called
     * by a corresponding listener. Starts the transfer as defined in the original process input.
     *
     * @param contractNegotiation the finalized contract negotiation.
     */
    public void negotiationConfirmed(ContractNegotiation contractNegotiation) {
        var process = findByNegotiation(contractNegotiation);

        if (process != null) {
            var agreementId = contractNegotiation.getContractAgreement().getId();

            var destination = createDataAddress(process.getInput().getDataDestination());

            var transferRequest = TransferRequest.Builder.newInstance()
                    .id(randomUUID().toString())
                    .connectorId(process.getInput().getConnectorId())
                    .connectorAddress(process.getInput().getConnectorAddress())
                    .protocol("dataspace-protocol-http")
                    .dataDestination(destination)
                    .assetId(process.getInput().getAssetId())
                    .contractId(agreementId)
                    .build();

            var result = transferProcessService.initiateTransfer(transferRequest);
            if (result.failed()) {
                process.getErrors().add(result.getFailureDetail());
            }

            process.setTransferProcessId(result.getContent().getId());
        }
    }

    /**
     * Returns information about a consumption process. Retrieves the corresponding contract
     * negotiation and transfer process, transforms them to an output format and returns them
     * together with other persisted information about the consumption process like the original
     * input.
     *
     * @param id the process id.
     * @return information about the process.
     */
    public ConsumptionOutputDto getConsumptionProcess(String id) {
        var process = consumptionProcesses.get(id);
        if (process == null) {
            return null;
        }

        var negotiationDto = Optional.ofNullable(process.getContractNegotiationId())
                .map(contractNegotiationStore::findById)
                .map(cn -> transformerRegistry.transform(cn, ContractNegotiationOutputDto.class))
                .map(this::throwIfFailedResult)
                .filter(Result::succeeded)
                .map(Result::getContent)
                .orElse(null);

        var transferProcessDto = Optional.ofNullable(process.getTransferProcessId())
                .map(transferProcessStore::findById)
                .map(tp -> transformerRegistry.transform(tp, TransferProcessOutputDto.class))
                .map(this::throwIfFailedResult)
                .filter(Result::succeeded)
                .map(Result::getContent)
                .orElse(null);

        return new ConsumptionOutputDto(id, process.getInput(), process.getErrors(),
                negotiationDto, transferProcessDto);
    }

    private void validateInput(ConsumptionInputDto input) {
        var message = "%s must not be null";

        if (input.getConnectorId() == null)
            throw new InvalidRequestException(format(message, "connectorId"));

        if (input.getConnectorAddress() == null)
            throw new InvalidRequestException(format(message, "connectorAddress"));

        if (input.getAssetId() == null)
            throw new InvalidRequestException(format(message, "assetId"));

        if (input.getOfferId() == null)
            throw new InvalidRequestException(format(message, "offerId"));

        if (input.getPolicy() == null)
            throw new InvalidRequestException(format(message, "policy"));

        var destination = input.getDataDestination();
        if (destination == null)
            throw new InvalidRequestException(format(message, "dataDestination"));

        if (!destination.containsKey("type") && !destination.containsKey(EDC_NAMESPACE + "type"))
            throw new InvalidRequestException("dataDestination must have type property.");
    }

    private ConsumptionDto findByNegotiation(ContractNegotiation contractNegotiation) {
        var id = contractNegotiation.getId();
        return consumptionProcesses.entrySet().stream()
                .filter(entry -> entry.getValue().getContractNegotiationId().equals(id))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    private DataAddress createDataAddress(Map<String, String> properties) {
        var nameSpacedProperties = properties.entrySet().stream()
                .map(entry -> {
                    if (isValidUri(entry.getKey())) {
                        return new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue());
                    }
                    var key = EDC_NAMESPACE + entry.getKey();
                    return new AbstractMap.SimpleEntry<>(key, entry.getValue());
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return DataAddress.Builder.newInstance()
                .properties(nameSpacedProperties)
                .build();
    }

    private boolean isValidUri(String string) {
        try {
            new URI(string);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private <T> Result<T> throwIfFailedResult(Result<T> result) {
        if (result.failed()) {
            var message = "Failed to transform contract negotiation or transfer process: %s";
            throw new EdcException(format(message, result.getFailureDetail()));
        }
        return result;
    }
}