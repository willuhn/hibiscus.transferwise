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

import java.security.KeyPair;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.BackgroundTaskDialog;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.transferwise.KeyStorage;
import de.willuhn.jameica.hbci.transferwise.Plugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Action fuer die Erstellung eines neuen Schluesselpaares.
 */
public class KeyPairCreate implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
  
  private KeyPair kp = null;

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    if (!(context instanceof Konto))
      throw new ApplicationException(i18n.tr("Bitte wählen Sie ein Konto aus."));
    
    final Konto konto = (Konto) context;
    
    BackgroundTask task = new BackgroundTask() {
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        // Wir starten den Progress in einem extra Thread
        final Thread progress = new Thread()
        {
          /**
           * @see java.lang.Thread#run()
           */
          @Override
          public void run()
          {
            for (int i=0;i<100;++i)
            {
              try
              {
                Thread.sleep(20L);
                monitor.addPercentComplete(1);
              }
              catch (InterruptedException e)
              {
                return;
              }
            }
          }
        };
        progress.start();
        
        kp = KeyStorage.getKey(konto); // Fuer den Fall, dass der User einen Schluessel erstellt aber nicht abgespeichert hat.
        if (kp == null)
          kp = KeyStorage.createKey(konto);
        
        progress.interrupt();
        monitor.setPercentComplete(100);
      }
      public boolean isInterrupted()
      {
        return false;
      }
      public void interrupt()
      {
      }
    };

    try
    {
      BackgroundTaskDialog bd = new BackgroundTaskDialog(BackgroundTaskDialog.POSITION_CENTER,task);
      bd.setTitle(i18n.tr("Schlüsselerstellung"));
      bd.setSideImage(SWTUtil.getImage("dialog-password.png"));
      bd.setPanelText(i18n.tr("Erstelle neues Schlüsselpaar"));
      bd.open();
      
      new PublicKeySave().handleAction(kp.getPublic());
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
      throw new ApplicationException(i18n.tr("Erstellung des Schlüsselpaares fehlgeschlagen: {0}",e.getMessage()));
    }
  }

}


