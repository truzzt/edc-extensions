package de.sovity.edc.ext.wrapper.api.usecase.transformer;

import de.sovity.edc.ext.wrapper.api.common.model.AtomicConstraintDto;
import de.sovity.edc.ext.wrapper.api.common.model.ExpressionDto;
import de.sovity.edc.ext.wrapper.api.common.model.PermissionDto;
import org.eclipse.edc.policy.model.*;
import org.eclipse.edc.transform.spi.TransformerContext;
import org.eclipse.edc.transform.spi.TypeTransformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PermissionDtoToPermissionTransformer implements TypeTransformer<PermissionDto, Permission> {

    /**
     * Currently only type "USE" supported, therefore this is hardcoded.
     */
    public static final String ACTION_TYPE = "USE";

    @Override
    public Class<PermissionDto> getInputType() {
        return PermissionDto.class;
    }

    @Override
    public Class<Permission> getOutputType() {
        return Permission.class;
    }

    @Override
    public @Nullable Permission transform(@NotNull PermissionDto permissionDto, @NotNull TransformerContext context) {
        var builder = Permission.Builder.newInstance()
                .action(Action.Builder.newInstance().type(ACTION_TYPE).build());
        if (permissionDto.getConstraints() == null) {
            return builder.build();
        }

        Optional.ofNullable(expressionToConstraint(permissionDto.getConstraints()))
                .ifPresent(builder::constraint);
        return builder.build();

    }

    private Constraint expressionToConstraint(ExpressionDto expression) {
        return switch (expression.getType()) {
            case EMPTY -> null;
            case ATOMIC_CONSTRAINT -> constraintDtoToAtomicConstraint(expression.getAtomicConstraint());
            case AND ->  {
                var builder = AndConstraint.Builder.newInstance();
                expression.getAnd().forEach(c -> builder.constraint(expressionToConstraint(c)));
                yield builder.build();
            }
            case OR -> {
                var builder = OrConstraint.Builder.newInstance();
                expression.getOr().forEach(c -> builder.constraint(expressionToConstraint(c)));
                yield builder.build();
            }
            case XOR -> {
                var builder = XoneConstraint.Builder.newInstance();
                expression.getXor().forEach(c -> builder.constraint(expressionToConstraint(c)));
                yield builder.build();
            }
        };
    }

    private Constraint constraintDtoToAtomicConstraint(AtomicConstraintDto dto) {
        return AtomicConstraint.Builder.newInstance()
                .leftExpression(new LiteralExpression(dto.getLeftExpression()))
                .rightExpression(new LiteralExpression(dto.getRightExpression()))
                .operator(Operator.valueOf(dto.getOperator().toString()))
                .build();
    }
}
