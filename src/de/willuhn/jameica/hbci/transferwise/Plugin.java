/**********************************************************************
 *
 * Copyright (c) 2020 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.transferwise;

import de.willuhn.jameica.plugin.AbstractPlugin;

/**
 * Plugin-Klasse.
 */
public class Plugin extends AbstractPlugin
{
  /**
   * Die BIC des Euro-Kontos von Transferwise.
   */
  public final static String BIC_TRANSFERWISE = "TRWIBEB1XXX";
  
  /**
   * Das Datums- und Zeitformat.
   */
  public final static String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
  
  /**
   * Die Zeitzone.
   */
  public final static String TIMEZONE = "UTC";
  
  /**
   * Meta-Parameter mit dem API-Key.
   */
  public final static String META_PARAM_APIKEY = "API-Key";

  /**
   * Meta-Parameter mit der Profil-ID.
   */
  public final static String META_PARAM_PROFILE = "profile.id";

  /**
   * Meta-Parameter mit der Account-ID.
   */
  public final static String META_PARAM_ACCOUNT = "account.id";
}
