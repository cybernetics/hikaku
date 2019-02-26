package de.codecentric.hikaku.converters.ktor

import de.codecentric.hikaku.SupportedFeatures
import de.codecentric.hikaku.converters.AbstractEndpointConverter
import de.codecentric.hikaku.converters.ktor.extensions.httpMethod
import de.codecentric.hikaku.endpoints.Endpoint
import io.ktor.routing.HttpMethodRouteSelector
import io.ktor.routing.Route
import io.ktor.routing.Routing

/**
 * Extracts and converts [Endpoint]s from ktor [Routing] object.
 * @param routing Ktor routing information.
 */
class KtorConverter(private val routing: Routing): AbstractEndpointConverter() {

    override val supportedFeatures = SupportedFeatures()

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
            return listOf(
                    Endpoint(
                            route.parent.toString(),
                            (route.selector as HttpMethodRouteSelector).httpMethod()
                    )
            )
        }

        return emptyList()
    }
}