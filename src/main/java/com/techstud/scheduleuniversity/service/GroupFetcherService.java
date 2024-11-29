package com.techstud.scheduleuniversity.service;

import com.techstud.scheduleuniversity.dto.fetcher.GroupData;

import java.net.URISyntaxException;
import java.util.List;

public interface GroupFetcherService {

    List<GroupData> fetchGroupsData();

}
