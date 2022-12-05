/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2016 ForgeRock AS.
 * Portions Copyright 2021 Wren Security.
 */
package org.forgerock.openam.test.apidescriptor;

import static org.forgerock.openam.test.apidescriptor.ApiAssertions.assertI18nDescription;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.api.ObjectAssert;
import org.forgerock.api.annotations.ApiError;
import org.forgerock.api.annotations.Schema;

/**
 * This class represents the {@link ApiError} annotation.
 *
 * @since 14.0.0
 */
public final class ApiErrorAssert extends AbstractListAssert<ApiErrorAssert, List<ApiError>, ApiError, ObjectAssert<ApiError>> {

    private final Class<?> annotatedClass;

    ApiErrorAssert(Class<?> annotatedClass, List<ApiError> actual) {
        super(actual, ApiErrorAssert.class);
        this.annotatedClass = annotatedClass;
    }

    /**
     * Assert that all descriptions use i18n and that the keys have valid entries in the specifies resource bundle.
     *
     * @return An instance of {@link ApiErrorAssert}.
     */
    public ApiErrorAssert hasI18nDescriptions() {
        for (ApiError error : actual) {
            assertI18nDescription(error.description(), annotatedClass);
        }
        return this;
    }

    /**
     * Get the test representative of {@link Schema}s in the annotated error.
     *
     * @return The {@link ApiSchemaAssert} containing the {@link Schema}s.
     */
    public ApiSchemaAssert schemas() {
        List<Schema> schemas = new ArrayList<>();
        for (ApiError error : actual) {
            schemas.add(error.detailSchema());
        }
        return new ApiSchemaAssert(annotatedClass, schemas);
    }

	@Override
	protected ObjectAssert<ApiError> toAssert(ApiError value, String description) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected ApiErrorAssert newAbstractIterableAssert(Iterable<? extends ApiError> iterable) {
		throw new UnsupportedOperationException();
	}

}
