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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.KeyPair;
import java.security.PublicKey;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import de.willuhn.io.IOUtil;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.transferwise.KeyStorage;
import de.willuhn.jameica.hbci.transferwise.Plugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Speichert den Public-Key.
 */
public class PublicKeySave implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
  
  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  @Override
  public void handleAction(Object context) throws ApplicationException
  {
    PublicKey key = null;
    
    if (context instanceof PublicKey)
      key = (PublicKey) context;
    else if (context instanceof Konto)
    {
      KeyPair kp = KeyStorage.getKey((Konto) context);
      key = kp != null ? kp.getPublic() : null;
    }

    if (key == null)
      throw new ApplicationException(i18n.tr("Bitte wählen Sie den zu speichernden Schlüssel aus."));

    // Schluessel serialisieren
    FileDialog d = new FileDialog(GUI.getShell(),SWT.SAVE);
    d.setText(Application.getI18n().tr("Bitte wählen den Ordner aus, in dem Sie den Schlüssel speichern möchten."));
    d.setFilterExtensions(new String[]{"*.pem"});
    d.setFileName("transferwise-pubkey.pem");
    d.setOverwrite(true);
    final String s = d.open();
    
    if (s == null || s.length() == 0)
      throw new OperationCanceledException();
    
    final File f = new File(s);
    JcaPEMWriter writer = null;
    try
    {
      writer = new JcaPEMWriter(new OutputStreamWriter(new FileOutputStream(f)));
      writer.writeObject(key);
      writer.flush();
    }
    catch (IOException e)
    {
      Logger.error("unable to save key",e);
      throw new ApplicationException(i18n.tr("Speichern des Schlüssels fehlgeschlagen: {0}",e.getMessage()));
    }
    finally
    {
      IOUtil.close(writer);
    }
    
  }

}
