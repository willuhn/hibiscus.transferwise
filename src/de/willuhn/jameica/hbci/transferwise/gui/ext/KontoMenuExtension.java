/**********************************************************************
 *
 * Copyright (c) 2020 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.transferwise.gui.ext;

import de.willuhn.jameica.gui.extension.Extendable;
import de.willuhn.jameica.gui.extension.Extension;
import de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem;
import de.willuhn.jameica.gui.parts.ContextMenuItem;
import de.willuhn.jameica.hbci.gui.menus.KontoList;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.transferwise.Plugin;
import de.willuhn.jameica.hbci.transferwise.gui.action.SetupTransferWiseStep1;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Erweitert das Kontextmenu der Konten um die TransferWise-Funktionen.
 */
public class KontoMenuExtension implements Extension
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.gui.extension.Extension#extend(de.willuhn.jameica.gui.extension.Extendable)
   */
  @Override
  public void extend(Extendable extendable)
  {
    final KontoList menu = (KontoList) extendable;

    menu.addItem(ContextMenuItem.SEPARATOR);
    menu.addItem(new CheckedSingleContextMenuItem(i18n.tr("TransferWise-Einrichtung starten..."), new SetupTransferWiseStep1(),"document-properties.png") {
      /**
       * @see de.willuhn.jameica.gui.parts.CheckedSingleContextMenuItem#isEnabledFor(java.lang.Object)
       */
      @Override
      public boolean isEnabledFor(Object o)
      {
        if (!(o instanceof Konto))
          return false;
        return super.isEnabledFor(o) && Plugin.getStatus((Konto) o).checkInitial();
      }
    });
  }
}
