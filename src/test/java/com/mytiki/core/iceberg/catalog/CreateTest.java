/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.core.iceberg.catalog;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.mytiki.core.iceberg.catalog.create.CreateHandler;
import com.mytiki.core.iceberg.catalog.create.CreateReq;
import com.mytiki.core.iceberg.catalog.create.CreateRsp;
import com.mytiki.core.iceberg.utils.ApiException;
import com.mytiki.core.iceberg.utils.Iceberg;
import com.mytiki.core.iceberg.utils.Mapper;
import com.mytiki.core.iceberg.catalog.mock.MockIceberg;
import com.mytiki.core.iceberg.catalog.mock.MockReq;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

@RunWith(MockitoJUnitRunner.class)
public class CreateTest {

    @Mock
    Iceberg iceberg;
    MockIceberg mockIceberg;

    @Before
    public void init() {
        mockIceberg = new MockIceberg(iceberg);
    }

    @Test
    public void HandleRequest_New_200() {
        CreateReq body = MockReq.createReq();
        APIGatewayV2HTTPEvent request = APIGatewayV2HTTPEvent.builder()
                .withBody(new Mapper().writeValueAsString(body))
                .withRequestContext(APIGatewayV2HTTPEvent.RequestContext.builder()
                        .withHttp(APIGatewayV2HTTPEvent.RequestContext.Http.builder()
                                .withMethod("POST")
                                .withPath("/api/latest")
                                .build())
                        .build())
                .build();
        APIGatewayV2HTTPResponse response = new CreateHandler(iceberg).handleRequest(request, null);
        assertEquals(200, response.getStatusCode());
        CreateRsp res = new Mapper().readValue(response.getBody(), CreateRsp.class);
        assertEquals(mockIceberg.getName(), res.getName());
        assertEquals(mockIceberg.getLocation(), res.getLocation());
    }

    @Test
    public void HandleRequest_Exists_400() {
        mockIceberg.setTableExists(true);
        CreateReq body = MockReq.createReq();
        APIGatewayV2HTTPEvent request = APIGatewayV2HTTPEvent.builder()
                .withBody(new Mapper().writeValueAsString(body))
                .withRequestContext(APIGatewayV2HTTPEvent.RequestContext.builder()
                        .withHttp(APIGatewayV2HTTPEvent.RequestContext.Http.builder()
                                .withMethod("POST")
                                .withPath("/api/latest")
                                .build())
                        .build())
                .build();
        ApiException exception = assertThrows(ApiException.class, () -> {
            new CreateHandler(iceberg).handleRequest(request, null);
        });
        assertEquals(400, exception.getStatus());
    }
}