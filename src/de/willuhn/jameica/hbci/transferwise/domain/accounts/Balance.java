/**********************************************************************
 *
 * Copyright (c) 2020 Olaf Willuhn
 * All rights reserved.
 * 
 * This software is copyrighted work licensed under the terms of the
 * Jameica License.  Please consult the file "LICENSE" for details. 
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.transferwise.domain.accounts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * JSON-Mapping.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Balance
{

  /**
   * Die W�hrung.
   */
  public String currency;
  
  /**
   * Die Bank-Details.
   */
  public BankDetails bankDetails;

}


