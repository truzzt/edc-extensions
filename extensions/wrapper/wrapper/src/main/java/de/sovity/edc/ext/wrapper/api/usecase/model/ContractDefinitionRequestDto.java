package de.sovity.edc.ext.wrapper.api.usecase.model;

import de.sovity.edc.ext.wrapper.api.common.model.CriterionDto;
import lombok.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ContractDefinitionRequestDto {

    /**
     * Default validity is set to one year.
     */
    private static final long DEFAULT_VALIDITY = TimeUnit.DAYS.toSeconds(365);

    private String id;
    private String accessPolicyId;
    private String contractPolicyId;
    private List<CriterionDto> assetsSelector;

}
