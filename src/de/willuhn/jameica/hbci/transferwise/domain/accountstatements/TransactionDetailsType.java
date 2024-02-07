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
public enum TransactionDetailsType
{
  /**
   * Unbekannt
   */
  UNKNOWN,
  
  /**
   * Kartenzahlung.
   */
  CARD,
  
  /**
   * Bestellung einer neuen Karte
   */
  CARD_ORDER_CHECKOUT,
  
  /**
   * Waehrungswechsel.
   */
  CONVERSION,
  
  /**
   * Einzahlung.
   */
  DEPOSIT,
  
  /**
   * Ueberweisung.
   */
  TRANSFER,
  
  /**
   * Geld hinzugefuegt.
   */
  MONEY_ADDED,
  
  /**
   * 
   */
  INCOMING_CROSS_BALANCE,
  
  /**
   * 
   */
  OUTGOING_CROSS_BALANCE,
  
  /**
   * Lastschrift.
   */
  DIRECT_DEBIT,
  
  /**
   * Abrechnungsgeb�hr.
   */
  ACCRUAL_CHARGE
}
