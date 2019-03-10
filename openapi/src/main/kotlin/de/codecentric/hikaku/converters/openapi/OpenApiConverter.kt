package de.codecentric.hikaku.converters.openapi

import de.codecentric.hikaku.SupportedFeatures
import de.codecentric.hikaku.converters.AbstractEndpointConverter
import de.codecentric.hikaku.converters.openapi.extensions.httpMethods
import de.codecentric.hikaku.endpoints.Endpoint
import de.codecentric.hikaku.endpoints.HttpMethod
import de.codecentric.hikaku.endpoints.PathParameter
import de.codecentric.hikaku.endpoints.QueryParameter
import io.swagger.v3.oas.models.parameters.QueryParameter as OpenApiQueryParameter
import io.swagger.v3.oas.models.parameters.PathParameter as OpenApiPathParameter
import io.swagger.v3.oas.models.parameters.HeaderParameter as OpenApiHeaderParameter
import de.codecentric.hikaku.SupportedFeatures.Feature
import de.codecentric.hikaku.endpoints.*
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.parser.OpenAPIV3Parser
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Path

/**
 * Extracts and converts [Endpoint]s from OpenAPI 3.0.X document. Either a *.yaml*, *.yml* or a *.json* file.
 *
 * In Java invoke via: `OpenApiConverter.usingPath(Paths.get("");` or `OpenApiConverter.usingFile(new File("");`
 */
class OpenApiConverter private constructor(private val openApiSpecification: String) : AbstractEndpointConverter() {

    private lateinit var openApi: OpenAPI

    init {
        if (openApiSpecification.isBlank()) {
            throw IllegalArgumentException("Given specification is blank.")
        }
    }

    override val supportedFeatures = SupportedFeatures(
            Feature.QueryParameter,
            Feature.PathParameter,
            Feature.HeaderParameter,
            Feature.Produces,
            Feature.Consumes
    )

    override fun convert(): Set<Endpoint> {
        openApi = OpenAPIV3Parser().readContents(openApiSpecification, null, null).openAPI

        return openApi.paths.flatMap { (path, pathItem) ->
            pathItem.httpMethods().map { (httpMethod: HttpMethod, operation: Operation?) ->
                createEndpoint(path, httpMethod, operation)
            }
        }.toSet()
    }

    private fun createEndpoint(
            path: String,
            httpMethod: HttpMethod,
            operation: Operation?
    ): Endpoint {
        val queryParameters = mutableSetOf<QueryParameter>()
        val pathParameters = mutableSetOf<PathParameter>()
        val headerParameters = mutableSetOf<HeaderParameter>()

        operation?.parameters?.forEach {
            when (it) {
                is OpenApiQueryParameter -> queryParameters += QueryParameter(it.name, it.required)
                is OpenApiPathParameter -> pathParameters += PathParameter(it.name)
                is OpenApiHeaderParameter -> headerParameters += HeaderParameter(it.name, it.required)
            }
        }

        return Endpoint(
                path = path,
                httpMethod = httpMethod,
                queryParameters = queryParameters,
                pathParameters = pathParameters,
                headerParameters = headerParameters,
                produces = extractProduceMediaTypes(operation),
                consumes = extractConsumesMediaTypes(operation)
        )
    }

    private fun extractConsumesMediaTypes(operation: Operation?): Set<String> {
        return operation?.requestBody
                ?.content
                ?.keys
                .orEmpty()
                .union(extractConsumesFromComponents(operation))
                .toSet()
    }

    private fun extractConsumesFromComponents(operation: Operation?): Set<String> {
        return operation?.requestBody
                ?.`$ref`
                ?.let {
                    Regex("#/components/requestBodies/(?<key>.+)")
                            .find(it)
                            ?.groups
                            ?.get("key")
                            ?.value
                }
                ?.let {
                    openApi.components
                            .requestBodies[it]
                            ?.content
                            ?.keys
                }
                .orEmpty()
    }

    private fun extractProduceMediaTypes(operation: Operation?): Set<String> {
        return operation?.responses
                ?.flatMap {
                    it.value
                            ?.content
                            ?.keys
                            .orEmpty()
                }
                .orEmpty()
                .union(extractResponsesFromComponents(operation))
                .toSet()
    }

    private fun extractResponsesFromComponents(operation: Operation?): Set<String> {
        return operation?.responses
                ?.mapNotNull { it.value.`$ref` }
                ?.map {
                    Regex("#/components/responses/(?<key>.+)")
                            .find(it)
                            ?.groups
                            ?.get("key")
                            ?.value
                }
                ?.flatMap {
                    openApi.components
                            .responses[it]
                            ?.content
                            ?.keys
                            .orEmpty()
                }
                .orEmpty()
                .toSet()
    }

    companion object {
        @JvmStatic
        @JvmName("usingPath")
        operator fun invoke(openApiSpecification: Path): OpenApiConverter {
            checkFileValidity(openApiSpecification)

            val expectedFileContent = Files.readAllLines(openApiSpecification, UTF_8).joinToString("\n")

            return OpenApiConverter(expectedFileContent)
        }

        private fun checkFileValidity(openApiSpecification: Path) {
            if (!Files.exists(openApiSpecification)) {
                throw IllegalArgumentException("Given specification file does not exist.")
            }

            if (!Files.isRegularFile(openApiSpecification)) {
                throw IllegalArgumentException("Given specification file is not a regular file.")
            }

            if (!isValidFileExtension(openApiSpecification)) {
                throw IllegalArgumentException("Given file is not of type *.json, *.yaml or *.yml.")
            }
        }

        @JvmStatic
        @JvmName("usingFile")
        operator fun invoke(openApiSpecification: File): OpenApiConverter {
            return OpenApiConverter(openApiSpecification.toPath())
        }

        private fun isValidFileExtension(openApiSpecification: Path): Boolean {
            val fileName = openApiSpecification.fileName.toString()

            return fileName.endsWith(".json")
                    || fileName.endsWith(".yaml")
                    || fileName.endsWith(".yml")
        }
    }
}