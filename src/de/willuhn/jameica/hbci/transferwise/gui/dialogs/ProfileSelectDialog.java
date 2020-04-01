/**********************************************************************
 *
 * Copyright (c) 2020 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.transferwise.gui.dialogs;

import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.dialogs.AbstractDialog;
import de.willuhn.jameica.gui.internal.buttons.Cancel;
import de.willuhn.jameica.gui.parts.Button;
import de.willuhn.jameica.gui.parts.ButtonArea;
import de.willuhn.jameica.gui.parts.TablePart;
import de.willuhn.jameica.gui.parts.table.FeatureSummary;
import de.willuhn.jameica.gui.util.Container;
import de.willuhn.jameica.gui.util.SimpleContainer;
import de.willuhn.jameica.hbci.transferwise.Plugin;
import de.willuhn.jameica.hbci.transferwise.domain.profiles.UserProfile;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Auswahldialog fuer das zu verwendende Profil.
 */
public class ProfileSelectDialog extends AbstractDialog<UserProfile>
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
  private final static int WINDOW_WIDTH  = 540;
  private final static int WINDOW_HEIGHT = 340;
  
  private List<UserProfile> profiles = null;
  private UserProfile profile = null;
  private TablePart table = null;
  private Button apply = null;
  
  /**
   * ct.
   * @param profiles die Liste der Profile.
   * @param position die Fenster-Position.
   */
  public ProfileSelectDialog(List<UserProfile> profiles, int position)
  {
    super(position);
    this.profiles = profiles;
    this.setTitle(i18n.tr("Auswahl des Benutzer-Profils"));
    this.setSize(WINDOW_WIDTH,WINDOW_HEIGHT);
  }
  
  /**
   * Liefert die Tabelle mit den Benutzerprofilen.
   * @return die Tabelle mit den Benutzerprofilen.
   */
  private TablePart getTable()
  {
    if (this.table != null)
      return this.table;
    
    this.table = new TablePart(this.profiles,new Action() {
      
      @Override
      public void handleAction(Object context) throws ApplicationException
      {
        doApply();
      }
    });
    this.table.removeFeature(FeatureSummary.class);
    this.table.addColumn(i18n.tr("ID"),"id");
    this.table.addColumn(i18n.tr("Typ"),"type");
    this.table.addColumn(i18n.tr("Vorname"),"details.firstName");
    this.table.addColumn(i18n.tr("Nachname"),"details.lastName");
    this.table.addColumn(i18n.tr("Firma"),"details.name");
    this.table.addSelectionListener(new Listener() {
      
      @Override
      public void handleEvent(Event event)
      {
        getApply().setEnabled(getTable().getSelection() != null);
      }
    });
    
    return this.table;
  }
  
  /**
   * Uebernimmt die Auswahl.
   */
  private void doApply()
  {
    this.profile = (UserProfile) getTable().getSelection();
    if (this.profile != null)
      close();
  }
  
  /**
   * Liefert den Apply-Button.
   * @return der Apply-Button.
   */
  private Button getApply()
  {
    if (this.apply != null)
      return this.apply;
    
    this.apply = new Button(i18n.tr("Übernehmen"), new Action() {
      public void handleAction(Object context) throws ApplicationException
      {
        doApply();
      }
    },null,true,"ok.png");
    this.apply.setEnabled(false);
    return this.apply;
  }
  
  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#paint(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void paint(Composite parent) throws Exception
  {
    Container c = new SimpleContainer(parent,true);
    c.addText(i18n.tr("Bitte wählen Sie das zu verwendende Benutzerprofil aus."),true);
    c.addPart(this.getTable());
    
    
    ButtonArea buttons = new ButtonArea();
    buttons.addButton(this.getApply());
    buttons.addButton(new Cancel());
    c.addButtonArea(buttons);
    
    getShell().setMinimumSize(getShell().computeSize(WINDOW_WIDTH,WINDOW_HEIGHT));
  }

  /**
   * @see de.willuhn.jameica.gui.dialogs.AbstractDialog#getData()
   */
  @Override
  protected UserProfile getData() throws Exception
  {
    return this.profile;
  }

}


