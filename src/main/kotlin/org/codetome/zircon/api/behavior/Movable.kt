package org.codetome.zircon.api.behavior

import org.codetome.zircon.api.Position

/**
 * A [Movable] object is a specialized [Positionable].
 * It can not only be positioned but moved as well.
 */
interface Movable : Positionable {

    /**
     * Sets the (absolute) position of this [Movable].
     */
    fun moveTo(position: Position)
}