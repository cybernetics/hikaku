package de.codecentric.hikaku.converters.openapi

import de.codecentric.hikaku.endpoints.PathParameter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class OpenApiConverterPathParameterTest {

    @Test
    fun `check that path parameter are extracted correctly`() {
        //given
        val file = Paths.get(this::class.java.classLoader.getResource("path_parameter.yaml").toURI())
        val pathParameter = PathParameter("id")

        //when
        val specification = OpenApiConverter(file)

        //then
        val resultingEndpoints = specification.conversionResult.toList()
        assertThat(resultingEndpoints[0].pathParameters).containsExactly(pathParameter)
        assertThat(resultingEndpoints[1].pathParameters).containsExactly(pathParameter)
        assertThat(resultingEndpoints[2].pathParameters).containsExactly(pathParameter)
    }
}