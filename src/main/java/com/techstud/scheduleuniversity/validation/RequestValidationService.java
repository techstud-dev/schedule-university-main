package com.techstud.scheduleuniversity.validation;

import com.techstud.scheduleuniversity.dto.ApiRequest;
import com.techstud.scheduleuniversity.dto.ImportDto;
import com.techstud.scheduleuniversity.exception.RequestException;

public interface RequestValidationService {

    void validateImportRequest(ApiRequest<ImportDto> importRequest) throws RequestException;
}
