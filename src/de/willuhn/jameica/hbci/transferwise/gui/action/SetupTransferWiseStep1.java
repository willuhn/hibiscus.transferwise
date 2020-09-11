/**********************************************************************
 *
 * Copyright (c) 2020 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.transferwise.gui.action;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.transferwise.Plugin;
import de.willuhn.jameica.hbci.transferwise.SupportStatus;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zum Einrichten eines TransferWise-Kontos.
 */
public class SetupTransferWiseStep1 implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    SupportStatus status = null;
    
    if (context instanceof SupportStatus)
      status = (SupportStatus) context;
    else if (context instanceof Konto)
      status = Plugin.getStatus((Konto) context);
    
    if (status == null)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Konto aus"));

    GUI.startView(de.willuhn.jameica.hbci.transferwise.gui.views.SetupTransferWiseStep1.class,status);
  }

}
