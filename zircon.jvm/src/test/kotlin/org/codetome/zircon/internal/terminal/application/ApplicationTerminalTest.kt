package org.codetome.zircon.internal.terminal.application

import org.assertj.core.api.Assertions.assertThat
import org.codetome.zircon.api.data.Size
import org.codetome.zircon.api.data.Tile
import org.codetome.zircon.api.builder.terminal.DeviceConfigurationBuilder
import org.codetome.zircon.api.font.Font
import org.codetome.zircon.api.font.FontTextureRegion
import org.codetome.zircon.api.input.KeyStroke
import org.codetome.zircon.api.resource.CP437TilesetResource
import org.codetome.zircon.api.terminal.CursorStyle
import org.codetome.zircon.internal.component.impl.DefaultLabelTest
import org.codetome.zircon.internal.event.Event
import org.codetome.zircon.internal.event.EventBus
import org.codetome.zircon.internal.font.impl.FontLoaderRegistry
import org.codetome.zircon.internal.font.impl.TestFontLoader
import org.codetome.zircon.internal.terminal.virtual.VirtualTerminal
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean


class ApplicationTerminalTest {

    lateinit var target: ApplicationTerminal
    lateinit var font: Font

    val fontTextureDraws = mutableListOf<Triple<FontTextureRegion<*>, Int, Int>>()
    val cursorDraws = mutableListOf<Triple<Tile, Int, Int>>()
    var rendered = AtomicBoolean(false)

    @Before
    fun setUp() {
        FontLoaderRegistry.setFontLoader(TestFontLoader())
        font = DefaultLabelTest.FONT.toFont()
        target = object : ApplicationTerminal(
                deviceConfiguration = CONFIG,
                terminal = VirtualTerminal(
                        initialSize = SIZE,
                        initialFont = font)) {
            override fun drawFontTextureRegion(fontTextureRegion: FontTextureRegion<*>, x: Int, y: Int) {
                fontTextureDraws.add(Triple(fontTextureRegion, x, y))
            }

            override fun drawCursor(character: Tile, x: Int, y: Int) {
                cursorDraws.add(Triple(character, x, y))
            }

            override fun getHeight() = SIZE.yLength * font.getHeight()

            override fun getWidth() = SIZE.xLength * font.getWidth()

            override fun doRender() {
                super.doRender()
                rendered.set(true)
            }
        }
    }


    @Ignore
    @Test
    fun shouldRenderAfterCreateIfCursorBlinksAndEnoughTimePassed() {
        target.doCreate()
        Thread.sleep(500)

        assertThat(rendered.get()).isTrue()
    }

    @Test
    fun shouldSendEofOnDispose() {
        val eofReceived = AtomicBoolean(false)
        EventBus.subscribe<Event.Input> {
            if (it.input == KeyStroke.EOF_STROKE) {
                eofReceived.set(true)
            }
        }

        target.doDispose()

        assertThat(eofReceived.get()).isTrue()
    }


    companion object {
        private const val BLINK_LEN_MS = 2L
        val SIZE = Size.create(10, 20)
        val CONFIG = DeviceConfigurationBuilder.newBuilder()
                .cursorBlinking(true)
                .blinkLengthInMilliSeconds(BLINK_LEN_MS)
                .cursorStyle(CursorStyle.USE_CHARACTER_FOREGROUND)
                .build()
        val FONT = CP437TilesetResource.WANDERLUST_16X16
    }
}