package com.regexflow.backend.Controller;

import com.regexflow.backend.Dto.RegexProcessRequest;
import com.regexflow.backend.Dto.RegexProcessResponse;
import com.regexflow.backend.Service.RegexProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for regex pattern processing
 * Handles requests to process regex patterns against raw SMS messages
 */
@RestController
@RequestMapping("/regex")
public class RegexController {

    @Autowired
    private RegexProcessService regexProcessService;

    /**
     * ENDPOINT: POST /regex/process
     * Processes a regex pattern against a raw SMS message and extracts fields
     * 
     * Request Body:
     * {
     *   "bankAddress": "001126",
     *   "smsType": "CREDIT",
     *   "paymentType": "UPI",
     *   "regexPattern": "Your\\s+A/c\\s+(\\d+)\\s+credited\\s+Rs\\.(\\d+\\.?\\d*)",
     *   "rawMsg": "Your A/c 123456 credited Rs.5000.00",
     *   "bankName": "ICICI Bank",
     *   "transactionType": "EMI Installment"
     * }
     * 
     * @param request The request containing regex pattern and raw message
     * @return Response with extracted fields
     */
    @PostMapping("/process")
    public ResponseEntity<RegexProcessResponse> processRegex(@RequestBody RegexProcessRequest request) {
        try {
            // Validate required fields
            if (request.getRegexPattern() == null || request.getRegexPattern().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            if (request.getRawMsg() == null || request.getRawMsg().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            
            RegexProcessResponse response = regexProcessService.processRegex(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Return default response with "-1" values on error
            RegexProcessResponse errorResponse = new RegexProcessResponse();
            // Initialize with default values using the service
            regexProcessService.initializeDefaultValues(errorResponse);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
