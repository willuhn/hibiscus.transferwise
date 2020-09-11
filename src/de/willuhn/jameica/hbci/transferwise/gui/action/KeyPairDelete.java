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
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.transferwise.KeyStorage;
import de.willuhn.jameica.hbci.transferwise.Plugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Action zum Loeschen eines Schluesselpaares.
 */
public class KeyPairDelete implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof Konto))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Konto aus."));
    
    try
    {
      if (!Application.getCallback().askUser(i18n.tr("Sind Sie sicher, dass Sie den Schlüssel löschen wollen?"),false))
        return;
      
      KeyStorage.deleteKey((Konto) context);
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (OperationCanceledException oce)
    {
    }
    catch (Exception e)
    {
      throw new ApplicationException(i18n.tr("Löschen des Schlüsselpaares fehlgeschlagen: {0}",e.getMessage()));
    }
  }

}
