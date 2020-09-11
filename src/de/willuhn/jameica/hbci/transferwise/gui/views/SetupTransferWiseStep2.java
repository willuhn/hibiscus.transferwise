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
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.gui.input.TextInput;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.InfoPanel;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.rmi.Konto;
import de.willuhn.jameica.hbci.transferwise.Plugin;
import de.willuhn.jameica.hbci.transferwise.SupportStatus;
import de.willuhn.jameica.hbci.transferwise.gui.action.SetupTransferWiseStep3;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;

/**
 * View zum Einrichten eines TransferWise-Kontos.
 */
public class SetupTransferWiseStep2 extends AbstractSetupTransferWise
{
  private Konto konto = null;
  private SupportStatus status = null;
  private Button next = null;
  private TextInput apiKey = null;
  private Listener listener = null;
  private ProgressBar bar = null;
  
  /**
   * @see de.willuhn.jameica.gui.AbstractView#bind()
   */
  @Override
  public void bind() throws Exception
  {
    GUI.getView().setTitle(i18n.tr("TransferWise-Konto - Schritt 2 von 3: API-Key"));

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
          
          try
          {
            final TextInput input = getApiKey();
            input.paint(newComp);
            input.getControl().addKeyListener(new KeyAdapter() {
              /**
               * @see org.eclipse.swt.events.KeyAdapter#keyReleased(org.eclipse.swt.events.KeyEvent)
               */
              @Override
              public void keyReleased(KeyEvent e)
              {
                getListener().handleEvent(null);
              }
            });
          }
          catch (RemoteException re)
          {
            Logger.error("unable to show input for api key",re);
          }
          return newComp;
        }
        return super.extend(state, comp, context);
        
      }
    };
    info.setTitle(i18n.tr("Schritt 2 von 3: API-Key"));
    info.setIcon("transferwise-large.png");
    
    final Button b = this.getNext();
    
    if (!status.checkApiKey())
    {
      info.setText(i18n.tr("Folgen Sie bitte den Anweisungen auf der Webseite, um einen neuen API-Key zu erstellen.\n" +
                           "Geben Sie den API-Key anschließend hier ein."));
      info.setUrl("https://www.willuhn.de/wiki/doku.php?id=support:hibiscus.transferwise");
      b.setEnabled(false);
    }
    else
    {
      info.setText(i18n.tr("Der API-Key des Kontos ist korrekt konfiguriert.\n" +
                           "Prüfen Sie bitte ggf. die Korrektheit des API-Keys."));
    }
    info.setComment(i18n.tr("IBAN des Kontos: {0}.\n\nSie können den API-Key später jederzeit in den Synchronisierungsoptionen des Kontos ändern.\nFür die Prüfung des Schlüsselpaares klicken Sie bitte auf \"Weiter\".",this.konto.getIban()));
    
    info.addButton(b);
    c.addPart(info);
    
    getListener().handleEvent(null);
  }
  
  /**
   * Liefert den Next-Button.
   * @return der Next-Button.
   */
  private Button getNext()
  {
    if (this.next != null)
      return this.next;
    
    this.next = new Button(i18n.tr("Weiter"),new Action() {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        new SetupTransferWiseStep3().handleAction(context);
      }
    },this.getCurrentObject(),false,"go-next.png");
    return this.next;
  }
  
  /**
   * Liefert das Eingabefeld fuer den API-Key.
   * @return das Eingabefeld fuer den API-Key.
   * @throws RemoteException
   */
  private TextInput getApiKey() throws RemoteException
  {
    if (this.apiKey != null)
      return this.apiKey;
    
    this.apiKey = new TextInput(this.konto.getMeta(Plugin.META_PARAM_APIKEY,null));
    this.apiKey.setHint(i18n.tr("Bitte geben Sie hier den API-Key ein."));
    this.apiKey.setMaxLength(36);
    this.apiKey.setValidChars("1234567890-abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
    this.apiKey.addListener(this.getListener());
    return this.apiKey;
  }
  
  /**
   * Liefert den Listener zum Speichern des API-Key.
   * @return der Listener.
   */
  private Listener getListener()
  {
    if (this.listener != null)
      return this.listener;

    this.listener = new Listener() {
      
      @Override
      public void handleEvent(Event event)
      {
        try
        {
          final String s = StringUtils.trimToNull((String) getApiKey().getValue());
          int len = s != null ? s.length() : 0;
          final boolean haveKey = s != null && len == 36;
          
          if (haveKey)
          {
            String current = konto.getMeta(Plugin.META_PARAM_APIKEY,null);
            konto.setMeta(Plugin.META_PARAM_APIKEY,s);
            if (!Objects.equals(s,current))
              Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("API-Key gespeichert"),StatusBarMessage.TYPE_SUCCESS));
          }
          else if (len < 36 && len > 0)
          {
            Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Der eingegebene API-Key ist zu kurz"),StatusBarMessage.TYPE_INFO));
          }
          
          bar.setSelection(haveKey ? 30 : 20);
          getNext().setEnabled(haveKey);
        }
        catch (RemoteException re)
        {
          Logger.error("unable to apply api key",re);
        }
      }
    };
    return this.listener;
    
  }
}
