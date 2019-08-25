package com.leyou.search.client;

import com.leyou.item.api.SpecAPI;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient("item-service")
public interface SpecClient extends SpecAPI {
}
