package de.codecentric.hikaku.converters.ktor

import de.codecentric.hikaku.endpoints.Endpoint
import de.codecentric.hikaku.endpoints.HttpMethod.GET
import de.codecentric.hikaku.endpoints.PathParameter
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.route
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class KtorConverterPathParameterTest {


    @Nested
    inner class HttpMethodFunctionTests {

        @Test
        fun `single entry, nested path`() {
            //given
            val routing = Routing(mockk()).apply {
                get("/todos/{id}") { }
            }

            val specification = setOf(
                    Endpoint(
                            path = "/todos/{id}",
                            httpMethod = GET,
                            pathParameters = setOf(
                                    PathParameter("id")
                            )
                    )
            )

            //when
            val implementation = KtorConverter(routing).conversionResult

            //then
            assertThat(implementation).containsExactlyInAnyOrderElementsOf(specification)
        }

        @Test
        fun `single entry, double nested path`() {
            //given
            val routing = Routing(mockk()).apply {
                get("/profiles/{companyId}/{userId}") { }
            }

            val specification = setOf(
                    Endpoint(
                            path = "/profiles/{companyId}/{userId}",
                            httpMethod = GET,
                            pathParameters = setOf(
                                    PathParameter("userId"),
                                    PathParameter("companyId")
                            )
                    )
            )

            //when
            val implementation = KtorConverter(routing).conversionResult

            //then
            assertThat(implementation).containsExactlyInAnyOrderElementsOf(specification)
        }

        @Test
        fun `optional path parameter gets normalized`() {
            //given
            val routing = Routing(mockk()).apply {
                get("/todos/{id?}") { }
            }

            val specification = setOf(
                    Endpoint(
                            path = "/todos/{id}",
                            httpMethod = GET,
                            pathParameters = setOf(
                                    PathParameter("id")
                            )
                    )
            )

            //when
            val implementation = KtorConverter(routing).conversionResult

            //then
            assertThat(implementation).containsExactlyInAnyOrderElementsOf(specification)
        }
    }

    @Nested
    inner class RouteFunctionTests {

        @Test
        fun `route function, nested entry, nested path`() {
            //given
            val routing = Routing(mockk()).apply {
                route("/todos") {
                    route("/{id}") {
                        get {

                        }
                    }
                }
            }

            val specification = setOf(
                    Endpoint(
                            path = "/todos/{id}",
                            httpMethod = GET,
                            pathParameters = setOf(
                                    PathParameter("id")
                            )
                    )
            )

            //when
            val implementation = KtorConverter(routing).conversionResult

            //then
            assertThat(implementation).containsExactlyInAnyOrderElementsOf(specification)
        }

        @Test
        fun `nested entry, double nested path`() {
            //given
            val routing = Routing(mockk()).apply {
                route("/profiles") {
                    route("/{companyId}") {
                        route("/{userId}") {
                            get {

                            }
                        }
                    }
                }
            }

            val specification = setOf(
                    Endpoint(
                            path = "/profiles/{companyId}/{userId}",
                            httpMethod = GET,
                            pathParameters = setOf(
                                    PathParameter("userId"),
                                    PathParameter("companyId")
                            )
                    )
            )

            //when
            val implementation = KtorConverter(routing).conversionResult

            //then
            assertThat(implementation).containsExactlyInAnyOrderElementsOf(specification)
        }

        @Test
        fun `optional path parameter gets normalized`() {
            //given
            val routing = Routing(mockk()).apply {
                route("/todos/{id?}") {
                    get { }
                }
            }

            val specification = setOf(
                    Endpoint(
                            path = "/todos/{id}",
                            httpMethod = GET,
                            pathParameters = setOf(
                                    PathParameter("id")
                            )
                    )
            )

            //when
            val implementation = KtorConverter(routing).conversionResult

            //then
            assertThat(implementation).containsExactlyInAnyOrderElementsOf(specification)
        }
    }
}