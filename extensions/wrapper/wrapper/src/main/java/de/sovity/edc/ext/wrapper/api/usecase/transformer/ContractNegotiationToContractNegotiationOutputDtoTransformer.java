package de.sovity.edc.ext.wrapper.api.usecase.transformer;

import de.sovity.edc.ext.wrapper.api.common.model.ContractAgreementDto;
import de.sovity.edc.ext.wrapper.api.usecase.model.ContractNegotiationOutputDto;
import org.eclipse.edc.connector.contract.spi.types.agreement.ContractAgreement;
import org.eclipse.edc.connector.contract.spi.types.negotiation.ContractNegotiation;
import org.eclipse.edc.connector.contract.spi.types.negotiation.ContractNegotiationStates;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.eclipse.edc.transform.spi.TypeTransformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ContractNegotiationToContractNegotiationOutputDtoTransformer implements TypeTransformer<ContractNegotiation, ContractNegotiationOutputDto> {
    @Override
    public Class<ContractNegotiation> getInputType() {
        return ContractNegotiation.class;
    }

    @Override
    public Class<ContractNegotiationOutputDto> getOutputType() {
        return ContractNegotiationOutputDto.class;
    }

    @Override
    public @Nullable ContractNegotiationOutputDto transform(@NotNull ContractNegotiation contractNegotiation, @NotNull TransformerContext context) {
        ContractAgreementDto contractAgreementDto = null;

        if (contractNegotiation.getContractAgreement() != null) {
            contractAgreementDto  = context.transform(contractNegotiation.getContractAgreement(), ContractAgreementDto.class);

            if (contractAgreementDto == null) {
                context.problem().nullProperty().type(ContractNegotiation.class).property("ContractAgreement").report();
                return null;
            }
        }

        return ContractNegotiationOutputDto.builder()
                .id(contractNegotiation.getId())
                .state(ContractNegotiationStates.from(contractNegotiation.getState()).name())
                .correlationId(contractNegotiation.getCorrelationId())
                .counterPartyId(contractNegotiation.getCounterPartyId())
                .counterPartyAddress(contractNegotiation.getCounterPartyAddress())
                .errorDetail(contractNegotiation.getErrorDetail())
                .protocol(contractNegotiation.getProtocol())
                .contractAgreement(contractAgreementDto)
                .build();
    }
}
