/**********************************************************************
 *
 * Copyright (c) 2020 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.transferwise.domain.accountstatements;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * JSON-Mapping.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class AccountStatementQuery
{
//  /**
//   * Startdatum.
//   * Hier fehlen die Millis, weil wir das mit "startOfDate" abrufen.
//   * Kann Jackson sonst nicht parsen.
//   */
//  @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss'Z'", timezone=Plugin.TIMEZONE)
//  public Date intervalStart;
//
//  /**
//   * Enddatum.
//   */
//  @JsonFormat(shape=JsonFormat.Shape.STRING, pattern=Plugin.DATEFORMAT, timezone=Plugin.TIMEZONE)
//  public Date intervalEnd;

  /**
   * Waehrung.
   */
  public String currency;
  
  /**
   * Die Konto-ID.
   */
  public Integer accountId;

}


