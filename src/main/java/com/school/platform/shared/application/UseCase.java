package com.school.platform.shared.application;

/**
 * Generic application port for command/query use cases.
 */
@FunctionalInterface
public interface UseCase<I, O> {
    O execute(I input);
}
