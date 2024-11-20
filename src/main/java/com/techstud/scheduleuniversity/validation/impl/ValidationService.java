package com.techstud.scheduleuniversity.validation.impl;

import com.techstud.scheduleuniversity.dto.ApiRequest;
import com.techstud.scheduleuniversity.dto.ImportDto;
import com.techstud.scheduleuniversity.exception.RequestException;
import com.techstud.scheduleuniversity.validation.RequestValidationService;
import com.techstud.scheduleuniversity.validation.ResponseValidationService;

import java.util.ArrayList;
import java.util.List;

public class ValidationService implements RequestValidationService, ResponseValidationService {
    @Override
    public void validateImportRequest(ApiRequest<ImportDto> importRequest) throws RequestException {
        List<String> emptyFields = new ArrayList<>();

        if (importRequest.getRequestId() == null) {
            emptyFields.add("requestId");
        }
        if (importRequest.getData() == null) {
            emptyFields.add("data");
        }

        if (importRequest.getData() != null && importRequest.getData().getGroupCode() == null) {
            emptyFields.add("data.groupCode");
        }

        if (importRequest.getData() != null && importRequest.getData().getUniversityName() == null) {
            emptyFields.add("data.universityName");
        }

        if (!emptyFields.isEmpty()) {
            throw new RequestException(emptyFields);
        }
    }
}
