package de.codecentric.hikaku.converters.ktor

import de.codecentric.hikaku.SupportedFeatures
import de.codecentric.hikaku.SupportedFeatures.*
import de.codecentric.hikaku.converters.AbstractEndpointConverter
import de.codecentric.hikaku.converters.ktor.extensions.hikakuHttpMethod
import de.codecentric.hikaku.converters.ktor.extensions.hikakuPathParameters
import de.codecentric.hikaku.endpoints.Endpoint
import io.ktor.routing.HttpMethodRouteSelector
import io.ktor.routing.Route
import io.ktor.routing.Routing

/**
 * Extracts and converts [Endpoint]s from ktor [Routing] object.
 * @param routing Ktor routing information.
 */
class KtorConverter(private val routing: Routing) : AbstractEndpointConverter() {

    override val supportedFeatures = SupportedFeatures(
            Feature.PathParameter
    )

    override fun convert(): Set<Endpoint> {
        return routing.children.flatMap {
            return@flatMap findEndpoints(it)
        }
        .toSet()
    }

    private fun findEndpoints(route: Route): List<Endpoint> {
        if (route.children.isNotEmpty()) {
            return route.children.flatMap {
                return@flatMap findEndpoints(it)
            }
        }

        if (route.selector is HttpMethodRouteSelector) {
            val normalizedPath = normalizePath(route.parent.toString())

            return listOf(
                    Endpoint(
                            path = normalizedPath,
                            httpMethod = (route.selector as HttpMethodRouteSelector).hikakuHttpMethod(),
                            pathParameters = route.hikakuPathParameters()
                    )
            )
        }

        return emptyList()
    }

    /**
     * Removes ? from optional path parameters, because an optional path parameter will lead to two endpoints.
     * Example:
     *      /todos/{id?}
     * will become
     *      /todos
     *      /todos/{id}
     */
    private fun normalizePath(path: String) = path.replace("?", "")
}