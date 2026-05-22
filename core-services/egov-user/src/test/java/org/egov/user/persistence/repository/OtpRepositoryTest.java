package org.egov.user.persistence.repository;

import org.egov.user.Resources;
import org.egov.user.domain.model.OtpValidationRequest;
import org.egov.user.web.contract.Otp;
import org.egov.user.web.contract.OtpValidateRequest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(MockitoJUnitRunner.class)
public class OtpRepositoryTest {

    private OtpRepository otpRepository;
    private MockRestServiceServer server;

    @Before
    public void setUp() throws Exception {

        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();

        this.otpRepository = new OtpRepository(
                "http://otp-host.com/",
                "otp/_search",
                "otp/_validate",
                restTemplate
        );
    }

    @Test
    public void testShouldReturnTrueWhenOtpHasBeenValidated() throws Exception {

        server.expect(once(),
                requestTo("http://otp-host.com/otp/_search"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(request -> {
                    JSONAssert.assertEquals(
                            new Resources().getFileContents(
                                    "otpSearchSuccessRequest.json"),
                            request.getBody().toString(),
                            false
                    );
                })
                .andRespond(withSuccess(
                        new Resources().getFileContents(
                                "otpSearchValidatedResponse.json"),
                        MediaType.APPLICATION_JSON_UTF8));

        boolean isOtpValidated =
                otpRepository.isOtpValidationComplete(buildRequest());

        server.verify();
        assertEquals(Boolean.TRUE, isOtpValidated);
    }

    @Test
    public void testShouldReturnFalseWhenOtpHasNotBeenValidated() throws Exception {

        server.expect(once(),
                requestTo("http://otp-host.com/otp/_search"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(request -> {
                    JSONAssert.assertEquals(
                            new Resources().getFileContents(
                                    "otpSearchSuccessRequest.json"),
                            request.getBody().toString(),
                            false
                    );
                })
                .andRespond(withSuccess(
                        new Resources().getFileContents(
                                "otpSearchNonValidatedResponse.json"),
                        MediaType.APPLICATION_JSON_UTF8));

        boolean isOtpValidated =
                otpRepository.isOtpValidationComplete(buildRequest());

        server.verify();
        assertFalse(isOtpValidated);
    }

    @Test
    public void testShouldReturnFalseWhenOtpIdentityDoesNotMatch() throws Exception {

        server.expect(once(),
                requestTo("http://otp-host.com/otp/_search"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(request -> {
                    JSONAssert.assertEquals(
                            new Resources().getFileContents(
                                    "otpSearchSuccessRequest.json"),
                            request.getBody().toString(),
                            false
                    );
                })
                .andRespond(withSuccess(
                        new Resources().getFileContents(
                                "otpSearchIdentityDifferentResponse.json"),
                        MediaType.APPLICATION_JSON_UTF8));

        boolean isOtpValidated =
                otpRepository.isOtpValidationComplete(buildRequest());

        server.verify();
        assertFalse(isOtpValidated);
    }

    private OtpValidationRequest buildRequest() {
        return OtpValidationRequest.builder()
                .otpReference("2b936aae-c3b6-4c89-b3b3-a098cdcbb706")
                .mobileNumber("9988776655")
                .tenantId("tenantId")
                .build();
    }

    private OtpValidateRequest buildValidateRequest() {
        Otp otp = Otp.builder()
                .otp("1234")
                .tenantId("default")
                .identity("9988776655")
                .build();

        return OtpValidateRequest.builder()
                .otp(otp)
                .build();
    }
}