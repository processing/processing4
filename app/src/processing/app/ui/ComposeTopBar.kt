@file:JvmName("ComposeTopBarBridge")
package processing.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import processing.app.Base
import java.awt.Color as AwtColor
import processing.app.Platform
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.JComponent
import javax.swing.JMenu
import javax.swing.KeyStroke
import javax.swing.event.MenuEvent

fun awtToCompose(c: AwtColor): Color {
    return Color(c.red, c.green, c.blue, c.alpha)
}

fun themeColorOrFallback(key: String, fallback: AwtColor): Color {
    val awt = Theme.getColor(key) ?: fallback
    return awtToCompose(awt)
}

data class TopBarItemData(
    val label: String,
    val onClick: (ComposePanel, Base, Editor, Int, Int) -> Unit
)

@Composable
fun TopBar(panel: ComposePanel, base: Base, editor: Editor) {
    val blueBarColor = themeColorOrFallback("toolbar.gradient.top", AwtColor(107, 160, 204))
    val textColor = themeColorOrFallback("toolbar.rollover.color", AwtColor(0, 0, 0))

    val items = listOf(
        TopBarItemData("File") { p, _, e, x, y ->
            showFilePopup(p, e, x, y)
        },
        TopBarItemData("Edit") { p, _, e, x, y ->
            showEditPopup(p, e, x, y)
        },
        TopBarItemData("Sketch") { p, _, e, x, y ->
            showSketchPopup(p,  e, x, y)
        },
        TopBarItemData("Debug") { p, b, e, x, y ->
            //showMenuPopup(p, b, e, x, y)
        },
        TopBarItemData("Tools") { p, b, e, x, y ->
            showToolsPopup(p, e,x,y )
        },
        TopBarItemData("Help") { p, _, e, x, y ->
            showHelpPopup(p, e, x, y)
        },
        TopBarItemData("Develop") { p, _, e, x, y ->
            showDevelopPopup(p, e, x, y)
        }
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(blueBarColor),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            TopBarItem(
                label = item.label,
                textColor = textColor,
                modifier = Modifier.weight(1f)
            ) { x, y ->
                item.onClick(panel, base, editor, x, y)
            }
        }
    }
}

@Composable
private fun TopBarItem(
    label: String,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: (x: Int, y: Int) -> Unit
) {
    val anchor = remember { mutableStateOf(IntOffset.Zero) }  //stores the location of the top bar item
    val height = remember { mutableStateOf(0) } //stores the height of the top bar item

    Box(
        modifier = modifier
            .heightIn(min = 32.dp)
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInRoot()    //top left corner of drop down
                anchor.value = IntOffset(position.x.toInt(), position.y.toInt()) //x and y positioning
                height.value = coordinates.size.height //height of the menu
            }
            .clickable {
                onClick(anchor.value.x, anchor.value.y + height.value)
            //anchor x lines up menu with top left edge of the clicked menu
                //anchor y + height value moves the pop up below the tool bar item
            }
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp, vertical = 4.dp)
        )
    }
}




///pop up menus///

private fun showFilePopup(panel: ComposePanel, editor: Editor, x: Int, y: Int) {
    val menu = editor.buildFileMenu()
    showPopupFromMenu(panel, menu, x, y)
}


private fun showEditPopup(panel: ComposePanel, editor: Editor, x: Int, y: Int) {
    val method = editor.javaClass.superclass.getDeclaredMethod("buildEditMenu")
    method.isAccessible = true
    val menu = method.invoke(editor) as JMenu
    showPopupFromMenu(panel, menu, x, y)
}

private fun showSketchPopup(panel: ComposePanel, editor: Editor, x: Int, y: Int) {
    val menu = editor.buildSketchMenu()
    showPopupFromMenu(panel, menu, x, y)
}

private fun showToolsPopup(panel: ComposePanel, editor: Editor, x: Int, y: Int) {
    val menu = editor.getToolMenu()
    showPopupFromMenu(panel, menu, x, y)
}

private fun showHelpPopup(panel: ComposePanel, editor: Editor, x: Int, y: Int) {
    val menu = editor.buildHelpMenu()
    showPopupFromMenu(panel, menu, x, y)
}

