/**********************************************************************
 *
 * Copyright (c) 2020 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.transferwise.gui.box;

import java.rmi.RemoteException;

import org.eclipse.swt.widgets.Composite;

import de.willuhn.jameica.gui.boxes.AbstractBox;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.hbci.gui.filter.KontoFilter;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.server.KontoUtil;
import de.willuhn.jameica.hbci.transferwise.Plugin;
import de.willuhn.jameica.hbci.transferwise.SupportStatus;
import de.willuhn.jameica.hbci.transferwise.gui.action.SetupTransferWiseStep1;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Box, die prueft, ob die Transferwise-Konten korrekt konfiguriert sind und bei Bedarf einen Assistenten startet.
 */
public class SetupTransferwiseBox extends AbstractBox
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getName()
   */
  @Override
  public String getName()
  {
    return i18n.tr("TransferWise: Setup-Assistent");
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultEnabled()
   */
  @Override
  public boolean getDefaultEnabled()
  {
    return true;
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#setEnabled(boolean)
   */
  public void setEnabled(boolean enabled)
  {
    // Das darf der User nicht.
  }

  /**
   * @see de.willuhn.jameica.gui.boxes.Box#getDefaultIndex()
   */
  @Override
  public int getDefaultIndex()
  {
    return 0;
  }

  /**
   * @see de.willuhn.jameica.gui.Part#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void paint(Composite parent) throws RemoteException
  {
    final SupportStatus status = this.getNextUnconfigured();
    if (status == null)
      return;

    InfoPanel panel = new InfoPanel();
    panel.setIcon("transferwise-large.png");
    panel.setTitle(i18n.tr("TransferWise-Konto konfigurieren"));
    panel.setText(i18n.tr("Das Konto mit der IBAN \"{0}\" ist noch nicht vollst‰ndig konfiguriert.\n" +
                          "Klicken Sie auf \"Konto einrichten\", um die Konfiguration zu abzuschlieﬂen.\n\n" +
                          "Im Wiki finden Sie weitere Informationen zur Einrichtung eines TransferWise-Kontos.",status.getKonto().getIban()));
    panel.setUrl("https://www.willuhn.de/wiki/doku.php?id=support:hibiscus.transferwise");

    Button button = new Button(i18n.tr("Konto einrichten"),new SetupTransferWiseStep1(),status,false,"go-next.png");
    panel.addButton(button);
    
    panel.paint(parent);
  }
  
  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#isActive()
   */
  @Override
  public boolean isActive()
  {
    return this.isEnabled();
  }
  
  /**
   * @see de.willuhn.jameica.gui.boxes.AbstractBox#isEnabled()
   */
  @Override
  public boolean isEnabled()
  {
    return this.getNextUnconfigured() != null;
  }
  
  /**
   * Liefert den Status des naechsten unkonfigurierten Transferwise-Kontos.
   * @return der Status des naechsten unkonfigurierten Transferwise-Kontos oder NULL, wenn keines existiert.
   */
  private SupportStatus getNextUnconfigured()
  {
    try
    {
      for (Konto k:KontoUtil.getKonten(KontoFilter.ACTIVE))
      {
        final SupportStatus status = Plugin.getStatus(k);
        
        // Ist es ueberhaupt ein Transferwise-Konto?
        if (!status.checkInitial())
          continue;

        if (!status.checkAll())
          return status;
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to check for transferwise accounts",e);
    }
    return null;
  }
}
