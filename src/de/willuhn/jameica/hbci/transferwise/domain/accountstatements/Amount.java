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
public class Amount
{
  /**
   * Betrag.
   */
  public BigDecimal value;
  
  /**
   * Waehrung.
   */
  public String currency;
}


