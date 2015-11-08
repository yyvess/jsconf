/**
 * Copyright 2015 Yves Galante
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jsconf.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static java.lang.String.format;

class BeanValidator {

    private static final Logger log = LoggerFactory.getLogger(BeanValidator.class);
    private static final ValidatorFactory factory;


    static {
        ValidatorFactory f = null;
        try {
            f = Validation.buildDefaultValidatorFactory();
            log.debug("Vadliation factory builded");
        } catch (ValidationException e) {
            log.debug("No vadliation factory found in classpath");
        } finally {
            factory = f;
        }
    }

    public static <T> void beanValidation(T bean, Class<T> beanInterface) throws BeanCreationException {
        if (factory != null) {
            Set<ConstraintViolation<T>> constraintsViolation = factory.getValidator().validate(bean);
            if (!constraintsViolation.isEmpty()) {
                throw new BeanCreationException(
                        format("Constraint violation on %s : %s"
                                , beanInterface
                                , constraintsViolation.iterator().next().getMessage()));
            }
        }
    }
}
