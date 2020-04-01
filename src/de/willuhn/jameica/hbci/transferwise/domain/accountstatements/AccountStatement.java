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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * JSON-Mapping.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class AccountStatement
{
  /**
   * Der Kontoinhaber.
   */
  public AccountHolder accountHolder;
  
  /**
   * Die Bank.
   */
  public Issuer issuer;
  
  /**
   * Die Liste der Transaktionen.
   */
  public List<Transaction> transactions = new ArrayList<Transaction>();
  
  /**
   * Schluss-Saldo.
   */
  public Amount endOfStatementBalance;
  
  /**
   * Die Abfrageparameter.
   */
  public AccountStatementQuery query;
  
}


