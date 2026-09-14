package org.example;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

class FormPrefixControllerTest extends BaseWebTest {

    @Test
    void nestedFormPrefix() {
        HttpResponse<String> res = client().request()
                .path("formprefix")
                .formParam("name", "bill")
                .formParam("birth-date", "1990-05-20")
                .formParam("hobbies", "swimming")
                .formParam("hobbies", "coding")
                .formParam("invoice.street", "Main Street")
                .formParam("invoice.city", "Springfield")
                .formParam("invoice.zip", "12345")
                .formParam("invoice.customer-info.phone", "555-1234")
                .formParam("invoice.customer-info.email", "bob@example.com")
                .formParam("shipping.street", "Square One")
                .formParam("shipping.city", "Shelbyville")
                .POST()
                .asPlainString();

        assertThat(res.statusCode()).isEqualTo(201);
        assertThat(res.body())
                .isEqualTo("bill|1990-05-20|[swimming, coding]|Main"
                        + " Street,Springfield,12345|555-1234@bob@example.com|Square One,Shelbyville");
    }

    @Test
    void nestedFormPrefix_partial() {
        // no nested keys submitted -> nested beans are created but their fields stay null
        HttpResponse<String> res = client().request()
                .path("formprefix")
                .formParam("name", "ann")
                .POST()
                .asPlainString();

        assertThat(res.statusCode()).isEqualTo(201);
        assertThat(res.body()).isEqualTo("ann|null|[]|null,null,null|null@null|null,null");
    }

    @Test
    void nestedFormPrefix_repeatedFieldNamesDoNotCollide() {
        // same nested field names under different prefixes must not collide
        HttpResponse<String> res = client().request()
                .path("formprefix")
                .formParam("name", "carol")
                .formParam("invoice.street", "Invoice Street")
                .formParam("invoice.customer-info.phone", "111")
                .formParam("shipping.street", "Shipping Street")
                .formParam("shipping.customer-info.phone", "222")
                .POST()
                .asPlainString();

        assertThat(res.statusCode()).isEqualTo(201);
        assertThat(res.body()).isEqualTo("carol|null|[]|Invoice Street,null,null|111@null|Shipping Street,null");
    }
}
