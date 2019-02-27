package de.codecentric.hikaku.converters.ktor.extensions

import de.codecentric.hikaku.endpoints.PathParameter
import io.ktor.routing.PathSegmentOptionalParameterRouteSelector
import io.ktor.routing.PathSegmentParameterRouteSelector
import io.ktor.routing.Route

fun Route.pathParameters() = findPathParameter(this).toSet()

private fun findPathParameter(route: Route): List<PathParameter> {

    val parameters = mutableListOf<PathParameter>()

    if (route.selector is PathSegmentParameterRouteSelector) {
        parameters += PathParameter((route.selector as PathSegmentParameterRouteSelector).name)
    }

    if (route.selector is PathSegmentOptionalParameterRouteSelector) {
        parameters += PathParameter((route.selector as PathSegmentOptionalParameterRouteSelector).name)
    }

    if (route.parent != null && route.parent is Route) {
        parameters += findPathParameter(route.parent!!)
    }

    return parameters
}