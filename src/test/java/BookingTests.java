
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.github.javafaker.Faker;

import Entities.Booking;
import Entities.BookingDates;
import Entities.User;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.filter.log.ErrorLoggingFilter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

@TestMethodOrder(MethodOrderer.MethodName.class)

public class BookingTests {

        private static User user;
        public static Faker faker;
        public static RequestSpecification request;

        private static BookingDates bookingDates;
        private static Booking booking;

        @BeforeAll
        public static void setup() {

                RestAssured.baseURI = "https://restful-booker.herokuapp.com";

                faker = new Faker();

                user = new User(faker.name().username(),
                                faker.name().firstName(),
                                faker.name().lastName(),
                                faker.internet().safeEmailAddress(),
                                faker.internet().password(8, 10),
                                faker.phoneNumber().toString());

                bookingDates = new BookingDates("2018-01-02", "2018-01-03");
                booking = new Booking(user.getFirstName(), user.getLastName(),
                                (float) faker.number().randomDouble(2, 50, 1000L), true, bookingDates, "");
                RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter(), new ErrorLoggingFilter());
        }

        @BeforeEach
        public void setRequest() {
                request = given()
                                .config(RestAssured.config()
                                                .logConfig(LogConfig.logConfig()
                                                                .enableLoggingOfRequestAndResponseIfValidationFails()))
                                
                                //.auth().basic("admin", "password123")
                                .header("Authorization","Basic YWRtaW46cGFzc3dvcmQxMjM=")
                                .contentType(ContentType.JSON);

        }

        @Test
        public void getAllBookingsById_returnOk() {
                Response response = request
                                .when()
                                .get("/booking")
                                .then()
                                .extract()
                                .response();

                Assertions.assertNotNull(response);
                Assertions.assertEquals(200, response.statusCode());

        }

        @Test
        public void getAllBookingsByUserFirtName_BookingExists_ReturnOk() {

                request
                                .when()
                                .queryParam("firstName", "Dani")
                                .get("/booking")
                                .then()
                                .assertThat()
                                .statusCode(200)
                                .contentType(ContentType.JSON)
                                .and()
                                .body("results", hasSize(greaterThan(0)));

        }

        @Test
        public void CreateBooking_WithValidData_returnOk() {
                given()
                                .config(RestAssured.config()
                                                .logConfig(LogConfig.logConfig()
                                                                .enableLoggingOfRequestAndResponseIfValidationFails()))
                                .contentType(ContentType.JSON)
                                .when()
                                .body(booking)
                                .post("/booking")
                                .then()
                                .body(JsonSchemaValidator
                                                .matchesJsonSchemaInClasspath("createBookingResponseSchema.json"))
                                .and()
                                .assertThat()
                                .statusCode(HttpStatus.SC_OK)
                                .contentType(ContentType.JSON);

        }

        @Test
        public void UpdateBooking_WithValidData_returnOk() {
                request
                                .config(RestAssured.config()
                                                .logConfig(LogConfig.logConfig()
                                                                .enableLoggingOfRequestAndResponseIfValidationFails()))
                                .contentType(ContentType.JSON)
                                .when()
                                .body(booking)
                                .put("/booking/24")
                                .then()
                                .statusCode(HttpStatus.SC_OK)
                                .and()
                                .assertThat()
                                .contentType(ContentType.JSON);
        }

        @Test
        public void PartialUpdate_WithValidData_returnOk() {
                request
                                .config(RestAssured.config()
                                                .logConfig(LogConfig.logConfig()
                                                                .enableLoggingOfRequestAndResponseIfValidationFails()))
                                .contentType(ContentType.JSON)
                                .when()
                                .body("{\"firstname\":\"Daniele\"}")
                                .patch("/booking/43")
                                .then()
                                .statusCode(HttpStatus.SC_OK)
                                .and()
                                .assertThat()
                                .contentType(ContentType.JSON);
        }

        @Test
        public void PartialUpdate_WithValidData_returnBadRequest() {
                request
                                .config(RestAssured.config()
                                                .logConfig(LogConfig.logConfig()
                                                                .enableLoggingOfRequestAndResponseIfValidationFails()))
                                .contentType(ContentType.JSON)
                                .when()
                                .body("enviando uma requisição inválida")
                                .patch("/booking/43")
                                .then()
                                .statusCode(HttpStatus.SC_BAD_REQUEST);
                               
        }
}
