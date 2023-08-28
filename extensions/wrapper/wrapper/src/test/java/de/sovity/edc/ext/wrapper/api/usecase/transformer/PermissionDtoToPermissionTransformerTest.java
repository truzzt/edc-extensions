package de.sovity.edc.ext.wrapper.api.usecase.transformer;

import de.sovity.edc.ext.wrapper.api.common.model.ExpressionDto;
import de.sovity.edc.ext.wrapper.api.common.model.PermissionDto;
import org.eclipse.edc.policy.model.Permission;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PermissionDtoToPermissionTransformerTest {
    private final TransformerContext context = mock(TransformerContext.class);
    private final PermissionDtoToPermissionTransformer transformer = new PermissionDtoToPermissionTransformer();

    @Test
    void types() {
        assertThat(transformer.getInputType()).isEqualTo(PermissionDto.class);
        assertThat(transformer.getOutputType()).isEqualTo(Permission.class);
    }

    @Test
    void transform_success_emptyConstraint() {
        // arrange
        var dto = new PermissionDto(new ExpressionDto(ExpressionDto.Type.EMPTY, null, null, null, null));

        // act
        var result = transformer.transform(dto, context);

        // assert
        assertThat(result).isNotNull();
        assertThat(result.getConstraints()).isEmpty();
        verify(context, never()).problem();
    }

}
