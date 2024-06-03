package kol.common.service;

import kol.common.config.DispenseConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * @author Gongminrui
 * @date 2023-03-22 19:48
 */
@Service
@Slf4j
public class HttpDispenseService {
    @Resource
    private DispenseConfig dispenseConfig;
    private RestTemplate restTemplate = new RestTemplate();

    public void post(HttpServletRequest request, String url) {
        List<String> serverUrl = dispenseConfig.getServerUrl();
        if (CollectionUtils.isEmpty(serverUrl)) {
            return;
        }

        try {
            request.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Map<String, String> headers = getHeaders(request);
        StringBuilder body = getBody(request);
        HttpEntity<String> requestHttp = setPostBody(body, headers);
        for (String server : serverUrl) {
            String toUrl = server + "" + url;
            try {
                ResponseEntity<String> response = restTemplate.postForEntity(toUrl, requestHttp, String.class);
                log.info(server + "返回结果: " + response.getBody());
            } catch (Exception e) {
                e.printStackTrace();
                log.error("发送 " + server + "失败： ", e);
            }
        }
    }

    private Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> result = new HashMap<>();
        Enumeration<String> headers = request.getHeaderNames();
        Collections.list(headers).forEach((key) -> {
            result.put(key, request.getHeader(key));
        });
        return result;
    }

    private StringBuilder getBody(HttpServletRequest request) {
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"))) {
            String line = "";
            while ((line = reader.readLine()) != null)
                body.append(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return body;
    }

    private HttpEntity<String> setPostBody(StringBuilder body, Map<String, String> requestHeaders) {
        HttpHeaders headers = new HttpHeaders();
        requestHeaders.forEach((x, y) -> {
            headers.add(x, y);
        });
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body.toString(), headers);
    }

    private HttpEntity<MultiValueMap<String, String>> setPostParam(Map<String, String[]> params, Map<String, String> requestHeaders) {
        HttpHeaders headers = new HttpHeaders();
        requestHeaders.forEach((x, y) -> {
            headers.add(x, y);
        });
        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        for (String key : params.keySet())
            map.add(key, params.get(key)[0]);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
        return request;
    }
}
