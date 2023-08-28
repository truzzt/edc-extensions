package de.sovity.edc.ext.wrapper.api.usecase.transformer;

import de.sovity.edc.ext.wrapper.api.common.model.PermissionDto;
import de.sovity.edc.ext.wrapper.api.common.model.PolicyDto;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.policy.model.PolicyType;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.eclipse.edc.transform.spi.TypeTransformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PolicyDtoToPolicyTransformer implements TypeTransformer<PolicyDto, Policy> {
    @Override
    public Class<PolicyDto> getInputType() {
        return PolicyDto.class;
    }

    @Override
    public Class<Policy> getOutputType() {
        return Policy.class;
    }

    /**
     * Converts a {@link PolicyDto} to an EDC {@link Policy}.
     *
     * @param policyDto The {@link PolicyDto}.
     * @return An EDC {@link Policy}
     */
    @Override
    public @Nullable Policy transform(@NotNull PolicyDto policyDto, @NotNull TransformerContext context) {
        if (policyDto.getPermission() == null) {
            context.problem().nullProperty().type(PermissionDto.class).property("PermissionDto");
            return null;
        }

        return Policy.Builder.newInstance()
                .type(PolicyType.SET)
                .permission(context.transform(policyDto.getPermission(), Permission.class))
                .build();
    }
}
