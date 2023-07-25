package de.sovity.edc.ext.wrapper.api.usecase.transformer;

import de.sovity.edc.ext.wrapper.api.common.model.ExpressionDto;
import de.sovity.edc.ext.wrapper.api.common.model.PermissionDto;
import de.sovity.edc.ext.wrapper.api.common.model.PolicyDto;
import org.eclipse.edc.policy.model.*;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PolicyToPolicyDtoTransformerTest {

    private final PolicyToPolicyDtoTransformer transformer = new PolicyToPolicyDtoTransformer();
    private final TransformerContext context = mock(TransformerContext.class);

    @Test
    void types() {
        assertThat(transformer.getInputType()).isEqualTo(Policy.class);
        assertThat(transformer.getOutputType()).isEqualTo(PolicyDto.class);
    }

    @Test
    void transform(){
        var permission = Permission.Builder.newInstance().action(Action.Builder.newInstance().type("USE").build()).build();
        var prohibition = Prohibition.Builder.newInstance().action(Action.Builder.newInstance().type("MODIFY").build()).build();
        var duty = Duty.Builder.newInstance().action(Action.Builder.newInstance().type("DELETE").build()).build();
        var policy = Policy.Builder.newInstance()
                .permission(permission)
                .prohibition(prohibition)
                .duty(duty)
                .assigner("assigner")
                .assignee("assignee")
                .inheritsFrom("inheritsFroms")
                .type(PolicyType.SET)
                .extensibleProperties(new HashMap<>())
                .build();


        var pmDto = new PermissionDto(new ExpressionDto());
        when(context.transform(any(Permission.class), eq(PermissionDto.class))).thenReturn(pmDto);
        // or when(context.transform(permission, PermissionDto.class)).thenReturn(new PermissionDto());

        var result = transformer.transform(policy,context);
        //var permissionDto  = result.getPermission();




        assertThat(result).isNotNull();
        assertThat(result.getPermission()).isNotNull();
        assertThat(result.getPermission().getConstraints()).isNotNull();
        assertThat(result.getPermission()).isEqualTo(pmDto);

        verify(context).transform(permission, PermissionDto.class);
    }
}