private fun showDevelopPopup(panel: ComposePanel, editor: Editor, x: Int, y: Int) {
    editor.buildDevelopMenu()

    val field = editor.javaClass.superclass.getDeclaredField("developMenu")
    field.isAccessible = true
    val menu = field.get(editor) as JMenu

    showPopupFromMenu(panel, menu, x, y)
}

private fun showPopupFromMenu(panel: ComposePanel, menu: JMenu, x: Int, y: Int) {
    val popup = menu.popupMenu

    fun refreshTopBar() {
        javax.swing.SwingUtilities.invokeLater {
            panel.revalidate()
            panel.repaint()
        }
    }

    popup.addPopupMenuListener(object : javax.swing.event.PopupMenuListener {
        override fun popupMenuWillBecomeVisible(e: javax.swing.event.PopupMenuEvent?) = Unit // nothing needed right before the popup becomes visible

        override fun popupMenuWillBecomeInvisible(e: javax.swing.event.PopupMenuEvent?) { // called when the popup closes normally
            refreshTopBar() //redraw top bar
        }

        override fun popupMenuCanceled(e: javax.swing.event.PopupMenuEvent?) {   // called when the popup is canceled, like clicking away
            refreshTopBar() //redrawing...
        }
    })

    val event = MenuEvent(menu)
    menu.menuListeners.forEach { it.menuSelected(event) }
    popup.show(panel, x, y)
}

///^^^ pop up menus ^^^////



//keyboard shortcuts do not work right now unless the dropdown is opened, this did not fix that.
//but it still has potential to be reworked.

//private fun bindShortcuts(editor: Editor, base: Base) {
//    val root = editor.rootPane
//
//    val input = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
//    val actions = root.actionMap
//
//    fun bind(name: String, key: KeyStroke?, action: () -> Unit) {
//        if (key == null) return
//        input.put(key, name)
//        actions.put(name, object : AbstractAction() {
//            override fun actionPerformed(e: ActionEvent?) {
//                action()
//            }
//        })
//    }
//
//    bind("new", Toolkit.getKeyStrokeExt("menu.file.new")) {
//        base.handleNew()
//    }
//
//    bind("open", Toolkit.getKeyStrokeExt("menu.file.open")) {
//        base.handleOpenPrompt()
//    }
//
//    bind("close", Toolkit.getKeyStrokeExt("menu.file.close")) {
//        base.handleClose(editor, false)
//    }
//
//    bind("save", Toolkit.getKeyStrokeExt("menu.file.save")) {
//        editor.handleSave(false)
//    }
//
//    bind("saveAs", Toolkit.getKeyStrokeExt("menu.file.save_as")) {
//        editor.handleSaveAs()
//    }
//
//    bind("print", Toolkit.getKeyStrokeExt("menu.file.print")) {
//        editor.handlePrint()
//    }
//
//    bind("pageSetup", Toolkit.getKeyStrokeExt("menu.file.page_setup")) {
//        editor.handlePageSetup()
//    }
//
//    if (!Platform.isMacOS()) {
//        bind("prefs", Toolkit.getKeyStrokeExt("menu.file.preferences")) {
//            base.handlePrefs()
//        }
//
//        bind("quit", Toolkit.getKeyStrokeExt("menu.file.quit")) {
//            base.handleQuit()
//        }
//    }
//}


fun mountTopBar(panel: ComposePanel, base: Base, editor: Editor) {
    val awtBg = Theme.getColor("toolbar.gradient.top") ?: AwtColor(107, 160, 204)
    panel.background = awtBg

//    bindShortcuts(editor, base)

    panel.setContent {
        TopBar(panel, base, editor)
    }
}



//Hi, so...
//line 1050 in editor.java, is the tool menu, so inside your function call editor.getToolMenu() to access the drop down stuff there
//without manually re-entering everything!
//the class is public so you should get away with copying and pasting the showSketchPopup and replace the names and what
//its calling obvi

//then line 1085 in editor.java, is the Help menu. This should be built the same way as showSketchPopup as well!
//good luck! if you happen to feel up to it Debug is the last one, but I have no idea where that is.





