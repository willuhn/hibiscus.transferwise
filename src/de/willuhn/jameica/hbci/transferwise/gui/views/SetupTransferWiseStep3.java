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

import java.rmi.RemoteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ProgressBar;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SWTUtil;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.transferwise.SupportStatus;
import de.willuhn.jameica.hbci.transferwise.gui.action.KeyPairCreate;
import de.willuhn.jameica.hbci.transferwise.gui.action.KeyPairDelete;
import de.willuhn.jameica.hbci.transferwise.gui.action.PublicKeySave;
import de.willuhn.jameica.hbci.transferwise.gui.action.SetupTransferWiseStep4;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * View zum Einrichten eines TransferWise-Kontos.
 */
public class SetupTransferWiseStep3 extends AbstractSetupTransferWise
{
  private Konto konto          = null;
  private SupportStatus status = null;
  
  private Button next     = null;
  private Button create   = null;
  private Button save     = null;
  private Button delete   = null;
  private ProgressBar bar = null;
  
  /**
   * @see de.willuhn.jameica.gui.AbstractView#reload()
   */
  @Override
  public void reload() throws ApplicationException
  {
    try
    {
      next   = null;
      create = null;
      save   = null;
      delete = null;
      bar    = null;
      
      final Composite comp = this.getParent();
      SWTUtil.disposeChildren(comp);
      this.bind();
      
      comp.redraw();
      comp.layout();
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unable to reload page",e);
      
      // In dem Fall laden wir einfach die ganze View neu
      GUI.startView(GUI.getCurrentView().getClass(),this.getCurrentObject());
    }
    finally
    {
      super.reload();
    }
  }
  
  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle(i18n.tr("TransferWise-Konto - Schritt 3 von 3: Schlüsselpaar"));

    this.status = (SupportStatus) this.getCurrentObject();
    this.konto = status.getKonto();
    final boolean haveKey = this.status.checkKeyPair();

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
          bar.setSelection(40);
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
            final ButtonArea buttons = new ButtonArea();
            if (haveKey)
            {
              buttons.addButton(getSave());
              buttons.addButton(getDelete());
            }
            else
            {
              buttons.addButton(getCreate());
            }
            buttons.paint(comp2);
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
    
    if (haveKey)
    {
      info.setText(i18n.tr("Das Schlüsselpaar des Kontos ist korrekt konfiguriert.\n" +
                           "Sie können den Schlüssel speichern oder löschen und anschließend einen neuen erzeugen."));
      info.setComment(i18n.tr("IBAN des Kontos: {0}.\n\nKlicken Sie bitte auf \"Fertigstellen\", um den Assistenten zu beenden.",konto.getIban()));
    }
    else
    {
      info.setText(i18n.tr("Klicken Sie bitte auf die Schaltfläche \"Neues Schlüsselpaar erstellen...\", um einen neuen Schlüssel zu erzeugen.\n" +
                           "Speichern Sie die erstellte Schlüsseldatei ab und folgen Sie anschließend den Anweisungen auf der Webseite, um den neuen Schlüssel hochzuladen."));
      info.setUrl("https://www.willuhn.de/wiki/doku.php?id=support:hibiscus.transferwise#schritt_4schluesselpaar_hochladen");
      info.setComment(i18n.tr("IBAN des Kontos: {0}.\n\nKlicken Sie bitte anschließend auf \"Fertigstellen\", um den Assistenten zu beenden.",konto.getIban()));
      b.setEnabled(false);
    }
    
    info.addButton(b);
    c.addPart(info);
    
    if (bar != null && haveKey)
      bar.setSelection(80);
  }
  
  /**
   * Liefert den Next-Button.
   * @return der Next-Button.
   */
  private Button getNext()
  {
    if (this.next != null)
      return this.next;
    
    this.next = new Button(i18n.tr("Fertigstellen"),new SetupTransferWiseStep4(),this.getCurrentObject(),false,"ok.png");
    return this.next;
  }

  /**
   * Liefert den Loeschen-Button.
   * @return der Loeschen-Button.
   */
  private Button getDelete()
  {
    if (this.delete != null)
      return this.delete;
    
    this.delete = new Button(i18n.tr("Schlüssel löschen..."),new Action() {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        new KeyPairDelete().handleAction(konto);
        
        // Seite neu laden
        GUI.getCurrentView().reload();
      }
    },this.konto,false,"user-trash-full.png");
    return this.delete;
  }

  /**
   * Liefert den Speichern-Button.
   * @return der Speichern-Button.
   */
  private Button getSave()
  {
    if (this.save != null)
      return this.save;
    
    this.save = new Button(i18n.tr("Schlüssel speichern..."),new Action() {
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        new PublicKeySave().handleAction(konto);
        finish();
      }
    },this.konto,false,"document-save.png");
    return this.save;
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
        // Die Action speichert den Schluessel auch gleich
        new KeyPairCreate().handleAction(konto);
        finish();
      }
    },this.getCurrentObject(),false,"stock_keyring.png");
    return this.create;
  }
  
  /**
   * Finalisiert den Assistenten.
   */
  private void finish()
  {
    if (bar != null)
      bar.setSelection(90);
    
    Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("TransferWise-Konto erfolgreich eingerichtet"),StatusBarMessage.TYPE_SUCCESS));
    getNext().setEnabled(true);
    getCreate().setEnabled(false);
  }
}
