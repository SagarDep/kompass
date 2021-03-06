package io.sellmair.kompass.compiler.destination.renderer

import io.sellmair.kompass.compiler.common.FileRenderer
import io.sellmair.kompass.compiler.common.RenderContext
import io.sellmair.kompass.compiler.common.Renderable
import io.sellmair.kompass.compiler.common.associatedBy
import io.sellmair.kompass.compiler.destination.tree.DestinationRenderTree
import io.sellmair.kompass.compiler.destination.tree.DestinationsRenderTree
import io.sellmair.kompass.compiler.destination.tree.ExtensionRenderTree
import io.sellmair.kompass.compiler.destination.tree.KompassBuilderExtensionsRenderTree
import javax.tools.Diagnostic


/*
################################################################################################
INTERNAL API
################################################################################################
*/

internal interface DestinationsTreeRenderer : Renderable<DestinationsRenderTree> {
    companion object
}

internal operator fun DestinationsTreeRenderer.Companion.invoke(
    context: RenderContext,
    fileRenderer: FileRenderer): DestinationsTreeRenderer {
    return DestinationsTreeRendererImpl(context, fileRenderer)
}

/*
################################################################################################
PRIVATE IMPLEMENTATION
################################################################################################
*/

private class DestinationsTreeRendererImpl(
    val context: RenderContext,
    val fileRenderer: FileRenderer) : DestinationsTreeRenderer {
    override fun render(target: DestinationsRenderTree) {
        context.messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "Rendering...")
        renderAutoCrane(target)
        renderAutoMap(target)
        renderDestinations(target)
        renderKompassBuilderExtensions(target)
    }


    /*
    ################################################################################################
    RENDER AUTO CRANE
    ################################################################################################
    */

    fun renderAutoCrane(target: DestinationsRenderTree) {
        val autoCrane = target.autoCrane
        val fileSpec = autoCrane.file
            .addType(autoCrane.type
                .addFunction(autoCrane.get.build())
                .build())
            .build()

        val associatedFile = fileSpec.associatedBy(target.context.elements)
        fileRenderer.render(associatedFile)
    }


    /*
    ################################################################################################
    RENDER AUTO MAP
    ################################################################################################
    */

    fun renderAutoMap(target: DestinationsRenderTree) {
        val autoMap = target.autoMap
        val fileSpec = autoMap.file
            .addType(autoMap.type
                .addFunction(autoMap.get.build())
                .build())
            .build()

        val associatedFile = fileSpec.associatedBy(target.context.elements)
        fileRenderer.render(associatedFile)
    }


    /*
    ################################################################################################
    RENDER DESTINATIONS
    ################################################################################################
    */

    fun renderDestinations(target: DestinationsRenderTree) {
        for (destination in target.destinations) {
            context.messager.printMessage(
                Diagnostic.Kind.MANDATORY_WARNING,
                "Rendering ${destination.element.simpleName}")
            renderDestination(destination)
        }
    }

    fun renderDestination(target: DestinationRenderTree) {
        renderDestinationExtensions(target.extensions)
    }

    fun renderDestinationExtensions(target: ExtensionRenderTree) {
        val fileSpec = target.file
            .addFunction(target.bundleExtensions.asDestination.build())
            .addFunction(target.bundleExtensions.tryAsDestination.build())
            .addFunction(target.destinationExtensions.asBundle.build())
            .addFunction(target.kompassCompanionExtensions.bundleAsDestination.build())
            .addFunction(target.kompassCompanionExtensions.destinationAsBundle.build())
            .build()

        val associatedFile = fileSpec.associatedBy(context.elements)
        fileRenderer.render(associatedFile)
    }


    /*
    ################################################################################################
    RENDER KOMPASS BUILDER EXTENSIONS
    ################################################################################################
    */

    fun renderKompassBuilderExtensions(target: DestinationsRenderTree) {
        renderKompassBuilderExtensions(target.kompassBuilderExtensions)
    }

    fun renderKompassBuilderExtensions(target: KompassBuilderExtensionsRenderTree) {
        val fileSpec = target.file
            .addFunction(target.autoCrane.build())
            .addFunction(target.autoMap.build())
            .build()

        val associatedFile = fileSpec.associatedBy(target.context.elements)
        fileRenderer.render(associatedFile)
    }
}


