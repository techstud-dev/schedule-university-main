package com.techstud.scheduleuniversity.dto.parser.request;


import com.google.gson.Gson;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParsingTask {

    /**
     * Название университета аббревиатурой
     */
    private String universityName;

    /**
     * Id группы (можно найти в урле)
     */
    private String groupId;

    /**
     * Номер подгруппы (нужен для некоторых университетов)
     */
    private String subGroupId = "";

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
