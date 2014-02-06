package org.motechproject.server.web;

import org.motechproject.config.core.domain.ConfigSource;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.motechproject.server.web.validator.ValidationUtils.validateEmptyOrWhitespace;
import static org.motechproject.server.web.validator.ValidationUtils.validateUrl;

/**
 * Validator that validates bootstrap configuration input by the user in the bootstrap config UI.
 */
public class BootstrapConfigFormValidator implements Validator {
    static final String ERROR_REQUIRED = "server.error.required.%s";

    @Override
    public boolean supports(Class<?> clazz) {
        return BootstrapConfigForm.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        String dbUrlField = "couchDbUrl";
        validateEmptyOrWhitespace(errors, ERROR_REQUIRED, dbUrlField);

        if (!errors.hasFieldErrors(dbUrlField)) {
            validateUrl(errors, dbUrlField);
        }

        String configSource = ((BootstrapConfigForm) target).getConfigSource();
        if (!ConfigSource.isValid(configSource)) {
            errors.rejectValue(configSource, "server.error.invalid.configSource");
        }
    }
}
