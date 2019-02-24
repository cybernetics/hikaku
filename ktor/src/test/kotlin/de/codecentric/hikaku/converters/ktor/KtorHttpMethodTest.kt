package de.codecentric.hikaku.converters.ktor

import de.codecentric.hikaku.endpoints.Endpoint
import de.codecentric.hikaku.endpoints.HttpMethod.*
import io.ktor.routing.*
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KtorHttpMethodTest {

    @Test
    fun `extract all http methods using http method providing the path`() {
        //given
        val routing = Routing(mockk()).apply {
            get("/todos") { }
            post("/todos") { }
            put("/todos") { }
            patch("/todos") { }
            delete("/todos") { }
            head("/todos") { }
            options("/todos") { }
        }

        val specification = setOf(
                Endpoint("/todos", GET),
                Endpoint("/todos", POST),
                Endpoint("/todos", PUT),
                Endpoint("/todos", PATCH),
                Endpoint("/todos", DELETE),
                Endpoint("/todos", HEAD),
                Endpoint("/todos", OPTIONS)
        )

        //when
        val implementation = KtorConverter(routing).conversionResult

        //then
        assertThat(implementation).containsExactlyInAnyOrderElementsOf(specification)
    }

    @Test
    fun `extract all http methods using http method nested in a route object`() {
        //given
        val routing = Routing(mockk()).apply {
            route("/todos") {
                get { }
                post { }
                put { }
                patch { }
                delete { }
                head { }
                options { }
            }
        }

        val specification = setOf(
                Endpoint("/todos", GET),
                Endpoint("/todos", POST),
                Endpoint("/todos", PUT),
                Endpoint("/todos", PATCH),
                Endpoint("/todos", DELETE),
                Endpoint("/todos", HEAD),
                Endpoint("/todos", OPTIONS)
        )

        //when
        val implementation = KtorConverter(routing).conversionResult

        //then
        assertThat(implementation).containsExactlyInAnyOrderElementsOf(specification)
    }

    @Test
    fun `extract all http methods using http method in a double nested route object`() {
        //given
        val routing = Routing(mockk()).apply {
            route("/todo") {
                route("/list") {
                    get { }
                    post { }
                    put { }
                    patch { }
                    delete { }
                    head { }
                    options { }
                }
            }
        }

        val specification = setOf(
                Endpoint("/todo/list", GET),
                Endpoint("/todo/list", POST),
                Endpoint("/todo/list", PUT),
                Endpoint("/todo/list", PATCH),
                Endpoint("/todo/list", DELETE),
                Endpoint("/todo/list", HEAD),
                Endpoint("/todo/list", OPTIONS)
        )

        //when
        val implementation = KtorConverter(routing).conversionResult

        //then
        assertThat(implementation).containsExactlyInAnyOrderElementsOf(specification)
    }
}