/**********************************************************************
 *
 * Copyright (c) 2020 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.transferwise.gui.views;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.rmi.RemoteException;
import java.security.KeyPair;

import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.ProgressBar;

import de.willuhn.io.IOUtil;
import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.dialogs.BackgroundTaskDialog;
import de.willuhn.jameica.gui.internal.action.Start;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.transferwise.KeyStorage;
import de.willuhn.jameica.hbci.transferwise.Plugin;
import de.willuhn.jameica.hbci.transferwise.SupportStatus;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * View zum Einrichten eines TransferWise-Kontos.
 */
public class SetupTransferWiseStep3 extends AbstractSetupTransferWise
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
  
  private Konto konto = null;
  private SupportStatus status = null;
  private Button next = null;
  
  private Button create = null;
  private ProgressBar bar = null;
  private KeyPair kp = null;
  
  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle(i18n.tr("TransferWise-Konto - Schritt 3 von 3: Schlüsselpaar"));

    this.status = (SupportStatus) this.getCurrentObject();
    this.konto = status.getKonto();

    final Container c = new SimpleContainer(this.getParent());

    final InfoPanel info = new InfoPanel()
    {
      /**
       * @see de.willuhn.jameica.gui.parts.InfoPanel#extend(de.willuhn.jameica.gui.parts.InfoPanel.DrawState, org.eclipse.swt.widgets.Composite, java.lang.Object)
       */
      @Override
      public Composite extend(DrawState state, Composite comp, Object context)
      {
        if (state == DrawState.TITLE_AFTER)
        {
          bar = createProgressBar(comp);
          bar.setSelection(50);
          return comp;
        }
        if (state == DrawState.COMMENT_BEFORE)
        {
          final Composite newComp = new Composite(comp,SWT.NONE);
          newComp.setBackground(comp.getBackground());
          newComp.setBackgroundMode(SWT.INHERIT_FORCE);
          newComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
          
          final GridLayout gl = new GridLayout(1,false);
          gl.marginWidth = 0;
          gl.horizontalSpacing = 0;
          newComp.setLayout(gl);

          final Composite comp2 = new Composite(newComp,SWT.NONE);
          comp2.setBackground(comp.getBackground());
          comp2.setBackgroundMode(SWT.INHERIT_FORCE);
          comp2.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
          final GridLayout gl2 = new GridLayout(1,false);
          gl2.marginWidth = 0;
          gl2.horizontalSpacing = 0;
          comp2.setLayout(gl2);

          try
          {
            final Button create = getCreate();
            create.paint(comp2);
          }
          catch (RemoteException re)
          {
            Logger.error("unable to show button for key pair",re);
          }
          return newComp;
        }
        return super.extend(state, comp, context);
        
      }
    };
    info.setTitle(i18n.tr("Schritt 3 von 3: Schlüsselpaar"));
    info.setIcon("transferwise-large.png");
    
    final Button b = this.getNext();
    
    if (!status.checkKeyPair())
    {
      info.setText(i18n.tr("Klicken Sie bitte auf die Schaltfläche \"Neues Schlüsselpaar erstellen...\", um einen neuen Schlüssel zu erzeugen.\n" +
                           "Speichern Sie die erstellte Schlüsseldatei ab und folgen Sie anschließend den Anweisungen auf der Webseite, um den neuen Schlüssel hochzuladen."));
      info.setUrl("https://www.willuhn.de/wiki/doku.php?id=support:hibiscus.transferwise#upload");
      b.setEnabled(false);
    }
    else
    {
      bar.setSelection(50);
      info.setText(i18n.tr("Das Schlüsselpaar des Kontos ist korrekt konfiguriert."));
      info.setComment(i18n.tr("Klicken Sie bitte auf \"Fertigstellen\", um den Assistenten zu beenden."));
    }
    
    info.addButton(b);
    c.addPart(info);
  }
  
  /**
   * Liefert den Next-Button.
   * @return der Next-Button.
   */
  private Button getNext()
  {
    if (this.next != null)
      return this.next;
    
    this.next = new Button(i18n.tr("Fertigstellen"),new Action() {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("TransferWise-Konto erfolgreich eingerichtet"),StatusBarMessage.TYPE_SUCCESS));
        try
        {
          Application.getCallback().notifyUser(i18n.tr("Vergessen Sie nicht, die Schlüsseldatei auf der TransferWise-Webseite hochzuladen."));
        }
        catch (Exception e)
        {
          Logger.error("unable to notify user",e);
        }
        new Start().handleAction(null);
      }
    },this.getCurrentObject(),false,"ok.png");
    return this.next;
  }

  /**
   * Liefert den Create-Button.
   * @return der Create-Button.
   */
  private Button getCreate()
  {
    if (this.create != null)
      return this.create;
    
    this.create = new Button(i18n.tr("Neues Schlüsselpaar erstellen..."),new Action() {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        // Installation starten
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
          
          // Schluessel serialisieren
          FileDialog d = new FileDialog(GUI.getShell(),SWT.SAVE);
          d.setText(Application.getI18n().tr("Bitte wählen den Ordner aus, in dem Sie den Schlüssel speichern möchten."));
          d.setFilterExtensions(new String[]{"*.pem"});
          d.setFileName("transferwise-pubkey.pem");
          d.setOverwrite(true);
          String s = d.open();
          if (s == null || s.length() == 0)
          {
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Erstellung des Schlüsselpaares abgebrochen"),StatusBarMessage.TYPE_INFO));
            return;
          }
          
          final File f = new File(s);
          JcaPEMWriter writer = null;
          try
          {
            writer = new JcaPEMWriter(new OutputStreamWriter(new FileOutputStream(f)));
            writer.writeObject(kp.getPublic());
            writer.flush();
          }
          finally
          {
            IOUtil.close(writer);
          }
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("TransferWise-Konto erfolgreich eingerichtet"),StatusBarMessage.TYPE_SUCCESS));
          bar.setSelection(100);
          getNext().setEnabled(true);
          getCreate().setEnabled(false);
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
          Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Erstellung des Schlüsselpaares fehlgeschlagen"),StatusBarMessage.TYPE_ERROR));
        }

      }
    },this.getCurrentObject(),false,"stock_keyring.png");
    return this.create;
  }
}
