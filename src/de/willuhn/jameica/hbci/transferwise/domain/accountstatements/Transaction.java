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

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import de.willuhn.jameica.hbci.transferwise.Plugin;

/**
 * JSON-Mapping.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Transaction
{
  /**
   * Typ der Transaktion.
   */
  public TransactionType type;
  
  /**
   * Datum.
   */
  @JsonFormat(shape=JsonFormat.Shape.STRING, pattern=Plugin.DATEFORMAT, timezone=Plugin.TIMEZONE)
  public Date date;
  
  /**
   * Betrag.
   */
  public Amount amount;
  
  /**
   * Gebuehren.
   */
  public Amount totalFees;
  
  /**
   * Details der Transaktion.
   */
  public TransactionDetails details;
  
  /**
   * Wechselkurs-Informationen.
   */
  public ExchangeDetails exchangeDetails;
  
  /**
   * Aktueller Saldo.
   */
  public Amount runningBalance;
  
  /**
   * Von Transferwise vergebene eindeutige Referenznummer der Buchung.
   */
  public String referenceNumber;
  
}


