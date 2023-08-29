package de.sovity.edc.ext.wrapper.api.usecase.transformer;

import de.sovity.edc.ext.wrapper.api.common.model.*;
import de.sovity.edc.ext.wrapper.api.common.model.ExpressionDto.Type;
import org.eclipse.edc.policy.model.*;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        var dto = new PermissionDto(new ExpressionDto(Type.EMPTY, null, null, null, null));

        // act
        var result = transformer.transform(dto, context);

        // assert
        assertThat(result).isNotNull();
        assertThat(result.getConstraints()).isEmpty();
        verify(context, never()).problem();
    }

    @Test
    void policyDtoToPolicy_atomicConstraint_returnPolicy() {
        // arrange
        var constraint = new AtomicConstraintDto("left", OperatorDto.EQ, "right");
        var dto = PermissionDto.builder()
                .constraints(new ExpressionDto(Type.ATOMIC_CONSTRAINT, constraint, null, null, null))
                .build();

        // act
        var result = transformer.transform(dto, context);

        // assert
        assertThat(result).isNotNull();
        assertThat(result.getAction().getType()).isEqualTo("USE");
        assertThat(result.getConstraints()).hasSize(1);
        assertThat(result.getConstraints().get(0)).isInstanceOf(AtomicConstraint.class);
    }

    @Test
    void policyDtoToPolicy_andConstraint_returnPolicy() {
        // arrange
        var constraint1 = new AtomicConstraintDto("left1", OperatorDto.EQ, "right1");
        var expression1 = new ExpressionDto(Type.ATOMIC_CONSTRAINT, constraint1, null, null, null);
        var constraint2 = new AtomicConstraintDto("left2", OperatorDto.EQ, "right2");
        var expression2 = new ExpressionDto(Type.ATOMIC_CONSTRAINT, constraint2, null, null, null);
        var dto = PermissionDto.builder()
                .constraints(new ExpressionDto(Type.AND, null, List.of(expression1, expression2), null, null))
                .build();

        // act
        var result = transformer.transform(dto, context);

        // assert
        assertThat(result).isNotNull();
        assertThat(result.getAction().getType()).isEqualTo("USE");
        assertThat(result.getConstraints()).hasSize(1);
        assertThat(result.getConstraints().get(0)).isInstanceOf(AndConstraint.class);

        var andConstraint = (AndConstraint) result.getConstraints().get(0);
        assertThat(andConstraint.getConstraints()).hasSize(2);
        andConstraint.getConstraints().forEach(this::assertAtomicConstraint);
    }

    @Test
    void policyDtoToPolicy_orConstraint_returnPolicy() {
        // arrange
        var constraint1 = new AtomicConstraintDto("left1", OperatorDto.EQ, "right1");
        var expression1 = new ExpressionDto(Type.ATOMIC_CONSTRAINT, constraint1, null, null, null);
        var constraint2 = new AtomicConstraintDto("left2", OperatorDto.EQ, "right2");
        var expression2 = new ExpressionDto(Type.ATOMIC_CONSTRAINT, constraint2, null, null, null);
        var dto = PermissionDto.builder()
                .constraints(new ExpressionDto(Type.OR, null, null, List.of(expression1, expression2), null))
                .build();

        // act
        var result = transformer.transform(dto, context);

        // assert
        assertThat(result).isNotNull();
        assertThat(result.getAction().getType()).isEqualTo("USE");
        assertThat(result.getConstraints()).hasSize(1);
        assertThat(result.getConstraints().get(0)).isInstanceOf(OrConstraint.class);

        var orConstraint = (OrConstraint) result.getConstraints().get(0);
        assertThat(orConstraint.getConstraints()).hasSize(2);
        orConstraint.getConstraints().forEach(this::assertAtomicConstraint);
    }

    @Test
    void policyDtoToPolicy_xorConstraint_returnPolicy() {
        // arrange
        var constraint1 = new AtomicConstraintDto("left1", OperatorDto.EQ, "right1");
        var expression1 = new ExpressionDto(Type.ATOMIC_CONSTRAINT, constraint1, null, null, null);
        var constraint2 = new AtomicConstraintDto("left2", OperatorDto.EQ, "right2");
        var expression2 = new ExpressionDto(Type.ATOMIC_CONSTRAINT, constraint2, null, null, null);
        var dto = PermissionDto.builder()
                .constraints(new ExpressionDto(Type.XOR, null, null, null, List.of(expression1, expression2)))
                .build();

        // act
        var result = transformer.transform(dto, context);

        // assert
        assertThat(result).isNotNull();
        assertThat(result.getAction().getType()).isEqualTo("USE");
        assertThat(result.getConstraints()).hasSize(1);
        assertThat(result.getConstraints().get(0)).isInstanceOf(XoneConstraint.class);

        var xoneConstraint = (XoneConstraint) result.getConstraints().get(0);
        assertThat(xoneConstraint.getConstraints()).hasSize(2);
        xoneConstraint.getConstraints().forEach(this::assertAtomicConstraint);
    }

    @Test
    void policyToPolicyDto_nestedLogicalConstraints_returnPolicy() {
        // arrange
        var constraint1 = new AtomicConstraintDto("left1", OperatorDto.EQ, "right1");
        var expression1 = new ExpressionDto(Type.ATOMIC_CONSTRAINT, constraint1, null, null, null);
        var constraint2 = new AtomicConstraintDto("left2", OperatorDto.EQ, "right2");
        var expression2 = new ExpressionDto(Type.ATOMIC_CONSTRAINT, constraint2, null, null, null);

        var andExpression1 = new ExpressionDto(Type.AND, null, List.of(expression1, expression2),
                null, null);
        var andExpression2 = new ExpressionDto(Type.AND, null, List.of(expression1, expression2),
                null, null);

        var orExpression = new ExpressionDto(Type.OR, null, null, List.of(andExpression1, andExpression2),
                null);

        var dto = PermissionDto.builder()
                .constraints(orExpression)
                .build();

        // act
        var result = transformer.transform(dto, context);

        // assert
        assertThat(result).isNotNull();
        assertThat(result.getAction().getType()).isEqualTo("USE");
        assertThat(result.getConstraints()).hasSize(1);
        assertThat(result.getConstraints().get(0)).isInstanceOf(OrConstraint.class);

        var orConstraint = (OrConstraint) result.getConstraints().get(0);
        assertThat(orConstraint.getConstraints()).hasSize(2);
        orConstraint.getConstraints().forEach(c -> {
            assertThat(c).isInstanceOf(AndConstraint.class);
            var andConstraint = (AndConstraint) c;
            assertThat(andConstraint.getConstraints()).hasSize(2);
            andConstraint.getConstraints().forEach(c2 -> assertThat(c2)
                    .isInstanceOf(AtomicConstraint.class));
        });
    }

    void assertAtomicConstraint(Constraint constraint) {
        assertThat(constraint).isInstanceOf(AtomicConstraint.class);

        var atomic = (AtomicConstraint) constraint;
        assertThat(((LiteralExpression) atomic.getLeftExpression()).getValue().toString()).isIn("left1", "left2");
        assertThat(atomic.getOperator()).isEqualTo(Operator.EQ);
        assertThat(((LiteralExpression) atomic.getRightExpression()).getValue().toString()).isIn("right1", "right2");
    }
}
