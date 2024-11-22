package com.techstud.scheduleuniversity.mapper;

import com.techstud.scheduleuniversity.dao.document.TimeSheet;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TimeSheetMapper {

    TimeSheet toDocument(com.techstud.scheduleuniversity.dto.parser.response.TimeSheet timeSheet);
}
