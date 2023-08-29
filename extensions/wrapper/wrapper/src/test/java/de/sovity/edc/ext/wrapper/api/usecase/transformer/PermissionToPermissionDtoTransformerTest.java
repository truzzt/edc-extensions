package de.sovity.edc.ext.wrapper.api.usecase.transformer;

import de.sovity.edc.ext.wrapper.api.common.model.ExpressionDto;
import de.sovity.edc.ext.wrapper.api.common.model.PermissionDto;
import de.sovity.edc.ext.wrapper.api.usecase.model.ContractNegotiationOutputDto;
import groovy.util.logging.Slf4j;
import org.eclipse.edc.connector.contract.spi.types.negotiation.ContractNegotiation;
import org.eclipse.edc.policy.model.*;
import org.eclipse.edc.transform.spi.ProblemBuilder;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.eclipse.edc.transform.spi.UnexpectedTypeBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Slf4j
public class PermissionToPermissionDtoTransformerTest {

    private final PermissionToPermissionDtoTransformer transformer = new PermissionToPermissionDtoTransformer();
    private final TransformerContext context = mock(TransformerContext.class);

    @Test
    void types() {
        assertThat(transformer.getInputType()).isEqualTo(Permission.class);
        assertThat(transformer.getOutputType()).isEqualTo(PermissionDto.class);
    }

    @Test
    void transform(){
        var atomicConstraint = AtomicConstraint.Builder.newInstance().rightExpression(new LiteralExpression("eu")).leftExpression(new LiteralExpression("absoluteSpatialPosition")).build();
        var permission = Permission.Builder.newInstance().action(Action.Builder.newInstance().type("USE").build()).constraint(atomicConstraint).build();

        var result = transformer.transform(permission,context);

        assertThat(result).isNotNull();
        assertThat(result.getConstraints()).isNotNull();
        assertThat(result.getConstraints().getType().name()).isEqualTo("ATOMIC_CONSTRAINT");
        assertThat(result.getConstraints().getAtomicConstraint().getLeftExpression()).isEqualTo("absoluteSpatialPosition");
        assertThat(result.getConstraints().getAtomicConstraint().getRightExpression()).isEqualTo("eu");
        assertThat(result.getConstraints().getAtomicConstraint().getOperator().name()).isEqualTo("EQ");
    }


    @Test
    void transformIllegal(){
//        when(context.problem()).thenCallRealMethod();

        Constraint illegalConstraint = new IllegalConstraint();
        var permission = Permission.Builder.newInstance().action(Action.Builder.newInstance().type("USE").build()).constraint(illegalConstraint).build();

        var problemBuilder = mock(ProblemBuilder.class);
        var unexpectedTypeBuilder = mock(UnexpectedTypeBuilder.class);
        when(context.problem()).thenReturn(problemBuilder);
        when(problemBuilder.unexpectedType()).thenReturn(unexpectedTypeBuilder);
        when(unexpectedTypeBuilder.actual(any(Class.class))).thenReturn(unexpectedTypeBuilder);
        when(unexpectedTypeBuilder.expected(any(Class.class))).thenReturn(unexpectedTypeBuilder);
        var result = transformer.transform(permission,context);

        assertThat(result).isNull();

        verify(unexpectedTypeBuilder, times(1)).actual(IllegalConstraint.class);
        verify(unexpectedTypeBuilder, times(4)).expected(any(Class.class));
        verify(unexpectedTypeBuilder, times(1)).report();
    }


    class IllegalConstraint extends Constraint{
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return null;
        }
    }
}