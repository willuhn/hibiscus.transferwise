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

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * JSON-Mapping.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class TransactionDetails
{
  /**
   * Art der Transaktion.
   */
  public TransactionDetailsType type;
  
  /**
   * Verwendungszweck.
   */
  public String description;
  
  /**
   * Betrag.
   */
  public Amount amount;

  /**
   * Quellbetrag.
   */
  public Amount sourceAmount;
  
  /**
   * Zielbetrag.
   */
  public Amount targetAmount;
  
  /**
   * Gebuehren.
   */
  public Amount fee;
  
  /**
   * Wechselkurs.
   */
  public BigDecimal rate;
  
  /**
   * Absendername.
   */
  public String senderName;
  
  /**
   * Absenderkonto.
   */
  public String senderAccount;
  
  /**
   * Verwendungszwecke.
   */
  public String paymentReference;
  
  /**
   * Kategorie.
   */
  public String category;
  
  /**
   * Merchant.
   */
  public Merchant merchant;

}


