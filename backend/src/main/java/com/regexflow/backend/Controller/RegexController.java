package com.regexflow.backend.Controller;

import com.regexflow.backend.Dto.RegexProcessRequest;
import com.regexflow.backend.Dto.RegexProcessResponse;
import com.regexflow.backend.Service.RegexProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/regex")
public class RegexController {

    @Autowired
    private RegexProcessService regexProcessService;

    @PostMapping("/process")
    public ResponseEntity<RegexProcessResponse> processRegex(
            @RequestBody RegexProcessRequest request) {

        if (request.getRegexPattern() == null || request.getRegexPattern().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        if (request.getRawMsg() == null || request.getRawMsg().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        RegexProcessResponse response = regexProcessService.processRegex(request);

        return ResponseEntity.ok(response);
    }
}
