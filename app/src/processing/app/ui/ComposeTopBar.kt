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
import processing.app.Language
import processing.app.Preferences
import processing.app.UpdateCheck
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import java.awt.Color as AwtColor

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
        TopBarItemData("File") { p, b, e, x, y ->
            showMenuPopup(p, b, e, x, y)
        },
        TopBarItemData("Edit") { p, b, e, x, y ->
            //showMenuPopup(p, b, e, x, y)
        },
        TopBarItemData("Sketch") { p, b, e, x, y ->
            //showMenuPopup(p, b, e, x, y)
        },
        TopBarItemData("Debug") { p, b, e, x, y ->
            //showMenuPopup(p, b, e, x, y)
        },
        TopBarItemData("Tools") { p, b, e, x, y ->
            //showMenuPopup(p, b, e, x, y)
        },
        TopBarItemData("Help") { p, b, e, x, y ->
            //showMenuPopup(p, b, e, x, y)
        },
        TopBarItemData("Develop") { p, b, e, x, y ->
            showDevelopPopup(p, b, x, y)
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
private fun showDevelopPopup(panel: ComposePanel, base: Base, x: Int, y: Int) {
    val popup = JPopupMenu()

    val updatesItem = JMenuItem("Check for Updates")
    updatesItem.addActionListener {
        Preferences.unset("update.last")
        Preferences.setInteger("update.beta_welcome", 0)
        UpdateCheck(base)
    }

    popup.add(updatesItem)
    popup.show(panel, x, y)
}

private fun showMenuPopup(panel: ComposePanel, base: Base, editor: Editor, x: Int, y: Int) {
    val popup = JPopupMenu()

    val fileNew = Toolkit.newJMenuItem(Language.text("menu.file.new"), 'N'.code);
    fileNew.addActionListener {
        base.handleNew()
    }
    popup.add(fileNew);

    val fileOpen = Toolkit.newJMenuItem(Language.text("menu.file.open"), 'O'.code);
    fileOpen.addActionListener {
        base.handleOpenPrompt();
    }
    popup.add(fileOpen);

    val fileSketchbook = Toolkit.newJMenuItemShift(Language.text("menu.file.sketchbook"), 'K'.code);
    fileSketchbook.addActionListener {
        base.showSketchbookFrame()
    }
    popup.add(fileSketchbook);

    val fileExamples = Toolkit.newJMenuItemShift(Language.text("menu.file.examples"), 'O'.code);
    fileExamples.addActionListener {
        base.showExamplesFrame()
    }
    popup.add(fileExamples);

    val fileClose = Toolkit.newJMenuItem(Language.text("menu.file.close"), 'W'.code);
    fileClose.addActionListener {
        base.handleClose(editor, false);
    }
    popup.add(fileClose);

    val fileSave = Toolkit.newJMenuItem(Language.text("menu.file.save"), 'S'.code)
    fileSave.addActionListener {
        editor.handleSave(false);
    }
    popup.add(fileSave);

    val fileSaveAs = Toolkit.newJMenuItemShift(Language.text("menu.file.save_as"), 'S'.code);
    fileSaveAs.addActionListener {
        editor.handleSaveAs();
    }
    popup.add(fileSaveAs);

    val filePageSetup = Toolkit.newJMenuItemShift(Language.text("menu.file.page_setup"), 'P'.code);
    filePageSetup.addActionListener {
        editor.handlePageSetup();
    }
    popup.add(filePageSetup);

    val filePrint = Toolkit.newJMenuItem(Language.text("menu.file.print"), 'P'.code);
    filePrint.addActionListener {
        editor.handlePrint();
    }
    popup.add(filePrint);


    //   UNDER MAC OS ONLY SECTION   /// - will have to deal with this.
    val filePreferences = Toolkit.newJMenuItem(Language.text("menu.file.preferences"), ','.code);
    filePreferences.addActionListener {
        base.handlePrefs()
    }
    popup.add(filePreferences);

    val fileQuit = Toolkit.newJMenuItem(Language.text("menu.file.quit"), 'Q'.code);
    fileQuit.addActionListener {
        base.handleQuit()
    }
    popup.add(fileQuit);

    //^^^ UNDER MAC OS ONLY SECTION ^^^/////



    popup.show(panel, x, y)
}




///^^^ pop up menus ^^^////



fun mountTopBar(panel: ComposePanel, base: Base, editor: Editor) {
    val awtBg = Theme.getColor("toolbar.gradient.top") ?: AwtColor(107, 160, 204)
    panel.background = awtBg

    panel.setContent {
        TopBar(panel, base, editor)
    }
}


