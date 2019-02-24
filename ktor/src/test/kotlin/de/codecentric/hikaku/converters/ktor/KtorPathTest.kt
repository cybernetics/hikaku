package de.codecentric.hikaku.converters.ktor

import de.codecentric.hikaku.endpoints.Endpoint
import de.codecentric.hikaku.endpoints.HttpMethod.*
import io.ktor.routing.*
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class KtorPathTest {

    @Nested
    inner class SimplePathTests {
        @Test
        fun `http method function`() {
            //given
            val routing = Routing(mockk()).apply {
                get("/todos") { }
            }

            val specification = setOf(
                    Endpoint("/todos", GET)
            )

            //when
            val implementation = KtorConverter(routing).conversionResult

            //then
            assertThat(implementation).containsExactlyInAnyOrderElementsOf(specification)
        }

        @Test
        fun `route function`() {
            //given
            val routing = Routing(mockk()).apply {
                route("/todos") {
                    get { }
                }
            }

            val specification = setOf(
                    Endpoint("/todos", GET)
            )

            //when
            val implementation = KtorConverter(routing).conversionResult

            //then
            assertThat(implementation).containsExactlyInAnyOrderElementsOf(specification)
        }
    }

    @Nested
    inner class NestedPathTests {

        @Test
        fun `http method function`() {
            //given
            val routing = Routing(mockk()).apply {
                get("/todo/list") { }
            }

            val specification = setOf(
                    Endpoint("/todo/list", GET)
            )

            //when
            val implementation = KtorConverter(routing).conversionResult

            //then
            assertThat(implementation).containsExactlyInAnyOrderElementsOf(specification)
        }

        @Test
        fun `simple route function`() {
            //given
            val routing = Routing(mockk()).apply {
                route("/todo/list") {
                    get { }
                }
            }

            val specification = setOf(
                    Endpoint("/todo/list", GET)
            )

            //when
            val implementation = KtorConverter(routing).conversionResult

            //then
            assertThat(implementation).containsExactlyInAnyOrderElementsOf(specification)
        }

        @Test
        fun `nested route function`() {
            //given
            val routing = Routing(mockk()).apply {
                route("/todo") {
                    route("/list") {
                        get { }
                    }
                }
            }

            val specification = setOf(
                    Endpoint("/todo/list", GET)
            )

            //when
            val implementation = KtorConverter(routing).conversionResult

            //then
            assertThat(implementation).containsExactlyInAnyOrderElementsOf(specification)
        }


        @Test
        fun `nested route function having multiple endpoints`() {
            //given
            val routing = Routing(mockk()).apply {
                route("/todo") {
                    route("/list") {
                        get { }
                    }

                    post { }
                    delete { }
                }
            }

            val specification = setOf(
                    Endpoint("/todo/list", GET),
                    Endpoint("/todo", DELETE),
                    Endpoint("/todo", POST)
            )

            //when
            val implementation = KtorConverter(routing).conversionResult

            //then
            assertThat(implementation).containsExactlyInAnyOrderElementsOf(specification)
        }
    }
}