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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ProgressBar;

import de.willuhn.jameica.gui.AbstractView;
import de.willuhn.jameica.hbci.transferwise.Plugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.util.I18N;

/**
 * Abstrakte Basis-Klasse mit den Views zur Einrichtung eines TransferWise-Kontos.
 */
public abstract class AbstractSetupTransferWise extends AbstractView
{
  protected final static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
  
  /**
   * Liefert den Progress-Bar.
   * @param comp das Composite.
   * @return der Progress-Bar.
   */
  protected ProgressBar createProgressBar(Composite comp)
  {
    final ProgressBar bar = new ProgressBar(comp, SWT.HORIZONTAL | SWT.SMOOTH);
    final GridData gd = new GridData(GridData.FILL_HORIZONTAL);
    bar.setLayoutData(gd);
    bar.setMaximum(100);
    return bar;
  }
}
