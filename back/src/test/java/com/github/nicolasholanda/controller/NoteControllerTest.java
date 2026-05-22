package com.github.nicolasholanda.controller;

import com.github.nicolasholanda.repository.NoteRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class NoteControllerTest {

    @Inject
    NoteRepository noteRepository;

    @BeforeEach
    @Transactional
    void cleanDb() {
        noteRepository.deleteAll();
    }

    @Test
    void createsNewNote() {
        given().contentType(ContentType.JSON)
            .body("{\"content\":\"hello\"}")
            .when().put("/api/notes/testnote")
            .then().statusCode(200)
            .body("content", equalTo("hello"))
            .body("version", is(1));
    }

    @Test
    void updateIncrementsVersion() {
        given().contentType(ContentType.JSON).body("{\"content\":\"first\"}")
            .when().put("/api/notes/testnote")
            .then().statusCode(200);

        given().contentType(ContentType.JSON).body("{\"content\":\"second\"}")
            .when().put("/api/notes/testnote")
            .then().statusCode(200)
            .body("content", equalTo("second"))
            .body("version", is(2));
    }

    @Test
    void fetchNonExistentReturns404() {
        given().when().get("/api/notes/doesnotexist")
            .then().statusCode(404);
    }

    @Test
    void fetchExistingReturnsContent() {
        given().contentType(ContentType.JSON).body("{\"content\":\"hello\"}")
            .when().put("/api/notes/testnote")
            .then().statusCode(200);

        given().when().get("/api/notes/testnote")
            .then().statusCode(200)
            .body("content", equalTo("hello"))
            .body("version", is(1));
    }

    @Test
    void invalidPathOnPutReturns400() {
        given().contentType(ContentType.JSON).body("{\"content\":\"x\"}")
            .when().put("/api/notes/bad!path")
            .then().statusCode(400);
    }

    @Test
    void invalidPathOnGetReturns400() {
        given().when().get("/api/notes/bad!path")
            .then().statusCode(400);
    }

    @Test
    void oversizedPayloadReturns413() {
        String big = "a".repeat(100 * 1024 + 1);
        given().contentType(ContentType.JSON)
            .body("{\"content\":\"" + big + "\"}")
            .when().put("/api/notes/testnote")
            .then().statusCode(413);
    }

    @Test
    void metaForExistingNote() {
        given().contentType(ContentType.JSON).body("{\"content\":\"x\"}")
            .when().put("/api/notes/testnote")
            .then().statusCode(200);

        given().when().get("/api/notes/testnote/meta")
            .then().statusCode(200)
            .body("exists", is(true))
            .body("hasPassword", is(false));
    }

    @Test
    void metaForMissingNote() {
        given().when().get("/api/notes/nothere/meta")
            .then().statusCode(200)
            .body("exists", is(false))
            .body("hasPassword", is(false));
    }
}
