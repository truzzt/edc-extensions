package de.sovity.edc.ext.wrapper.api.usecase.transformer;

import de.sovity.edc.ext.wrapper.api.common.model.ExpressionDto;
import de.sovity.edc.ext.wrapper.api.common.model.PermissionDto;
import de.sovity.edc.ext.wrapper.api.common.model.PolicyDto;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.policy.model.Policy;
import org.eclipse.edc.policy.model.PolicyType;
import org.eclipse.edc.transform.spi.NullPropertyBuilder;
import org.eclipse.edc.transform.spi.ProblemBuilder;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class PolicyDtoToPolicyTransformerTest {
    private final PolicyDtoToPolicyTransformer transformer = new PolicyDtoToPolicyTransformer();
    private final TransformerContext context = mock(TransformerContext.class);

    @Test
    void types() {
        assertThat(transformer.getInputType()).isEqualTo(PolicyDto.class);
        assertThat(transformer.getOutputType()).isEqualTo(Policy.class);
    }

    @Test
    void transform_success() {
        // arrange
        var dto = new PolicyDto().toBuilder().permission(new PermissionDto(new ExpressionDto())).build();
        when(context.transform(any(PermissionDto.class), eq(Permission.class))).thenReturn(new Permission());

        // act
        var result = transformer.transform(dto, context);

        // assert
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(PolicyType.SET);
        assertThat(result.getPermissions()).isNotEmpty();
    }

    @Test
    void transform_fail_missingPermission() {
        // arrange
        var dto = new PolicyDto().toBuilder().build();
        when(context.transform(any(PermissionDto.class), eq(Permission.class))).thenReturn(new Permission());

        var problemBuilder = mock(ProblemBuilder.class);
        var nullPropBuilder = mock(NullPropertyBuilder.class);
        when(context.problem()).thenReturn(problemBuilder);
        when(problemBuilder.nullProperty()).thenReturn(nullPropBuilder);
        when(nullPropBuilder.type(eq(PermissionDto.class))).thenReturn(nullPropBuilder);
        when(nullPropBuilder.property(anyString())).thenReturn(nullPropBuilder);

        // act
        var result = transformer.transform(dto, context);

        // assert
        assertThat(result).isNull();
        verify(context, times(1)).problem();
    }
}
