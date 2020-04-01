/**********************************************************************
 *
 * Copyright (c) 2020 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.transferwise.transport;

/**
 * Kapselt die API-Endpunkte.
 */
public enum ApiEndpoint
{
  /**
   * Sandbox-Umgebung.
   */
  SANDBOX("api.sandbox.transferwise.tech"),
  
  /**
   * Live-Umgebung.
   */
  LIVE("api.transferwise.com")
  
  ;
  
  private String hostname;
  
  /**
   * ct.
   * @param hostname der Hostname des API-Endpunktes.
   */
  private ApiEndpoint(String hostname)
  {
    this.hostname = hostname;
  }
  
  /**
   * Liefert den Hostnamen.
   * @return der Hostname.
   */
  public String getHostname()
  {
    return hostname;
  }
}
